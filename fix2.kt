                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Slate700),
                        contentAlignment = Alignment.Center
                    ) {
                        val customUrl = noteImages[noteName.normalizeNoteName()]
                        val finalUrl = if (customUrl.isNullOrBlank()) {
                            com.aistudio.perfumatico.data.local.DefaultNoteImages[noteName.normalizeNoteName()] 
                            ?: noteName.getNoteImageUrl()
                        } else customUrl
                        
                        if (finalUrl.isBlank()) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            coil.compose.SubcomposeAsyncImage(
                                model = finalUrl,
                                contentDescription = noteName,
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
                    Text(
                        text = noteName,
                        color = Slate200,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
