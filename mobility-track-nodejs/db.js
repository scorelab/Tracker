// Load configurations
// if test env, load example file
var env = process.env.NODE_ENV || 'development',
  config = require('./config/database.js')[env],
  mongoose = require('mongoose');

// Bootstrap db connection
// Connect to mongodb
var connect = function () {
  var options = { server: { socketOptions: { keepAlive: 1 } } };
  mongoose.connect(config.db, options);
};
connect();

// Error handler
mongoose.connection.on('error', function (err) {
  console.log(err);
});

// Reconnect when closed
mongoose.connection.on('disconnected', function () {
  connect();
});

var models_path = __dirname + '/models';

fs = require('fs');
fs.readdirSync(models_path).forEach(function (file) {
  if (~file.indexOf('.js')) {
    require(models_path + '/' + file);
  }
});
