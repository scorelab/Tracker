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

