package com.radiant.sms.ui.screens.admin

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDetailsDto
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.util.MultipartUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

data class AdminMemberDetailsState(
    val loading: Boolean = true,
    val error: String? = null,
    val member: AdminMemberDetailsDto? = null
)

class AdminMemberDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(NetworkModule.api(app.applicationContext))

    private val _state = MutableStateFlow(AdminMemberDetailsState())
    val state: StateFlow<AdminMemberDetailsState> = _state

    fun load(memberId: Long) {
        viewModelScope.launch {
            try {
                _state.value = AdminMemberDetailsState(loading = true)
                val resp = repo.adminMemberDetails(memberId)
                _state.value = AdminMemberDetailsState(loading = false, member = resp.member)
            } catch (e: Exception) {
                _state.value = AdminMemberDetailsState(loading = false, error = e.message ?: "Failed")
            }
        }
    }

    fun update(memberId: Long, parts: List<MultipartBody.Part>, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.adminUpdateMember(memberId, parts)
                onDone()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Update failed")
            }
        }
    }
}

@Composable
fun AdminMemberDetailsScreen(
    nav: NavController,
    memberId: Long,
    vm: AdminMemberDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val s by vm.state.collectAsState()

    LaunchedEffect(memberId) { vm.load(memberId) }

    var fullName by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var share by remember { mutableStateOf("1") }
    var nomineeName by remember { mutableStateOf("") }
    var nomineeNid by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var memberPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var nomineePhotoUri by remember { mutableStateOf<Uri?>(null) }

    var showUpdateConfirm by remember { mutableStateOf(false) }

    val pickMemberPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        memberPhotoUri = uri
    }
    val pickNomineePhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        nomineePhotoUri = uri
    }

    LaunchedEffect(s.member) {
        s.member?.let { m ->
            fullName = m.fullName ?: ""
            nid = m.nid ?: ""
            email = m.email ?: ""
            mobile = m.mobileNumber ?: ""
            share = (m.share ?: 1).toString()
            nomineeName = m.nomineeName ?: ""
            nomineeNid = m.nomineeNid ?: ""
        }
    }

    val scrollState = rememberScrollState()

    AdminScaffold(nav = nav, title = "Update Member", hideTitle = false, showHamburger = true) {

        // Make whole content scrollable so Save button is reachable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            when {
                s.loading -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                s.error != null -> {
                    Text(s.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
            }

            val m = s.member

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nid,
                    onValueChange = { nid = it },
                    label = { Text("NID") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = share,
                    onValueChange = { share = it },
                    label = { Text("Share Count") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (Optional)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nomineeName,
                    onValueChange = { nomineeName = it },
                    label = { Text("Nominee Name") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = nomineeNid,
                    onValueChange = { nomineeNid = it },
                    label = { Text("Nominee NID") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Member Photo (JPG)")
            Button(onClick = { pickMemberPhoto.launch("image/*") }) { Text("Choose file") }

            m?.imageUrl?.let { url ->
                if (memberPhotoUri == null) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("Nominee Photo (JPG)")
            Button(onClick = { pickNomineePhoto.launch("image/*") }) { Text("Choose file") }

            m?.nomineePhotoUrl?.let { url ->
                if (nomineePhotoUri == null) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Save button (will always be reachable due to scroll)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (newPassword.isNotBlank() && newPassword != confirmPassword) {
                        Toast.makeText(context, "Password mismatch", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showUpdateConfirm = true
                }
            ) {
                Text("Save Changes")
            }

            // Extra bottom space so button isn't stuck at edge
            Spacer(Modifier.height(24.dp))

            if (showUpdateConfirm) {
                AlertDialog(
                    onDismissRequest = { showUpdateConfirm = false },
                    title = { Text("Confirm Update") },
                    text = { Text("Save these changes for this member?") },
                    confirmButton = {
                        Button(onClick = {
                            showUpdateConfirm = false

                            val parts = mutableListOf<MultipartBody.Part>()
                            parts += MultipartUtil.textPart("full_name", fullName)
                            parts += MultipartUtil.textPart("nid", nid)
                            parts += MultipartUtil.textPart("email", email)
                            parts += MultipartUtil.textPart("mobile_number", mobile)
                            parts += MultipartUtil.textPart("nominee_name", nomineeName)
                            parts += MultipartUtil.textPart("nominee_nid", nomineeNid)
                            parts += MultipartUtil.textPart("share", share)

                            if (newPassword.isNotBlank()) {
                                parts += MultipartUtil.textPart("password", newPassword)
                            }

                            MultipartUtil.filePart(context, "image", memberPhotoUri)?.let { parts += it }
                            MultipartUtil.filePart(context, "nominee_photo", nomineePhotoUri)?.let { parts += it }

                            vm.update(memberId, parts) {
                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                vm.load(memberId)
                                nav.previousBackStackEntry?.savedStateHandle?.set("members_refresh", true)
                            }
                        }) { Text("Confirm") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showUpdateConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
