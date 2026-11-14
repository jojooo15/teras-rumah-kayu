package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.*
import com.jojo.terasrumah.data.repository.AddressRepository
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(
    address: UserAddress? = null, // null for add, non-null for edit
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    
    val addressRepository = remember { AddressRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Setup callback to update user data when addresses change
    LaunchedEffect(addressRepository, authViewModel) {
        addressRepository.setAddressUpdateCallback { userId, updatedAddresses ->
            scope.launch {
                authViewModel.updateUserAddresses(userId, updatedAddresses)
            }
        }
    }
    
    val isEditMode = address != null
    val title = if (isEditMode) "Edit Alamat" else "Tambah Alamat"
    
    // Simplified form state - phone number automatically from user profile
    var label by remember { mutableStateOf(address?.label ?: "") }
    var fullAddress by remember { mutableStateOf(address?.fullAddress ?: "") }
    var city by remember { mutableStateOf(address?.city ?: "") }
    var notes by remember { mutableStateOf(address?.notes ?: "") }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }

    var isLoading by remember { mutableStateOf(false) }

    // Simplified validation - phone number from user profile, no need to validate
    val isFormValid = label.isNotBlank() &&
                     fullAddress.isNotBlank() &&
                     city.isNotBlank()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Label alamat
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label Alamat") },
                placeholder = { Text("Contoh: Rumah, Kantor, Kos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Removed recipientName and phoneNumber fields - using user profile phone number
            
            // Alamat lengkap
            OutlinedTextField(
                value = fullAddress,
                onValueChange = { fullAddress = it },
                label = { Text("Alamat Lengkap") },
                placeholder = { Text("Jalan, nomor rumah, RT/RW") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Removed district field - simplified structure
            
            // Kota
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Kota/Kabupaten") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Removed province and postalCode fields - simplified structure
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Catatan
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (Opsional)") },
                placeholder = { Text("Patokan, warna rumah, dll") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Checkbox alamat default
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBrown)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jadikan alamat default",
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save button
            Button(
                onClick = {
                    currentUser?.let { user ->
                        scope.launch {
                            isLoading = true
                            
                            if (isEditMode && address != null) {
                                // Update existing address (simplified) - use user profile phone number
                                val updateRequest = UpdateAddressRequest(
                                    label = label.trim(),
                                    phoneNumber = user.phoneNumber, // Use phone number from user profile
                                    fullAddress = fullAddress.trim(),
                                    city = city.trim(),
                                    notes = notes.trim(),
                                    isDefault = isDefault
                                )
                                
                                addressRepository.updateAddress(user.id, address.id, updateRequest).collect { result ->
                                    when (result) {
                                        is ApiResult.Success -> {
                                            isLoading = false
                                            snackbarHostState.showSnackbar(
                                                message = "Alamat berhasil diperbarui",
                                                duration = SnackbarDuration.Short
                                            )
                                            onSaveSuccess()
                                        }
                                        is ApiResult.Error -> {
                                            isLoading = false
                                            snackbarHostState.showSnackbar(
                                                message = "Gagal memperbarui alamat: ${result.exception.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                            } else {
                                // Add new address (simplified) - use user profile phone number
                                val addRequest = AddAddressRequest(
                                    label = label.trim(),
                                    phoneNumber = user.phoneNumber, // Use phone number from user profile
                                    fullAddress = fullAddress.trim(),
                                    city = city.trim(),
                                    notes = notes.trim(),
                                    isDefault = isDefault
                                )
                                
                                addressRepository.addAddress(user.id, addRequest).collect { result ->
                                    when (result) {
                                        is ApiResult.Success -> {
                                            isLoading = false
                                            snackbarHostState.showSnackbar(
                                                message = "Alamat berhasil ditambahkan",
                                                duration = SnackbarDuration.Short
                                            )
                                            onSaveSuccess()
                                        }
                                        is ApiResult.Error -> {
                                            isLoading = false
                                            snackbarHostState.showSnackbar(
                                                message = "Gagal menambahkan alamat: ${result.exception.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Perbarui Alamat" else "Simpan Alamat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EditAddressScreenWrapper(
    addressId: String?,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val addressRepository = remember { AddressRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Setup callback to update user data when addresses change
    LaunchedEffect(addressRepository, authViewModel) {
        addressRepository.setAddressUpdateCallback { userId, updatedAddresses ->
            scope.launch {
                authViewModel.updateUserAddresses(userId, updatedAddresses)
            }
        }
    }

    var address by remember { mutableStateOf<UserAddress?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load address by ID
    LaunchedEffect(addressId, currentUser) {
        isLoading = true
        errorMessage = null

        val user = currentUser
        if (addressId != null && user != null) {
            try {
                // Use a safer approach with try-catch and timeout
                withTimeoutOrNull(5000) { // 5 second timeout
                    try {
                        addressRepository.getUserAddresses(user.id).collect { result ->
                            when (result) {
                                is ApiResult.Success -> {
                                    address = result.data.find { it.id == addressId }
                                    isLoading = false
                                    if (address == null) {
                                        errorMessage = "Alamat tidak ditemukan"
                                    }
                                }
                                is ApiResult.Error -> {
                                    isLoading = false
                                    errorMessage = "Gagal memuat alamat: ${result.exception.message}"
                                }
                                else -> {}
                            }
                        }
                    } catch (e: Exception) {
                        // Handle exception inside the timeout block
                        isLoading = false
                        errorMessage = "Gagal memuat alamat: ${e.message}"
                    }
                } ?: run {
                    // Timeout occurred
                    isLoading = false
                    errorMessage = "Waktu habis saat memuat alamat"
                }
            } catch (e: Exception) {
                // Handle outer exception
                isLoading = false
                errorMessage = "Gagal memuat alamat: ${e.message}"
            }
        } else {
            isLoading = false
            errorMessage = "ID alamat tidak valid"
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown)
                    ) {
                        Text("Kembali")
                    }
                }
            }
        }
        else -> {
            AddEditAddressScreen(
                address = address,
                onBackClick = onBackClick,
                onSaveSuccess = onSaveSuccess
            )
        }
    }
}
