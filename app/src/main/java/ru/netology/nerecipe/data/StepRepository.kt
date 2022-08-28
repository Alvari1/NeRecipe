package ru.netology.nerecipe.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.dto.Step
import ru.netology.nerecipe.util.EMPTY_UUID
import java.util.*

interface StepRepository {
    fun getAll(context: Context): LiveData<List<Step>>
    val currentSteps: MutableLiveData<List<Step>?>
    val stepImages: MutableLiveData<HashMap<UUID, String?>>
    val activeStep: MutableLiveData<Step?>

    fun save(steps: List<Step>)

    fun deleteByRecipeId(recipeId: UUID)

    companion object {
        val EMPTY_STEP = Step(
            id = EMPTY_UUID,
            recipeId = EMPTY_UUID,
            position = 0,
            description = "",
        )
    }
}