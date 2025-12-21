package com.example.travelupa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travelupa.data.*
import com.example.travelupa.ui.theme.TravelupaTheme
import com.example.travelupa.viewmodel.AuthViewModel
import com.example.travelupa.viewmodel.FirestoreViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

val ElectricBlue = Color(0xFF00C3FF)
val ElectricBlueLight = Color(0xFF80D4FF)
val DarkBackgroundStart = Color(0xFF0D1117)
val DarkBackgroundEnd = Color(0xFF161B22)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelupaTheme {
                val authViewModel: AuthViewModel = viewModel()
                val firestoreViewModel: FirestoreViewModel = viewModel()

                val screenSaver = Saver<ScreenState, String>(
                    save = { it.route },
                    restore = { route ->
                        when (route) {
                            "splash" -> ScreenState.Splash
                            "dashboard" -> ScreenState.Dashboard
                            "tambah_wisata" -> ScreenState.TambahWisata
                            "login" -> ScreenState.Login
                            "register" -> ScreenState.Register
                            else -> ScreenState.Splash
                        }
                    }
                )

                var currentScreen by rememberSaveable(stateSaver = screenSaver) { mutableStateOf<ScreenState>(ScreenState.Splash) }
                
                val userSession by authViewModel.userSession.collectAsState()
                val wisataList by firestoreViewModel.wisataList.collectAsState()

                LaunchedEffect(userSession) {
                    currentScreen = if (userSession == null) {
                        if (currentScreen !is ScreenState.Splash && currentScreen !is ScreenState.Register) ScreenState.Login else currentScreen
                    } else {
                        if (currentScreen is ScreenState.Login || currentScreen is ScreenState.Register || currentScreen is ScreenState.Splash) ScreenState.Dashboard else currentScreen
                    }
                }

                when (currentScreen) {
                    ScreenState.Splash -> {
                        HalamanPembukaFuturistik(
                            onStartClicked = {
                                currentScreen = if (userSession == null) ScreenState.Login else ScreenState.Dashboard
                            }
                        )
                    }
                    ScreenState.Login -> {
                        AuthScreen(
                            isLogin = true,
                            authViewModel = authViewModel,
                            onNavigateToRegister = { currentScreen = ScreenState.Register },
                            onNavigateToLogin = {},
                            onAuthSuccess = { currentScreen = ScreenState.Dashboard }
                        )
                    }
                    ScreenState.Register -> {
                        AuthScreen(
                            isLogin = false,
                            authViewModel = authViewModel,
                            onNavigateToRegister = {},
                            onNavigateToLogin = { currentScreen = ScreenState.Login },
                            onAuthSuccess = { currentScreen = ScreenState.Dashboard }
                        )
                    }
                    ScreenState.Dashboard -> {
                        TravelupaDashboard(
                            listData = wisataList,
                            authViewModel = authViewModel,
                            firestoreViewModel = firestoreViewModel, 
                            onTambahData = { currentScreen = ScreenState.TambahWisata },
                            onDeleteWisata = { firestoreViewModel.deleteWisata(it) },
                            onLogout = { authViewModel.logout() }
                        )
                    }
                    ScreenState.TambahWisata -> {
                        HalamanTambahWisata(
                            firestoreViewModel = firestoreViewModel,
                            onDataAdded = { currentScreen = ScreenState.Dashboard },
                            onBack = { currentScreen = ScreenState.Dashboard }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun HalamanPembukaFuturistik(onStartClicked: () -> Unit) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        contentVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingY by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse), label = "floatingAnim"
    )
    val entryOffsetY by animateFloatAsState(
        targetValue = if (contentVisible) 0f else 200f,
        animationSpec = tween(1500, delayMillis = 300, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)), label = "entryAnim"
    )
    val alphaContent by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(1200, delayMillis = 300), label = "alphaAnim"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Image(painter = painterResource(id = R.drawable.bg_jatim), contentDescription = "Pemandangan Alam Jawa Timur", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xF0000000)), startY = 500f)))
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 80.dp).graphicsLayer {
                translationY = entryOffsetY * density + floatingY * density
                alpha = alphaContent
            }, horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "TRAVELUPA", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(shadow = Shadow(color = ElectricBlue.copy(alpha = 0.5f), blurRadius = 8f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Akses eksklusif ke destinasi terbaik Jawa Timur. Mulai perjalanan digital Anda!", fontSize = 18.sp, color = Color.LightGray.copy(alpha = 0.9f), fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(48.dp))
            if (onStartClicked != {}) {
                Button(
                    onClick = onStartClicked,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp).shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp), spotColor = ElectricBlue),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Mulai Petualangan Eksklusif", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel, 
    isLogin: Boolean, 
    onNavigateToRegister: () -> Unit, 
    onNavigateToLogin: () -> Unit, 
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val isFormValid = email.isNotBlank() && password.length >= 6

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, if (isLogin) "Login berhasil!" else "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                onAuthSuccess()
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBackgroundStart, DarkBackgroundEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_travelupa), 
                contentDescription = "Travelupa Logo",
                modifier = Modifier.size(120.dp).padding(bottom = 24.dp)
            )
            Text(
                text = if (isLogin) "SIGN IN" else "CREATE ACCOUNT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(ElectricBlue, blurRadius = 10f)
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            ModernTextField(value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Default.Email, keyboardType = KeyboardType.Email, enabled = !isLoading)
            Spacer(modifier = Modifier.height(24.dp))
            ModernTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, keyboardType = KeyboardType.Password, isPassword = true, enabled = !isLoading)
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (isLogin) {
                        authViewModel.login(email, password)
                    } else {
                        authViewModel.register(email, password)
                    }
                },
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(colors = listOf(ElectricBlue, ElectricBlueLight)),
                        shape = RoundedCornerShape(50)
                    )
                    .shadow(elevation = 15.dp, spotColor = ElectricBlue, shape = RoundedCornerShape(50)),
                contentPadding = PaddingValues()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                } else {
                    Text(
                        if (isLogin) "LOGIN" else "REGISTER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = {
                if (isLogin) onNavigateToRegister() else onNavigateToLogin()
            }, enabled = !isLoading) {
                Text(
                    text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    keyboardType: KeyboardType, 
    enabled: Boolean, 
    isPassword: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedBottomLine by animateDpAsState(targetValue = if (isFocused) 2.dp else 1.dp, label = "line anim")
    val animatedGlow by animateColorAsState(targetValue = if (isFocused) ElectricBlue else Color.Gray.copy(alpha = 0.5f), label = "glow anim")

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Gray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = animatedGlow) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                cursorColor = ElectricBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            enabled = enabled
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(animatedBottomLine)
            .background(animatedGlow)
            .shadow(elevation = 4.dp, spotColor = animatedGlow)
        )
    }
}

