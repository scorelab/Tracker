
/*
 * GET users listing.
 */
 
var mongoose = require('mongoose')
   , TrainLocation = mongoose.model('TrainLocation')

exports.list = function (req, res) {
       TrainLocation.find(function (err, data) {
            if (err) console.log(err)
            res.setHeader('content-type','application/json');
            res.send(data);
       }); 
};
exports.add = function (req, res) {
       var tl = new TrainLocation(req.body);
       tl.save(function (err, rcd) {
            if (err) console.log(err)
       });
};