package ru.netology.nerecipe.handlers

import com.google.android.material.tabs.TabLayout
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.viewModel.RecipeViewModel

class TabSelectedHandler {
    companion object {
        fun handleOnTabSelected(tab: TabLayout.Tab?, viewModel: RecipeViewModel) {
            viewModel.setTabSelected(tab?.position)
            when (tab?.position) {
                0 -> {
                    if (viewModel.getActionState() == ActionState.EDIT) {
                        viewModel.setRecipeAppBarStateByObject(
                            viewModel.getRecipeAppBarState().copy(isAdd = false)
                        )
                    }
                }
                1 -> {
//                    viewModel.onStepsTabSelected()
                    if (viewModel.getActionState() == ActionState.EDIT) {
                        viewModel.setRecipeAppBarStateByObject(
                            viewModel.getRecipeAppBarState().copy(isAdd = true)
                        )
                    }
                }
                2 -> {
//                    viewModel.onIngredientsTabSelected()
                    if (viewModel.getActionState() == ActionState.EDIT) {
                        val appState = viewModel.getRecipeAppBarState()
                        if (appState != null) viewModel.setRecipeAppBarStateByObject(
                            appState.copy(
                                isAdd = true
                            )
                        )
                    }
                }
            }
        }
    }

}