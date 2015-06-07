var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

/* Resource Schema*/

var resourcesSchema =  new Schema({
    name : {type : String},
    icon : {type : String}, //icon url
    marker : {type : String}, //marker url
    attributes :{type : Array }
    });

mongoose.model('Resources', resourcesSchema);


