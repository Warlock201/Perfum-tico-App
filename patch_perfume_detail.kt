                        val context = LocalContext.current
                        val resId = context.getNoteDrawableResId(noteName)
                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = noteName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
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
