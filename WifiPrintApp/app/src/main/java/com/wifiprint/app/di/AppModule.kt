package com.wifiprint.app.di

import android.content.Context
import androidx.room.Room
import com.wifiprint.app.data.api.PrintApiService
import com.wifiprint.app.data.db.AppDatabase
import com.wifiprint.app.data.db.PrintJobDao
import com.wifiprint.app.data.db.ServerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt DI module providing singletons for database, networking, and API services.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wifiprint.db")
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun providePrintJobDao(db: AppDatabase): PrintJobDao = db.printJobDao()

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    /**
     * Shared OkHttp baseline. Per-server TLS handling is configured in the repository
     * so we can enforce a pinned certificate fingerprint for each saved server.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit instance — base URL is set dynamically after server discovery,
     * so we use a placeholder that gets overridden by the repository.
     */
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://localhost:5000/") // Placeholder, overridden per-request
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePrintApiService(retrofit: Retrofit): PrintApiService =
        retrofit.create(PrintApiService::class.java)
}
