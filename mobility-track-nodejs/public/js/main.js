function createTracker() {
  $('.dup-warn').hide();

  dname = $('#devicename').val();
  var dmac = '';
  $( ".devicemac" ).find('input').each(function(i,o){
    dmac = dmac + $(this).val().toUpperCase();
  });

  // Check whether MAC already exists in the db if not create a new Tracker
  if (validateMAC() === true) {
    $.get('/api/tracker/findmac/' + dmac).done(function(res){
      if (res.length === 0) {
        $.post('/api/tracker/create', { name: dname, device: {mac: dmac} });
      } else {
        $('.dup-warn').show();}
    });
  }
}


function validateMAC() {
  var regex = /^[0-9A-Fa-f]{2}$/;
  var valid = true;

  $( ".devicemac" ).find('input').each(function(i,o){
    var validmac = regex.test($(this).val());
    if (!validmac){
      $(this).addClass("incorrect-mac");
      valid = valid && false;
    } else {
      $(this).removeClass("incorrect-mac");
    }
  });
  return valid;
}

//function to create new resource

function createResource(){

	var resName = $('#resourceid').val();
	var resCat = $('#resource_cat').val();

	$.post('/api/resources/create', {name : resName, icon : resCat, marker : '', attributes : []});
}

function initialize() {
  
  map = initializeMap();

  var header = $('.content-header > h1').text();

  if(/Dashboard/.test(header)){    
    drawLocations(map);
  }
  else if(/Path Analyzer/.test(header)){
    var id = $('.content-header').attr('trackerid');
    drawTrails(map, id);
    
  }

}

function setMarker(res, map){
  getTrackerFromId(res.id || res._id, function(tracker){
    var name = tracker.name;
    var latLng = markerCoords(res);
    var marker = new google.maps.Marker({
      position: latLng,
      map: map,
      title: name
    });
    var content = '<h1>' + name + '</h1>' +
        '<p>Last seen: ' + new Date(res.timestamp).toLocaleString() + '</p>' +
        '<p>Device ID: ' + tracker._id + '</p>';
    if (tracker.device && tracker.device.mac) {
      content += '<p>MAC: ' + tracker.device.mac + '</p>';
    }
    var infoWindow = new google.maps.InfoWindow({
      content: content
    });
    marker.addListener('click', function() {
      infoWindow.open(map, marker);
    });
  });
}

function getTrackerFromId(id, cb){
  $.get('/api/tracker/'+id).done(function(res){
    cb(res[0]);
  });
}

function initializeMap(){

  var mylatlng = new google.maps.LatLng(6.9344, 79.8428); // Should find a way to automatically set the focus
  var mapProp = {
    center: mylatlng,
    zoom:7,
    mapTypeId:google.maps.MapTypeId.ROADMAP
  };

  var map = new google.maps.Map($("#googleMap")[0], mapProp);

  return map;  
}

function drawTrails(map, id){

	var pathCoords = [];

	requestStr = '/api/tracker/' + id + '/location/data';

	$.get(requestStr).done(function(res){
		var bounds = new google.maps.LatLngBounds();
		for(var i = 0; i < res.length; i++){
			for (var j = 0; j < res[i].data.length; j++) {
				var coords = new google.maps.LatLng(res[i].data[j].latitude, res[i].data[j].longitude);
				pathCoords.push(coords);
				bounds.extend(coords);
			}
		}
		// set marker to last position of tracker
		if (res.length > 0) {
			setMarker(res[res.length-1], map);
		}
		map.fitBounds(bounds);

		var path = new google.maps.Polyline({
			path : pathCoords,
			geodesic : true,
			strokeColor: '#0000FF',
			strokeOpacity: 1.0,
			strokeWeight: 2
	  	});

		path.setMap(map);

	});	
}

function markerCoords(res) {
  var path = res.path || res.data;
  return new google.maps.LatLng(path[path.length - 1].latitude, path[path.length - 1].longitude);
}

function drawLocations(map){

  $.get('/api/tracker/locations').done(function(res){
    var bounds = new google.maps.LatLngBounds();
    for(var i=0; i<res.length; i++){
      setMarker(res[i], map);
      bounds.extend(markerCoords(res[i]));
    }
    map.fitBounds(bounds);
  });

}

function listTrackers(){

    $.get('/api/trackers').done(function(res){

        var list = '';

        for(var i = 0; i < res.length; i++){
        	list += '<li> <a id="trail" href="/trails/' + res[i]._id + '">' + res[i].name + '</a> </li>';        	
        }

    	$('#trackers').html(list);

    });
    	
}

$('document').ready(function(){

	$('.col-xs-2').keyup(function(){
		if ($(this).children().val().length == 2){
    			$(this).next('.col-xs-2').children().focus();
    		}    
	 });

});

listTrackers(); 

google.maps.event.addDomListener(window, 'load', initialize);

