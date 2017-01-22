
/*
 * GET users listing.
 */
 
var mongoose = require('mongoose')
   , TrackerLocations = mongoose.model('TrackerLocations')
   , Tracker = mongoose.model('Tracker')

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
exports.locationAndTrackerByIdTag = function (req, res) {
	Tracker.find({ "_id":  req.params.id }, function (err, rcd) {
	  if(err) console.log(err);
          res.setHeader('content-type', 'application/json');
          res.send(rcd);
       	});
	TrackerLocations.find({"id" : req.params.id}, function(err, rcd){
       	  if(err) console.log(err);
    	  res.setHeader('content-type', 'application/json');
   	  res.send(rcd);
      	});
}
exports.locationAndTrackerByDistance = function (req, res, givenLong,givenLat, xdistance) {
	Tracker.find(function(err, data){
    	  if(err) console.log(err);
    	  data.forEach(function(entry) {
		  var tracker = entry.toObject();
		  TrackerLocations.find(function (err, data) { 
				if (err) { console.log(err); res.send([]); return; } 
						if (!data[0]) { console.log('No data for '+req.params.id); res.send([]); return; } 
						res.setHeader('content-type','application/json');
						tracker.data = data[0].data;
						tracker.longitude = data[0].data[data[0].data.length-1].o;
						tracker.latitude = data[0].data[data[0].data.length-1].a
						var xdistancesquared = xdistance*xdistance;
						var difInPos = (givenLat - tracker.latitude)*(givenLat - tracker.latitude) + (givenLong - tracker.longitude)*(givenLong - tracker.longitude);
						if(difInPos < xdistancesquared){
						     res.send([tracker]);
						}
			  
		  	});
			  
		  });
  	}); 
}

