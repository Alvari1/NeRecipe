package ru.netology.nerecipe.activity.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.jetbrains.annotations.Nullable
import ru.netology.nerecipe.adapter.StepsAdapter
import ru.netology.nerecipe.databinding.FragmentStepsBinding
import ru.netology.nerecipe.viewModel.RecipeViewModel
import java.io.File


class StepsFragment : Fragment() {
    private val viewModel: RecipeViewModel by activityViewModels()

    private lateinit var uri: Uri
    
    private val simpleCallback =
        object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val startPosition = viewHolder.adapterPosition
                val endPosition = target.adapterPosition

                viewModel.swapCurrentSteps(startPosition, endPosition)
                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
                return true
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val imageStream = context?.contentResolver?.openInputStream(it)
            viewModel.setActiveStepImage(
                BitmapFactory.decodeStream(
                    imageStream
                )
            )
        }
    }

    var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                try {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(uri)
                        .into(object : CustomTarget<Bitmap?>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                viewModel.setCurrentRecipeImage(resource)
                                context?.contentResolver?.delete(uri, null, null)
                            }

                            override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                        })
                } catch (e: Exception) {
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStepsBinding.inflate(inflater, container, false)

        val adapter = StepsAdapter(viewModel, context)

        binding.stepsRecyclerView.adapter = adapter
        adapter.submitList(viewModel.getCurrentSteps()?.toList())

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.stepsRecyclerView)

        if (!binding.stepsRecyclerView.canScrollVertically(-1))
            binding.stepScrollUpButton.visibility = View.GONE
        else
            binding.stepScrollUpButton.visibility = View.VISIBLE

        binding.stepsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(-1))
                    binding.stepScrollUpButton.visibility = View.GONE
                else
                    binding.stepScrollUpButton.visibility = View.VISIBLE
            }
        })

        binding.stepScrollUpButton.setOnClickListener {
            binding.stepScrollUpButton.visibility = View.GONE
            binding.stepsRecyclerView.smoothScrollToPosition(0)
        }

        viewModel.navigateCurrentStepsChanged.observe(viewLifecycleOwner) {
            adapter.submitList(viewModel.getCurrentSteps()?.toList())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateStepGetImageFromGallery.observe(viewLifecycleOwner) {
            galleryLauncher.launch("image/*")
        }

        viewModel.navigateStepGetImageFromCamera.observe(viewLifecycleOwner) {
            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            cameraLauncher.launch(uri)
        }
    }
}