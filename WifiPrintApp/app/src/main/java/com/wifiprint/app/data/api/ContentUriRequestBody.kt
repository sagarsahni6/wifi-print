package com.wifiprint.app.data.api

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val contentType: MediaType?,
    private val contentLength: Long
) : RequestBody() {

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = contentLength

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.use { input ->
            sink.writeAll(input.source())
        } ?: error("Cannot open file for upload")
    }
}
