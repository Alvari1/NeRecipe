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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nerecipe.R
import ru.netology.nerecipe.databinding.StepItemBinding
import ru.netology.nerecipe.dto.Step
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.viewModel.RecipeViewModel

internal class StepsAdapter(
    private val interactionListener: RecipeViewModel,
    private val context: Context?,
) : ListAdapter<Step, StepsAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: StepItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var step: Step

        fun bindPreview(
            position: Int
        ) {
            this.step = interactionListener.getCurrentSteps()!![position]

            with(binding) {
                recipeStepEditCard.visibility = View.GONE

                if (step.description.trim().isEmpty()) {
                    recipeStepViewCard.visibility = View.GONE
                    recipeStepEmptyCard.visibility = View.VISIBLE
                } else {
                    recipeStepViewCard.visibility = View.VISIBLE
                    recipeStepEmptyCard.visibility = View.GONE

                    recipeStepPos.text = (position + 1).toString()
                    recipeStep.text = step.description
                    recipeStep.ellipsize = null
                    recipeStep.maxLines = Integer.MAX_VALUE

                    if (step.imageBitmap == null)
                        recipeStepImage.visibility = View.GONE
                    else {
                        recipeStepImage.visibility = View.VISIBLE
                        recipeStepImage.setImageBitmap(step.imageBitmap)

                        recipeStepImage.setOnClickListener {
                            interactionListener.zoomImageFromThumb(
                                recipeStepImage,
                                step.imageBitmap!!
                            )
                        }
                    }
                }
            }
        }

        fun bindEdit(
            position: Int,
        ) {
            this.step = interactionListener.getCurrentSteps()!![position]

            with(binding) {
                recipeStepViewCard.visibility = View.GONE
                recipeStepEditCard.visibility = View.VISIBLE
                recipeStepEmptyCard.visibility = View.GONE

                recipeStepEditPos.text = (position + 1).toString()
                recipeStepEdit.editText?.setText(step.description)
                if (step.description.isEmpty())
                    recipeStepEdit.editText?.maxLines = 1
                else
                    recipeStepEdit.editText?.maxLines = Integer.MAX_VALUE

                if (step.imageBitmap == null) {
                    editStepImage.setImageResource(R.drawable.ic_add_a_photo_white_48)

                    editStepImage.adjustViewBounds = false
                    editStepImage.scaleType = ImageView.ScaleType.CENTER
                    context?.getColor(R.color.common_color)
                        ?.let { editStepImage.setBackgroundColor(it) }
                } else {
                    editStepImage.setImageBitmap(step.imageBitmap)

                    editStepImage.adjustViewBounds = true
                    editStepImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    context?.getColor(android.R.color.transparent)
                        ?.let { editStepImage.setBackgroundColor(it) }
                }

                editStepImage.setOnClickListener {
                    interactionListener.setActiveStep(step, position)
                    showMenu(it)
                }

                recipeStepEdit.editText?.addTextChangedListener(object : TextWatcher {
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
                        if (step.description != s.toString())
                            interactionListener.updateStepDescription(
                                step.id,
                                s.toString()
                            )
                        interactionListener.setActiveStep(step, position)
                    }
                })

                stepActionButton.setOnClickListener {
                    if (interactionListener.getCurrentSteps()!!.size > 1) {
                        try {
                            interactionListener.removeStep(step)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else
                        interactionListener.doSnackbar(resId = R.string.cant_delete_last_step)
                }
            }
        }


        private fun showMenu(v: View) {
            val urlDialog by lazy {
                val editText = EditText(context)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                editText.layoutParams = lp

                context?.let {
                    AlertDialog.Builder(it).apply {
                        setView(editText);
                        setMessage("Image URL")
                        setTitle("Image from Internet")
                        setPositiveButton(
                            R.string.url_load,
                            DialogInterface.OnClickListener { dialog, which ->
                                Glide.with(context)
                                    .asBitmap()
                                    .load(Uri.parse(editText.text.toString()))
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onLoadCleared(placeholder: Drawable?) {
                                        }

                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {
                                            interactionListener.setActiveStepImage(
                                                resource
                                            )
                                        }
                                    })
                            })
                        setNegativeButton(R.string.url_load_cancel,
                            DialogInterface.OnClickListener { dialog, which ->
                            })
                    }.create()
                }
            }

            if (v.context != null) {
                PopupMenu(v.context, v).apply {
                    inflate(R.menu.edit_image_options)

                    this.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.image_from_gallery -> {
                                interactionListener.onGetStepImageFromGalleryClicked()
                            }
                            R.id.image_from_url -> {
                                urlDialog?.show()
                            }
                            R.id.image_from_camera -> {
                                interactionListener.onGetStepImageFromCameraClicked()
                            }
                            R.id.delete_image -> interactionListener.deleteActiveStepImage()
                            R.id.rotate_image -> interactionListener.rotateCurrentStepImage()
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
        val binding = StepItemBinding.inflate(inflator, parent, false)

        return ViewHolder(
            binding = binding,
        )
    }

    override fun getItem(position: Int): Step =
        interactionListener.getCurrentSteps()!![position]

//    override fun getItemViewType(position: Int) = position

//    override fun getItemId(position: Int) = position.toLong()
//
//    override fun getItemCount(): Int {
//        return interactionListener.getCurrentSteps()?.size ?: 0
//    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.setIsRecyclable(false)

        if (interactionListener.getActionState() == ActionState.EDIT)
            holder.bindEdit(position)
        else
            holder.bindPreview(position)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Step>() {
        override fun areItemsTheSame(oldItem: Step, newItem: Step) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Step, newItem: Step) =
            oldItem == newItem
    }

}
