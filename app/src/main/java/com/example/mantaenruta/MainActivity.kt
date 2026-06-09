package com.example.mantaenruta

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mantaenruta.data.AppDatabase
import com.example.mantaenruta.data.Usuario
import com.example.mantaenruta.data.UsuarioDao
import com.example.mantaenruta.ui.theme.MantaEnRutaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val GreenPrimary = Color(0xFF0F6E56)
val GreenMid = Color(0xFF1D9E75)
val GreenLight = Color(0xFFE1F5EE)
val GreenDark = Color(0xFF085041)
val AmberLight = Color(0xFFFAEEDA)
val AmberDark = Color(0xFF854F0B)
val AmberDeep = Color(0xFF633806)
val RedLight = Color(0xFFFCEBEB)
val RedDark = Color(0xFFA32D2D)
val RedDeep = Color(0xFF791F1F)
val GrayText = Color(0xFF5F5E5A)
val GrayBorder = Color(0xFFD3D1C7)
val SurfaceGray = Color(0xFFF1EFE8)

data class BusRoute(
    val number: String,
    val name: String,
    val buses: Int,
    val status: RouteStatus,
    val punctualityPct: Int
)

data class BusUnit(
    val id: String,
    val routeNumber: String,
    val driver: String,
    val status: FleetStatus
)

data class Alert(
    val title: String,
    val description: String,
    val time: String,
    val type: AlertType
)

enum class RouteStatus { ON_TIME, DELAYED, WARNING }
enum class FleetStatus { ACTIVE, MAINTENANCE, BROKEN }
enum class AlertType { WARNING, DANGER, SUCCESS }
enum class AppScreen { LOGIN, DASHBOARD, REGISTER }

val sampleRoutes = listOf(
    BusRoute("101", "Terminal Norte a La Y", 6, RouteStatus.ON_TIME, 98),
    BusRoute("112", "Carcelen a El Recreo", 5, RouteStatus.DELAYED, 72),
    BusRoute("130", "Quitumbe a Cotocollao", 7, RouteStatus.ON_TIME, 95),
    BusRoute("205", "El Inca a Solanda", 4, RouteStatus.WARNING, 83),
    BusRoute("220", "Carapungo a El Ejido", 6, RouteStatus.ON_TIME, 97),
    BusRoute("310", "Colmena a Guajalo", 5, RouteStatus.ON_TIME, 91),
    BusRoute("401", "Pomasqui a La Ofelia", 5, RouteStatus.ON_TIME, 96),
    BusRoute("502", "Tumbaco a La Marin", 4, RouteStatus.WARNING, 79),
)

val sampleFleet = listOf(
    BusUnit("B-01", "101", "Luis Toapanta", FleetStatus.ACTIVE),
    BusUnit("B-02", "130", "Ana Guerrero", FleetStatus.ACTIVE),
    BusUnit("B-03", "220", "Pedro Moreta", FleetStatus.ACTIVE),
    BusUnit("B-04", "310", "Silvia Arcos", FleetStatus.ACTIVE),
    BusUnit("B-05", "401", "Marco Vaca", FleetStatus.MAINTENANCE),
    BusUnit("B-06", "112", "Rosa Lema", FleetStatus.ACTIVE),
    BusUnit("B-07", "Sin ruta", "Sin asignar", FleetStatus.BROKEN),
    BusUnit("B-08", "205", "Jaime Pillajo", FleetStatus.ACTIVE),
    BusUnit("B-09", "502", "Carla Nunez", FleetStatus.ACTIVE),
    BusUnit("B-10", "101", "Diego Salcedo", FleetStatus.MAINTENANCE),
)

