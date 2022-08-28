package ru.netology.nerecipe.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.databinding.IngredientItemBinding
import ru.netology.nerecipe.dto.Ingredient
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.viewModel.RecipeViewModel

internal class IngredientsAdapter(
    private val interactionListener: RecipeViewModel,
    private val context: Context?,
) : ListAdapter<Ingredient, IngredientsAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: IngredientItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var ingredient: Ingredient

        fun bindPreview(
            position: Int,
        ) {
            this.ingredient = interactionListener.getCurrentIngredients()!![position]

            with(binding) {
                recipeIngredientEditCard.visibility = View.GONE

                if (ingredient.description.trim().isEmpty()) {
                    recipeIngredientViewCard.visibility = View.GONE
                    recipeIngredientEmptyCard.visibility = View.VISIBLE
                } else {
                    recipeIngredientViewCard.visibility = View.VISIBLE
                    recipeIngredientEmptyCard.visibility = View.GONE

                    recipeIngredient.text = ingredient.description
                    recipeIngredient.ellipsize = null
                    recipeIngredient.maxLines = Integer.MAX_VALUE
                }
            }
        }

        fun bindEdit(
            position: Int,
        ) {
            this.ingredient = interactionListener.getCurrentIngredients()!![position]

            with(binding) {
                recipeIngredientViewCard.visibility = View.GONE
                recipeIngredientEditCard.visibility = View.VISIBLE
                recipeIngredientEmptyCard.visibility = View.GONE

                recipeIngredientEdit.editText?.setText(ingredient.description)
                if (ingredient.description.isEmpty())
                    recipeIngredientEdit.editText?.maxLines = 1
                else
                    recipeIngredientEdit.editText?.maxLines = Integer.MAX_VALUE

                ingredientActionButton.setOnClickListener {
                    if (interactionListener.getCurrentIngredients()!!.size > 1) {
                        try {
                            interactionListener.removeIngredient(ingredient.id)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else
                        interactionListener.doSnackbar(resId = R.string.cant_delete_last_ingredient)
                }

                recipeIngredientEdit.editText?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                    }

                    override fun beforeTextChanged(
                        s: CharSequence, start: Int,
                        count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence, start: Int,
                        before: Int, count: Int
                    ) {
                        if (ingredient.description != s.toString())
                            interactionListener.updateIngredientDescription(
                                ingredient.copy(description = s.toString())
                            )
                    }
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = IngredientItemBinding.inflate(inflator, parent, false)

        return ViewHolder(
            binding = binding,
        )
    }

    override fun getItem(position: Int): Ingredient =
        interactionListener.getCurrentIngredients()!![position]

//    override fun getItemViewType(position: Int) = position

//    override fun getItemId(position: Int) =
//        interactionListener.getCurrentIngredients()?.get(position).hashCode().toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.setIsRecyclable(false)

        if (interactionListener.getActionState() == ActionState.EDIT)
            holder.bindEdit(position)
        else
            holder.bindPreview(position)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient) =
            oldItem == newItem
    }

}
