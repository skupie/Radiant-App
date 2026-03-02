package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
                // Fetch both for best coverage (photo is in share-details)
                val p = repo.memberProfile()
                val s = repo.memberShareDetails()
                profile = p
                shareDetails = s
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

            val cur = currentPassword.trim()
            val newP = newPassword.trim()

            if (cur.isEmpty()) {
                error = "Current password is required"
                return@launch
            }
            if (newP.isEmpty()) {
                error = "New password is required"
                return@launch
            }
            if (newP.length < 6) {
                error = "New password must be at least 6 characters"
                return@launch
            }

            isLoading = true
            try {
                val res = repo.changePassword(currentPassword = cur, newPassword = newP)
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
        if (isLoading && profile == null && shareDetails == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (success != null) {
                Text(
                    text = success ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Photo (circle)
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
                    loading = {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.height(80.dp)
                            )
                        }
                    }
                )
            }

            // Name
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )

            // Change Password Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.outlinedTextFieldColors()
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
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.outlinedTextFieldColors()
                    )
                    TextButton(
                        onClick = { showNew = !showNew },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (showNew) "Hide" else "Show")
                    }

                    Button(
                        onClick = { if (!isLoading) changePassword() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(if (isLoading) "Please wait..." else "Update Password")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
