import re

with open("D:/modula-mobile/native-launcher/ZalithLauncher/src/main/java/com/modulamobile/ui/screens/HomeScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# 1. Add OrbitronFamily to imports
content = content.replace("import com.modulamobile.ui.theme.*", "import com.modulamobile.ui.theme.*\nimport com.modulamobile.ui.theme.OrbitronFamily")

# 2. Add SectionHeader composable at the top of HomeScreen function body
header_func = """
    @Composable
    fun SectionHeader(title: String) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(title.uppercase(), style = ModulaTypography.headlineMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Black), color = themeAccentColor)
            Text("EXPLORE ALL >", style = ModulaTypography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = themeAccentColor)
        }
    }
"""
content = content.replace("val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary", "val themeAccentColor = com.modulamobile.ui.theme.LocalModulaColors.current.primary\n" + header_func)

# 3. Change "LAUNCH GAME" button to "Launch Game" and add icon
content = content.replace("""GlassButton(
                                text = "LAUNCH GAME",
                                onClick = { onLaunchGame(currentVersion.value) },
                                variant = GlassVariant.GOLD,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )""", """GlassButton(
                                text = "Launch Game",
                                onClick = { onLaunchGame(currentVersion.value) },
                                variant = GlassVariant.GOLD,
                                modifier = Modifier.fillMaxWidth(0.8f),
                                icon = Icons.Filled.PlayArrow
                            )""")

# 4. Change Status Badges
old_stats = """Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.Memory, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${ramAllocationMb / 1024}GB", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                            Text("ALLOCATED", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                        }
                    }
                    GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = if(unlockFps) themeAccentColor else TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if(unlockFps) "UNLOCKED" else "CAPPED", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                            Text("ENGINE", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                        }
                    }
                    GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ACTIVE", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50))
                            Text("SECURITY", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                        }
                    }
                }"""

new_stats = """Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Memory, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${ramAllocationMb / 1024}GB", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                                Text("ALLOCATED", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = if(unlockFps) themeAccentColor else TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if(unlockFps) "UNLOCKED" else "CAPPED", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                                Text("ENGINE", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Security, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ACTIVE", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50))
                                Text("SECURITY", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.weight(1f).height(80.dp)) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = themeAccentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ON", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                                Text("WIFI", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }"""
content = content.replace(old_stats, new_stats)

# 5. Golden Flux Engine Text & Badge
content = content.replace("""Text("Golden Flux Engine", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)""", """Text("GOLDEN FLUX ENGINE", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontFamily = OrbitronFamily, fontSize = 12.sp), color = Color.White)""")
content = content.replace("""GlassBadge(text = if(isEngineActive) "ACTIVE" else "PARTIAL")""", """Box(modifier = Modifier.background(if(isEngineActive) Color(0xFF2ECA71) else Color(0xFF4CAF50).copy(alpha=0.5f), RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(if(isEngineActive) "ACTIVE" else "PARTIAL", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize=9.sp), color = ColorBg0)
                        }""")

# 6. Section Headers and new sections
content = content.replace("""Text("RECENT ACTIVITY", style = ModulaTypography.titleLarge, color = Color.White, modifier = Modifier.padding(top = 16.dp))""", """SectionHeader("MODERN MODPACKS")
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.width(240.dp).height(120.dp)) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1607513746994-51f730a43854?q=80&w=600&auto=format&fit=crop",
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, ColorBg0))))
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Bottom) {
                                Text("BETTER MINECRAFT", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontFamily = OrbitronFamily, fontSize=12.sp), color = Color.White)
                                Text("FABRIC 1.20.1", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                    item {
                        GlassCard(variant = GlassVariant.DARK, modifier = Modifier.width(240.dp).height(120.dp)) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1542314831-c6a4d14eff85?q=80&w=600&auto=format&fit=crop",
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, ColorBg0))))
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Bottom) {
                                Text("RLCRAFT OPTIMIZED", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontFamily = OrbitronFamily, fontSize=12.sp), color = Color.White)
                                Text("FORGE 1.12.2", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp), color = TextMuted)
                            }
                        }
                    }
                }
                
                SectionHeader("RECENT ACTIVITY")""")

content = content.replace("""Text("COMMUNITY NEWS", style = ModulaTypography.titleLarge, color = Color.White, modifier = Modifier.padding(top = 16.dp))""", """SectionHeader("COMMUNITY NEWS")""")

# 7. Add Banner at the bottom
banner = """
            item {
                GlassCard(variant = GlassVariant.DARK, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF0D1829))) {
                            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("KERALA'S OWN", style = ModulaTypography.headlineMedium.copy(fontSize = 12.sp, fontFamily=OrbitronFamily), color = Color(0xFF4CA0E0))
                                    Text("MODULA IS MADE FROM", style = ModulaTypography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("GOD'S OWN COUNTRY", style = ModulaTypography.headlineMedium.copy(fontSize = 16.sp, fontFamily=OrbitronFamily), color = Color(0xFF2ECA71))
                                }
                                Box(modifier = Modifier.size(64.dp).background(Color(0xFF1E3A8A), RoundedCornerShape(8.dp)))
                            }
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(modifier = Modifier.background(themeAccentColor, RoundedCornerShape(2.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("UPDATE", style = ModulaTypography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ColorBg0)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Coming Soon: Official Textures!", style = ModulaTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize=14.sp), color = Color.White)
                            Text("We are hard at work on the first official Modula high-performance texture pack. Stay tuned to our Discord for the early access beta!", style = ModulaTypography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                        }
                    }
                }
            }
"""
content = content.replace("        }\n    }\n}", banner + "\n        }\n    }\n}")

with open("D:/modula-mobile/native-launcher/ZalithLauncher/src/main/java/com/modulamobile/ui/screens/HomeScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)
