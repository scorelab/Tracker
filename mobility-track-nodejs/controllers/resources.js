
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

//update by mongodb id

exports.update = function(req, res){
	
	resource.update({_id : req.params.id}, req.body, {}, function (error, count){

		if (error) console.log(error);
		res.setHeader('content-type', 'application/json');
		res.send({'updated' : count});
	});
}; 

//find resources by name

exports.findByName = function(req, res){
	resource.find({name : req.params.name}, function(err, rcd){
		if(err) console.log(err);
		res.setHeader('content-type', 'application/json');
		res.send(rcd);
	});
};

//delete resources by mongodb id

exports.delete = function(req, res){
	resources.remove({ _id : req.params.id }, function(err){
		if(err) console.log(err);
	});
};