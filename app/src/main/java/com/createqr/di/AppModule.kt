package com.createqr.di

import android.content.Context
import androidx.room.Room
import com.createqr.data.local.AppDatabase
import com.createqr.data.local.QRItemDao
import com.createqr.data.repository.QRGeneratorRepositoryImpl
import com.createqr.data.repository.QRItemRepositoryImpl
import com.createqr.data.repository.QRTypeRepositoryImpl
import com.createqr.domain.repository.QRGeneratorRepository
import com.createqr.domain.repository.QRItemRepository
import com.createqr.domain.repository.QRTypeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "createqr_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideQRItemDao(database: AppDatabase): QRItemDao {
        return database.qrItemDao()
    }

    @Provides
    @Singleton
    fun provideQRTypeRepository(): QRTypeRepository {
        return QRTypeRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideQRItemRepository(qrItemDao: QRItemDao): QRItemRepository {
        return QRItemRepositoryImpl(qrItemDao)
    }

    @Provides
    @Singleton
    fun provideQRGeneratorRepository(): QRGeneratorRepository {
        return QRGeneratorRepositoryImpl()
    }
}
