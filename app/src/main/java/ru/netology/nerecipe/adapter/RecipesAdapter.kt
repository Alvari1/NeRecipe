package ru.netology.nerecipe.adapter

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.databinding.RecipeItemBinding
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.Formatters.getFormattedCounter
import ru.netology.nerecipe.util.Formatters.getFormattedDate
import ru.netology.nerecipe.util.THUMB_IMAGE_POSTFIX
import ru.netology.nerecipe.util.getLocalizedNameByResName
import ru.netology.nerecipe.viewModel.RecipeViewModel


internal class RecipesAdapter(
    private val interactionListener: RecipeViewModel,
    private val context: Context?,
) : ListAdapter<Recipe, RecipesAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: RecipeItemBinding,
    ) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        private lateinit var recipe: Recipe

        fun bind(
            recipe: Recipe,
        ) {
            this.recipe = recipe
            val recipeState = interactionListener.getActionState()

            with(binding) {
                recipeItemViewCard.visibility = View.VISIBLE
                recipeItemEditCard.visibility = View.GONE

                recipeAuthorName.text =
                    recipe.author.ifEmpty { context?.getString(R.string.author_unknown) ?: "" }
                recipeTitle.text = recipe.title
                recipeDescription.text = recipe.description
                date.text = getFormattedDate(recipe.published)
                recipeCuisine.text = context?.getLocalizedNameByResName(
                    context,
                    interactionListener.getCuisineNameById(recipe.cuisine)
                ).plus(" ").plus(context?.getString(R.string.cuisine_title))

                likePic.isChecked = recipe.likedByMe
                likePic.text = getFormattedCounter(recipe.likes)

                sharesPic.text = getFormattedCounter(recipe.shares)
                viewsPic.text = getFormattedCounter(recipe.views)

                likePic.setOnClickListener {
                    interactionListener.onRecipeLikeClicked(
                        recipe
                    )
                }
                sharesPic.setOnClickListener {
                    interactionListener.onRecipeShareClicked(recipe)
                }

                if (recipe.image == null)
                    recipeImage.setImageResource(R.drawable.recipe_image)
                else {
                    context?.let {
                        val bitmap = interactionListener.getImageFromStorage(
                            recipe.image.plus(
                                THUMB_IMAGE_POSTFIX
                            )
                        )
                        recipeImage.setImageBitmap(bitmap)
                    }
                }

                if (recipeState == ActionState.FEED ||
                    recipeState == ActionState.FAVORITES
                ) {
                    interactionListener.setRecipeAppBarState(isSearch = true)

                    recipeItem.setOnClickListener {
                        interactionListener.onRecipeItemClicked(recipe)
                    }

                    root.setOnCreateContextMenuListener { menu, v, menuInfo ->
                        val edit: MenuItem = menu!!.add(Menu.NONE, 1, 1, R.string.menu_edit)
                        val delete: MenuItem =
                            menu.add(Menu.NONE, 2, 2, R.string.menu_delete)
                        edit.setOnMenuItemClickListener(onContextMenuAction)
                        delete.setOnMenuItemClickListener(onContextMenuAction)
                    }
                }
            }
        }

        private val onContextMenuAction: MenuItem.OnMenuItemClickListener =
            MenuItem.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> interactionListener.onRecipeEditClicked(recipe)
                    2 -> interactionListener.recipeDeleteConfirmDialog(recipe)
                }
                true
            }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = RecipeItemBinding.inflate(inflator, parent, false)

        return ViewHolder(
            binding = binding,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) =
            oldItem == newItem
    }
}