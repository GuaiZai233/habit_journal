package com.example.habbitjournal.core.di

import com.example.habbitjournal.data.repository.GoodHabitRepositoryImpl
import com.example.habbitjournal.domain.repository.GoodHabitRepository
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
    abstract fun bindGoodHabitRepository(impl: GoodHabitRepositoryImpl): GoodHabitRepository
}
