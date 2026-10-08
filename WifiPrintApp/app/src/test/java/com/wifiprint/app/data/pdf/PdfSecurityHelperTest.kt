package com.wifiprint.app.data.pdf

import org.junit.Assert.*
import org.junit.Test

class PdfSecurityHelperTest {

    @Test
    fun testNonPdfBytesRejected() {
        val nonPdf = "Hello World Plain Text".toByteArray()
        val result = PdfSecurityHelper.inspectPdfBytes(nonPdf)
        assertFalse(result.isPdf)
        assertEquals(1, result.pageCount)
    }

    @Test
    fun testExtractPageCountFromPagesDictionary() {
        val mockPdf = """
            %PDF-1.4
            1 0 obj
            << /Type /Catalog /Pages 2 0 R >>
            endobj
            2 0 obj
            << /Type /Pages /Kids [3 0 R 4 0 R 5 0 R] /Count 7 >>
            endobj
            %%EOF
        """.trimIndent().toByteArray(Charsets.ISO_8859_1)

        val result = PdfSecurityHelper.inspectPdfBytes(mockPdf)
        assertTrue(result.isPdf)
        assertFalse(result.isEncrypted)
        assertEquals(7, result.pageCount)
        assertTrue(result.isPasswordValid == true)
        assertFalse(result.requiresPassword)
    }

    @Test
    fun testEncryptedPdfRequiresPasswordWhenEmpty() {
        val mockEncryptedPdf = """
            %PDF-1.4
            1 0 obj
            << /Type /Catalog /Pages 2 0 R >>
            endobj
            2 0 obj
            << /Type /Pages /Count 3 >>
            endobj
            trailer
            << /Root 1 0 R /Encrypt 3 0 R >>
            %%EOF
        """.trimIndent().toByteArray(Charsets.ISO_8859_1)

        val result = PdfSecurityHelper.inspectPdfBytes(mockEncryptedPdf, password = null)
        assertTrue(result.isPdf)
        assertTrue(result.isEncrypted)
        assertEquals(3, result.pageCount)
        assertTrue(result.requiresPassword)
        assertNull(result.isPasswordValid)
    }

    @Test
    fun testLinearizedPdfPageCountExtraction() {
        val mockLinearized = """
            %PDF-1.6
            1 0 obj
            << /Linearized 1.0 /L 54321 /O 12 /E 34567 /N 15 /T 45678 >>
            endobj
            %%EOF
        """.trimIndent().toByteArray(Charsets.ISO_8859_1)

        val result = PdfSecurityHelper.inspectPdfBytes(mockLinearized)
        assertTrue(result.isPdf)
        assertEquals(15, result.pageCount)
    }
}
