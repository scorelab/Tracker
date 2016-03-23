// get an instance of mongoose and mongoose.Schema
var mongoose = require('mongoose');
var bcrypt = require('bcrypt-nodejs');

var userSchema = mongoose.Schema({
    userDetails: {
		name: String,
		username: String,
		password: String,
		created: {
			type: Date,
			default: Date.now
		}
	}
});

// generating a hash
userSchema.methods.generateHash = function (password) {
    return bcrypt.hashSync(password, bcrypt.genSaltSync(8), null);
};

// checking if password is valid
userSchema.methods.validPassword = function (password) {
    return bcrypt.compareSync(password, this.userDetails.password);
};

// create the model for users and expose it
module.exports = mongoose.model('User', userSchema);