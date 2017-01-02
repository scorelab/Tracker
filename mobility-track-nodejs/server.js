/**
 * Module dependencies.
 *
 * Routs
 *  Views
 * 		index
 * 	API
 * 		tracker
 * 		-locations
 * 		-location
 *
 */


  var express = require('express')
  var app = express();
  var port = process.env.PORT || 3000;
  var passport = require('passport');
  var flash = require('connect-flash');

  var cookieParser = require('cookie-parser');
  var bodyParser = require('body-parser');
  var session = require('express-session');

  var path = require('path');

  var engine = require('ejs-locals');

  require('./db');
  require('./models/passport')(passport);

  app.use(express.logger('dev'));
  app.use(cookieParser());
  app.use(bodyParser());

  app.set('view engine', 'ejs');
  app.engine('ejs', engine);
  app.set('views', __dirname + '/views');
  app.set('view engine', 'ejs');
  app.use(function(req, res, next) {
  res.header('Access-Control-Allow-Origin', '*');
  return next();
  });

  //require for passport
  app.use(session({
    secret: 'yowhatsup ',
    resave: false,
    saveUnitialized:false
  }));
  app.use(passport.initialize());
  app.use(passport.session());
  app.use(flash());

  require('./routes')(app, passport);

  app.use(app.router);
  app.use(express.static(path.join(__dirname, 'public')));

  var controllers = require('./controllers'),
      tracker = require('./controllers/tracker');
  require('./')

  app.configure('development', function(){
    app.use(express.errorHandler());
  });
  app.listen(port);
  console.log('Express server listening on port '+port);
