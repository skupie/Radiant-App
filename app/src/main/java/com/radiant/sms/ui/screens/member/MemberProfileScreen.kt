package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.radiant.sms.data.Repository
import com.radiant.sms.network.MemberProfileResponse
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun MemberProfileScreen(nav: NavController) {

    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    var profile by remember { mutableStateOf<MemberProfileResponse?>(null) }
    var shareDetails by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                profile = repo.memberProfile()
                shareDetails = repo.memberShareDetails()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load profile"
            } finally {
                isLoading = false
            }
        }
    }

    fun changePassword() {
        scope.launch {
            error = null
            success = null

            if (currentPassword.isBlank()) {
                error = "Current password is required"
                return@launch
            }

            if (newPassword.length < 6) {
                error = "New password must be at least 6 characters"
                return@launch
            }

            isLoading = true
            try {
                val res = repo.changePassword(currentPassword.trim(), newPassword.trim())
                success = res.message ?: "Password changed successfully"
                currentPassword = ""
                newPassword = ""
            } catch (e: Exception) {
                error = e.message ?: "Failed to change password"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    ScreenScaffold(
        nav = nav,
        title = "",
        hideTitle = true,
        showHamburger = true
    ) {

        if (isLoading && profile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ScreenScaffold
        }

        val displayName =
            shareDetails?.member?.displayName
                ?: profile?.member?.fullName
                ?: ""

        val photoUrl = shareDetails?.member?.displayPhotoUrl

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (success != null) {
                Text(
                    text = success!!,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    error = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                )
            }

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation =
                        if (showCurrent) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    TextButton(
                        onClick = { showCurrent = !showCurrent },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (showCurrent) "Hide" else "Show")
                    }

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation =
                        if (showNew) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    TextButton(
                        onClick = { showNew = !showNew },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (showNew) "Hide" else "Show")
                    }

                    Button(
                        onClick = { if (!isLoading) changePassword() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLoading) "Please wait..." else "Update Password")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
