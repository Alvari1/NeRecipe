package ru.netology.nerecipe.activity.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.adapter.IngredientsAdapter
import ru.netology.nerecipe.databinding.FragmentIngredientsBinding
import ru.netology.nerecipe.viewModel.RecipeViewModel


class IngredientsFragment : Fragment() {
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

                viewModel.swapCurrentIngredients(startPosition, endPosition)
                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentIngredientsBinding.inflate(inflater, container, false)

        val adapter = IngredientsAdapter(viewModel, context)

        binding.ingredientsRecyclerView.adapter = adapter
        adapter.submitList(viewModel.getCurrentIngredients()?.toList())

        viewModel.navigateCurrentIngredientsChanged.observe(viewLifecycleOwner) {
            adapter.submitList(viewModel.getCurrentIngredients()?.toList())
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.ingredientsRecyclerView)

        if (!binding.ingredientsRecyclerView.canScrollVertically(-1))
            binding.ingredientScrollUpButton.visibility = View.GONE
        else
            binding.ingredientScrollUpButton.visibility = View.VISIBLE

        binding.ingredientsRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(-1))
                    binding.ingredientScrollUpButton.visibility = View.GONE
                else
                    binding.ingredientScrollUpButton.visibility = View.VISIBLE
            }
        })

        binding.ingredientScrollUpButton.setOnClickListener {
            binding.ingredientScrollUpButton.visibility = View.GONE
            binding.ingredientsRecyclerView.smoothScrollToPosition(0)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}