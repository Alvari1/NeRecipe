package ru.netology.nerecipe.data.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ru.netology.nerecipe.dao.CuisineDao
import ru.netology.nerecipe.data.CuisineRepository
import ru.netology.nerecipe.dto.Cuisine
import ru.netology.nerecipe.entity.CuisineEntity

class CuisineRepositoryDao(
    private val dao: CuisineDao,
) : CuisineRepository {
    override val currentSelectedCuisines = MutableLiveData<List<Int>?>(null)

    override fun getAll(): LiveData<List<Cuisine>> = Transformations.map(dao.getAll()) { list ->
        list.map {
            it.toDto()
        }
    }

    override fun saveAll(cuisines: List<Cuisine>) {
        dao.saveAll(cuisines.map {
            CuisineEntity.fromDto(it)
        })
    }

    override fun save(cuisine: Cuisine) {
        dao.save(CuisineEntity.fromDto(cuisine))
    }

    override fun delete(id: Int) {
        dao.delete(id)
    }
}