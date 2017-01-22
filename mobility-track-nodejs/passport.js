var LocalStrategy = require('passport-local').Strategy;

var User = require('./models/User');

module.exports = function (passport) {

    // used to serialize the user for the session
    passport.serializeUser(function (user, done) {
        done(null, user._id);
    });

    // used to deserialize the user
    passport.deserializeUser(function (id, done) {
        User.findOne({
			_id: id
		}).exec(function (error, user) {
			if (error) {
				done(error);
			} else {
				if (user) {
					done(null, user);
				} else {
					done(null, false);
				}
			}
		});
    });

    // =========================================================================
    // LOCAL LOGIN =============================================================
    // =========================================================================
    passport.use('local-login', new LocalStrategy({
		/* by default, local strategy uses username and password, we will override with email*/
		usernameField: 'username',
		passwordField: 'password',
		passReqToCallback: true // allows us to pass in the req from our route (lets us check if a user is logged in or not)
	},
	function (req, username, password, done) {
		var tempUser;
		process.nextTick(function () {
			User.findOne({'userDetails.username': username}, function (err, user) {
				if (err) {
					return done(err);
				}
				if (!user) {
					return done(null, false, req.flash('loginMessage', 'No user found.'));
				}
				if (!user.validPassword(password)) {
					return done(null, false, req.flash('loginMessage', 'Oops! Wrong password.'));
				}
				else {
					return done(null, user);
				}
			});
		});
	}));
};
