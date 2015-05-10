
/*
 * GET home page.
 */

exports.index = function(req, res){
  res.render('index');
};

exports.newtracker = function(req, res){
  res.render('newtracker');
};

exports.newresource = function(req, res){
  res.render('newresource');
};
