package com.example.transactionhistoryexercise.di

import com.example.transactionhistoryexercise.api.ApiService
import com.example.transactionhistoryexercise.ui.TransactionHistoryAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun getApiService(): ApiService = ApiService.create()

    @Provides
    @Singleton
    fun getHistoryAdapter(): TransactionHistoryAdapter = TransactionHistoryAdapter(ArrayList(), mutableSetOf())
}