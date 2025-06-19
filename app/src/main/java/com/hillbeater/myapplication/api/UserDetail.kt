package com.hillbeater.myapplication.api

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_detail")
data class UserDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val company_name: String,
    val dob: String,
    val email: String,
    val first_name: String,
    val is_working: Boolean,
    val last_name: String
)

data class Response(
    val status: Boolean,
    val message: String
)

