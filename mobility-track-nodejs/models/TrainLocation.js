var mongoose = require('mongoose')
    , Schema = mongoose.Schema


/**
 * Article Schema
 */

var TrainLocationSchema = new Schema({
  id: {type : Number },
  lat: {type : Number },
  lon: {type : Number},
  rot: {type : Number },
  spd: {type : Number},
  tm : {type : Date, default : Date.now}
})


mongoose.model('TrainLocation', TrainLocationSchema)

