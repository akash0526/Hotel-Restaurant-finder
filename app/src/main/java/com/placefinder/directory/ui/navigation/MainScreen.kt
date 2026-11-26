package com.placefinder.directory.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomNavigation
import androidx.compose.material3.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.placefinder.directory.R
import com.placefinder.directory.data.repository.FirestorePlaceRepository
import com.placefinder.directory.ui.screens.ExploreScreen
import com.placefinder.directory.ui.screens.PlaceMapScreen
import com.placefinder.directory.ui.screens.MyPlacesScreen
import com.placefinder.directory.ui.screens.PlaceDetailScreen
import com.placefinder.directory.ui.screens.PlaceEditorScreen

private const val ExploreRoute = "explore"
private const val MapRoute = "map"
private const val MyPlacesRoute = "my_places"
private const val PlaceEditorRoute = "place_editor"

/**
 * App shell: bottom navigation (Explore / My Places) on top of a NavHost.
 * Stage 2+ will keep the same structure while adding the Map screen.
 */
@Composable
fun MainScreen() {
    // Stage 4: remote repository is the source of truth. Room (`LocalPlaceRepository`)
    // stays available if you want a local cache or offline mode later.
    val repository = remember { FirestorePlaceRepository(FirebaseFirestore.getInstance()) }
    LaunchedEffect(repository) { repository.startListening() }

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
        }
    ) { innerPadding ->
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
                    onBack = { navController.popBackStack(ExploreRoute, inclusive = false) },
                )
            }
        }
    }
}
