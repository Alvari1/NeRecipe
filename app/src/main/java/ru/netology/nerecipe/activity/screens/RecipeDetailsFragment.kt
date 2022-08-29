package ru.netology.nerecipe.activity.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.jetbrains.annotations.Nullable
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.databinding.FragmentRecipeDetailsBinding
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.EMPTY_UUID
import ru.netology.nerecipe.viewModel.RecipeViewModel
import java.io.File


class RecipeDetailsFragment : Fragment(), View.OnCreateContextMenuListener {
    private val viewModel: RecipeViewModel by activityViewModels()

    private lateinit var uri: Uri

    var galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val imageStream = context?.contentResolver?.openInputStream(it)
            viewModel.setCurrentRecipeImage(
                BitmapFactory.decodeStream(
                    imageStream
                )
            )
        }
    }

    var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                try {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(uri)
                        .into(object : CustomTarget<Bitmap?>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                viewModel.setCurrentRecipeImage(resource)
                                context?.contentResolver?.delete(uri, null, null)
                            }

                            override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                        })
                } catch (e: Exception) {
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)

        val adapter = RecipeAdapter(viewModel, context)
        binding.recipeRecyclerView.adapter = adapter

        viewModel.editRecipe.observe(viewLifecycleOwner) { recipe ->
            if (recipe != null) {
                adapter.submitList(listOf(recipe).toList())

                //setup appbar menus
                if (viewModel.getActionState() == ActionState.PREVIEW)
                    viewModel.setRecipeAppBarState(isEdit = true, isDelete = true)
                else
                    viewModel.setRecipeAppBarState(
                        isCancel = true,
                        isDelete = recipe.id != EMPTY_UUID
                    )
            } else findNavController().navigate(R.id.feedFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateFromRecipe.observe(viewLifecycleOwner) {
            if (viewModel.getCurrentRecipe() == null)
                viewModel.createEmptyCurrentRecipeWithStepsAndIngredients()
        }

        viewModel.navigateRecipeCancel.observe(viewLifecycleOwner) {
            if (viewModel.getActionState() == ActionState.FEED)
                findNavController().navigate(R.id.feedFragment)
            else
                findNavController().navigate(R.id.recipeFragment)
        }

        viewModel.navigateRecipeShare.observe(viewLifecycleOwner) { recipe ->
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, recipe)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, getString(R.string.share_intent_title))
            startActivity(shareIntent)
        }

        viewModel.navigateRecipeGetImageFromGallery.observe(viewLifecycleOwner) {
            galleryLauncher.launch("image/*")
        }

        viewModel.navigateRecipeGetImageFromCamera.observe(viewLifecycleOwner) {
            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            cameraLauncher.launch(uri)
        }
    }
}