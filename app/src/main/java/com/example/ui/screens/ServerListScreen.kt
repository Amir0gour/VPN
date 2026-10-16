package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ServerFilter
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBorderDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.vpn.model.VpnServer

@Composable
fun ServerListScreen(
    servers: List<VpnServer>,
    selectedServer: VpnServer,
    searchQuery: String,
    selectedFilter: ServerFilter,
    isPremiumUser: Boolean,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (ServerFilter) -> Unit,
    onServerSelected: (VpnServer) -> Unit,
    onAddCustomServer: (name: String, ip: String, port: Int, publicKey: String) -> Unit,
    onImportConfig: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VPN Locations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${servers.size} high-speed servers available",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceVariantDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import WireGuard Config",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("Search country, city, or server...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CyberSurfaceDark,
                    unfocusedContainerColor = CyberSurfaceDark,
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberBorderDark
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(ServerFilter.values()) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChanged(filter) },
                        label = {
                            Text(
                                text = filter.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CyberSurfaceDark,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CyberBorderDark,
                            selectedBorderColor = CyberCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Servers List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(servers, key = { it.id }) { server ->
                    ServerItemCard(
                        server = server,
                        isSelected = server.id == selectedServer.id,
                        isPremiumUser = isPremiumUser,
                        onSelect = { onServerSelected(server) }
                    )
                }
            }
        }

        // FAB to add custom server
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = CyberCyan,
            contentColor = Color(0xFF031623),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_server_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Custom WireGuard Server"
            )
        }
    }

    // Add Custom Server Dialog
    if (showAddDialog) {
        AddCustomServerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, ip, port, pubKey ->
                onAddCustomServer(name, ip, port, pubKey)
                showAddDialog = false
            }
        )
    }

    // Import WireGuard Config Dialog
    if (showImportDialog) {
        ImportWireGuardDialog(
            onDismiss = { showImportDialog = false },
            onImport = { configText ->
                val success = onImportConfig(configText)
                if (success) {
                    showImportDialog = false
                }
                success
            }
        )
    }
}

@Composable
fun ServerItemCard(
    server: VpnServer,
    isSelected: Boolean,
    isPremiumUser: Boolean,
    onSelect: () -> Unit
) {
    val isLocked = server.isPremium && !isPremiumUser

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("server_card_${server.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberSurfaceVariantDark else CyberSurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyberCyan else CyberBorderDark
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = server.flagEmoji,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = server.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (server.isPremium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFA855F7).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${server.city}, ${server.country}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = " • ", color = CyberBorderDark, fontSize = 11.sp)
                        Text(
                            text = "${server.pingMs} ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (server.pingMs < 35) CyberEmerald else if (server.pingMs < 90) CyberAmber else CyberRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Load Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        LinearProgressIndicator(
                            progress = { server.loadPercentage / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (server.loadPercentage < 50) CyberEmerald else if (server.loadPercentage < 80) CyberAmber else CyberRed,
                            trackColor = CyberBorderDark.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${server.loadPercentage}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Selection indicator or lock
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CyberCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF031623),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (isLocked) {
                Surface(
                    color = Color(0xFFA855F7).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Upgrade",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomServerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, ip: String, port: Int, publicKey: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var endpointIp by remember { mutableStateOf("") }
    var endpointPort by remember { mutableStateOf("51820") }
    var publicKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom WireGuard Server",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Connect to your own self-hosted WireGuard VPS instance with zero third-party telemetry.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name (e.g. My Ubuntu VPS)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endpointIp,
                    onValueChange = { endpointIp = it },
                    label = { Text("Server IP or Domain") },
                    placeholder = { Text("192.0.2.1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endpointPort,
                    onValueChange = { endpointPort = it },
                    label = { Text("WireGuard UDP Port") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = publicKey,
                    onValueChange = { publicKey = it },
                    label = { Text("Server Public Key (Base64)") },
                    placeholder = { Text("rM8F4eF768jZ+X2d73pL0eWd9P3cQ2/H4bA8fT6mU1Y=") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = endpointPort.toIntOrNull() ?: 51820
                    if (endpointIp.isNotBlank() && publicKey.isNotBlank()) {
                        onConfirm(name, endpointIp, port, publicKey)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF031623))
            ) {
                Text("Save & Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = CyberSurfaceDark
    )
}

@Composable
fun ImportWireGuardDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Boolean
) {
    var configText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import WireGuard (.conf)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Paste standard WireGuard client configuration [Interface] and [Peer] blocks:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = configText,
                    onValueChange = {
                        configText = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "[Interface]\nPrivateKey = ...\nAddress = 10.66.66.2/32\n\n[Peer]\nPublicKey = ...\nEndpoint = 192.0.2.1:51820\nAllowedIPs = 0.0.0.0/0"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    maxLines = 10
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = CyberRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = onImport(configText)
                    if (!ok) {
                        errorMessage = "Invalid WireGuard format. Ensure PrivateKey, PublicKey, and Endpoint are specified."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF031623))
            ) {
                Text("Import Config")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = CyberSurfaceDark
    )
}
