package com.camuel.sanchay.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ui.components.NeonButton
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingMapScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val allExpenses by viewModel.allExpenses.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val expensesWithLocation = remember(allExpenses) {
        allExpenses.filter { it.latitude != null && it.longitude != null && !it.isDeleted }
    }

    val totalHeatmapSpent = remember(expensesWithLocation) {
        expensesWithLocation.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }

    val maxExpenseAmount = remember(expensesWithLocation) {
        expensesWithLocation.maxOfOrNull { it.amount.toDoubleOrNull() ?: 1.0 } ?: 1.0
    }.coerceAtLeast(1.0)

    // Ensure OSMDroid configuration is loaded immediately
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    var mapViewReference by remember { mutableStateOf<MapView?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
    }

    val fusedClient = remember { 
        try { 
            LocationServices.getFusedLocationProviderClient(context) 
        } catch (t: Throwable) { 
            null 
        } 
    }

    // Fetch user device location
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && fusedClient != null) {
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLocation = GeoPoint(loc.latitude, loc.longitude)
                    }
                }
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            userLocation = GeoPoint(loc.latitude, loc.longitude)
                        }
                    }
            } catch (t: Throwable) { }
        }
    }

    // Lifecycle observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewReference?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewReference?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewReference?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewReference?.onDetach()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Spending Heatmap",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Geotagged spending locations",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (expensesWithLocation.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "${expensesWithLocation.size} zones",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // OSMDroid Map View
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        val initialCenter: GeoPoint = when {
                            expensesWithLocation.isNotEmpty() -> {
                                val avgLat = expensesWithLocation.map { it.latitude!! }.average()
                                val avgLng = expensesWithLocation.map { it.longitude!! }.average()
                                GeoPoint(avgLat, avgLng)
                            }
                            userLocation != null -> userLocation!!
                            else -> GeoPoint(28.6139, 77.2090) // Default India center
                        }

                        controller.setZoom(if (expensesWithLocation.isNotEmpty()) 14.5 else 13.0)
                        controller.setCenter(initialCenter)
                        mapViewReference = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.overlays.clear()

                    // 1. Draw User Location Indicator if available
                    userLocation?.let { uLoc ->
                        val userHalo = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(uLoc, 45.0)
                            fillPaint.color = android.graphics.Color.argb(80, 59, 130, 246)
                            fillPaint.style = Paint.Style.FILL
                            outlinePaint.color = android.graphics.Color.argb(200, 37, 99, 235)
                            outlinePaint.strokeWidth = 2f
                            outlinePaint.style = Paint.Style.STROKE
                        }
                        mapView.overlays.add(userHalo)

                        val userCenter = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(uLoc, 12.0)
                            fillPaint.color = android.graphics.Color.argb(255, 37, 99, 235)
                            fillPaint.style = Paint.Style.FILL
                            outlinePaint.color = android.graphics.Color.WHITE
                            outlinePaint.strokeWidth = 2f
                            outlinePaint.style = Paint.Style.STROKE
                        }
                        mapView.overlays.add(userCenter)
                    }

                    // 2. Draw Spending Heat Density Clusters (Concentric Polygons)
                    expensesWithLocation.forEach { expense ->
                        val pt = GeoPoint(expense.latitude!!, expense.longitude!!)
                        val amountVal = expense.amount.toDoubleOrNull() ?: 0.0
                        val intensity = (amountVal / maxExpenseAmount).coerceIn(0.2, 1.0)

                        // Outer thermal dissipation glow
                        val outerRadius = (200.0 * intensity).coerceAtLeast(80.0)
                        val outerGlow = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(pt, outerRadius)
                            fillPaint.color = android.graphics.Color.argb(40, 239, 68, 68)
                            fillPaint.style = Paint.Style.FILL
                            outlinePaint.color = android.graphics.Color.TRANSPARENT
                            outlinePaint.strokeWidth = 0f
                        }
                        mapView.overlays.add(outerGlow)

                        // Middle heat ring
                        val midRadius = (100.0 * intensity).coerceAtLeast(40.0)
                        val midRing = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(pt, midRadius)
                            fillPaint.color = android.graphics.Color.argb(85, 245, 158, 11)
                            fillPaint.style = Paint.Style.FILL
                            outlinePaint.color = android.graphics.Color.TRANSPARENT
                            outlinePaint.strokeWidth = 0f
                        }
                        mapView.overlays.add(midRing)

                        // Core intense heat signature
                        val coreRadius = (45.0 * intensity).coerceAtLeast(18.0)
                        val coreDisc = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(pt, coreRadius)
                            fillPaint.color = android.graphics.Color.argb(160, 220, 38, 38)
                            fillPaint.style = Paint.Style.FILL
                            outlinePaint.color = android.graphics.Color.TRANSPARENT
                            outlinePaint.strokeWidth = 0f
                        }
                        mapView.overlays.add(coreDisc)

                        // Interactive pin marker at epicenter
                        val marker = Marker(mapView).apply {
                            position = pt
                            title = "${expense.category}: $currencySymbol${expense.amount}"
                            snippet = expense.note.ifBlank { "${expense.date} • ${expense.paymentMethod}" }
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedExpense = expense
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }

                    mapView.invalidate()
                }
            )

            // Top Summary Overlay Badge
            if (expensesWithLocation.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔥", fontSize = 14.sp)
                        Text(
                            text = "${expensesWithLocation.size} Clusters",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Total $currencySymbol${String.format(Locale.getDefault(), "%.2f", totalHeatmapSpent)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LeafGreenDeep
                        )
                    }
                }
            }

            // Floating Controls (Right side)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Zoom In
                SmallFloatingActionButton(
                    onClick = { mapViewReference?.controller?.zoomIn() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                // Zoom Out
                SmallFloatingActionButton(
                    onClick = { mapViewReference?.controller?.zoomOut() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                // Fit All Clusters
                if (expensesWithLocation.isNotEmpty()) {
                    SmallFloatingActionButton(
                        onClick = {
                            val points = expensesWithLocation.map { GeoPoint(it.latitude!!, it.longitude!!) }
                            try {
                                val bbox = BoundingBox.fromGeoPoints(points)
                                mapViewReference?.zoomToBoundingBox(bbox, true, 120)
                            } catch (e: Exception) {
                                val avgLat = points.map { it.latitude }.average()
                                val avgLng = points.map { it.longitude }.average()
                                mapViewReference?.controller?.animateTo(GeoPoint(avgLat, avgLng))
                                mapViewReference?.controller?.setZoom(14.0)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.ZoomOutMap, contentDescription = "Fit Clusters")
                    }
                }

                // My Location
                FloatingActionButton(
                    onClick = {
                        if (!hasLocationPermission) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            userLocation?.let {
                                mapViewReference?.controller?.animateTo(it)
                                mapViewReference?.controller?.setZoom(16.0)
                            } ?: run {
                                try {
                                    fusedClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                        ?.addOnSuccessListener { loc ->
                                            if (loc != null) {
                                                val gp = GeoPoint(loc.latitude, loc.longitude)
                                                userLocation = gp
                                                mapViewReference?.controller?.animateTo(gp)
                                                mapViewReference?.controller?.setZoom(16.0)
                                            } else {
                                                Toast.makeText(context, "Locating GPS position...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } catch (t: Throwable) {
                                    Toast.makeText(context, "Location unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }

            // Bottom Overlay: Selected Expense Detail Card
            AnimatedVisibility(
                visible = selectedExpense != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedExpense?.let { exp ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = exp.category,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = LeafGreenPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = exp.paymentMethod,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false,
                                            color = LeafGreenDeep,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (exp.note.isNotBlank()) {
                                    Text(
                                        text = exp.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "Date: ${exp.date}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${exp.amount}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseColor
                                )
                                Spacer(Modifier.height(4.dp))
                                IconButton(
                                    onClick = { selectedExpense = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Empty State Card with One-Tap Geotagging
            if (expensesWithLocation.isEmpty() && selectedExpense == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = LeafGreenDeep,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "No Geotagged Expenses Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Expenses record coordinates when location is enabled. Or tag recent transactions to your current location to generate a heatmap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        NeonButton(
                            onClick = {
                                if (!hasLocationPermission) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    userLocation?.let { loc ->
                                        viewModel.tagExpensesWithLocation(loc.latitude, loc.longitude)
                                        mapViewReference?.controller?.animateTo(loc)
                                        mapViewReference?.controller?.setZoom(15.0)
                                        Toast.makeText(context, "Tagged recent transactions to here! Heatmap generated.", Toast.LENGTH_SHORT).show()
                                    } ?: run {
                                        fusedClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                            ?.addOnSuccessListener { l ->
                                                if (l != null) {
                                                    val gp = GeoPoint(l.latitude, l.longitude)
                                                    userLocation = gp
                                                    viewModel.tagExpensesWithLocation(l.latitude, l.longitude)
                                                    mapViewReference?.controller?.animateTo(gp)
                                                    mapViewReference?.controller?.setZoom(15.0)
                                                    Toast.makeText(context, "Tagged recent transactions to here! Heatmap generated.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Acquiring GPS location, please retry...", Toast.LENGTH_SHORT).show()
                                                }
                                            } ?: Toast.makeText(context, "Location service unavailable.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PinDrop, contentDescription = null)
                                Text("📍 Tag Recent Transactions Here", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
