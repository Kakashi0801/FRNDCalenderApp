package com.example.interviewcontent.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.interviewcontent.repository.CalanderRepository
import com.example.interviewcontent.viewmodel.CalanderViewModel


class CalanderViewModelProvider(val calanderRepository:CalanderRepository ): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalanderViewModel(calanderRepository) as T
    }
}