val sampleAlerts = listOf(
    Alert(
        "Retraso - Ruta 112",
        "Bus B-12 lleva 18 min de retraso. Causa: trafico en Av. 10 de Agosto.",
        "13:04",
        AlertType.WARNING
    ),
    Alert(
        "Averia - Bus B-07",
        "Falla mecanica reportada. Bus fuera de servicio. Unidad sustituta asignada.",
        "11:42",
        AlertType.DANGER
    ),
    Alert(
        "Conductor ausente - Ruta 205",
        "Conductor Roberto Aguirre no se presento al turno. Requiere reasignacion.",
        "06:15",
        AlertType.WARNING
    ),
    Alert(
        "Mantenimiento completado",
        "Bus B-19 retorno a servicio tras mantenimiento preventivo.",
        "07:30",
        AlertType.SUCCESS
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MantaEnRutaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TransitOpsNavigation()
                }
            }
        }
    }
}

@Composable
fun TransitOpsNavigation() {
    val context = LocalContext.current
    val usuarioDao = remember { AppDatabase.getDatabase(context).usuarioDao() }
    val usuariosRegistrados by usuarioDao.observarUsuarios().collectAsState(initial = emptyList())
    var screen by remember { mutableStateOf(AppScreen.LOGIN) }
    var usuarioAutenticado by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        seedInitialUser(usuarioDao)
    }

    when (screen) {
        AppScreen.LOGIN -> LoginScreen(
            usuarioDao = usuarioDao,
            onLoginSuccess = { usuario ->
                usuarioAutenticado = usuario
                screen = AppScreen.DASHBOARD
            }
        )

        AppScreen.DASHBOARD -> DashboardScreen(
            usuario = usuarioAutenticado,
            usuariosRegistrados = usuariosRegistrados,
            onRegister = { screen = AppScreen.REGISTER },
            onLogout = {
                usuarioAutenticado = null
                screen = AppScreen.LOGIN
            }
        )

        AppScreen.REGISTER -> RegisterUserScreen(
            usuarioDao = usuarioDao,
            onBack = { screen = AppScreen.DASHBOARD },
            onUserSaved = { screen = AppScreen.DASHBOARD }
        )
    }
}

