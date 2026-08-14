package com.tracker.finance_app.di

import com.tracker.finance_app.data.repository.AccountRepositoryImpl
import com.tracker.finance_app.data.repository.AuthRepositoryImpl
import com.tracker.finance_app.data.repository.CategoryRepositoryImpl
import com.tracker.finance_app.data.repository.SmsRepositoryImpl
import com.tracker.finance_app.data.repository.TransactionRepositoryImpl
import com.tracker.finance_app.domain.repository.AccountRepository
import com.tracker.finance_app.domain.repository.AuthRepository
import com.tracker.finance_app.domain.repository.CategoryRepository
import com.tracker.finance_app.domain.repository.SmsRepository
import com.tracker.finance_app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository
}
