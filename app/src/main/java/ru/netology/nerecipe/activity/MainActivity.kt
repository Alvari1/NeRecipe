package ru.netology.nerecipe.activity

import android.Manifest.permission.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nerecipe.R
import ru.netology.nerecipe.databinding.ActivityMainBinding
import ru.netology.nerecipe.dto.AppBarState
import ru.netology.nerecipe.dto.LargeImageStore
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.viewModel.RecipeViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private val viewModel by viewModels<RecipeViewModel>()

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0
    private var saveAppBarState = AppBarState()

    private var PERMISSIONS = arrayOf(
        INTERNET,
        CAMERA,
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE,
        MANAGE_EXTERNAL_STORAGE
    )

    private val requestMultiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                Log.i("Permission: ", "Granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        viewModel.setRecipeAppBarState()
        viewModel.navigateRecipeAppBarState.observe(this) {
            invalidateOptionsMenu()
        }

        viewModel.data.observe(this) { viewModel.updateFeedRecipes() }

        viewModel.cuisineData.observe(this) {
            if (viewModel.getCurrentSelectedCuisines().isNullOrEmpty())
                viewModel.initializeSelectedCuisines()

            updateFiltersBadges(binding)
        }

        viewModel.ingredientData.observe(this) { it?.size }

        viewModel.stepData.observe(this) { it?.size }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.feedFragment,
                R.id.favoritesFragment,
                R.id.filterFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavBar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(DestinationFragmentsListener(binding))

        viewModel.navigateCuisineFilterSetBadges.observe(this) {
            updateFiltersBadges(binding)
        }

        viewModel.navigateLargeImageView.observe(this) {
            zoomImageFromThumb(viewModel.getLargeImage())
        }

        viewModel.navigateRecipeDelete.observe(this) {
            wannaDeleteRecipe(it)
        }

        viewModel.navigateSnackbar.observe(this) {
            if (it.resId == null)
                Snackbar.make(binding.root, it.text!!, it.duration).show()
            else
                Snackbar.make(binding.root, it.resId, it.duration).show()
        }

        this.onBackPressedDispatcher.addCallback(this) {
            when (viewModel.getActionState()) {
                ActionState.FEED ->
                    wannaQuit()
                ActionState.PREVIEW, ActionState.FAVORITES, ActionState.FILTER -> {
                    if (viewModel.getZoomMode()) {
                        viewModel.setZoomMode(false)
                        viewModel.getLargeImage().thumbView?.alpha = 1f
                        binding.imageLarge.visibility = View.GONE
                        viewModel.setRecipeAppBarStateByObject(viewModel.getLargeImage().appBarState)
                        binding.navHostFragment.visibility = View.VISIBLE
                        currentAnimator = null
                    } else
                        navController.navigate(R.id.feedFragment)
                }
                ActionState.EDIT ->
                    wannaCancelEdit()

            }
        }

        setContentView(binding.root)

        if (!hasPermissions(this, PERMISSIONS)) {
            requestMultiplePermissionLauncher.launch(PERMISSIONS)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.recipe_action, menu)

        val searchView = menu.findItem(R.id.action_search)?.actionView as SearchView
        searchView.queryHint = String.format(resources.getString(R.string.search_hint))

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(content: String?): Boolean {
                viewModel.filterSQLSearch(content)

                searchView.requestFocus()

                return true
            }

            override fun onQueryTextChange(content: String?): Boolean {
                viewModel.filterSQLSearch(content)

                searchView.requestFocus()
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.filterSQLSearch(null)
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    inner class DestinationFragmentsListener(val binding: ActivityMainBinding) :
        NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)

            binding.bottomNavBar.visibility = when (destination.id) {
                R.id.feedFragment, R.id.favoritesFragment, R.id.filterFragment -> View.VISIBLE
                else -> View.GONE
            }

            supportActionBar?.title = when (destination.id) {
                R.id.feedFragment -> getString(R.string.actionbar_title_recipes)
                R.id.favoritesFragment -> getString(R.string.actionbar_title_favorites)
                R.id.filterFragment -> getString(R.string.actionbar_title_filters)
                R.id.recipeFragment -> getString(R.string.actionbar_title_recipe)
                else -> getString(R.string.app_name)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_add -> {
                viewModel.onAddClicked()
                true
            }

            R.id.action_edit -> {
                viewModel.onRecipeEditClicked(viewModel.getCurrentRecipe())
                true
            }

            R.id.action_save -> {
                if (viewModel.validateCurrentsSaveConditions()) {
                    viewModel.doSnackbar(resId = R.string.recipe_saving)
                    viewModel.onSaveButtonClicked(viewModel.getCurrentRecipe())
                    viewModel.doSnackbar(resId = R.string.recipe_saved)
                } else
                    viewModel.doSnackbar(resId = R.string.save_conditions_violated)
                true
            }

            R.id.action_cancel -> {
                wannaCancelEdit()
                true
            }

            R.id.action_delete -> {
                viewModel.recipeDeleteConfirmDialog(viewModel.getCurrentRecipe())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        return run {
            val appBatState = viewModel.getRecipeAppBarState()
            menu.findItem(R.id.action_search).isVisible = appBatState.isSearch
            menu.findItem(R.id.action_add).isVisible = appBatState.isAdd
            menu.findItem(R.id.action_edit).isVisible = appBatState.isEdit
            menu.findItem(R.id.action_save).isVisible = appBatState.isSave
            menu.findItem(R.id.action_cancel).isVisible = appBatState.isCancel
            menu.findItem(R.id.action_delete).isVisible = appBatState.isDelete

            true
        }
    }

    private fun wannaQuit() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.exit_application_title)
        alertDialogBuilder
            .setMessage(R.string.exit_application_prompt)
            .setCancelable(false)
            .setPositiveButton(R.string.option_yes,
                DialogInterface.OnClickListener { dialog, id ->
                    moveTaskToBack(true)
                    Process.killProcess(Process.myPid())
                    System.exit(1)
                })
            .setNegativeButton(R.string.option_no,
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun wannaCancelEdit() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.cancel_recipe_title)
        alertDialogBuilder
            .setMessage(R.string.cancel_recipe_prompt)
            .setCancelable(false)
            .setPositiveButton(R.string.option_yes,
                DialogInterface.OnClickListener { dialog, id ->
                    viewModel.getCurrentRecipe()?.let { viewModel.onCancelEditClicked(it) }
                })
            .setNegativeButton(R.string.option_no,
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun wannaDeleteRecipe(recipe: Recipe) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.delete_recipe_title)
        alertDialogBuilder
            .setMessage(R.string.delete_recipe_prompt)
            .setCancelable(false)
            .setPositiveButton(R.string.option_yes,
                DialogInterface.OnClickListener { dialog, id ->
                    viewModel.onRecipeDeleteClicked(recipe)
                })
            .setNegativeButton(R.string.option_no,
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun updateFiltersBadges(binding: ActivityMainBinding) {
        val selectedCuisines = viewModel.getCurrentSelectedCuisines()
        if (selectedCuisines != null)
            if (selectedCuisines.contains(-1)) {
                binding.bottomNavBar.getOrCreateBadge(R.id.filterFragment).isVisible = false
                binding.bottomNavBar.getOrCreateBadge(R.id.filterFragment).number = 0
                viewModel.setCuisineFilterValue(false)
            } else {
                binding.bottomNavBar.getOrCreateBadge(R.id.filterFragment).isVisible = true
                binding.bottomNavBar.getOrCreateBadge(R.id.filterFragment).number =
                    selectedCuisines.size
                viewModel.setCuisineFilterValue(true)
            }
    }

    private fun zoomImageFromThumb(largeImage: LargeImageStore) {
        currentAnimator?.cancel()

        if (largeImage.thumbView != null && largeImage.imageBitmap != null) {
            // If there's an animation in progress, cancel it
            // immediately and proceed with this one.

            // Load the high-resolution "zoomed-in" image.
            val expandedImageView: ImageView =
                window.decorView.rootView.findViewById(R.id.imageLarge)
            expandedImageView.setImageBitmap(largeImage.imageBitmap)
            viewModel.setZoomMode(true)

            // Calculate the starting and ending bounds for the zoomed-in image.
            // This step involves lots of math. Yay, math.
            val startBoundsInt = Rect()
            val finalBoundsInt = Rect()
            val globalOffset = Point()

            // The start bounds are the global visible rectangle of the thumbnail,
            // and the final bounds are the global visible rectangle of the container
            // view. Also set the container view's offset as the origin for the
            // bounds, since that's the origin for the positioning animation
            // properties (X, Y).
            largeImage.thumbView.getGlobalVisibleRect(startBoundsInt)
            (window.decorView.rootView.findViewById(R.id.mainContainer) as View)
                .getGlobalVisibleRect(finalBoundsInt, globalOffset)
            startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
            finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

            val startBounds = RectF(startBoundsInt)
            val finalBounds = RectF(finalBoundsInt)

            // Adjust the start bounds to be the same aspect ratio as the final
            // bounds using the "center crop" technique. This prevents undesirable
            // stretching during the animation. Also calculate the start scaling
            // factor (the end scaling factor is always 1.0).
            val startScale: Float
            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                // Extend start bounds horizontally
                startScale = startBounds.height() / finalBounds.height()
                val startWidth: Float = startScale * finalBounds.width()
                val deltaWidth: Float = (startWidth - startBounds.width()) / 2
                startBounds.left -= deltaWidth.toInt()
                startBounds.right += deltaWidth.toInt()
            } else {
                // Extend start bounds vertically
                startScale = startBounds.width() / finalBounds.width()
                val startHeight: Float = startScale * finalBounds.height()
                val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
                startBounds.top -= deltaHeight.toInt()
                startBounds.bottom += deltaHeight.toInt()
            }

            // Hide the thumbnail and show the zoomed-in view. When the animation
            // begins, it will position the zoomed-in view in the place of the
            // thumbnail.
            largeImage.thumbView.alpha = 0f
            expandedImageView.visibility = View.VISIBLE
            (window.decorView.rootView.findViewById(R.id.nav_host_fragment) as View).visibility =
                View.INVISIBLE
            viewModel.setRecipeAppBarState()

            // Set the pivot point for SCALE_X and SCALE_Y transformations
            // to the top-left corner of the zoomed-in view (the default
            // is the center of the view).
            expandedImageView.pivotX = 0f
            expandedImageView.pivotY = 0f

            // Construct and run the parallel animation of the four translation and
            // scale properties (X, Y, SCALE_X, and SCALE_Y).
            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        expandedImageView,
                        View.X,
                        startBounds.left,
                        finalBounds.left
                    )
                ).apply {
                    with(
                        ObjectAnimator.ofFloat(
                            expandedImageView,
                            View.Y,
                            startBounds.top,
                            finalBounds.top
                        )
                    )
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        currentAnimator = null
                    }
                })
                start()
            }

            // Upon clicking the zoomed-in image, it should zoom back down
            // to the original bounds and show the thumbnail instead of
            // the expanded image.
            expandedImageView.setOnClickListener {
                currentAnimator?.cancel()

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                currentAnimator = AnimatorSet().apply {
                    play(
                        ObjectAnimator.ofFloat(
                            expandedImageView,
                            View.X,
                            startBounds.left
                        )
                    ).apply {
                        with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                        with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                        with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
                    }
                    duration = shortAnimationDuration.toLong()
                    interpolator = DecelerateInterpolator()
                    addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            largeImage.thumbView.alpha = 1f
                            expandedImageView.visibility = View.GONE
                            (window.decorView.rootView.findViewById(R.id.nav_host_fragment) as View).visibility =
                                View.VISIBLE
                            viewModel.setRecipeAppBarStateByObject(largeImage.appBarState)
                            viewModel.setZoomMode(false)
                            currentAnimator = null
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            largeImage.thumbView.alpha = 1f
                            expandedImageView.visibility = View.GONE
                            (window.decorView.rootView.findViewById(R.id.nav_host_fragment) as View).visibility =
                                View.VISIBLE
                            viewModel.setRecipeAppBarStateByObject(largeImage.appBarState)
                            viewModel.setZoomMode(false)
                            currentAnimator = null
                        }
                    })
                    start()
                }
            }
        }
    }

    private fun zoomImageFromThumb(thumbView: View, imageBitmap: Bitmap) {

    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
}