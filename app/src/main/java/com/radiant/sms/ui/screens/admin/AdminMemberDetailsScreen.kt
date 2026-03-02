package com.radiant.sms.ui.screens.admin

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.radiant.sms.data.NetworkModule
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDetailsDto
import com.radiant.sms.util.MultipartUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import com.radiant.sms.network.NetworkModule

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
    nav: androidx.navigation.NavController,
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

    val pickMemberPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        memberPhotoUri = uri
    }
    val pickNomineePhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        nomineePhotoUri = uri
    }

    // populate once loaded
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

    AdminScaffold(nav = nav, title = "Update Member", hideTitle = false, showHamburger = true) {
        when {
            s.loading -> {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }
            s.error != null -> {
                Text(s.error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }
        }

        val m = s.member

        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(fullName, { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(nid, { nid = it }, label = { Text("NID") }, modifier = Modifier.weight(1f))
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.weight(1f))
                OutlinedTextField(share, { share = it }, label = { Text("Share Count") }, modifier = Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    newPassword,
                    { newPassword = it },
                    label = { Text("New Password (Optional)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    confirmPassword,
                    { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(nomineeName, { nomineeName = it }, label = { Text("Nominee Name") }, modifier = Modifier.weight(1f))
                OutlinedTextField(nomineeNid, { nomineeNid = it }, label = { Text("Nominee NID") }, modifier = Modifier.weight(1f))
            }

            Text("Member Photo (JPG)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { pickMemberPhoto.launch("image/*") }) { Text("Choose file") }
                Spacer(Modifier.weight(1f))
            }
            m?.imageUrl?.let { url ->
                if (memberPhotoUri == null) {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.height(120.dp))
                }
            }

            Text("Nominee Photo (JPG)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { pickNomineePhoto.launch("image/*") }) { Text("Choose file") }
                Spacer(Modifier.weight(1f))
            }
            m?.nomineePhotoUrl?.let { url ->
                if (nomineePhotoUri == null) {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.height(120.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (newPassword.isNotBlank() && newPassword != confirmPassword) {
                        Toast.makeText(context, "Password mismatch", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

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
                    }
                }
            ) {
                Text("Save Changes")
            }
        }
    }
}
