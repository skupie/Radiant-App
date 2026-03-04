package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminActivityDto
import com.radiant.sms.network.AdminTeamMemberDto
import com.radiant.sms.network.AdminTeamMemberUpsertRequest
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

/**
 * Admin Panel:
 *  - Admin Activity feed
 *  - Team Members management (create / edit / delete + role)
 *
 * (Per request) No Member list, and no PDF/Excel export buttons here.
 */
@Composable
fun AdminPanelScreen(
    nav: NavController,
    modifier: Modifier = Modifier
) {
    val api = remember { NetworkModule.api(nav.context) }
    val repo = remember { Repository(api) }

    val screenBg = Color(0xFFF6F7FB)
    val cardBg = Color.White
    val subtleText = Color(0xFF6B7280)
    val cardShape = RoundedCornerShape(22.dp)

    var tabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Admin",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))

                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("Activity") }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("Team Members") }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when (tabIndex) {
            0 -> AdminActivityTab(
                repo = repo,
                cardBg = cardBg,
                subtleText = subtleText,
                cardShape = cardShape
            )

            else -> AdminTeamMembersTab(
                repo = repo,
                cardBg = cardBg,
                subtleText = subtleText,
                cardShape = cardShape
            )
        }
    }
}

@Composable
private fun AdminActivityTab(
    repo: Repository,
    cardBg: Color,
    subtleText: Color,
    cardShape: RoundedCornerShape
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<AdminActivityDto>>(emptyList()) }

    suspend fun load() {
        loading = true
        error = null
        try {
            items = repo.adminActivity()
        } catch (e: Exception) {
            error = e.message ?: "Failed to load activity"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Couldn’t load activity", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { scope.launch { load() } }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Text("Retry", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        items.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("No admin activity yet", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "When admins create/update/delete team members or perform actions, they will appear here.",
                        color = subtleText
                    )
                }
            }
        }

        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { a ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = a.action ?: "Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = buildString {
                                if (!a.actorName.isNullOrBlank()) append("By ${a.actorName}")
                                if (!a.createdAt.isNullOrBlank()) {
                                    if (isNotEmpty()) append("  •  ")
                                    append(a.createdAt)
                                }
                            }.ifBlank { "" },
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText
                        )
                        if (!a.description.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(a.description ?: "", color = subtleText)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTeamMembersTab(
    repo: Repository,
    cardBg: Color,
    subtleText: Color,
    cardShape: RoundedCornerShape
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<AdminTeamMemberDto>>(emptyList()) }

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AdminTeamMemberDto?>(null) }

    var confirmDeleteId by remember { mutableStateOf<Long?>(null) }

    suspend fun load() {
        loading = true
        error = null
        try {
            items = repo.adminTeamMembers()
        } catch (e: Exception) {
            error = e.message ?: "Failed to load team members"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Team Members", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Create, edit or delete team members and set their role.", color = subtleText)
            }
            IconButton(
                onClick = {
                    editing = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Couldn’t load team members", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { scope.launch { load() } }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text("Retry", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = u.name ?: "Unnamed",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(u.email ?: "", style = MaterialTheme.typography.bodySmall, color = subtleText)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Role", style = MaterialTheme.typography.labelSmall, color = subtleText)
                                Text(
                                    text = (u.role ?: "-").uppercase(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Divider()
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    editing = u
                                    showDialog = true
                                }
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }

                            IconButton(
                                onClick = {
                                    val id = u.id ?: return@IconButton
                                    confirmDeleteId = id
                                }
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Delete confirm dialog
    if (confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Delete team member?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = confirmDeleteId
                        confirmDeleteId = null
                        if (id == null) return@TextButton

                        scope.launch {
                            loading = true
                            error = null
                            try {
                                repo.adminDeleteTeamMember(id)
                                load()
                            } catch (e: Exception) {
                                error = e.message ?: "Delete failed"
                                loading = false
                            }
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteId = null }) { Text("Cancel") } }
        )
    }

    // Create / Edit dialog
    if (showDialog) {
        TeamMemberUpsertDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { req ->
                val id = editing?.id
                showDialog = false

                scope.launch {
                    loading = true
                    error = null
                    try {
                        if (id == null) repo.adminCreateTeamMember(req) else repo.adminUpdateTeamMember(id, req)
                        load()
                    } catch (e: Exception) {
                        error = e.message ?: "Save failed"
                        loading = false
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamMemberUpsertDialog(
    initial: AdminTeamMemberDto?,
    onDismiss: () -> Unit,
    onSave: (AdminTeamMemberUpsertRequest) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var password by remember { mutableStateOf("") }

    // ✅ Jetstream roles (NO viewer)
    val roles = listOf("admin", "editor")
    var role by remember { mutableStateOf(initial?.role ?: "editor") }
    var roleMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Team Member" else "Edit Team Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (initial == null) "Password" else "Password (leave blank to keep)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { roleMenu = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Role")
                            }
                        }
                    )
                    DropdownMenu(expanded = roleMenu, onDismissRequest = { roleMenu = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.uppercase()) },
                                onClick = {
                                    role = r
                                    roleMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AdminTeamMemberUpsertRequest(
                            name = name.trim().ifBlank { null },
                            email = email.trim().ifBlank { null },
                            password = password.ifBlank { null },
                            role = role.trim().ifBlank { null }
                        )
                    )
                },
                enabled = name.trim().isNotBlank() && email.trim().isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
