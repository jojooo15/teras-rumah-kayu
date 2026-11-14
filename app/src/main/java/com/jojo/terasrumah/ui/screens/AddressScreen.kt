package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.*
import com.jojo.terasrumah.data.repository.AddressRepository
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBackClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (UserAddress) -> Unit
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
    
    var addresses by remember { mutableStateOf<List<UserAddress>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<UserAddress?>(null) }
    
    // Load addresses when screen opens
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            addressRepository.getUserAddresses(user.id).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        isLoading = true
                    }
                    is ApiResult.Success -> {
                        addresses = result.data
                        isLoading = false
                    }
                    is ApiResult.Error -> {
                        isLoading = false
                        snackbarHostState.showSnackbar(
                            message = "Gagal memuat alamat: ${result.exception.message}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alamat Saya") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAddressClick,
                containerColor = PrimaryBrown
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Alamat",
                    tint = Color.White
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBrown)
                }
            } else if (addresses.isEmpty()) {
                EmptyAddressState(onAddAddressClick = onAddAddressClick)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(addresses) { address ->
                        AddressCard(
                            address = address,
                            onEditClick = { onEditAddressClick(address) },
                            onDeleteClick = { showDeleteDialog = address },
                            onSetDefaultClick = {
                                currentUser?.let { user ->
                                    scope.launch {
                                        addressRepository.setDefaultAddress(user.id, address.id).collect { result ->
                                            when (result) {
                                                is ApiResult.Success -> {
                                                    // Refresh addresses
                                                    addressRepository.getUserAddresses(user.id).collect { refreshResult ->
                                                        if (refreshResult is ApiResult.Success) {
                                                            addresses = refreshResult.data
                                                        }
                                                    }
                                                    snackbarHostState.showSnackbar(
                                                        message = "Alamat default berhasil diubah",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                is ApiResult.Error -> {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Gagal mengubah alamat default",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        showDeleteDialog?.let { address ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Hapus Alamat") },
                text = { Text("Apakah Anda yakin ingin menghapus alamat '${address.label}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            currentUser?.let { user ->
                                scope.launch {
                                    addressRepository.deleteAddress(user.id, address.id).collect { result ->
                                        when (result) {
                                            is ApiResult.Success -> {
                                                addresses = addresses.filter { it.id != address.id }
                                                snackbarHostState.showSnackbar(
                                                    message = "Alamat berhasil dihapus",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                            is ApiResult.Error -> {
                                                snackbarHostState.showSnackbar(
                                                    message = "Gagal menghapus alamat",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Hapus", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyAddressState(onAddAddressClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada alamat tersimpan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tambahkan alamat untuk mempermudah pemesanan",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddAddressClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Alamat")
        }
    }
}

@Composable
fun AddressCard(
    address: UserAddress,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (address.isDefault) PrimaryBrown.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with label and default badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = PrimaryBrown,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = PrimaryBrown,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Default",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address label (simplified - no separate recipient name)
            Text(
                text = address.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = address.phoneNumber,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Address
            Text(
                text = address.getFormattedAddress(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            if (address.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: ${address.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Set as default button (only show if not already default)
            if (!address.isDefault) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSetDefaultClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBrown
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryBrown)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Jadikan Alamat Default", fontSize = 12.sp)
                }
            }
        }
    }
}
