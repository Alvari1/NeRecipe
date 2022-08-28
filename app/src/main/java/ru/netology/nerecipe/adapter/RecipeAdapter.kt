package ru.netology.nerecipe.adapter

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nerecipe.R
import ru.netology.nerecipe.databinding.RecipeItemBinding
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.Formatters.getFormattedCounter
import ru.netology.nerecipe.util.Formatters.getFormattedDate
import ru.netology.nerecipe.util.getLocalizedNameByResName
import ru.netology.nerecipe.viewModel.RecipeViewModel


internal class RecipeAdapter(
    private val interactionListener: RecipeViewModel,
    private val context: Context?,
) : ListAdapter<Recipe, RecipeAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: RecipeItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var recipe: Recipe

        fun bindPreview(
            recipe: Recipe
        ) {
            this.recipe = recipe

            with(binding) {
                recipeItemViewCard.visibility = View.VISIBLE
                recipeItemEditCard.visibility = View.GONE

                recipeAuthorName.text = recipe.author
                recipeTitle.text = recipe.title

                recipeDescription.text = recipe.description
                recipeDescription.ellipsize = null
                recipeDescription.maxLines = Integer.MAX_VALUE

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
                    interactionListener.onRecipeLikeClicked(recipe)
                }
                sharesPic.setOnClickListener {
                    interactionListener.onRecipeShareClicked(recipe)
                }

                if (recipe.imageBitmap == null) {
                    recipeImage.setImageResource(R.drawable.recipe_image)
                } else {
                    recipeImage.setImageBitmap(recipe.imageBitmap)

                    recipeImage.setOnClickListener {
                        interactionListener.zoomImageFromThumb(recipeImage, recipe.imageBitmap)
                    }
                }
            }
        }


        fun bindEdit(
            recipe: Recipe
        ) {
            this.recipe = recipe

            with(binding) {
                recipeItemViewCard.visibility = View.GONE
                recipeItemEditCard.visibility = View.VISIBLE

                val adapter = context?.let { context ->
                    ArrayAdapter(
                        context,
                        R.layout.list_item,
                        interactionListener.getCuisinesText().values.toMutableList()
                            .map { resName ->
                                context.getLocalizedNameByResName(
                                    context,
                                    resName
                                )
                            },
                    )
                }

                val cuisineText = context?.getLocalizedNameByResName(
                    context,
                    interactionListener.getCuisineNameById(recipe.cuisine)
                )

                if (!cuisineText.isNullOrEmpty())
                    (editCuisine.editText as? AutoCompleteTextView)?.setText(cuisineText)
                (editCuisine.editText as? AutoCompleteTextView)?.setAdapter(adapter)
                (editCuisine.editText as? AutoCompleteTextView)?.onItemClickListener =
                    AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
                        val selected = arg0.adapter.getItem(arg2)
                        (editCuisine.editText as? AutoCompleteTextView)?.tag = selected
                        interactionListener.updateCurrentRecipeCuisine(arg2)
                    }

                editTitle.editText?.setText(recipe.title)
                editAuthor.editText?.setText(recipe.author)
                editDescription.editText?.setText(recipe.description)
                if (recipe.description.isNullOrEmpty())
                    editDescription.editText?.maxLines = 1
                else
                    editDescription.editText?.maxLines = Integer.MAX_VALUE

                if (recipe.imageBitmap == null) {
                    editRecipeImage.setImageResource(R.drawable.ic_add_a_photo_white_48)
                    editRecipeImage.adjustViewBounds = false
                    editRecipeImage.scaleType = ImageView.ScaleType.CENTER
                    context?.getColor(R.color.common_color)
                        ?.let { editRecipeImage.setBackgroundColor(it) }
                } else {
                    editRecipeImage.setImageBitmap(recipe.imageBitmap)
                    editRecipeImage.adjustViewBounds = true
                    editRecipeImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    context?.getColor(android.R.color.transparent)
                        ?.let { editRecipeImage.setBackgroundColor(it) }
                }

                interactionListener.validateCurrentsSaveConditions()

                editRecipeImage.setOnClickListener { showMenu(it) }

                editTitle.editText?.addTextChangedListener(object : TextWatcher {
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
                        interactionListener.updateCurrentRecipeTitle(s.toString())
                    }
                })

                editAuthor.editText?.addTextChangedListener(object : TextWatcher {
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
                        interactionListener.updateCurrentRecipeAuthor(s.toString())
                    }
                })

                editDescription.editText?.addTextChangedListener(object : TextWatcher {
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
                        interactionListener.updateCurrentRecipeDescription(s.toString())
                    }
                })
            }
        }

        private val urlDialog by lazy {
            val editText = EditText(context)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            editText.layoutParams = lp

            AlertDialog.Builder(context!!).apply {
                setView(editText);
                setMessage("Image URL")
                setTitle("Image from Internet")
                setPositiveButton(
                    R.string.url_load,
                    DialogInterface.OnClickListener { dialog, which ->
                        Glide.with(context!!)
                            .asBitmap()
                            .load(Uri.parse(editText.text.toString()))
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {
                                }

                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    interactionListener.setCurrentRecipeImage(resource)
                                }
                            })
                    })
                setNegativeButton(R.string.url_load_cancel,
                    DialogInterface.OnClickListener { dialog, which ->
                    })
            }.create()
        }

        private fun showMenu(v: View) {
            if (v.context != null) {
                PopupMenu(v.context, v).apply {
                    inflate(R.menu.edit_image_options)

                    this.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.image_from_gallery -> {
                                interactionListener.onGetRecipeImageFromGalleryClicked()
                            }
                            R.id.image_from_url -> {
                                urlDialog.show()
                            }
                            R.id.image_from_camera -> {
                                interactionListener.onGetRecipeImageFromCameraClicked()
                            }
                            R.id.delete_image -> interactionListener.deleteCurrentRecipeImage()
                            R.id.rotate_image -> interactionListener.rotateCurrentRecipeImage()
                            else -> {}
                        }
                        true
                    }
                    show()
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = RecipeItemBinding.inflate(inflator, parent, false)

        return ViewHolder(
            binding = binding,
        )
    }

//    override fun getItem(position: Int): Recipe =
//        interactionListener.getCurrentRecipe()!!.copy()

//    override fun getItemViewType(position: Int) = position

    //    override fun getItemId(position: Int) =
//        interactionListener.getCurrentRecipe()?.get(position).hashCode().toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (interactionListener.getActionState() == ActionState.EDIT)
            holder.bindEdit(getItem(position))
        else
            holder.bindPreview(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) =
            oldItem == newItem
    }
}