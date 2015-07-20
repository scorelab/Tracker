
var mongoose = require('mongoose'),
	resource = mongoose.model('Resources')

//Create new resource

exports.create = function(req, res){

	var newRes = new resource(req.body);

	newRes.save(function(err, rcd){
		if(err) console.log(err);
		res.setHeader('content-type', 'application/json');
		res.send(rcd);
	});
};

exports.update = function(req, res){
	
	resource.update({id : req.params.id}, req.body, {}, function (error, count){

		if (error) console.log(error);
		res.setHeader('content-type', 'application/json');
		res.send({'updated' : count});
	});
}; 

exports.findByName = function(req, res){
	resource.find({name : req.params.name}, function(err, rcd){
		if(err) console.log(err);
		res.setHeader('content-type', 'application/json');
		res.send(rcd);
	});
};