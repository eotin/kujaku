package io.ona.kujaku.sample.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.plugins.annotation.Circle;

import java.util.List;

import io.ona.kujaku.layers.FillBoundaryLayer;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.OnKujakuLayerLongClickListener;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
import io.ona.kujaku.listeners.OnDrawingCircleLongClickListener;
import io.ona.kujaku.views.KujakuMapView;

public class DrawingBoundariesActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = DrawingBoundariesActivity.class.getName();

    private KujakuMapView kujakuMapView;

    private Button deleteBtn ;
    private Button drawingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_drawingBoundaries_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        this.deleteBtn = findViewById(R.id.btn_drawingBoundaries_delete);
        this.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kujakuMapView.deleteDrawingCurrentCircle();
                view.setEnabled(false);
            }
        });

        this.drawingBtn = findViewById(R.id.btn_drawingBoundaries_drawing);
        this.drawingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Drawing from scratch
                if (! kujakuMapView.isDrawingEnabled()) {
                    startDrawing(null);
                } else {
                    stopDrawing();
                }
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                kujakuMapView.focusOnUserLocation(true);
                mapboxMap.setStyle(Style.MAPBOX_STREETS,  new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        kujakuMapView.createDrawingManager(style);

                       /* try {
                            InputStreamReader streamReader = new InputStreamReader(getBaseContext().getResources().getAssets().open("annotations.json"));
                            BufferedReader reader = new BufferedReader(streamReader);
                            StringBuilder out = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                out.append(line);
                            }
                            reader.close();

                            LineManager lineManager = new LineManager(kujakuMapView, mapboxMap, style);
                            lineManager.create(out.toString());
                        } catch (IOException e) {
                            throw new RuntimeException("Unable to parse annotations.json");
                        }*/

                        kujakuMapView.addOnDrawingCircleClickListener(new OnDrawingCircleClickListener() {
                            @Override
                            public void onCircleClick(Circle circle) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle clicked"),Toast.LENGTH_SHORT).show();

                                kujakuMapView.unsetCurrentCircleDraggable();

                                if (circle.isDraggable()) {
                                    deleteBtn.setEnabled(false);
                                    kujakuMapView.setCircleDraggable(false, circle);

                                } else if (!circle.isDraggable()) {
                                    deleteBtn.setEnabled(true);
                                    kujakuMapView.setCircleDraggable(true, circle);
                                }

                            }

                            @Override
                            public void onCircleNotClick(@NonNull LatLng latLng) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle NOT clicked"),Toast.LENGTH_SHORT).show();

                                if (kujakuMapView.getCurrentKujakuCircle() != null) {
                                    kujakuMapView.unsetCurrentCircleDraggable();
                                    deleteBtn.setEnabled(false);
                                } else {
                                    kujakuMapView.drawCircle(latLng);
                                }
                            }
                        });

                        kujakuMapView.addOnDrawingCircleLongClickListener(new OnDrawingCircleLongClickListener() {
                            @Override
                            public void onCircleLongClick(Circle circle) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle long clicked"),Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCircleNotLongClick(@NonNull LatLng point) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle NOT long clicked"),Toast.LENGTH_SHORT).show();
                            }
                        });

                        kujakuMapView.setOnKujakuLayerLongClickListener(new OnKujakuLayerLongClickListener() {
                            @Override
                            public void onKujakuLayerLongClick(KujakuLayer kujakuLayer) {
                                if (!kujakuMapView.isDrawingEnabled()) {
                                    Geometry geometry = kujakuLayer.getFeatureCollection().features().get(0).geometry();
                                    if (geometry instanceof Polygon) {
                                        kujakuLayer.removeLayerOnMap(mapboxMap);
                                        Polygon polygon = (Polygon) geometry;
                                        List<Point> points = polygon.coordinates().get(0);
                                        startDrawing(points);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void startDrawing(List<Point> points) {
        kujakuMapView.startDrawing(points);
        drawingBtn.setText(R.string.drawing_boundaries_stop_draw);
    }

    private void stopDrawing() {
        Polygon polygon = kujakuMapView.stopDrawing();
        drawingBtn.setText(R.string.drawing_boundaries_start_draw);
        deleteBtn.setEnabled(false);

        Feature feature = Feature.fromGeometry(polygon);
        FeatureCollection collection = FeatureCollection.fromFeature(feature);
        FillBoundaryLayer layer = new FillBoundaryLayer.Builder(collection)
                .setBoundaryColor(Color.BLACK)
                .setBoundaryWidth(3f)
                .build();

        kujakuMapView.addLayer(layer);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_drawing_boundaries_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_drawing_boundaries;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (kujakuMapView != null) kujakuMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (kujakuMapView != null) kujakuMapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (kujakuMapView != null) kujakuMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kujakuMapView != null) kujakuMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (kujakuMapView != null) kujakuMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (kujakuMapView != null) kujakuMapView.onLowMemory();
    }
}
