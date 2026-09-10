                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Slate700),
                        contentAlignment = Alignment.Center
                    ) {
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
