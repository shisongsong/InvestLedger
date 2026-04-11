package com.investledger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.investledger.data.AppDatabase
import com.investledger.data.StatisticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * ViewModel 工厂
 */
class InvestViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvestViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val statisticsService = StatisticsService(
                statisticsDao = database.statisticsDao(),
                coroutineScope = CoroutineScope(SupervisorJob())
            )
            @Suppress("UNCHECKED_CAST")
            return InvestViewModel(database, statisticsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
