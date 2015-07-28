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
  var mylatlng = new google.maps.LatLng(6.9344, 79.8428); // Should find a way to automatically set the focus
  var mapProp = {
    center: mylatlng,
    zoom:7,
    mapTypeId:google.maps.MapTypeId.ROADMAP
  };
  var map=new google.maps.Map($("#googleMap")[0], mapProp);

  $.get('api/tracker/locations').done(function(res){
    for(var i=0; i<res.length; i++){
      setMarker(res[i], map);
    }
  });

}

function setMarker(res, map){
  getNameFromId(res._id, function(name){
    var latLng = new google.maps.LatLng(res.path[0], res.path[1]);
    var marker = new google.maps.Marker({
      position: latLng,
      map: map,
      title: name
    });
  });
}

function getNameFromId(id, cb){
  $.get('api/tracker/'+id).done(function(res){
    cb(res[0].name);
  });
}

google.maps.event.addDomListener(window, 'load', initialize);

