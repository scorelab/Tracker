module.exports = function(app, passport){
  var controllers = require('./controllers/index');
  var tracker = require('./controllers/tracker');
  var resources = require('./controllers/resources');


  //Account stuff
  //Maybe put in a controller, but maybe next time, ey?
  app.get('/', function(req, res){
    res.render('index.ejs');
  });

  app.get('/login', function(req, res){
    res.render('login.ejs', {message: req.flash('loginMessage')});
  });
  app.post('/login', passport.authenticate('local-login',{
    sucessRedirect: '/dash',
    failureReditect: '/login',
    failureFlash: true
  }));

  app.get('/signup', function(req, res){
    res.render('signup.ejs', {message: req.flash('signupMessage')});
  });
  app.post('/signup', passport.authenticate('local-signup', {
    sucessRedirect: '/dash',
    failureReditect: '/signup',
    failureFlash: true
  }));

  app.get('/logout', function(req, res) {
    req.logout();
    res.redirect('/');
  });

  //Index--Dashboard
  app.get('/dash', loggedIn, controllers.dash);
  app.get('/tracker/new', loggedIn, controllers.newtracker);
  app.get('/resource/new', loggedIn, controllers.newresource);
  app.get('/trails/:id', loggedIn, controllers.trails);

  //Tracker
  app.get('/api/tracker/locations', loggedIn, tracker.locations);
  app.get('/api/tracker/:id/location', loggedIn, tracker.locationOf);
  app.get('/api/tracker/locations/initial', loggedIn, tracker.initialLocations);
  app.get('/api/tracker/', loggedIn, tracker.index);
  app.post('/api/tracker/create', loggedIn, tracker.create);
  app.get('/api/tracker/:id', loggedIn, tracker.get);
  app.post('/api/tracker/:id/update', loggedIn, tracker.update);
  app.get('/api/tracker/:id/delete', loggedIn, tracker.delete);
  app.get('/api/tracker/findmac/:mac', loggedIn, tracker.findMac);
  app.get('/api/trackers', loggedIn, tracker.getTrackers);

  //TrackerLocation
  app.get('/api/tracker/location/data', loggedIn, tracker.listLocationData);
  app.post('/api/tracker/location/data', loggedIn, tracker.addLocationData);
  app.get('/api/tracker/:id/location/data', loggedIn, tracker.listLocationsById);

  //Resources
  app.post('/api/resources/create', loggedIn, resources.create);

};

function loggedIn(req, res, next) {

  //if(req.isAuthenticated())
    return next();

  res.redirect('/');

}
