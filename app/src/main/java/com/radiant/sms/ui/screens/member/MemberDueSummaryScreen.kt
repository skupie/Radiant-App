package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDueSummaryScreen(
    navController: NavController,
    memberId: Int
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val coroutineScope = rememberCoroutineScope()

    val api = remember {
        NetworkModule.createApiService {
            tokenStore.getTokenSync()
        }
    }
    val repo = remember { Repository(api) }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var dueSummary by remember { mutableStateOf<Any?>(null) }

    fun load() {
        coroutineScope.launch {
            isLoading = true
            error = null
            try {
                val res = repo.getMemberDueSummary(memberId)
                dueSummary = res
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(memberId) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Due Summary") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { load() }) { Text("Retry") }
            } else {
                Text("Due summary loaded: ${dueSummary != null}")
            }
        }
    }
}
