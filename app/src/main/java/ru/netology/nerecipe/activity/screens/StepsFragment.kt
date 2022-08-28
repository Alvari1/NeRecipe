package ru.netology.nerecipe.activity.screens

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.adapter.StepsAdapter
import ru.netology.nerecipe.databinding.FragmentStepsBinding
import ru.netology.nerecipe.viewModel.RecipeViewModel


class StepsFragment : Fragment() {
    private val viewModel: RecipeViewModel by activityViewModels()

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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                viewModel.setActiveStepImage(bitmap)
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
            cameraLauncher.launch()
        }
    }
}