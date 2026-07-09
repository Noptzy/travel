package com.oop.traveloop.ui.plan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import com.oop.traveloop.domain.model.TripPlan
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaTeal

@Composable
fun RouteMap(plan: TripPlan) {
    val positions = remember(plan.routePoints) { plan.routePoints.map { Position(it.longitude, it.latitude) } }
    if (positions.size < 2) return
    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(
                longitude = (plan.originPoint.longitude + plan.destinationPoint.longitude) / 2,
                latitude = (plan.originPoint.latitude + plan.destinationPoint.latitude) / 2,
            ),
            zoom = 4.6,
        ),
    )
    LaunchedEffect(positions) {
        camera.jumpTo(
            boundingBox = BoundingBox(
                southwest = Position(positions.minOf { it.longitude }, positions.minOf { it.latitude }),
                northeast = Position(positions.maxOf { it.longitude }, positions.maxOf { it.latitude }),
            ),
            padding = PaddingValues(26.dp),
        )
    }
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(250.dp)) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                cameraState = camera,
            ) {
                val routeSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(FeatureCollection(listOf(Feature(geometry = LineString(positions), properties = Unit)))),
                )
                val markerSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        FeatureCollection(
                            listOf(
                                Feature(geometry = Point(positions.first()), properties = Unit),
                                Feature(geometry = Point(positions.last()), properties = Unit),
                            ),
                        ),
                    ),
                )
                LineLayer(
                    id = "trip-route",
                    source = routeSource,
                    color = const(SenjaTeal),
                    width = const(5.dp),
                    cap = const(LineCap.Round),
                    join = const(LineJoin.Round),
                )
                CircleLayer(
                    id = "trip-markers",
                    source = markerSource,
                    radius = const(8.dp),
                    color = const(SenjaSand),
                    strokeColor = const(Color.White),
                    strokeWidth = const(3.dp),
                )
            }
            Surface(
                color = if (plan.routeEstimated) SenjaSand else SenjaTeal,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            ) {
                Text(
                    if (plan.routeEstimated) "Rute estimasi" else "Rute OSM langsung",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = if (plan.routeEstimated) Color(0xFF20242A) else Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
