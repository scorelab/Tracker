
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

var express = require('express'),
  http = require('http'),
  path = require('path'),
  engine = require('ejs-locals'),
  flash = require('connect-flash'),
  passport = require('passport'),
  cookieParser = require('cookie-parser')
  bodyParser = require('body-parser'),
  session = require('express-session');

var app = express();

require('./models/passport')(passport);

app.configure(function(){
  app.set('port', process.env.PORT || 3000);

  // use ejs-locals for all ejs templates:
  app.engine('ejs', engine);
  app.set('views', __dirname + '/views');
  app.set('view engine', 'ejs');
  app.use(function(req, res, next) {
	res.header('Access-Control-Allow-Origin', '*');
	return next();
  });

  //for login/sign-up
  app.use(cookieParser());
  app.use(bodyParser());

  app.use(express.favicon());
  app.use(express.methodOverride());
  app.use(express.logger('dev'));
  app.use(express.bodyParser());


  //require for passport
  app.use(session({
    secret: 'yowhatsup ',
    resave: false,
    saveUnitialized:false
  }));
  app.use(passport.initialize());
  app.use(passport.session());
  app.use(flash());


  app.use(app.router);
  app.use(express.static(path.join(__dirname, 'public')));
});

require('./db');


var controllers = require('./controllers'),
  tracker = require('./controllers/tracker');


app.configure('development', function(){
  app.use(express.errorHandler());
});

//Routes
require('./routes')(app, passport);

//===============================


//Start server

http.createServer(app).listen(app.get('port'), function(){
  console.log("Express server listening on port " + app.get('port'));
});
