package ru.netology.nerecipe.data.impl

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ru.netology.nerecipe.dao.StepDao
import ru.netology.nerecipe.data.StepRepository
import ru.netology.nerecipe.dto.Step
import ru.netology.nerecipe.entity.StepEntity
import ru.netology.nerecipe.util.ImageStorageManager.Companion.isImageExistInExternalStorage
import java.util.*

class StepRepositoryDao(
    private val dao: StepDao,
) : StepRepository {
    override val currentSteps = MutableLiveData<List<Step>?>(null)
    override val stepImages = MutableLiveData<HashMap<UUID, String?>>(null)
    override val activeStep = MutableLiveData<Step?>(null)


    override fun getAll(context: Context): LiveData<List<Step>> =
        Transformations.map(dao.getAll()) { list ->
            list.map { it.toDto().copy(image = isImageExist(context, it.image)) }
        }

    private fun isImageExist(context: Context, imageFileName: String?): String? =
        if (imageFileName.isNullOrEmpty()) null
        else
//            if (isImageExistInInternalStorage(context, imageFileName))
            if (isImageExistInExternalStorage(imageFileName))
                imageFileName
            else null

    override fun save(
        steps: List<Step>,
    ) {
        dao.save(steps.map { StepEntity.fromDto(it) })
    }

    override fun deleteByRecipeId(recipeId: UUID) {
        dao.deleteByRecipeId(recipeId)
    }

}
