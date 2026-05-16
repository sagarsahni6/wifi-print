package com.wifiprint.app.network

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger
import java.security.Principal
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class CertificatePinningTest {

    @Test
    fun `sha256 fingerprint is uppercase colon separated`() {
        val certificate = object : X509Certificate() {
            override fun getEncoded(): ByteArray = byteArrayOf(0x01, 0x02, 0x03, 0x04)
            override fun checkValidity() = Unit
            override fun checkValidity(date: Date?) = Unit
            override fun getVersion(): Int = 3
            override fun getSerialNumber(): BigInteger = BigInteger.ONE
            override fun getIssuerDN(): Principal = X500Principal("CN=issuer")
            override fun getSubjectDN(): Principal = X500Principal("CN=subject")
            override fun getNotBefore(): Date = Date()
            override fun getNotAfter(): Date = Date()
            override fun getTBSCertificate(): ByteArray = byteArrayOf()
            override fun getSignature(): ByteArray = byteArrayOf()
            override fun getSigAlgName(): String = "SHA256withRSA"
            override fun getSigAlgOID(): String = "1.2.840.113549.1.1.11"
            override fun getSigAlgParams(): ByteArray? = null
            override fun getIssuerUniqueID(): BooleanArray? = null
            override fun getSubjectUniqueID(): BooleanArray? = null
            override fun getKeyUsage(): BooleanArray? = null
            override fun getBasicConstraints(): Int = -1
            override fun verify(key: PublicKey?) = Unit
            override fun verify(key: PublicKey?, sigProvider: String?) = Unit
            override fun toString(): String = "fake-cert"
            override fun getPublicKey(): PublicKey {
                throw UnsupportedOperationException("Not needed for this test")
            }

            override fun hasUnsupportedCriticalExtension(): Boolean = false
            override fun getCriticalExtensionOIDs(): MutableSet<String>? = null
            override fun getNonCriticalExtensionOIDs(): MutableSet<String>? = null
            override fun getExtensionValue(oid: String?): ByteArray? = null
        }

        val fingerprint = CertificatePinning.sha256Fingerprint(certificate)

        assertEquals(
            "9F:64:A7:47:E1:B9:7F:13:1F:AB:B6:B4:47:29:6C:9B:6F:02:01:E7:9F:B3:C5:35:6E:6C:77:E8:9B:6A:80:6A",
            fingerprint
        )
    }
}
