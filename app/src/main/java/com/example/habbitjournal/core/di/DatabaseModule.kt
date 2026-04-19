package com.example.habbitjournal.core.di

import android.content.Context
import androidx.room.Room
import com.example.habbitjournal.core.database.AppDatabase
import com.example.habbitjournal.data.local.dao.GoodHabitLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "habbit_journal.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideGoodHabitLogDao(db: AppDatabase): GoodHabitLogDao = db.goodHabitLogDao()
}
