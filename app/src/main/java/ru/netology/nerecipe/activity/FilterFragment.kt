package ru.netology.nerecipe.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.netology.nerecipe.adapter.CuisinesAdapter
import ru.netology.nerecipe.databinding.FragmentFiltersBinding
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.LongArg
import ru.netology.nerecipe.util.StringArg
import ru.netology.nerecipe.viewModel.RecipeViewModel


class FilterFragment : Fragment() {
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
        val binding = FragmentFiltersBinding.inflate(inflater, container, false)

        viewModel.setActionState(ActionState.FILTER)
        viewModel.setRecipeAppBarState()

        val cuisinesAdapter = CuisinesAdapter(viewModel, context)
        binding.cuisinesList.adapter = cuisinesAdapter
        viewModel.cuisineData.observe(viewLifecycleOwner) { cuisine ->
            cuisinesAdapter.submitList(cuisine?.toList())
        }

        binding.allCuisinesCheckbox.setOnClickListener() {
            if (binding.allCuisinesCheckbox.isChecked) {
                //check all cuisines
                viewModel.setAllCuisinesChecked()
            } else
                if (viewModel.getCheckedCuisinesCount() == viewModel.cuisineData.value?.size)
                //if all checkbox on, you can't uncheck all_cuisines checkbox
                    binding.allCuisinesCheckbox.isChecked = true
        }

        viewModel.navigateAllCuisinesCheckbox.observe(viewLifecycleOwner) {
            binding.allCuisinesCheckbox.isChecked = it
        }

        return binding.root
    }
}