private suspend fun seedInitialUser(usuarioDao: UsuarioDao) {
    withContext(Dispatchers.IO) {
        if (usuarioDao.contarUsuarios() == 0) {
            usuarioDao.insertar(
                Usuario(
                    0,
                    "Miguel Operador",
                    "miguel@miguel.com",
                    "miguel",
                    "123456",
                    "0999999999",
                    currentDateTime(),
                    0.0,
                    0.0
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    usuarioDao: UsuarioDao,
    onLoginSuccess: (Usuario) -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GreenDark, GreenPrimary)))
                .padding(top = 64.dp, start = 24.dp, end = 24.dp, bottom = 36.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BUS", fontSize = 13.sp, fontWeight = FontWeight.Black, color = GreenPrimary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Bustrans", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Sistema de gestion urbana", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Bienvenido\nde vuelta",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 32.sp
                )
                Text(
                    "Inicia sesion para continuar",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-16).dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                item {
                    Text("Correo electronico o usuario", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GrayText)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = login,
                        onValueChange = {
                            login = it
                            loginError = null
                        },
                        placeholder = { Text("miguel@miguel.com o miguel", fontSize = 14.sp) },
                        isError = loginError != null,
                        supportingText = { loginError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenMid,
                            unfocusedBorderColor = GrayBorder
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Contrasena", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GrayText)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        placeholder = { Text("123456", fontSize = 14.sp) },
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Ocultar" else "Mostrar", fontSize = 12.sp, color = GreenPrimary)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenMid,
                            unfocusedBorderColor = GrayBorder
                        )
                    )

                    Text(
                        "Usuario inicial: miguel@miguel.com / 123456",
                        color = GrayText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            loginError = null
                            passwordError = null
                            var isValid = true
                            if (login.isBlank()) {
                                loginError = "Ingrese su correo o usuario."
                                isValid = false
                            }
                            if (password.isBlank()) {
                                passwordError = "Ingrese su contrasena."
                                isValid = false
                            }
                            if (isValid) {
                                isLoading = true
                                scope.launch {
                                    delay(500)
                                    val usuario = withContext(Dispatchers.IO) {
                                        usuarioDao.buscarPorLogin(login.trim())
                                    }
                                    isLoading = false
                                    when {
                                        usuario == null -> loginError = "El usuario no existe."
                                        usuario.contrasena != password -> passwordError = "Contrasena incorrecta."
                                        else -> {
                                            Toast.makeText(context, "Inicio de sesion exitoso.", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(usuario)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Ingresar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserScreen(
    usuarioDao: UsuarioDao,
    onBack: () -> Unit,
    onUserSaved: () -> Unit
) {
    var nombres by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fechaRegistro by remember { mutableStateOf(currentDateTime()) }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            val location = readLastKnownLocation(context)
            if (location != null) {
                latitud = location.latitude
                longitud = location.longitude
                formError = null
            } else {
                formError = "No se pudo obtener la ubicacion. Active GPS o datos de ubicacion."
            }
        } else {
            formError = "La app necesita permiso de ubicacion para registrar el lugar de creacion."
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDark, GreenPrimary)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Registro de usuario", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Guarda datos, fecha y ubicacion", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                    }
                    TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                        Text("Volver")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RegisterField("Nombres completos", nombres, { nombres = it }, KeyboardType.Text)
                RegisterField("Correo electronico", correo, { correo = it }, KeyboardType.Email)
                RegisterField("Usuario", usuario, { usuario = it }, KeyboardType.Text)
                RegisterPasswordField("Contrasena", contrasena, { contrasena = it })
                RegisterPasswordField("Confirmacion de contrasena", confirmarContrasena, { confirmarContrasena = it })
                RegisterField("Telefono", telefono, { telefono = it }, KeyboardType.Phone)

                OutlinedTextField(
                    value = fechaRegistro,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de registro") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenMid,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceGray
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Ubicacion de creacion", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (latitud != null && longitud != null) {
                                "Latitud: %.6f\nLongitud: %.6f".format(latitud, longitud)
                            } else {
                                "Presione capturar ubicacion antes de guardar."
                            },
                            fontSize = 12.sp,
                            color = GrayText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                if (hasLocationPermission(context)) {
                                    val location = readLastKnownLocation(context)
                                    if (location != null) {
                                        latitud = location.latitude
                                        longitud = location.longitude
                                        formError = null
                                    } else {
                                        formError = "No se pudo obtener la ubicacion. Active GPS o datos de ubicacion."
                                    }
                                } else {
                                    locationLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                        ) {
                            Text("Capturar ubicacion")
                        }
                    }
                }

                formError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        fechaRegistro = currentDateTime()
                        formError = validateRegisterForm(
                            nombres = nombres,
                            correo = correo,
                            usuario = usuario,
                            contrasena = contrasena,
                            confirmarContrasena = confirmarContrasena,
                            telefono = telefono,
                            latitud = latitud,
                            longitud = longitud
                        )
                        if (formError == null) {
                            isSaving = true
                            scope.launch {
                                val duplicado = withContext(Dispatchers.IO) {
                                    usuarioDao.buscarDuplicado(correo.trim(), usuario.trim())
                                }
                                if (duplicado != null) {
                                    formError = "Ya existe un usuario con el mismo correo o usuario."
                                    isSaving = false
                                    return@launch
                                }
                                withContext(Dispatchers.IO) {
                                    usuarioDao.insertar(
                                        Usuario(
                                            0,
                                            nombres.trim(),
                                            correo.trim(),
                                            usuario.trim(),
                                            contrasena,
                                            telefono.trim(),
                                            fechaRegistro,
                                            latitud ?: 0.0,
                                            longitud ?: 0.0
                                        )
                                    )
                                }
                                isSaving = false
                                Toast.makeText(context, "Usuario registrado correctamente.", Toast.LENGTH_LONG).show()
                                onUserSaved()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar usuario", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenMid,
            unfocusedBorderColor = GrayBorder
        )
    )
}

@Composable
private fun RegisterPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(if (visible) "Ocultar" else "Mostrar", fontSize = 12.sp, color = GreenPrimary)
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenMid,
            unfocusedBorderColor = GrayBorder
        )
    )
}

private fun validateRegisterForm(
    nombres: String,
    correo: String,
    usuario: String,
    contrasena: String,
    confirmarContrasena: String,
    telefono: String,
    latitud: Double?,
    longitud: Double?
): String? {
    if (
        nombres.isBlank() ||
        correo.isBlank() ||
        usuario.isBlank() ||
        contrasena.isBlank() ||
        confirmarContrasena.isBlank() ||
        telefono.isBlank()
    ) {
        return "Complete todos los campos obligatorios."
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
        return "Ingrese un correo electronico valido."
    }
    if (contrasena.length < 6) {
        return "La contrasena debe tener al menos 6 caracteres."
    }
    if (contrasena != confirmarContrasena) {
        return "La contrasena y la confirmacion deben ser iguales."
    }
    if (latitud == null || longitud == null) {
        return "Capture la ubicacion antes de guardar el usuario."
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: Usuario?,
    usuariosRegistrados: List<Usuario>,
    onRegister: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Resumen", "Rutas", "Alertas", "Flota")

    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(GreenDark, GreenPrimary)))
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Bienvenido", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                Text(
                                    usuario?.nombres ?: "Operador",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Usuario: ${usuario?.usuario ?: "-"}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            TextButton(onClick = onLogout, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                                Text("Cerrar sesion", fontSize = 13.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShiftChip("Turno: 06:00 - 14:00")
                            ShiftChip("Activo", highlight = true)
                        }
                        Button(
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GreenPrimary)
                        ) {
                            Text("Registrar nuevo usuario", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ) {
                    tabs.forEachIndexed { i, label ->
                        Tab(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            text = {
                                Text(
                                    label,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == i) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTab == i) Color.White else Color.White.copy(alpha = 0.65f)
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TabResumen(usuariosRegistrados)
                1 -> TabRutas()
                2 -> TabAlertas()
                3 -> TabFlota()
            }
        }
    }
}

@Composable
private fun ShiftChip(label: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (highlight) GreenMid else Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
            color = Color.White
        )
    }
}