@Composable
fun WisataCard(wisata: Wisata, isEditMode: Boolean, onDelete: (Wisata) -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val canDelete = isEditMode && wisata.userId == currentUserId

    Card(
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).shadow(
            elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = ElectricBlue.copy(alpha = 0.5f)
        ), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.height(220.dp).fillMaxWidth()) {
                Image(painter = rememberAsyncImagePainter(model = wisata.imageUrl.ifBlank { R.drawable.bg_jatim }), contentDescription = wisata.nama, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xAA000000)), startY = 300f)))
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = wisata.nama, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${wisata.rating}", fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Lokasi", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = wisata.lokasi, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(wisata.deskripsiSingkat, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis) 
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canDelete) {
                        IconButton(onClick = { onDelete(wisata) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus Data", tint = Color.Red.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = { /* TODO: Navigasi ke Detail */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Lihat", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelupaDashboard(
    listData: List<Wisata>,
    authViewModel: AuthViewModel,
    firestoreViewModel: FirestoreViewModel, 
    onTambahData: () -> Unit,
    onDeleteWisata: (Wisata) -> Unit,
    onLogout: () -> Unit
) {
    var isEditMode by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userEmail by authViewModel.userSession.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBackgroundEnd,
                modifier = Modifier.background(Brush.verticalGradient(listOf(DarkBackgroundStart, DarkBackgroundEnd)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(painter = painterResource(id = R.drawable.logo_travelupa), contentDescription = "Logo", modifier = Modifier.size(80.dp).padding(bottom = 16.dp))
                    Text("Selamat datang,", fontSize = 16.sp, color = Color.Gray)
                    Text(userEmail ?: "Pengguna", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                }
                HorizontalDivider(color = ElectricBlue.copy(alpha = 0.3f))
                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.White) },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onLogout()
                     },
                    icon = { Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu", tint = ElectricBlue)
                        }
                    },
                    title = { Text("DESTINASI EKSKLUSIF", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ElectricBlue) },
                    actions = {
                        IconButton(onClick = { isEditMode = !isEditMode }) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Filled.Done else Icons.Filled.Edit,
                                contentDescription = if (isEditMode) "Selesai Edit" else "Aktifkan Mode Hapus",
                                tint = if (isEditMode) Color(0xFF4CAF50) else ElectricBlue
                            )
                        }
                        IconButton(onClick = onTambahData) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Data", tint = ElectricBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    modifier = Modifier.shadow(4.dp)
                )
            }
        ) { paddingValues ->
            if (listData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.Inbox, contentDescription = "Kotak Kosong", tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada data wisata.", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Tambahkan data baru dengan menekan tombol '+' di pojok kanan atas.", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(listData, key = { it.id }) { wisata ->
                        WisataCard(
                            wisata = wisata,
                            isEditMode = isEditMode,
                            onDelete = onDeleteWisata
                        )
                    }
                }
            }
        }
    }
}

