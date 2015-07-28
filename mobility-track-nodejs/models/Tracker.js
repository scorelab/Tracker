var mongoose = require('mongoose')
    , Schema = mongoose.Schema


/**
 * Boat Schema
 */

var TrackerSchema = new Schema({
  status : { type : Number }, // 1: "Active", 0: "Inactive"
  name : {type : String },
  device : {
		mac: { type : String }
	}
  }
);   

mongoose.model('Tracker', TrackerSchema);

