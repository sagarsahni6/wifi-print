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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

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
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePrintJobDao(db: AppDatabase): PrintJobDao = db.printJobDao()

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    /**
     * OkHttpClient that trusts self-signed certificates for local network use.
     * In production, you'd pin certificates instead.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Trust all certs for self-signed HTTPS on local network
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
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
