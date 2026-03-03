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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.util.MultipartUtil
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AdminCreateMemberViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(NetworkModule.api(app.applicationContext))

    var loading by mutableStateOf(false)
        private set

    fun create(parts: List<MultipartBody.Part>, onDone: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                loading = true
                repo.adminCreateMember(parts)
                loading = false
                onDone()
            } catch (e: Exception) {
                loading = false
                onError(e.message ?: "Create failed")
            }
        }
    }
}

@Composable
fun AdminCreateMemberScreen(
    nav: NavController,
    vm: AdminCreateMemberViewModel = viewModel()
) {
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var share by remember { mutableStateOf("1") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nomineeName by remember { mutableStateOf("") }
    var nomineeNid by remember { mutableStateOf("") }

    var memberPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var nomineePhotoUri by remember { mutableStateOf<Uri?>(null) }

    var showConfirm by remember { mutableStateOf(false) }

    val pickMemberPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        memberPhotoUri = uri
    }
    val pickNomineePhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        nomineePhotoUri = uri
    }

    AdminScaffold(nav = nav, title = "Create Member", hideTitle = false, showHamburger = true) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            if (vm.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

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
                OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.weight(1f))
                OutlinedTextField(confirmPassword, { confirmPassword = it }, label = { Text("Confirm Password") }, modifier = Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(nomineeName, { nomineeName = it }, label = { Text("Nominee Name") }, modifier = Modifier.weight(1f))
                OutlinedTextField(nomineeNid, { nomineeNid = it }, label = { Text("Nominee NID") }, modifier = Modifier.weight(1f))
            }

            Text("Member Photo (JPG)")
            Button(onClick = { pickMemberPhoto.launch("image/*") }) { Text("Choose file") }

            Text("Nominee Photo (JPG)")
            Button(onClick = { pickNomineePhoto.launch("image/*") }) { Text("Choose file") }

            Spacer(Modifier.height(6.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (password.length < 8) {
                        Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Password mismatch", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showConfirm = true
                }
            ) {
                Text("Create Member")
            }

            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("Confirm Create") },
                    text = { Text("Create this member now?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirm = false

                                val parts = mutableListOf<MultipartBody.Part>()
                                parts += MultipartUtil.textPart("full_name", fullName)
                                parts += MultipartUtil.textPart("nid", nid)
                                parts += MultipartUtil.textPart("email", email)
                                parts += MultipartUtil.textPart("password", password)
                                parts += MultipartUtil.textPart("mobile_number", mobile)
                                parts += MultipartUtil.textPart("nominee_name", nomineeName)
                                parts += MultipartUtil.textPart("nominee_nid", nomineeNid)
                                parts += MultipartUtil.textPart("share", share)

                                MultipartUtil.filePart(context, "image", memberPhotoUri)?.let { parts += it }
                                MultipartUtil.filePart(context, "nominee_photo", nomineePhotoUri)?.let { parts += it }

                                vm.create(
                                    parts = parts,
                                    onDone = {
                                        Toast.makeText(context, "Member created", Toast.LENGTH_SHORT).show()
                                        // Tell AdminPanel to refresh when we go back
                                        nav.previousBackStackEntry?.savedStateHandle?.set("members_refresh", true)
                                        nav.popBackStack()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        ) { Text("Confirm") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
