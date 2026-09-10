                                    val context = LocalContext.current
                                    val resId = context.getNoteDrawableResId(note.nome)
                                    if (resId != 0) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = note.nome,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
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
