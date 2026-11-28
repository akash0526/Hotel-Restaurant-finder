package com.placefinder.directory.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.placefinder.directory.R
import com.placefinder.directory.data.database.PlaceDatabase
import com.placefinder.directory.data.repository.LocalPlaceRepository
import com.placefinder.directory.data.seed.PlaceSeeder
import com.placefinder.directory.ui.screens.ExploreScreen
import com.placefinder.directory.ui.screens.MyPlacesScreen
import com.placefinder.directory.ui.screens.PlaceDetailScreen
import com.placefinder.directory.ui.screens.PlaceEditorScreen
import com.placefinder.directory.ui.screens.PlaceMapScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ExploreRoute = "explore"
private const val MapRoute = "map"
private const val MyPlacesRoute = "my_places"
private const val PlaceEditorRoute = "place_editor"

private data class TabItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun MainScreen() {
    val context = LocalContext.current.applicationContext
    val database = remember { PlaceDatabase.get(context) }
    val repository = remember { LocalPlaceRepository(database.placeDao()) }
    var catalogReady by remember { mutableStateOf(false) }

    LaunchedEffect(database) {
        withContext(Dispatchers.IO) {
            PlaceSeeder.seedIfNeeded(context, database)
        }
        catalogReady = true
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(ExploreRoute, MapRoute, MyPlacesRoute)

    val tabs = listOf(
        TabItem(ExploreRoute, R.string.explore, Icons.Filled.Explore, Icons.Outlined.Explore),
        TabItem(MapRoute, R.string.map, Icons.Filled.Map, Icons.Outlined.Map),
        TabItem(MyPlacesRoute, R.string.my_places, Icons.Filled.Storefront, Icons.Outlined.Storefront),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    for (tab in tabs) {
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        if (!catalogReady) {
            LoadingCatalog(Modifier.padding(innerPadding).fillMaxSize())
        } else {
            NavHost(
                navController,
                ExploreRoute,
                Modifier.padding(innerPadding),
            ) {
                composable(ExploreRoute) {
                    ExploreScreen(
                        repository = repository,
                        onPlaceClick = { navController.navigate("place_detail/${it.id}") },
                    )
                }
                composable(MapRoute) {
                    PlaceMapScreen(
                        repository = repository,
                        onPlaceClick = { navController.navigate("place_detail/${it.id}") },
                    )
                }
                composable(MyPlacesRoute) {
                    MyPlacesScreen(
                        repository = repository,
                        onPlaceClick = { navController.navigate("place_detail/${it.id}") },
                        onAddPlace = { navController.navigate(PlaceEditorRoute) },
                    )
                }
                composable(PlaceEditorRoute) {
                    PlaceEditorScreen(
                        repository = repository,
                        onBack = { navController.popBackStack(MyPlacesRoute, inclusive = false) },
                    )
                }
                composable("place_editor/{placeId}") { entry ->
                    val placeId = entry.arguments?.getString("placeId") ?: ""
                    PlaceEditorScreen(
                        placeId = placeId,
                        repository = repository,
                        onBack = { navController.popBackStack(MyPlacesRoute, inclusive = false) },
                    )
                }
                composable("place_detail/{placeId}") { entry ->
                    val placeId = entry.arguments?.getString("placeId") ?: ""
                    PlaceDetailScreen(
                        placeId = placeId,
                        repository = repository,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCatalog(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Hotel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.brand_name), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.loading_hotels),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