@Composable
fun TabResumen(usuariosRegistrados: List<Usuario>) {
    val hourlyData = listOf(30, 55, 65, 45, 25, 20, 48, 60)
    val hours = listOf("06h", "07h", "08h", "09h", "10h", "11h", "12h", "13h")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("42", "Buses activos", null, Modifier.weight(1f))
                StatCard("8", "Rutas operando", null, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(usuariosRegistrados.size.toString(), "Usuarios guardados", GreenPrimary, Modifier.weight(1f))
                StatCard("94%", "Puntualidad", GreenPrimary, Modifier.weight(1f))
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Pasajeros por hora - hoy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Total: 12,840 pasajeros", fontSize = 11.sp, color = GrayText, modifier = Modifier.padding(bottom = 12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        hourlyData.forEachIndexed { i, v ->
                            val isLast = i == hourlyData.lastIndex
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((v * 60 / 65).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (isLast) GreenLight
                                            else if (v >= 60) GreenPrimary
                                            else if (v >= 45) GreenMid
                                            else Color(0xFF9FE1CB)
                                        )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    hours[i],
                                    fontSize = 9.sp,
                                    color = if (isLast) GreenPrimary else GrayText,
                                    fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = GreenLight
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Ultimos usuarios registrados", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenDark)
                    Spacer(Modifier.height(6.dp))
                    usuariosRegistrados.take(3).forEach { user ->
                        Text(
                            "${user.nombres} - ${user.usuario} - ${user.fechaRegistro}",
                            fontSize = 12.sp,
                            color = GreenPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, valueColor: Color?, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = SurfaceGray) {
        Column(Modifier.padding(14.dp)) {
            Text(
                value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
            Text(label, fontSize = 12.sp, color = GrayText)
        }
    }
}

@Composable
fun TabRutas() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Text(
                "Rutas en operacion - martes 06 mayo",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        items(sampleRoutes) { route ->
            RouteRow(route)
            HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun RouteRow(route: BusRoute) {
    val (badgeText, badgeBg, badgeFg) = when (route.status) {
        RouteStatus.ON_TIME -> Triple("Puntual", GreenLight, GreenPrimary)
        RouteStatus.DELAYED -> Triple("Retraso", RedLight, RedDark)
        RouteStatus.WARNING -> Triple("Atencion", AmberLight, AmberDark)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text("BUS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = GreenPrimary)
        }
        Column(Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Ruta ${route.number}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = GreenPrimary
                )
                Surface(shape = RoundedCornerShape(99.dp), color = badgeBg) {
                    Text(
                        badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeFg
                    )
                }
            }
            Text(route.name, fontSize = 12.sp, color = GrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${route.punctualityPct}%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            Text("${route.buses} buses", fontSize = 10.sp, color = GrayText)
        }
    }
}

@Composable
fun TabAlertas() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Alertas activas", fontSize = 13.sp, color = GrayText, modifier = Modifier.padding(bottom = 4.dp))
        }
        items(sampleAlerts) { alert -> AlertCard(alert) }
    }
}

@Composable
fun AlertCard(alert: Alert) {
    val bg: Color
    val iconColor: Color
    val titleColor: Color
    val descColor: Color
    when (alert.type) {
        AlertType.WARNING -> {
            bg = AmberLight
            iconColor = AmberDark
            titleColor = AmberDark
            descColor = AmberDeep
        }

        AlertType.DANGER -> {
            bg = RedLight
            iconColor = RedDark
            titleColor = RedDark
            descColor = RedDeep
        }

        AlertType.SUCCESS -> {
            bg = GreenLight
            iconColor = GreenPrimary
            titleColor = GreenDark
            descColor = GreenPrimary
        }
    }
    val icon = when (alert.type) {
        AlertType.WARNING -> "!"
        AlertType.DANGER -> "X"
        AlertType.SUCCESS -> "OK"
    }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = bg) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(icon, fontSize = 15.sp, fontWeight = FontWeight.Black, color = iconColor, modifier = Modifier.padding(top = 1.dp))
            Column {
                Text(alert.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(alert.description, fontSize = 12.sp, color = descColor, modifier = Modifier.padding(top = 2.dp))
                Text(alert.time, fontSize = 11.sp, color = iconColor, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun TabFlota() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("50", "Total flota", null, Modifier.weight(1f))
                StatCard("42", "En servicio", GreenPrimary, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("5", "En mantenimiento", AmberDark, Modifier.weight(1f))
                StatCard("3", "Fuera de servicio", RedDark, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Text("Detalle por unidad", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        }
        items(sampleFleet) { unit -> FleetUnitCard(unit) }
    }
}

@Composable
fun FleetUnitCard(unit: BusUnit) {
    val (badgeText, badgeBg, badgeFg) = when (unit.status) {
        FleetStatus.ACTIVE -> Triple("Activo", GreenLight, GreenPrimary)
        FleetStatus.MAINTENANCE -> Triple("Mant.", AmberLight, AmberDark)
        FleetStatus.BROKEN -> Triple("Averia", RedLight, RedDark)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    unit.id,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = badgeFg
                )
            }
            Column(Modifier.weight(1f)) {
                Text(unit.driver, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Ruta ${unit.routeNumber}", fontSize = 11.sp, color = GrayText)
            }
            Surface(shape = RoundedCornerShape(99.dp), color = badgeBg) {
                Text(
                    badgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeFg
                )
            }
        }
    }
}

private fun currentDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun readLastKnownLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    )
    return providers
        .filter { provider -> locationManager.getProviders(true).contains(provider) }
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { it.time }
}
