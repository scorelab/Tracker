
/*
 * GET users listing.
 */
 
var jwt = require('jsonwebtoken');

var mongoose = require('mongoose')
   , TrackerLocations = mongoose.model('TrackerLocations')
   , Tracker = mongoose.model('Tracker')

var User = require('../models/User');
   
   
   
exports.locations = function (req, res) {

       TrackerLocations.aggregate(
       [
        {
            $sort: {
                timestamp : -1
            }
        },
        {
            $project: {
                id: 1,
                timestamp : 1,
                data : 1,
                status: 1
            }
        },
        {
            $group: {
                _id: '$id',
                timestamp : { $first: '$timestamp'},
                status : { $first: "$status"},
                path : { $first: "$data"}
            }
        }
       ]).exec(function (err, data) {
            if (err) console.log(err);
            res.setHeader('content-type','application/json');
            res.send(data);
       }); 
};

exports.locationOf = function (req, res) {
	   Tracker.find({ "_id":  req.params.id }, function (err, data) {
            if (err) { console.log(err); res.send([]); return; } 
            data.forEach(function(entry) {
				var tracker = entry.toObject();
				TrackerLocations.find({ "id":  req.params.id }).sort({"timestamp": 1}).limit(1).exec(
				   function (err, data) { 
						if (err) { console.log(err); res.send([]); return; } 
						if (!data[0]) { console.log('No data for '+req.params.id); res.send([]); return; } 
						res.setHeader('content-type','application/json');
						tracker.data = data[0].data;
						tracker.longitude = data[0].data[data[0].data.length-1].o;
						tracker.latitude = data[0].data[data[0].data.length-1].a;
						res.send([tracker]); // Sending as an array as requested 
				   }
				); 
		   });
		});  
};

exports.initialLocations = function (req, res) {
	   
	   TrackerLocations.aggregate(
		[
			{ $match: { "type" : 1 } },
			{
				$sort: {
					timestamp : -1
				}
			},
			{
				$project: {
					id: 1,
					timestamp : 1,
					data : 1,
					status: 1
				}
			},
			{$unwind:'$data'},
			{
				$group: {
					_id: '$id',
					timestamp : { $first: '$timestamp'},
					status : { $first: "$status"},
					path : { $push: "$data" }
				}
			}
        ]).
        exec(function (err, data) {
				if (err) { console.log(err); res.send(500); return; }
				var response = [];
				var i = 0;
				data.forEach(function(tracker) {
					Tracker.find({ "id":  tracker._id, "status":  1 }, function (err, rcd) {
						i++;
						if (err) { console.log(err);  }
						if(!rcd.length) { 
							console.log("No tracker found for tracker "+tracker._id);
						}
						else{
							tracker['name'] = rcd[0].name;
							tracker['device'] = rcd[0].device;
						}
						tracker["latitude"] = tracker.path[tracker.path.length-1].a;
						tracker['longitude'] = tracker.path[tracker.path.length-1].o;
						
						console.log(rcd);
						console.log(tracker._id);
						response.push(tracker);
						if(i==data.length){
							res.setHeader('content-type','application/json');
							res.send(response);
					    }
				   });
				})
		   }
		); 
};

exports.index = function (req, res) {
       Tracker.find({ "status":  1 }, function (err, rcd) {
            if (err) console.log(err);
            res.setHeader('content-type','application/json');
            res.send(rcd);
       });
};

exports.get = function (req, res) {
       Tracker.find({ "_id":  req.params.id }, function (err, rcd) {
            if (err) console.log(err);
            res.setHeader('content-type','application/json');
            res.send(rcd);
       });
};

exports.getTrackers = function(req, res){
  Tracker.find(function(err, data){
    if(err) console.log(err);
    res.setHeader('content-type','application'/'json');
    res.send(data);
  });  
};

exports.findMac = function (req, res) {
       Tracker.find({ "device":  {"mac": req.params.mac} }, function (err, rcd) {
            if (err) console.log(err);
            res.setHeader('content-type','application/json');
            res.send(rcd);
       });
};

