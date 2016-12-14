package ogr.scorelab.ucsc.mobility_track;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kailas on 12/10/2016.
 */

class MapHandler implements GoogleMap.OnMarkerDragListener{

    private GoogleMap map;

    private HashMap<String,Polyline> paths = new HashMap<>();

    MapHandler(GoogleMap map) {
        this.map = map;
        map.setOnMarkerDragListener(this);
    }

    void drawPath(LatLng... points){
        if(points.length <= 1){
            return;
        }

        PolylineOptions ops = new PolylineOptions();
        ops.add(points);
        Polyline line = map.addPolyline(ops);
        paths.put(line.getId(),line);

        for (LatLng pos:
             points) {
            map.addMarker(new MarkerOptions().position(pos).draggable(true)).setTag(new MarkerTag(line.getId(),pos));
        }

    }

    //Draw path with default locations
    void drawPath(){
        drawPath(new LatLng(38,-122),new LatLng(37,-122));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        MarkerTag tag = ((MarkerTag) marker.getTag());
        Polyline line = paths.get(tag.getLineID());

        List<LatLng> points = line.getPoints();
        int index = points.indexOf(tag.getPos());
        points.set(index,marker.getPosition());
        tag.updatePos(marker.getPosition());

        line.setPoints(points);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private void showTrackers(){

    }

    //Class set as tag to marker to aid in identification
    private class MarkerTag{

        private LatLng pos;
        private String lineID;

        MarkerTag(String lineID, LatLng pos) {
            this.lineID = lineID;
            this.pos = pos;
        }

        void updatePos(LatLng newPos){
            this.pos = newPos;
        }

        LatLng getPos() {
            return pos;
        }

        String getLineID() {
            return lineID;
        }
    }
}
