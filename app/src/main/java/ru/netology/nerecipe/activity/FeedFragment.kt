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
import ru.netology.nerecipe.databinding.FragmentFeedBinding
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.LongArg
import ru.netology.nerecipe.util.StringArg
import ru.netology.nerecipe.viewModel.RecipeViewModel


class FeedFragment : Fragment() {
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
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        viewModel.setActionState(ActionState.FEED)
        viewModel.clearCurrents()
        viewModel.setRecipeAppBarState(isSearch = true)

        val feedAdapter = RecipesAdapter(viewModel, context)
        binding.recipesList.adapter = feedAdapter

        viewModel.feedRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isEmpty())
                binding.emptyRecipesViewGroup.visibility = View.VISIBLE
            else binding.emptyRecipesViewGroup.visibility = View.GONE

            feedAdapter.submitList(recipes?.toList())
        }

        if (!binding.recipesList.canScrollVertically(-1))
            binding.scrollUpButton.visibility = View.GONE
        else
            binding.scrollUpButton.visibility = View.VISIBLE

        binding.recipesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(-1))
                    binding.scrollUpButton.visibility = View.GONE
                else
                    binding.scrollUpButton.visibility = View.VISIBLE
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

        binding.scrollUpButton.setOnClickListener {
            binding.recipesList.scrollToPosition(0)
            //binding.recipesList.smoothScrollToPosition(0)
            binding.scrollUpButton.visibility = View.GONE
        }

        viewModel.navigateFromFeed.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_feedFragment_to_recipeFragment)
        }

        viewModel.navigateFeedView.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_feedFragment_to_recipeFragment)
        }

        binding.addRecipeButton.setOnClickListener {
            viewModel.createEmptyCurrentRecipeWithStepsAndIngredients()
            viewModel.setActionState(ActionState.EDIT)
            findNavController().navigate(R.id.action_feedFragment_to_recipeFragment)
        }

        viewModel.navigateCuisineFilterUpdate.observe(viewLifecycleOwner) {
            if (it) binding.clearCuisineFilterButton.visibility = View.VISIBLE
            else binding.clearCuisineFilterButton.visibility = View.GONE
        }

        binding.clearCuisineFilterButton.setOnClickListener {
            viewModel.setAllCuisinesChecked()
            viewModel.updateFeedRecipes()
        }

        return binding.root
    }
}
