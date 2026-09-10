                                    contentAlignment = Alignment.Center
                                ) {
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
