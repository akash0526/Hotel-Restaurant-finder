package com.placefinder.directory.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomNavigation
import androidx.compose.material3.BottomNavigationItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

/**
 * App shell: bottom navigation (Explore / Map / My Places).
 * Catalog hotels are seeded into Room from assets so tourists see nearby listings
 * without a Firebase project.
 */
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

    val navItems = listOf(
        Triple(ExploreRoute, stringResource(R.string.explore), Icons.Default.Search),
        Triple(MapRoute, stringResource(R.string.map), Icons.Default.LocationOn),
        Triple(MyPlacesRoute, stringResource(R.string.my_places), Icons.Default.Star),
    )

    Scaffold(
        bottomBar = {
            BottomNavigation {
                for ((route, label, icon) in navItems) {
                    BottomNavigationItem(
                        icon = { Icon(imageVector = icon, contentDescription = null) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = { navController.navigate(route) },
                    )
                }
            }
        },
    ) { innerPadding ->
        if (!catalogReady) {
            Box(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.loading_hotels))
                }
            }
        } else {
            NavHost(navController, ExploreRoute, Modifier.padding(innerPadding)) {
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
                composable("place_editor/{placeId}") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
                    PlaceEditorScreen(
                        placeId = placeId,
                        repository = repository,
                        onBack = { navController.popBackStack(MyPlacesRoute, inclusive = false) },
                    )
                }
                composable("place_detail/{placeId}") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
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
