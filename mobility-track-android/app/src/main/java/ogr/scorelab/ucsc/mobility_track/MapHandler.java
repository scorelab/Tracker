package ogr.scorelab.ucsc.mobility_track;

import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
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

    private final double SEARCH_RADIUS_IN_M = 1000;

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

        showTrackers(line.getPoints());

    }

    //Draw path with default locations
    void drawPath(){
        drawPath(new LatLng(37.2,-122),new LatLng(37,-122));
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
        showTrackers(paths.get(((MarkerTag) marker.getTag()).getLineID()).getPoints());
    }

    private void showTrackers(List<LatLng> points){

        List<LatLngBounds> boxes = new RouteBoxer().getRouteBoxes(points,SEARCH_RADIUS_IN_M);

        for (LatLngBounds box :
                boxes) {

            LatLng northEast = box.northeast;
            LatLng southWest = box.southwest;

            map.addPolygon(new PolygonOptions().add(new LatLng(northEast.latitude,northEast.longitude),new LatLng(northEast.latitude,southWest.longitude),new LatLng(southWest.latitude,southWest.longitude),new LatLng(southWest.latitude,northEast.longitude)));
        }


        LatLng[] trackers = generateTrackers();

        for (int i = 0; i < trackers.length; i++) {
            for (LatLngBounds box
                    :boxes) {
                if(box.contains(trackers[i])){
                    map.addMarker(new MarkerOptions().position(trackers[i]));
                }
            }
        }

    }

    private LatLng[] generateTrackers(){
        LatLng[] trackers = new LatLng[100];

        for(int i = 0; i < trackers.length; i++){
            trackers[i] = new LatLng((Math.random()/2)+37,(Math.random()/2)-122);
        }
        return trackers;
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


    private class RouteBoxer{
        double R = 6371;
        ArrayList<Double> vertLines = new ArrayList<>();
        ArrayList<Double> horLines = new ArrayList<>();
        ArrayList<LatLngBounds> boxesX = new ArrayList<>();
        ArrayList<LatLngBounds> boxesY = new ArrayList<>();
        boolean[][] grid;

        private List<LatLngBounds> getRouteBoxes(List<LatLng> points,double radius){
            ArrayList<LatLngBounds> boxes = new ArrayList<>();

            LatLngBounds.Builder bigBoundsBuilder = new LatLngBounds.Builder();


            for (LatLng point :
                    points) {
                bigBoundsBuilder.include(point);
            }

            LatLngBounds bigBounds = bigBoundsBuilder.build();
            LatLng center = bigBounds.getCenter();

            vertLines.add(center.latitude);
            vertLines.add(rhumbDestinationPoint(0,radius,center).latitude);
            for(int i = 2;vertLines.get(i-2) < bigBounds.northeast.latitude;i++){
                vertLines.add(rhumbDestinationPoint(0,radius*i,center).latitude);
            }

            for(int i = 1;vertLines.get(1) > bigBounds.southwest.latitude;i++){
                vertLines.add(0,rhumbDestinationPoint(180,radius*i,center).latitude);
            }

            horLines.add(center.longitude);
            horLines.add(rhumbDestinationPoint(90,radius,center).longitude);
            for(int i = 2;horLines.get(i-2) < bigBounds.northeast.longitude;i++){
                horLines.add(rhumbDestinationPoint(90,radius*i,center).longitude);
            }

            for(int i = 1;horLines.get(1) > bigBounds.southwest.longitude;i++){
                horLines.add(0,rhumbDestinationPoint(270,radius*i,center).longitude);
            }

            grid = new boolean[horLines.size()][vertLines.size()];

            findCellsInPath(points);
            mergeIntersectingCells();
//
//            for (int i = 1; i < horLines.size(); i++) {
//                for (int j = 1; j < vertLines.size(); j++) {
//                    Log.e("Debug","I: "+i+"  size: "+horLines.size());
//                    Log.e("Debug","J: "+i+"  size: "+vertLines.size());
//                    LatLngBounds box = getCellBounds(new int[]{i-1,j-1});
//                    boxesX.add(box);
//                    boxesY.add(box);
//                }
//            }

            return boxesX;
        }

        private void findCellsInPath(List<LatLng> points){
            // Find the cell where the path begins
            int[] hintXY = this.getCellCordsOfPoint(points.get(0));

            // Mark that cell and it's neighbours for inclusion in the boxes
            this.markCell(hintXY);

            // Work through each vertex on the path identifying which grid cell it is in
            for (int i = 1; i < points.size(); i++) {
                // Use the known cell of the previous vertex to help find the cell of this vertex
                int[] gridXY = this.getGridCoordsFromHint(points.get(i), points.get(i-1), hintXY);

                if (gridXY[0] == hintXY[0] && gridXY[1] == hintXY[1]) {
                    // This vertex is in the same cell as the previous vertex
                    // The cell will already have been marked for inclusion in the boxes
                    continue;

                } else if ((Math.abs(hintXY[0] - gridXY[0]) == 1 && hintXY[1] == gridXY[1]) ||
                        (hintXY[0] == gridXY[0] && Math.abs(hintXY[1] - gridXY[1]) == 1)) {
                    // This vertex is in a cell that shares an edge with the previous cell
                    // Mark this cell and it's neighbours for inclusion in the boxes
                    this.markCell(gridXY);

                } else {
                    // This vertex is in a cell that does not share an edge with the previous
                    //  cell. This means that the path passes through other cells between
                    //  this vertex and the previous vertex, and we must determine which cells
                    //  it passes through
                    this.getGridIntersects(points.get(i - 1), points.get(i), hintXY, gridXY);
                }

                // Use this cell to find and compare with the next one
                hintXY = gridXY;
            }
        }

        private void getGridIntersects(LatLng start,LatLng end,int[] startXY,int[] endXY) {
            int i;
            LatLng edgePoint;
            int[] edgeXY;
            double brng = rhumbBearingTo(start,end);         // Step 1.


            LatLng hint = start;
            int[] hintXY = startXY;

            // Handle a line segment that travels south first
            if (end.latitude > start.latitude) {
                // Iterate over the east to west grid lines between the start and end cells
                for (i = startXY[1] + 1; i <= endXY[1]; i++) {
                    // Find the latlng of the point where the path segment intersects with
                    //  this grid line (Step 2 & 3)
                    edgePoint = this.getGridIntersect(start, brng, this.vertLines.get(i));

                    // Find the cell containing this intersect point (Step 4)
                    edgeXY = this.getGridCoordsFromHint(edgePoint, hint, hintXY);

                    // Mark every cell the path has crossed between this grid and the start,
                    //   or the previous east to west grid line it crossed (Step 5)
                    this.fillInGridSquares(hintXY[0], edgeXY[0], i - 1);

                    // Use the point where it crossed this grid line as the reference for the
                    //  next iteration
                    hint = edgePoint;
                    hintXY = edgeXY;
                }

                // Mark every cell the path has crossed between the last east to west grid
                //  line it crossed and the end (Step 5)
                this.fillInGridSquares(hintXY[0], endXY[0], i - 1);

            } else {
                // Iterate over the east to west grid lines between the start and end cells
                for (i = startXY[1]; i > endXY[1]; i--) {
                    // Find the latlng of the point where the path segment intersects with
                    //  this grid line (Step 2 & 3)
                    edgePoint = this.getGridIntersect(start, brng, this.vertLines.get(i));

                    // Find the cell containing this intersect point (Step 4)
                    edgeXY = this.getGridCoordsFromHint(edgePoint, hint, hintXY);

                    // Mark every cell the path has crossed between this grid and the start,
                    //   or the previous east to west grid line it crossed (Step 5)
                    this.fillInGridSquares(hintXY[0], edgeXY[0], i);

                    // Use the point where it crossed this grid line as the reference for the
                    //  next iteration
                    hint = edgePoint;
                    hintXY = edgeXY;
                }

                // Mark every cell the path has crossed between the last east to west grid
                //  line it crossed and the end (Step 5)
                this.fillInGridSquares(hintXY[0], endXY[0], i);

            }
        }

        private void mergeIntersectingCells() {
            int x, y;
            LatLngBounds box;

            // The box we are currently expanding with new cells
            LatLngBounds currentBox = null;

//            // Traverse the grid a row at a time
//            for (y = 0; y < this.grid[0].length; y++) {
//                for (x = 0; x < this.grid.length; x++) {
//
//                    if (this.grid[x][y]) {
//                        // This cell is marked for inclusion. If the previous cell in this
//                        //   row was also marked for inclusion, merge this cell into it's box.
//                        // Otherwise start a new box.
//                        box = this.getCellBounds(new int[]{x, y});
//                        if (currentBox != null) {
//                            currentBox.including(box.northeast);
//                        } else {
//                            currentBox = box;
//                        }
//
//                    } else {
//                        // This cell is not marked for inclusion. If the previous cell was
//                        //  marked for inclusion, merge it's box with a box that spans the same
//                        //  columns from the row below if possible.
//                        this.mergeBoxesY(currentBox);
//                        currentBox = null;
//                    }
//                }
//                // If the last cell was marked for inclusion, merge it's box with a matching
//                //  box from the row below if possible.
//                this.mergeBoxesY(currentBox);
//                currentBox = null;
//            }

            // Traverse the grid a column at a time
            Log.e("Debug","Grid: "+grid.length+" rows, "+grid[0].length+" columns");
            Log.e("Debug","Vert: "+vertLines.size()+" Hor: "+horLines.size());
            for (x = 0; x < this.grid.length-1; x++) {
                for (y = 0; y < this.grid[0].length-1; y++) {
                    if (this.grid[x][y]) {

                        // This cell is marked for inclusion. If the previous cell in this
                        //   column was also marked for inclusion, merge this cell into it's box.
                        // Otherwise start a new box.
//                        if (currentBox != null) {
//                            Log.e("Debug","X: "+x+" Y: "+y);

//                            box = this.getCellBounds(new int[]{x, y});
//                            currentBox.including(box.northeast);
//                        } else {
//                            currentBox = this.getCellBounds(new int[]{x, y});
//                        }

                        currentBox = this.getCellBounds(new int[]{x, y});
                        boxesX.add(currentBox);

                    } else {
                        // This cell is not marked for inclusion. If the previous cell was
                        //  marked for inclusion, merge it's box with a box that spans the same
                        //  rows from the column to the left if possible.
//                        this.mergeBoxesX(currentBox);
//                        currentBox = null;

                    }
                }
                // If the last cell was marked for inclusion, merge it's box with a matching
                //  box from the column to the left if possible.
//                this.mergeBoxesX(currentBox);
//                currentBox = null;
            }
        }


/**
 * Search for an existing box in an adjacent row to the given box that spans the
 * same set of columns and if one is found merge the given box into it. If one
 * is not found, append this box to the list of existing boxes.
 *
 * @param {LatLngBounds}  The box to merge
 */
        private void mergeBoxesX(LatLngBounds box) {
            if (box != null) {
                for (LatLngBounds currentBox :
                        boxesX) {
                    if (box.northeast.longitude == box.southwest.longitude &&
                            currentBox.southwest.latitude == box.southwest.latitude &&
                            currentBox.northeast.latitude == box.northeast.latitude) {
                        currentBox.including(box.northeast);
                        return;
                    }
                }
                this.boxesX.add(box);
            }
        };

/**
 * Search for an existing box in an adjacent column to the given box that spans
 * the same set of rows and if one is found merge the given box into it. If one
 * is not found, append this box to the list of existing boxes.
 *
 * @param {LatLngBounds}  The box to merge
 */
        private void mergeBoxesY(LatLngBounds box) {
            if (box != null) {
                for (LatLngBounds currentBox :
                        boxesY) {
                    if (currentBox.northeast.latitude == box.southwest.latitude &&
                            currentBox.southwest.longitude == box.southwest.longitude &&
                            currentBox.northeast.longitude == box.northeast.longitude) {
                        currentBox.including(box.northeast);
                        return;
                    }
                }
                this.boxesY.add(box);
            }
        }

        private LatLngBounds getCellBounds(int[] cell) {
            return new LatLngBounds(new LatLng(vertLines.get(cell[1]), this.horLines.get(cell[0])),
                    new LatLng(this.vertLines.get(cell[1] + 1), this.horLines.get(cell[0] + 1)));
        };

        private LatLng getGridIntersect(LatLng start, double brng, double gridLineLat) {
            double d = this.R * ((toRad(gridLineLat) - toRad(start.latitude) / Math.cos(toRad(brng))));
            return rhumbDestinationPoint(brng,d,start);
        }

        private void fillInGridSquares(int startx,int endx,int y) {
            int x;
            if (startx < endx) {
                for (x = startx; x <= endx; x++) {
                    this.markCell(new int[]{x, y});
                }
            } else {
                for (x = startx; x >= endx; x--) {
                    this.markCell(new int[]{x, y});
                }
            }
        }

        private void markCell(int[] cell) {
            int x = cell[0];
            int y = cell[1];
            this.grid[x - 1][y - 1] = true;
            this.grid[x][y - 1] = true;
            this.grid[x + 1][y - 1] = true;
            this.grid[x - 1][y] = true;
            this.grid[x][y] = true;
            this.grid[x + 1][y] = true;
            this.grid[x - 1][y + 1] = true;
            this.grid[x][y + 1] = true;
            this.grid[x + 1][y + 1] = true;
        };

        private int[] getCellCordsOfPoint(LatLng point){
            int x = 0;
            int y = 0;
            while (this.horLines.get(x) < point.longitude) {x++;}
            while (this.vertLines.get(y) < point.latitude) {y++;}
            return (new int[]{x, y});
        }

        private int[] getGridCoordsFromHint(LatLng point,LatLng hintlatlng,int[] hint) {
            int x, y;
            if (point.longitude > hintlatlng.longitude) {
                for (x = hint[0]; this.horLines.get(x + 1) < point.longitude; x++) {}
            } else {
                for (x = hint[0]; this.horLines.get(x) > point.longitude; x--) {}
            }

            if (point.latitude > hintlatlng.latitude) {
                for (y = hint[1]; this.vertLines.get(y + 1) < point.latitude; y++) {}
            } else {
                for (y = hint[1]; this.vertLines.get(y) > point.latitude; y--) {}
            }

            return (new int[]{x, y});
        };

        private LatLng rhumbDestinationPoint(double brng, double dist, LatLng pos) {
            double d = dist/6378137;
            double lat1 = toRad(pos.latitude), lon1 = toRad(pos.longitude);
            brng = toRad(brng);

            double dLat = d*Math.cos(brng);

            if (Math.abs(dLat) < 1e-10) dLat = 0;

            double lat2 = lat1 + dLat;
            double dPhi = Math.log(Math.tan(lat2/2+Math.PI/4)/Math.tan(lat1/2+Math.PI/4));
            double q = (dPhi!=0) ? dLat/dPhi : Math.cos(lat1);
            double dLon = d*Math.sin(brng)/q;

            if (Math.abs(lat2) > Math.PI/2) lat2 = lat2>0 ? Math.PI-lat2 : -Math.PI-lat2;

            double lon2 = (lon1+dLon+3*Math.PI)%(2*Math.PI) - Math.PI;

            return new LatLng(toDeg(lat2), toDeg(lon2));
        }

        private double rhumbBearingTo(LatLng start, LatLng dest) {
            double dLon = toRad(dest.longitude - start.longitude);
            double dPhi = Math.log(Math.tan(toRad(dest.latitude) / 2 + Math.PI / 4) / Math.tan(toRad(start.latitude) / 2 + Math.PI / 4));
            if (Math.abs(dLon) > Math.PI) {
                dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
            }
            return toBrng(Math.atan2(dLon, dPhi));
        }


        private double toRad(double value) {
            return value * Math.PI / 180;
        }

        private double toDeg(double value) {
            return value * 180 / Math.PI;
        }

        private double toBrng(double value) {
            return (toDeg(value) + 360) % 360;
        }
    }
}
