package ru.netology.nerecipe.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.dto.Cuisine

interface CuisineRepository {
    fun getAll(): LiveData<List<Cuisine>>
    val currentSelectedCuisines: MutableLiveData<List<Int>?>

    fun delete(id: Int)
    fun saveAll(cuisines: List<Cuisine>)
    fun save(cuisine: Cuisine)
}