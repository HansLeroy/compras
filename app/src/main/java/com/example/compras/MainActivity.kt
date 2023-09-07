package com.example.compras

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.compras.bd.AplicacionDataBase
import com.example.compras.bd.Compras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            val comprasDao = AplicacionDataBase.getInstance(this@MainActivity).ComprasDao()
            val cantRegistros = comprasDao.contar()
            if (cantRegistros < 2) {
                comprasDao.insertar(Compras(0, "lechuga", false))
                comprasDao.insertar(Compras(1, "leche", true))
                comprasDao.insertar(Compras(2, "papas", true))
                comprasDao.insertar(Compras(3, "queso", false))
                comprasDao.insertar(Compras(4, "manjar", true))
            }
        }
        setContent {
            AppComprasUI()
        }
    }
}

enum class Accion {
    LISTAR, CREAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppComprasUI() {
    val contexto = LocalContext.current
    val (compras, setCompras) = remember { mutableStateOf(emptyList<Compras>()) }
    val (accion, setAccion) = remember { mutableStateOf(Accion.LISTAR) }

    LaunchedEffect(compras) {
        withContext(Dispatchers.IO) {
            val db = AplicacionDataBase.getInstance(contexto)
            setCompras(db.ComprasDao().findAll())
            Log.v("AppComprasUI", "LaunchedEffect()")
        }
    }

    when (accion) {
        Accion.CREAR -> ComprasFormUI { setAccion(Accion.LISTAR) }

        else -> ComprasListUI(
            compras = compras,
            onSave = { setAccion(Accion.CREAR) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasListUI(compras: List<Compras>, onSave: () -> Unit = {}) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onSave() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "agregar"
                    )
                },
                text = { Text("Agregar") }
            )
        }
    ) { contentPadding ->
        if (compras.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(compras) { compra ->
                    ComprasItemUI(compra) {
                        onSave()
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay productos que mostrar.")
            }
        }
    }
}

@Composable
fun ComprasItemUI(compra: Compras, onSave: () -> Unit = {}) {
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 17.dp, horizontal = 20.dp)
    ) {
        if (compra.realizada) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Compra Realizada",
                modifier = Modifier.clickable {
                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = AplicacionDataBase.getInstance(contexto).ComprasDao()
                        compra.realizada = false
                        dao.actualizar(compra)
                        onSave()
                    }
                }
            )
        } else {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Compra Por realizar",
                modifier = Modifier.clickable {
                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = AplicacionDataBase.getInstance(contexto).ComprasDao()
                        compra.realizada = true
                        dao.actualizar(compra)
                        onSave()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = compra.compras,
            modifier = Modifier.weight(2f)
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Eliminar Compra",
            modifier = Modifier.clickable {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = AplicacionDataBase.getInstance(contexto).ComprasDao()
                    dao.eliminar(compra)
                    onSave()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasFormUI(onSave: () -> Unit = {}) {
    val contexto = LocalContext.current
    var compras by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(16.dp)) }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = compras,
                onValueChange = { compras = it },
                label = { Text("Nombre") }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = AplicacionDataBase.getInstance(contexto).ComprasDao()
                    val nuevaCompra = Compras(0, compras, false)
                    dao.insertar(nuevaCompra)
                    snackbarHostState.showSnackbar("Compra agregada")
                    compras = ""
                    onSave()
                }
            }) {
                Text("Guardar")
            }
        }
    }
}
