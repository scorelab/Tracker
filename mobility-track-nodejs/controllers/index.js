
/*
 * GET home page.
 */

 //Test commit

exports.index = function(req, res){
  res.render('index', { heading: "Dashboard" });
};

exports.newtracker = function(req, res){
  res.render('newtracker', { heading: "Add a new Tracker" });
};

exports.newresource = function(req, res){
  res.render('newresource', { heading: 'Add a new resource' });
};