exports.create = function (req, res) {
       var b = new Tracker(req.body);
       b.save(function (err, rcd) {
            if (err) console.log(err);
            res.setHeader('content-type','application/json');
            res.send(rcd);
       });
};

exports.update = function (req, res) {
       Tracker.update({ "id":  req.params.id }, req.body , {} , function (err, count) {
		   if (err) console.log(err);
           res.setHeader('content-type','application/json');
           res.send({ "updated" : count });
	   });
};

exports.delete = function (req, res) {
       Tracker.update({ "id":  req.params.id }, { "status": 0 } , {} , function (err, count) {
		   if (err) console.log(err);
           res.setHeader('content-type','application/json');
           res.send({ "deleted" : count });
	   });
};

exports.add = function (req, res) {
       var tl = new TrackerLocations(req.body);
       tl.save(function (err, rcd) {
            if (err) console.log(err);
            res.setHeader('content-type','application/json');
            res.send(rcd);
       });
};

exports.listLocationData = function (req, res) {
       TrackerLocations.find(function (err, data) {
            if (err) console.log(err)
            res.setHeader('content-type','application/json');
            res.send(data);
       }); 
};

exports.addLocationData = function (req, res) {
       var tl = new TrackerLocations(req.body);
       tl.save(function (err, rcd) {
            if (err) console.log(err)
            res.setHeader('content-type','application/json');
            res.send(rcd);
       });
};

exports.listLocationsById = function(req, res){

  TrackerLocations.find({"id" : req.params.id}, function(err, rcd){
    if(err) console.log(err);
    res.setHeader('content-type', 'application/json');
    res.send(rcd);
  });
}

exports.authenticate = function (req, res) {
    var username = req.body.username;
    var password = req.body.password;
	if (username) {
		if (password) {
			User.findOne({
				'userDetails.username': username
			}, function (err, user) {
				if (err) throw err;
				if (!user) {
					res.json({
						success: false,
						message: 'Authentication failed. User not found:' + username
					});
				} else if (user) {
					var hash = user.generateHash(password);
					if (!user.validPassword(password)) {	// check if password matches
						res.json({
							success: false,
							message: 'Authentication failed. Wrong password.'
						});
					} else {
						sendToken(req, res, user);
					}
				}
			});
		}else {
			res.status(400).json({
				success: false,
				message: 'Authentication failed. Password required.'
			});
		}
	} else {
		res.status(400).json({
			success: false,
			message: 'Authentication failed. Username required.'
		});
	}
};

var sendToken = function (req, res, user) {
	var apiSecret = 'temporarySecret';
	var tempUser = {
		app: 'tracker',
		context: {
			username: user.userDetails.username,
		}
	};
		
	var token = jwt.sign(tempUser, apiSecret, {
		expiresInMinutes: 1440 // expires in 24 hours
	});

	return res.json({
		success: true,
		token: token
	});
};

exports.signup = function (req, res) {
	var username = req.body.username;
	var password = req.body.password;
	var name = req.body.name;

	if (username) {
		if (password) {
			User.findOne({
				'userDetails.username': username
			}, function (err, user) {
				if (err) {
					return res.json({
						success: false,
						message: 'Unexpected Error while checking user'
					});
				} else {
					if (user) {
						return res.json({
							success: false,
							message: 'Error: User already exists'
						});
					} else {

						var newUser = new User();

						newUser.userDetails.username = username;
						newUser.userDetails.name = name;
						newUser.userDetails.password = newUser.generateHash(password);

						newUser.save(function (err) {
							if (err) {
								return res.json({
									success: false,
									message: 'Unexpected Error while saving new user'
								});
							}
							else{
								return res.status(200).json({
									success: true,
									message: 'User created'
								});
							}
						});
					}
				}
			});
		} else {
			res.status(400).json({
				success: false,
				message: 'Authentication failed. Password required.'
			});
		}
	} else {
		res.status(400).json({
			success: false,
			message: 'Authentication failed. Username required.'
		});
	}
});



















