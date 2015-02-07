var mongoose = require('mongoose')
    , Schema = mongoose.Schema


/**
 * TrackerLocation Schema
 */

var TrackerLocationsSchema = new Schema({
  id : { type : String },
  status : { type : Number },
  timestamp : {type : Date, default : Date.now},
  data : Schema.Types.Mixed
});


mongoose.model('TrackerLocations', TrackerLocationsSchema);

