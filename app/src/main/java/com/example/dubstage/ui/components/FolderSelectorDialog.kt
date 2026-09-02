package com.example.dubstage.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dubstage.model.DubPackFolder
import com.example.dubstage.ui.theme.Acc
import com.example.dubstage.ui.theme.AccDark
import com.example.dubstage.ui.theme.AccDarkest
import com.example.dubstage.ui.theme.AccHi
import com.example.dubstage.ui.theme.BgBot
import com.example.dubstage.ui.theme.Dim
import com.example.dubstage.ui.theme.Edge
import com.example.dubstage.ui.theme.EdgeHi
import com.example.dubstage.ui.theme.Panel
import com.example.dubstage.ui.theme.PanelHi
import com.example.dubstage.ui.theme.Txt

@Composable
fun FolderSelectorDialog(
    folders: List<DubPackFolder>,
    selectedFolderId: String?,
    onSelectFolder: (String?) -> Unit,
    onImportFolder: (name: String, path: String, uriString: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddCustomFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderPath by remember { mutableStateOf("/storage/emulated/0/DubStage/Packs") }

    // Android Document Tree Folder Picker launcher
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val pathSegment = uri.lastPathSegment ?: "Selected Folder"
            val displayFolderName = pathSegment.substringAfterLast(":").substringAfterLast("/")
            val cleanName = if (displayFolderName.isNotBlank()) displayFolderName else "Imported Folder"
            onImportFolder(cleanName, uri.toString(), uri.toString())
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Edge, RoundedCornerShape(28.dp)),
            color = BgBot
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AccDark,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = AccHi,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DUBPACK DIRECTORY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = Acc
                            )
                            Text(
                                text = "Select Pack Folder",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Filter the Studio DubPacks from local device storage or studio internal directories.",
                    fontSize = 12.sp,
                    color = Dim
                )

                Spacer(Modifier.height(16.dp))

                // Folder List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All Folders" Option
                    item {
                        FolderItemRow(
                            name = "All DubPacks (Every Folder)",
                            path = "Combined directory view",
                            isSelected = selectedFolderId == null,
                            icon = Icons.Default.FolderSpecial,
                            onClick = { onSelectFolder(null) }
                        )
                    }

                    items(folders) { folder ->
                        FolderItemRow(
                            name = folder.name,
                            path = folder.pathDisplay,
                            isSelected = folder.id == selectedFolderId,
                            icon = Icons.Default.Folder,
                            onClick = { onSelectFolder(folder.id) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (showAddCustomFolder) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Panel)
                            .border(1.dp, Edge, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "REGISTER LOCAL DIRECTORY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Acc
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = { Text("e.g. My Anime Dubs", fontSize = 12.sp, color = Dim) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Acc,
                                unfocusedBorderColor = Edge,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newFolderPath,
                            onValueChange = { newFolderPath = it },
                            placeholder = { Text("Path", fontSize = 12.sp, color = Dim) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Acc,
                                unfocusedBorderColor = Edge,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showAddCustomFolder = false },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = Dim)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        onImportFolder(newFolderName, newFolderPath, null)
                                        showAddCustomFolder = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Acc, contentColor = AccDarkest),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add Folder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Action Buttons: Pick Directory via SAF / Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            directoryPicker.launch(null)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = AccHi,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Browse Device...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccHi
                        )
                    }

                    Button(
                        onClick = { showAddCustomFolder = !showAddCustomFolder },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PanelHi,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            tint = Acc,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderItemRow(
    name: String,
    path: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) PanelHi else Panel,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Acc else Edge
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) AccHi else Dim,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Txt
                    )
                    Text(
                        text = path,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Dim
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Acc),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AccDarkest,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
