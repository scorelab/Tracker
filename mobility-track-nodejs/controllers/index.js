
/*
 * GET home page.
 */

 //Test commit

exports.dash = function(req, res){
  res.render('dash', { heading: "Dashboard" });
};

exports.newtracker = function(req, res){
  res.render('newtracker', { heading: "Add a new Tracker" });
};

exports.newresource = function(req, res){
  res.render('newresource', { heading: 'Add a new resource' });
};

exports.trails = function(req, res){
	res.render('trails', {heading: 'Path Analyzer', id: req.params.id});
};
