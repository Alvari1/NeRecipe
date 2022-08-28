package ru.netology.nerecipe.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ru.netology.nerecipe.R
import ru.netology.nerecipe.activity.screens.IngredientsFragment
import ru.netology.nerecipe.activity.screens.RecipeDetailsFragment
import ru.netology.nerecipe.activity.screens.StepsFragment
import ru.netology.nerecipe.adapter.ViewPagerAdapter
import ru.netology.nerecipe.databinding.FragmentRecipeBinding
import ru.netology.nerecipe.dto.FragmentList
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.handlers.TabSelectedHandler
import ru.netology.nerecipe.viewModel.RecipeViewModel


class RecipeFragment : Fragment() {
    private val viewModel: RecipeViewModel by activityViewModels()

    private var fragmentList: MutableList<FragmentList> = mutableListOf(
        FragmentList(
            RecipeDetailsFragment(),
            "RecipeDetailsFragment",
            "RecipeDetailsFragment",
            R.string.recipe_tab_title_detail,
            R.id.recipeDetailsFragment,
        ),
        FragmentList(
            StepsFragment(),
            "StepsFragment",
            "StepsFragment",
            R.string.recipe_tab_title_steps,
            R.id.stepsFragment,
        ),
        FragmentList(
            IngredientsFragment(),
            "IngredientsFragment",
            "IngredientsFragment",
            R.string.recipe_tab_title_ingredients,
            R.id.ingredientsFragment,
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRecipeBinding.inflate(inflater, container, false)

        val viewAdapter =
            ViewPagerAdapter(
                fragmentList,
                childFragmentManager,
                lifecycle
            )

        if (viewModel.getActionState() == ActionState.PREVIEW)
            viewModel.setRecipeAppBarState(isEdit = true, isDelete = true)
        else
            viewModel.setRecipeAppBarState(isCancel = true, isDelete = true)

        binding.viewPager2.adapter = viewAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = fragmentList[position].titleId?.let { getString(it) }
        }.attach()

        binding.tabLayout.clearFocus()
        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                TabSelectedHandler.handleOnTabSelected(tab, viewModel)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        viewModel.navigateRecipeSave.observe(viewLifecycleOwner) {
            if (viewModel.getActionState() == ActionState.FEED)
                findNavController().navigate(R.id.feedFragment)
            else {
                findNavController().navigate(R.id.recipeFragment)
            }
        }

        viewModel.navigateActionStateChanged.observe(viewLifecycleOwner) {
            var fragment: Fragment? = null

            val newFragmentsList =
                mutableListOf(
                    RecipeDetailsFragment(),
                    StepsFragment(),
                    IngredientsFragment(),
                )

            val fragmentManager = childFragmentManager

            fragmentList.forEachIndexed { index, fItem ->
                fragment = fragmentManager.findFragmentByTag("f$index")
                if (fragment != null) {
                    fragmentList[index].fragment = newFragmentsList[index]
                    fragmentManager.beginTransaction().remove(fragment!!)
                        .add(fragmentList[index].fragment, "f$index")
                        .commitNowAllowingStateLoss()
                    fragmentManager.popBackStack()
                }
            }

            if (viewModel.getActionState() != ActionState.FEED)
                findNavController().navigate(R.id.recipeFragment)
        }

        return binding.root
    }

}