var map, g,boxer,paths=[],markers = [],trackers = getTrackerLocations();

function initMap() {
  g = google.maps;
  map = new g.Map(document.getElementById('map'),{
    zoom:8,
    center:{lat:37,lng:-122},
  });
  drawPath();
}

function drawPath() {
  var points = [];
  for (point of arguments) {
    if(point.hasOwnProperty("lat")&&point.hasOwnProperty("lng")){
      points.push(point);
    }
  }

  if(points.length <= 1){
    //Default path
    points = [{"lat":37.5,"lng":-122},{"lat":37,"lng":-122}];
  }

  var newPath = new g.Polyline({
    path: points,
    strokeWeight: 5,
    strokeOpacity: 1.0,
    strokeColor: '#0C7FDD',
    editable:true,
    draggable:true,
  });

  newPath.setMap(map);

  paths.push(newPath);

  g.event.addListener(newPath.getPath(),'insert_at',function() {
    updateMarkers(paths.indexOf(newPath));
  });
  g.event.addListener(newPath.getPath(),'remove_at',function() {
    updateMarkers(paths.indexOf(newPath));
  });
  g.event.addListener(newPath,'dragend',function() {
    updateMarkers(paths.indexOf(newPath));
  });
  g.event.addListener(newPath.getPath(),'set_at',function() {
    updateMarkers(paths.indexOf(newPath));
  });
}

function updateMarkers(pathIndex) {

  var boxer = new RouteBoxer();
  var boxes = boxer.box(paths[pathIndex],1);

  //Remove all markers
  for(marker of markers){
    marker.setMap(null);
  }
  markers.length = 0;

  for (box of boxes) {
    for (tracker of trackers) {
      if(box.contains(tracker)){
        var marker = new g.Marker({position:tracker,map:map,animation: g.Animation.DROP});
        markers.push(marker);
      }
    }
  }
}

function getTrackerLocations() {
  var trackers = [];
  //replace with query to tracker DB
  for (var i = 0; i < 500; i++) {
    trackers.push({"lat":(Math.random())+37,"lng":(Math.random())-122});
  }
  return trackers;
}
