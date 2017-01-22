var LocalStrategy = require('passport-local').Strategy;
var User = require('../models/user.js');


module.exports = function(passport){

  passport.serializeUser(function(user, done){
    done(null, user.id);
  });

  passport.deserializeUser(function(id, done) {
       User.findById(id, function(err, user) {
           done(err, user);
       });
   });

   //signup
   passport.use('local-signup', new LocalStrategy({
       usernameField: 'email',
       passwordField: 'password',
       passReqToCallback: true
     },

   function(req, email, password, done){

         User.findOne({'local.email': email}, function(err,user){

           if(err)
            return done(err);

            if(user){
              return done(null, false, req.flash('signupMessage', 'That email is already registered'));
            } else {
              var newUser = new User();

              newUser.local.email = email;
              newUser.local.password = newUser.generateHash(password);

              newUser.save(function(err){
                if(err)
                  throw err;
                return done(null, newUser);
              });
            }
         });
     }));


     passport.use('local-login', new LocalStrategy({
         // by default, local strategy uses username and password, we will override with email
         usernameField : 'email',
         passwordField : 'password',
         passReqToCallback : true // allows us to pass back the entire request to the callback
     },
     function(req, email, password, done) { // callback with email and password from our form

         // find a user whose email is the same as the forms email
         // we are checking to see if the user trying to login already exists
         User.findOne({ 'local.email' :  email }, function(err, user) {
             // if there are any errors, return the error before anything else
             if (err)
                 return done(err);

             // if no user is found, return the message
             if (!user)
                 return done(null, false, req.flash('loginMessage', 'No user found.')); // req.flash is the way to set flashdata using connect-flash

             // if the user is found but the password is wrong
             if (!user.validPassword(password))
                 return done(null, false, req.flash('loginMessage', 'Oops! Wrong password.')); // create the loginMessage and save it to session as flashdata

             // all is well, return successful user
             return done(null, user);
         });

     }));

};
