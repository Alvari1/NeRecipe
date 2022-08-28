package ru.netology.nerecipe.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nerecipe.entity.CuisineEntity

@Dao
interface CuisineDao {
    @Query("SELECT * FROM CuisineEntity ORDER BY ID, resName ASC")
    fun getAll(): LiveData<List<CuisineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cuisine: CuisineEntity)

    fun saveAll(cuisines: List<CuisineEntity>) {
        if (cuisines.isNotEmpty()) {
            cuisines.forEach { insert(it) }
        }
    }

    fun save(cuisine: CuisineEntity) {
        insert(cuisine)
    }

    @Query("DELETE FROM CuisineEntity WHERE id = :id")
    fun delete(id: Int)
}