fun createImageUri(context: Context): Uri {
    val imageFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanTambahWisata(
    firestoreViewModel: FirestoreViewModel,
    onDataAdded: () -> Unit,
    onBack: () -> Unit
) {
    var nama by rememberSaveable { mutableStateOf("") }
    var lokasi by rememberSaveable { mutableStateOf("") }
    var deskripsi by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val firestoreState by firestoreViewModel.firestoreState.collectAsState()
    val isLoading = firestoreState is FirestoreState.Loading
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    var tempCameraUri: Uri? by remember { mutableStateOf(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempCameraUri
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                val newUri = createImageUri(context)
                tempCameraUri = newUri
                cameraLauncher.launch(newUri)
            } else {
                Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val isFormValid = nama.isNotBlank() && lokasi.isNotBlank() && deskripsi.isNotBlank() && rating.isNotBlank() && imageUri != null

    LaunchedEffect(firestoreState) {
        if (firestoreState is FirestoreState.Success) {
            Toast.makeText(context, "Data berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            firestoreViewModel.resetState()
            onDataAdded()
        }
        if (firestoreState is FirestoreState.Error){
            Toast.makeText(context, (firestoreState as FirestoreState.Error).message, Toast.LENGTH_LONG).show()
            firestoreViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TAMBAH DESTINASI BARU") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Kembali") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackgroundEnd,
                    titleContentColor = ElectricBlue,
                    navigationIconContentColor = ElectricBlue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(colors = listOf(DarkBackgroundStart, DarkBackgroundEnd)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Upload", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text("Sentuh untuk pilih gambar", color = Color.Gray)
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Gambar Wisata",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { galleryLauncher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.2f))) {
                        Icon(Icons.Default.Image, contentDescription = "Galeri", modifier = Modifier.padding(end = 8.dp))
                        Text("Galeri")
                    }
                    Button(
                        onClick = { 
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val newUri = createImageUri(context)
                                tempCameraUri = newUri
                                cameraLauncher.launch(newUri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Kamera", modifier = Modifier.padding(end = 8.dp))
                        Text("Kamera")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                ModernTextField(value = nama, onValueChange = { nama = it }, label = "Nama Wisata", icon = Icons.Default.Terrain, keyboardType = KeyboardType.Text, enabled = !isLoading)
                Spacer(modifier = Modifier.height(16.dp))
                ModernTextField(value = lokasi, onValueChange = { lokasi = it }, label = "Lokasi (e.g., Malang)", icon = Icons.Default.Map, keyboardType = KeyboardType.Text, enabled = !isLoading)
                Spacer(modifier = Modifier.height(16.dp))
                ModernTextField(value = deskripsi, onValueChange = { deskripsi = it }, label = "Deskripsi Singkat", icon = Icons.Default.Description, keyboardType = KeyboardType.Text, enabled = !isLoading)
                Spacer(modifier = Modifier.height(16.dp))
                ModernTextField(value = rating, onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,1}$"))) rating = it }, label = "Rating (e.g., 4.5)", icon = Icons.Default.Star, keyboardType = KeyboardType.Decimal, enabled = !isLoading)
                
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        // ✅ PERBAIKAN DI SINI
                        firestoreViewModel.addWisata(
                            nama = nama,
                            lokasi = lokasi,
                            deskripsi = deskripsi,
                            rating = rating.toDoubleOrNull() ?: 0.0,
                            imageUri = imageUri
                        )
                    },
                    enabled = isFormValid && !isLoading,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(brush = Brush.horizontalGradient(colors = listOf(ElectricBlue, ElectricBlueLight)), shape = CircleShape)
                        .shadow(elevation = 15.dp, spotColor = ElectricBlue, shape = CircleShape),
                    contentPadding = PaddingValues()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
                    } else {
                        Text("SIMPAN DATA", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}