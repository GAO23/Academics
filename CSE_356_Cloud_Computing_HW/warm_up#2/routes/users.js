var express = require('express');
var bodyParser = require('body-parser');
var router = express.Router();
var User = require('../models/user');
var crypto = require('crypto');
var bcrypt = require('bcrypt');
var nodemailer = require('nodemailer');
var Token = require('../models/token');
var passport = require('passport');
var protecting_routes = require('../models/protecting_routes');


/* TODO
* 1) validate user input, including password confirmed, can probably do this on client side
* 2) resending the token feature
* 3) drop all data base data for user when user creation fails
* 4) Confirmed the verification input, can do this on the client side
* */

const validJson = {status: "OK"};
const invalidJson = {status: "ERROR"};



/* GET users listing. */
router.get('/adduser', protecting_routes.authen_redirect, function(req, res, next) {
  res.render('register');
});

router.get('/login', protecting_routes.authen_redirect, function(req, res, next) {
  res.render('login');
});

router.get('/verify', protecting_routes.authen_redirect, function(req, res, next){
  res.render('verify');
});

router.get('/logout', function(req, res){
    req.logOut();
    res.json(validJson);
});

router.post('/login', protecting_routes.authen_redirect, async function(req, res, next) {
    passport.authenticate('local', function(err, user, info) {
        if (err) { res.staus(500).send({msg: err.message})};
        if (!user || (user.isVerified == false)) { return res.json(invalidJson)};
        req.logIn(user, function(err) {
            if (err) { return res.status(500).send({msg: err.message})};
            return res.json(validJson);
        });
    })(req, res, next);
});

router.post('/logout', (req, res) => {
  req.logOut();
  res.json(validJson);
});

router.post('/adduser', protecting_routes.authen_redirect, function(req, res, next) {

  // error checking, we dont do error checking muahahahahaha
  // Make sure this account doesn't already exist
  User.findOne({email: req.body.email}, async function (eer, user) {
    // make sure user is none existent
    if (user) return res.status(200).send(invalidJson);
    var hashedPassword;
    try{
       hashedPassword = await bcrypt.hash(req.body.password, 10);
    }catch(err) {
      return res.status(500).send({msg: 'can\'t hash your password'});
    }

    // Create and save the user
    user = new User({ username: req.body.username, email: req.body.email, password: hashedPassword});

    user.save(function (err) {
      if (err) { return res.status(500).send({ msg: err.message }); }
      // Create a verification token for this user
      var token = new Token({ _userId: user._id, token: crypto.randomBytes(16).toString('hex') });

      // Save the verification token
      token.save(function (err) { if (err) { return res.status(500).send({ msg: err.message });}

        // send node without auth
          const transporter = nodemailer.createTransport({
              port: 25,
              host: 'localhost',
              tls: {
                  rejectUnauthorized: false
              },
          });

        // Send the email
        //var transporter = nodemailer.createTransport({ service: 'gmail', auth: { user: process.env.YAHOO_USERNAME, pass: process.env.YAHOO_PASSWORD } });
        // const email_body =  'Hello,\n\n' + 'Please verify your account by clicking the link: \nhttp:\/\/' + req.headers.host + '\/confirmation\/' + token.token + '.\n'
        const email_body = `validation key: <${token.token}>`;
        var mailOptions = { from: process.env.YAHOO_ADDRESS, to: user.email, subject: 'TTT Verification Token', text: email_body};
        transporter.sendMail(mailOptions, function (err) {
          if (err) { return res.status(500).send({ msg: err.message }); }
          res.json(validJson)
        });
      })
    })
  })
});


router.post('/verify', protecting_routes.authen_redirect, function(req, res, next){
  // backdoor key per HW instruction
  if(req.body.key === "abracadabra"){
    User.findOne({email: req.body.email}, function (err, user) {
      if(user == null) return res.status(200).send(invalidJson);
      if(err) return res.status(500).send({msg: err.message});
      if(user.isVerified) return res.status(200).send(invalidJson);
      // verify the user
      user.isVerified = true;
      user.save(function (err) {
        if(err) return res.status(500).send({msg: err.message});
        return res.json(validJson);
      })
    });

    return;
  }

  Token.findOne({token: req.body.key}, function (err, token) {
    if(token == null) return res.status(200).send(invalidJson);

    User.findOne({ _id: token._userId, email: req.body.email}, function (err, user) {
      if (!user) return res.status(200).send(invalidJson);
      if(user.isVerified) return res.status(200).send(invalidJson);

      // verify the user
      user.isVerified = true;
      user.save(function (err) {
        if(err) return res.status(500).send({msg: err.message});
        res.json(validJson);
      })
    })
  })
});




module.exports = router;
