package com.trustengine.verifier.di

import android.content.Context
import com.trustengine.verifier.data.remote.VerihubsApiService
import com.trustengine.verifier.data.remote.VerihubsClient
import com.trustengine.verifier.nfc.NFCEKTPReader
import com.trustengine.verifier.pdf.CertificatePDFGenerator
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
    fun provideNFCEKTPReader(): NFCEKTPReader {
        return NFCEKTPReader()
    }
    
    @Provides
    @Singleton
    fun provideVerihubsClient(): VerihubsClient {
        return VerihubsClient()
    }
    
    @Provides
    @Singleton
    fun provideVerihubsApiService(verihubsClient: VerihubsClient): VerihubsApiService {
        return verihubsClient.apiService
    }
    
    @Provides
    @Singleton
    fun provideCertificatePDFGenerator(
        @ApplicationContext context: Context
    ): CertificatePDFGenerator {
        return CertificatePDFGenerator(context)
    }
}