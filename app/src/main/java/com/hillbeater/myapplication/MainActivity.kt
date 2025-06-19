package com.hillbeater.myapplication

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hillbeater.myapplication.api.RetrofitClient
import com.hillbeater.myapplication.api.UserDetail
import com.hillbeater.myapplication.dataBase.AppDatabase
import com.hillbeater.myapplication.databinding.ActivityMainBinding
import com.hillbeater.myapplication.utils.NetworkChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitButton.setOnClickListener {
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val companyName = binding.companyName.text.toString().trim()
            val dob = binding.dob.text.toString().trim()
            val isWorking = binding.isWorkingCheckBox.isChecked

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || companyName.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Please fill in all details.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isLoading(true)

            val userDetail = UserDetail(
                first_name = firstName,
                last_name = lastName,
                email = email,
                dob = dob,
                is_working = isWorking,
                company_name = companyName
            )

            if (isNetworkConnected(this@MainActivity)) {
                syncDataToServer(userDetail)
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@MainActivity).userDetailDao()
                        .insertUserInfo(userDetail)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Saved locally (In Database).",
                            Toast.LENGTH_SHORT
                        ).show()
                        isLoading(false)
                        clearForm()
                    }
                }
            }
        }

    }

    private fun syncDataToServer(userDetail: UserDetail) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.request(userDetail)
                withContext(Dispatchers.Main) {
                    if (response.status && response.message == "User saved successfully.") {
                        Toast.makeText(this@MainActivity, response.message, Toast.LENGTH_SHORT)
                            .show()
                        clearForm()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to save user. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    isLoading(false)
                }
            }
        }
    }

    private fun clearForm() {
        binding.firstName.text?.clear()
        binding.lastName.text?.clear()
        binding.email.text?.clear()
        binding.companyName.text?.clear()
        binding.dob.text?.clear()
        binding.isWorkingCheckBox.isChecked = false
    }

    private fun isLoading(isLoading: Boolean) {
        binding.submitTextView.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.progressCircle.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun isNetworkConnected(context: MainActivity): Boolean {

        val connectivityManager =  context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private suspend fun getUserDetailFromDb(): UserDetail? {
        val userData = AppDatabase.getDatabase(this).userDetailDao().getUserDetail()
        if(userData != null){
            return userData
        }
        return null
    }

    private suspend fun deleteDataFromDb() {
        AppDatabase.getDatabase(this).userDetailDao().deleteUser()
    }

    override fun onResume() {
        super.onResume()
        networkReceiver = NetworkChangeReceiver {
            syncDataIfExistsInDB()
        }
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    private fun syncDataIfExistsInDB() {
        lifecycleScope.launch {
            val user = getUserDetailFromDb()
            if (user != null) {
                withContext(Dispatchers.Main) {isLoading(true)}
                Toast.makeText(this@MainActivity, "Syncing old data to server, please wait...", Toast.LENGTH_LONG).show()
                syncDataToServer(user)
                Toast.makeText(this@MainActivity, "User saved successfully.", Toast.LENGTH_SHORT).show()
                deleteDataFromDb()
                withContext(Dispatchers.Main) {isLoading(false)}
            }
        }
    }

}

