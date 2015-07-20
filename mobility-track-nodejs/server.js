
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
  engine = require('ejs-locals');

var app = express();


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
  app.use(express.favicon());
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(app.router);
  app.use(express.static(path.join(__dirname, 'public')));
});

require('./db');

var controllers = require('./controllers'),
  tracker = require('./controllers/tracker');
  //resources = require('./controllers/resources');

app.configure('development', function(){
  app.use(express.errorHandler());
});

//Routes
require('./routes')(app);

//===============================





//Start server

http.createServer(app).listen(app.get('port'), function(){
  console.log("Express server listening on port " + app.get('port'));
});
