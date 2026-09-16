package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.aistudio.perfumatico.data.remote.CommunitySotd
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityDialog(
    viewModel: PerfumeViewModel,
    onDismiss: () -> Unit
) {
    val communityList by viewModel.communitySotdList.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Amber400
                        )
                        Text(
                            text = "Rede Perfumática",
                            color = Slate50,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (communityList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ninguém registrou o perfume do dia ainda.",
                            color = Slate400,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(communityList, key = { it.userId }) { sotd ->
                            CommunitySotdItem(sotd = sotd)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunitySotdItem(sotd: CommunitySotd) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate800, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Perfume Image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Slate700),
            contentAlignment = Alignment.Center
        ) {
            if (sotd.perfumeImage.isNotBlank()) {
                AsyncImage(
                    model = sotd.perfumeImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = sotd.perfumeName.take(1).uppercase(),
                    color = Slate300,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${sotd.userName} está usando:",
                color = Slate400,
                fontSize = 12.sp
            )
            Text(
                text = sotd.perfumeName,
                color = Amber400,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sotd.perfumeBrand,
                color = Slate300,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
