package ru.netology.nerecipe.activity.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.databinding.FragmentRecipeDetailsBinding
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.EMPTY_UUID
import ru.netology.nerecipe.viewModel.RecipeViewModel

class RecipeDetailsFragment : Fragment(), View.OnCreateContextMenuListener {
    private val viewModel: RecipeViewModel by activityViewModels()

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
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                viewModel.setCurrentRecipeImage(bitmap)
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
            cameraLauncher.launch()
        }
    }
}