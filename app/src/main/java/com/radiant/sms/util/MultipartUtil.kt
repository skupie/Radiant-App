package com.radiant.sms.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object MultipartUtil {

    fun textPart(name: String, value: String?): MultipartBody.Part {
        val v = (value ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, null, v)
    }

    fun boolPart(name: String, value: Boolean): MultipartBody.Part {
        val v = (if (value) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, null, v)
    }

    fun filePart(context: Context, name: String, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val contentResolver = context.contentResolver
        val mime = contentResolver.getType(uri) ?: "image/*"

        val input = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)

        FileOutputStream(tempFile).use { out ->
            input.use { it.copyTo(out) }
        }

        val body = tempFile.asRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, tempFile.name, body)
    }
}
