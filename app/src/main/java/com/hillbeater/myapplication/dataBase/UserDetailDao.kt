package com.hillbeater.myapplication.dataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hillbeater.myapplication.api.UserDetail

@Dao
interface UserDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInfo(userInfo: UserDetail)

    @Query("SELECT * FROM user_detail LIMIT 1")
    suspend fun getUserDetail(): UserDetail?

    @Query("DELETE FROM user_detail")
    suspend fun deleteUser()
}