package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.util.DownloadHelper
import com.radiant.sms.AppConfig
import com.radiant.sms.data.TokenStore
import androidx.compose.ui.platform.LocalContext

@Composable
fun AdminReportsScreen(nav: NavController) {

    val context = LocalContext.current
    val token = TokenStore(context).getTokenSync()

    AdminScaffold(
        nav = nav,
        title = "Reports",
        hideTitle = false
    ) {

        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    val url = AppConfig.BASE_URL + "/api/admin/members/export/pdf"

                    DownloadHelper.downloadWithAuth(
                        context,
                        url,
                        token!!,
                        "members.pdf",
                        "application/pdf"
                    )

                }
            ) {
                Text("Members Report (PDF)")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    val url = AppConfig.BASE_URL + "/api/admin/members/export/excel"

                    DownloadHelper.downloadWithAuth(
                        context,
                        url,
                        token!!,
                        "members.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )

                }
            ) {
                Text("Members Report (Excel)")
            }

        }
    }
}
