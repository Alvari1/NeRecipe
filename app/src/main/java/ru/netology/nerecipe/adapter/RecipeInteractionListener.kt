package ru.netology.nerecipe.adapter

import ru.netology.nerecipe.dto.Recipe

interface RecipeInteractionListener {
    fun onRecipeLikeClicked(recipe: Recipe?)
    fun onRecipeShareClicked(recipe: Recipe?)
    fun onRecipeDeleteClicked(recipe: Recipe?)
    fun onRecipeEditClicked(recipe: Recipe?)
    fun onCancelEditClicked(recipe: Recipe?)
    fun onRecipeItemClicked(recipe: Recipe?)
}