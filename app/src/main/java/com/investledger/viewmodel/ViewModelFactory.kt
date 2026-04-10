package com.investledger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.investledger.data.AppDatabase

/**
 * ViewModel 工厂
 */
class InvestViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvestViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            return InvestViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}