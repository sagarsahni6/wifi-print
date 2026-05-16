package com.wifiprint.app.network

import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class TlsBundle(
    val sslSocketFactory: SSLSocketFactory,
    val trustManager: X509TrustManager
)

object CertificatePinning {

    fun createTlsBundle(
        expectedFingerprint: String?,
        allowTrustOnFirstUse: Boolean,
        onCertificateSeen: ((String) -> Unit)? = null
    ): TlsBundle {
        val trustManager = FingerprintTrustManager(
            expectedFingerprint = expectedFingerprint,
            allowTrustOnFirstUse = allowTrustOnFirstUse,
            onCertificateSeen = onCertificateSeen
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        return TlsBundle(sslContext.socketFactory, trustManager)
    }

    fun sha256Fingerprint(certificate: X509Certificate): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(certificate.encoded)
        return digest.joinToString(":") { byte -> "%02X".format(byte) }
    }
}

private class FingerprintTrustManager(
    private val expectedFingerprint: String?,
    private val allowTrustOnFirstUse: Boolean,
    private val onCertificateSeen: ((String) -> Unit)?
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        val serverCertificate = chain.firstOrNull()
            ?: throw CertificateException("Missing server certificate")

        val fingerprint = CertificatePinning.sha256Fingerprint(serverCertificate)
        onCertificateSeen?.invoke(fingerprint)

        if (expectedFingerprint != null) {
            if (!expectedFingerprint.equals(fingerprint, ignoreCase = true)) {
                throw CertificateException("Trusted server certificate does not match the saved fingerprint")
            }
            return
        }

        if (!allowTrustOnFirstUse) {
            throw CertificateException("No trusted certificate fingerprint is saved for this server")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
