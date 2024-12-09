package com.example.interviewcontent.di

import android.content.Context
import androidx.room.Room
import com.example.interviewcontent.db.DailyTaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class) // This will make the module available globally
object DatabaseModule {

    @Provides
    fun provideContext(application: android.app.Application): Context {
        return application.applicationContext
    }

    @Provides
    fun provideDailyTaskDatabase(context: Context): DailyTaskDatabase {
        return Room.databaseBuilder(
            context,
            DailyTaskDatabase::class.java,
            "dailytask_db.db"
        ).build()
    }
}


