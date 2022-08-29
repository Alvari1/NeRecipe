package ru.netology.nerecipe.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipesAdapter
import ru.netology.nerecipe.databinding.FragmentFavoritesBinding
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.LongArg
import ru.netology.nerecipe.util.StringArg
import ru.netology.nerecipe.viewModel.RecipeViewModel


class FavoritesFragment : Fragment() {
    private val viewModel: RecipeViewModel by activityViewModels()

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.longArg: Long by LongArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        viewModel.setActionState(ActionState.FAVORITES)
        viewModel.setRecipeAppBarState(isSearch = true)
//        viewModel.clearCurrents()

        val favoritesAdapter = RecipesAdapter(viewModel, context)
        binding.recipesFavoritesList.adapter = favoritesAdapter

        viewModel.feedRecipes.observe(viewLifecycleOwner) { recipes ->
            val items = recipes?.filter { it.likedByMe } ?: listOf()

            if (items.isEmpty())
                binding.emptyFavoritesViewGroup.visibility = View.VISIBLE
            else binding.emptyFavoritesViewGroup.visibility = View.GONE

            favoritesAdapter.submitList(items.toList())
        }

        if (!binding.recipesFavoritesList.canScrollVertically(-1))
            binding.scrollUpButton.hide()
        else
            binding.scrollUpButton.show()

        binding.recipesFavoritesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(-1))
                    binding.scrollUpButton.hide()
                else
                    binding.scrollUpButton.show()
            }

//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//
//                if (!recyclerView.canScrollVertically(-1))
//                    binding.scrollUp.visibility = View.GONE
//                else
//                    binding.scrollUp.visibility = View.VISIBLE
//            }
        })

        binding.scrollUpButton.setOnClickListener {
            binding.recipesFavoritesList.scrollToPosition(0)
            //binding.recipesList.smoothScrollToPosition(0)
            binding.scrollUpButton.hide()
        }

        viewModel.navigateRecipeShare.observe(viewLifecycleOwner) { recipe ->
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, recipe)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(
                intent, getString(R.string.share_intent_title)
            )

            startActivity(shareIntent)
        }

        viewModel.navigateFromFavorites.observe(viewLifecycleOwner) { _ ->
            findNavController().navigate(R.id.action_favoritesFragment_to_recipeFragment)
        }

        viewModel.navigateCuisineFilterUpdate.observe(viewLifecycleOwner) {
            if (it) binding.clearCuisineFilterButton.show()
            else binding.clearCuisineFilterButton.hide()
        }

        binding.clearCuisineFilterButton.setOnClickListener {
            viewModel.setAllCuisinesChecked()
            viewModel.updateFeedRecipes()
        }

        return binding.root
    }
}
