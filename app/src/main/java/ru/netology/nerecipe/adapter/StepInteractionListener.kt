package ru.netology.nerecipe.adapter

import ru.netology.nerecipe.dto.Step
import java.util.*

interface StepInteractionListener {
    fun addEmptyStep(recipeId: UUID)
    fun removeStep(step: Step)

}