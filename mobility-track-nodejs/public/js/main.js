function createTracker() {
  $('.dup-warn').hide();

  dname = $('#devicename').val();
  dmac = $('#devicemac').val();

  // Check whether MAC already exists in the db if not create a new Tracker
  $.get('/api/tracker/findmac/' + dmac).done(function(res){
    if (res.length === 0) {
      $.post('/api/tracker/create', { name: dname, device: {mac: dmac} });
    } else {
      $('.dup-warn').show();}
  });

}
