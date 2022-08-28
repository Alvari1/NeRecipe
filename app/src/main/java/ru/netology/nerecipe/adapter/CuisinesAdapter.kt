package ru.netology.nerecipe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nerecipe.R
import ru.netology.nerecipe.databinding.FilterItemBinding
import ru.netology.nerecipe.dto.Cuisine
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.getLocalizedNameByResName
import ru.netology.nerecipe.viewModel.RecipeViewModel

internal class CuisinesAdapter(
    private val interactionListener: RecipeViewModel,
    private val context: Context?,
) : ListAdapter<Cuisine, CuisinesAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: FilterItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var cuisine: Cuisine

        fun bind(cuisine: Cuisine) {
            this.cuisine = cuisine
            val cusineState = interactionListener.getActionState()

            with(binding) {
                if (cusineState == ActionState.FILTER) {
                    cuisineCheckbox.text =
                        context?.getLocalizedNameByResName(context, cuisine.resName)

                    cuisineId.text = cuisine.id.toString()
                    cuisineCheckbox.isChecked = cuisine.isSelected

                    if (cuisineCheckbox.isChecked)
                        interactionListener.addCuisineToCurrentSelectedCuisines(cuisine.id)
                    else
                        interactionListener.removeCuisineFromCurrentSelectedCuisines(cuisine.id)


                    cuisineCheckbox.setOnClickListener {
                        if (cuisineCheckbox.isChecked) {
                            if (interactionListener.getCheckedCuisinesCount() ==
                                interactionListener.cuisineData.value?.size
                            ) {
//                                binding.allCuisines.isChecked = true
                                interactionListener.setAllCuisinesChecked()
                            } else {
                                interactionListener.addCuisineToCurrentSelectedCuisines(
                                    cuisine.id,
                                    true
                                )
                            }
                        } else {
                            //you can't uncheck last remained checkbox
                            if (interactionListener.getCheckedCuisinesCount() <= 1) {
                                cuisineCheckbox.isChecked = true
                                Snackbar.make(
                                    binding.root,
                                    R.string.last_cuisine_warning,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                interactionListener.removeCuisineFromCurrentSelectedCuisines(
                                    cuisine.id,
                                    true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = FilterItemBinding.inflate(inflator, parent, false)

        return ViewHolder(
            binding = binding,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<Cuisine>() {
        override fun areItemsTheSame(oldItem: Cuisine, newItem: Cuisine) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Cuisine, newItem: Cuisine) =
            oldItem == newItem
    }
}
