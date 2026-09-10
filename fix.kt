                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Slate700),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val dbUrl = noteImages[note.nome.normalizeNoteName()]
                                    val finalUrl = if (dbUrl.isNullOrBlank()) {
                                        com.aistudio.perfumatico.data.local.DefaultNoteImages[note.nome.normalizeNoteName()] 
                                        ?: note.nome.getNoteImageUrl()
                                    } else dbUrl

                                    if (finalUrl.isBlank()) {
                                        Icon(
                                            imageVector = Icons.Default.Spa,
                                            contentDescription = null,
                                            tint = Emerald400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        coil.compose.SubcomposeAsyncImage(
                                            model = finalUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            loading = {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    CircularProgressIndicator(color = Emerald400, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                }
                                            },
                                            error = {
                                                Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        )
                                    }
                                }
                                androidx.compose.material3.Text(
                                    text = note.nome,
                                    color = Slate100,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogPerfumeCard(
    perfume: com.aistudio.perfumatico.data.local.PerfumeEntity,
    onClick: () -> Unit,
    onAddWant: () -> Unit,
    onAddHave: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate700)
            ) {
                coil.compose.AsyncImage(
                    model = perfume.imageUrl,
                    contentDescription = perfume.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = perfume.brand,
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.material3.Text(
                    text = perfume.name,
                    color = Slate50,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.material3.Text(
                        text = perfume.family,
                        color = Emerald400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onAddWant,
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Cyan500),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.Text("Quero", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.FilledTonalButton(
                        onClick = onAddHave,
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(containerColor = Amber400, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.Text("Tenho", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
