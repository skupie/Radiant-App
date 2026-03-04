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

    // ✅ Wrap in AdminScaffold so drawer/hamburger appears
    AdminScaffold(nav = nav, showHamburger = true) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(screenBg)
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
                    OutlinedButton(onClick = { scope.launch { load() } }) {
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
                            color = subtleText
                        )
                        if (!a.details.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(a.details ?: "", color = subtleText)
                        }
                    }
                }
            }
        }
    }
}

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
    var members by remember { mutableStateOf<List<AdminTeamMemberDto>>(emptyList()) }

    var showCreate by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf<AdminTeamMemberDto?>(null) }
    var showDelete by remember { mutableStateOf<AdminTeamMemberDto?>(null) }

    suspend fun load() {
        loading = true
        error = null
        try {
            members = repo.adminTeamMembers()
        } catch (e: Exception) {
            error = e.message ?: "Failed to load team members"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(onClick = { showCreate = true }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add", modifier = Modifier.padding(start = 8.dp))
        }
    }

    Spacer(Modifier.height(10.dp))

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
        }

        members.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("No team members", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Create your first team member from the Add button.", color = subtleText)
                }
            }
        }

        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(members) { m ->
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
                                    text = m.name ?: "Team Member",
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = (m.email ?: "").ifBlank { "-" },
                                    color = subtleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row {
                                IconButton(onClick = { showEdit = m }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { showDelete = m }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(top = 12.dp))

                        Spacer(Modifier.height(10.dp))

                        RoleSelector(
                            currentRole = m.role ?: "staff",
                            onRoleSelected = { role ->
                                scope.launch {
                                    try {
                                        repo.adminTeamMemberUpsert(
                                            AdminTeamMemberUpsertRequest(
                                                id = m.id,
                                                name = m.name ?: "",
                                                email = m.email ?: "",
                                                role = role
                                            )
                                        )
                                        load()
                                    } catch (_: Exception) { }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // dialogs kept as-is
    if (showCreate) {
        TeamMemberDialog(
            title = "Create Team Member",
            initialName = "",
            initialEmail = "",
            initialRole = "staff",
            onDismiss = { showCreate = false },
            onSave = { name, email, role ->
                scope.launch {
                    try {
                        repo.adminTeamMemberUpsert(
                            AdminTeamMemberUpsertRequest(
                                id = null,
                                name = name,
                                email = email,
                                role = role
                            )
                        )
                        showCreate = false
                        load()
                    } catch (_: Exception) { }
                }
            }
        )
    }

    showEdit?.let { member ->
        TeamMemberDialog(
            title = "Edit Team Member",
            initialName = member.name ?: "",
            initialEmail = member.email ?: "",
            initialRole = member.role ?: "staff",
            onDismiss = { showEdit = null },
            onSave = { name, email, role ->
                scope.launch {
                    try {
                        repo.adminTeamMemberUpsert(
                            AdminTeamMemberUpsertRequest(
                                id = member.id,
                                name = name,
                                email = email,
                                role = role
                            )
                        )
                        showEdit = null
                        load()
                    } catch (_: Exception) { }
                }
            }
        )
    }

    showDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { showDelete = null },
            title = { Text("Delete team member?") },
            text = { Text("This will permanently delete ${member.name ?: "this member"}.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            repo.adminTeamMemberDelete(member.id ?: return@launch)
                            showDelete = null
                            load()
                        } catch (_: Exception) { }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RoleSelector(
    currentRole: String,
    onRoleSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("admin", "staff")

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Role: ${currentRole.uppercase()}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.uppercase()) },
                    onClick = {
                        expanded = false
                        onRoleSelected(role)
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamMemberDialog(
    title: String,
    initialName: String,
    initialEmail: String,
    initialRole: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var role by remember { mutableStateOf(initialRole) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                RoleSelector(currentRole = role, onRoleSelected = { role = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), email.trim(), role) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
