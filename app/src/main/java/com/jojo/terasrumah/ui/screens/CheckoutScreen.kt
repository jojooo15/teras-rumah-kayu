package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jojo.terasrumah.data.CartRepository
import com.jojo.terasrumah.data.CartItem
import com.jojo.terasrumah.data.model.*
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.theme.BackgroundCream
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory
import com.jojo.terasrumah.ui.components.PermissionHandler
import com.jojo.terasrumah.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderComplete: (String) -> Unit, // "transfer" or "cod"
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))
) {
    PermissionHandler(
        onPermissionGranted = {
            // Permission granted, continue with normal flow
        },
        onPermissionDenied = {
            // Permission denied, show message
        }
    ) { requestPermission ->
        CheckoutScreenContent(
            onBackClick = onBackClick,
            onOrderComplete = onOrderComplete,
            authViewModel = authViewModel,
            requestPermission = requestPermission
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutScreenContent(
    onBackClick: () -> Unit,
    onOrderComplete: (String) -> Unit,
    authViewModel: AuthViewModel,
    requestPermission: () -> Unit
) {
    val cartItems by CartRepository.cartItems
    val totalPrice = CartRepository.getTotalPrice()
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)

    // Simplified form states
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var deliveryNotes by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    // Initialize form with user data
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Only autofill if fields are empty (to preserve user edits)
            if (customerName.isBlank()) {
                customerName = user.name
            }
            if (customerPhone.isBlank()) {
                customerPhone = user.phoneNumber
            }

            // Simplified address autofill
            user.getDefaultAddress()?.let { address ->
                if (fullAddress.isBlank()) {
                    fullAddress = "${address.fullAddress}, ${address.city}"
                }
                if (deliveryNotes.isBlank()) {
                    deliveryNotes = address.notes
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Form Checkout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Combined Customer & Address Information
            item {
                CompactOrderFormCard(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    fullAddress = fullAddress,
                    deliveryNotes = deliveryNotes,
                    onNameChange = { customerName = it },
                    onPhoneChange = { customerPhone = it },
                    onFullAddressChange = { fullAddress = it },
                    onDeliveryNotesChange = { deliveryNotes = it }
                )
            }

            // Payment Method & Order Summary Combined
            item {
                CompactCheckoutCard(
                    cartItems = cartItems,
                    totalPrice = totalPrice,
                    selectedPaymentMethod = selectedPaymentMethod,
                    onMethodSelected = { selectedPaymentMethod = it },
                    customerName = customerName,
                    customerPhone = customerPhone,
                    fullAddress = fullAddress,
                    requestPermission = requestPermission,
                    onPlaceOrder = { method ->
                        onOrderComplete(method)
                    }
                )
            }
        }
    }
}

// Removed - replaced with CompactOrderFormCard

@Composable
private fun CompactOrderFormCard(
    customerName: String,
    customerPhone: String,
    fullAddress: String,
    deliveryNotes: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onFullAddressChange: (String) -> Unit,
    onDeliveryNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Informasi Pemesanan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Name and Phone in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onNameChange,
                    label = { Text("Nama") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        focusedLabelColor = PrimaryBrown
                    )
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = onPhoneChange,
                    label = { Text("No. HP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        focusedLabelColor = PrimaryBrown
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Full Address Field
            OutlinedTextField(
                value = fullAddress,
                onValueChange = onFullAddressChange,
                label = { Text("Alamat Lengkap") },
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBrown,
                    focusedLabelColor = PrimaryBrown
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Delivery Notes Field
            OutlinedTextField(
                value = deliveryNotes,
                onValueChange = onDeliveryNotesChange,
                label = { Text("Catatan (Opsional)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBrown,
                    focusedLabelColor = PrimaryBrown
                )
            )
        }
    }
}



// Removed - replaced with CompactCheckoutCard

// Removed - replaced with PaymentOptionButton in CompactCheckoutCard

@Composable
private fun OrderSummaryCard(
    totalPrice: Int,
    customerName: String,
    customerPhone: String,
    fullAddress: String,
    selectedPaymentMethod: String,
    requestPermission: () -> Unit,
    onPlaceOrder: (String) -> Unit
) {
    val canPlaceOrder = customerName.isNotBlank() &&
                       customerPhone.isNotBlank() &&
                       fullAddress.isNotBlank() &&
                       selectedPaymentMethod.isNotEmpty()

    Button(
        onClick = {
            if (selectedPaymentMethod == "cod") {
                // For COD, request permission first
                requestPermission()
            }
            onPlaceOrder(selectedPaymentMethod)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = canPlaceOrder,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBrown,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (canPlaceOrder) "Pesan Sekarang - ${formatPrice(totalPrice)}"
                   else "Lengkapi Data Terlebih Dahulu",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    if (!canPlaceOrder) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                customerName.isBlank() -> "Silakan isi nama lengkap"
                customerPhone.isBlank() -> "Silakan isi nomor telepon"
                fullAddress.isBlank() -> "Silakan isi alamat lengkap"
                selectedPaymentMethod.isEmpty() -> "Silakan pilih metode pembayaran"
                else -> ""
            },
            fontSize = 12.sp,
            color = Color.Red,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BottomTotalCard(
    cartItems: List<com.jojo.terasrumah.data.CartItem>,
    totalPrice: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Ringkasan Pesanan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Order Items List
            cartItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.foodItem.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "${formatPrice(item.foodItem.price)} x ${item.quantity}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = formatPrice(item.foodItem.price * item.quantity),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBrown
                    )
                }

                if (item != cartItems.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = formatPrice(totalPrice),
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delivery Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Biaya Pengiriman",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Gratis",
                    fontSize = 14.sp,
                    color = Color.Green,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(
                color = PrimaryBrown.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Pembayaran",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Text(
                    text = formatPrice(totalPrice),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
            }
        }
    }
}

private fun formatPrice(price: Int): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
    return formatter.format(price).replace("IDR", "Rp")
}

@Composable
private fun CompactCheckoutCard(
    cartItems: List<CartItem>,
    totalPrice: Int,
    selectedPaymentMethod: String,
    onMethodSelected: (String) -> Unit,
    customerName: String,
    customerPhone: String,
    fullAddress: String,
    requestPermission: () -> Unit,
    onPlaceOrder: (String) -> Unit
) {
    val canPlaceOrder = customerName.isNotBlank() &&
                       customerPhone.isNotBlank() &&
                       fullAddress.isNotBlank() &&
                       selectedPaymentMethod.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Payment Method Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Metode Pembayaran",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Payment Options in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentOptionButton(
                    text = "Transfer",
                    icon = Icons.Default.AccountBalance,
                    isSelected = selectedPaymentMethod == "transfer",
                    onClick = { onMethodSelected("transfer") },
                    modifier = Modifier.weight(1f)
                )

                PaymentOptionButton(
                    text = "COD",
                    icon = Icons.Default.LocalShipping,
                    isSelected = selectedPaymentMethod == "cod",
                    onClick = { onMethodSelected("cod") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${cartItems.size} item",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Total Pembayaran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Text(
                    text = formatPrice(totalPrice),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Button
            Button(
                onClick = {
                    if (selectedPaymentMethod == "cod") {
                        requestPermission()
                    }
                    onPlaceOrder(selectedPaymentMethod)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = canPlaceOrder,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBrown,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pesan Sekarang",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PrimaryBrown else Color.Gray.copy(alpha = 0.1f),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
