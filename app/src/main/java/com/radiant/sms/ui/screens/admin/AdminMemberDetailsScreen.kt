package com.radiant.sms.ui.screens.admin

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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
    val cardShape = RoundedCornerShape(18.dp)

    // ✅ Existing URLs from API (absolute-safe)
    val memberPhotoUrl = NetworkModule.absoluteUrl(s.member?.imageUrl)
    val nomineePhotoUrl = NetworkModule.absoluteUrl(s.member?.nomineePhotoUrl)

    AdminScaffold(nav = nav, title = "Update Member", hideTitle = false, showHamburger = true) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                // ✅ FIX: keep content close to top, but safe from system bars
                .padding(top = 6.dp)
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (s.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            if (s.error != null) {
                Text(s.error ?: "", color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password\n(Optional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = false
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm\nPassword") },
                    modifier = Modifier.weight(1f),
                    singleLine = false
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

            Text("Member Photo (JPG)", style = MaterialTheme.typography.titleMedium)

            Card(modifier = Modifier.fillMaxWidth(), shape = cardShape) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PhotoPreview(localUri = memberPhotoUri, remoteUrl = memberPhotoUrl, height = 190.dp, shape = cardShape)
                    Button(
                        onClick = { pickMemberPhoto.launch("image/*") },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(52.dp)
                    ) { Text("Choose file") }
                }
            }

            Text("Nominee Photo (JPG)", style = MaterialTheme.typography.titleMedium)

            Card(modifier = Modifier.fillMaxWidth(), shape = cardShape) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PhotoPreview(localUri = nomineePhotoUri, remoteUrl = nomineePhotoUrl, height = 190.dp, shape = cardShape)
                    Button(
                        onClick = { pickNomineePhoto.launch("image/*") },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(52.dp)
                    ) { Text("Choose file") }
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(50),
                onClick = {
                    if (newPassword.isNotBlank() && newPassword != confirmPassword) {
                        Toast.makeText(context, "Password mismatch", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showUpdateConfirm = true
                }
            ) { Text("Save Changes") }

            Spacer(Modifier.height(20.dp))

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

@Composable
private fun PhotoPreview(
    localUri: Uri?,
    remoteUrl: String?,
    height: Dp,
    shape: RoundedCornerShape
) {
    val context = LocalContext.current

    val model = when {
        localUri != null -> localUri
        !remoteUrl.isNullOrBlank() -> remoteUrl
        else -> null
    }

    if (model == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("No photo uploaded")
        }
        return
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context).data(model).crossfade(true).build(),
        contentDescription = "Photo",
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Text("Failed to load image") }
        }
    )
}
