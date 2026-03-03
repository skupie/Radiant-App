package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radiant.sms.data.NetworkModule
import com.radiant.sms.network.AdminDepositItem
import kotlinx.coroutines.launch

@Composable
fun AdminDepositsScreen() {

    val repo = NetworkModule.repository
    val scope = rememberCoroutineScope()

    var deposits by remember { mutableStateOf<List<AdminDepositItem>>(emptyList()) }
    var total by remember { mutableStateOf(0.0) }
    var page by remember { mutableStateOf(1) }
    var lastPage by remember { mutableStateOf(1) }

    fun load() {
        scope.launch {
            val res = repo.adminDepositsList(page = page)
            deposits = res.data
            total = res.summary?.filteredTotal ?: 0.0
            lastPage = res.meta?.lastPage ?: 1
        }
    }

    LaunchedEffect(page) { load() }

    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Deposits", style = MaterialTheme.typography.titleLarge)
            Text("Total: BDT %.2f".format(total))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(deposits) { deposit ->
                DepositCard(deposit)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                enabled = page > 1,
                onClick = { page-- }
            ) { Text("Prev") }

            Text("Page $page of $lastPage")

            Button(
                enabled = page < lastPage,
                onClick = { page++ }
            ) { Text("Next") }
        }
    }
}

@Composable
fun DepositCard(item: AdminDepositItem) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.member?.name ?: "")
            Text("${item.month} ${item.year}")
            Text("Base: ${item.baseAmount}")
            Text("Total: ${item.totalAmount}")
            Text("Type: ${item.type}")
            Text("Logged: ${item.loggedAt}")
        }
    }
}
