package com.radiant.sms.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri

object DownloadHelper {

    fun downloadWithAuth(
        context: Context,
        url: String,
        token: String,
        fileName: String,
        mimeType: String
    ) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading...")
            .setMimeType(mimeType)
            .addRequestHeader("Authorization", "Bearer $token")
            .addRequestHeader("Accept", "application/json")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}
