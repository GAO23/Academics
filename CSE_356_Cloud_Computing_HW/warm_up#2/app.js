var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var mongoose = require('mongoose');
var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');
var gameRouter = require('./routes/ttt');
var flash = require('express-flash');
var session = require('express-session');
var env = require('dotenv').config();
var passport = require('passport');
var initPassport = require('./models/passport_init');
var User = require('./models/user');
var app = express();
app.use(flash());
app.use(session({
  secret: process.env.SESSION_SECRET,
  resave: false,
  saveUninitialized: false
}));


var getUserByUserName = async function (userName) {
  try{
    var pass_in = await User.findOne({username: userName});
    return pass_in;
  }catch (e) {
    return null;
  }
}

var getUserById =  async function(id) {
  try{
    var user = await User.findOne({_id: id});
    return user;
  }catch (e) {
    return null;
  }

}

initPassport(passport, getUserByUserName, getUserById);

app.use(passport.initialize());
app.use(passport.session());

mongoose.connect('mongodb://localhost/warm_up_3', { useNewUrlParser: true });
var connection = mongoose.connection;
connection.on('connected', function() {
  console.log('Connected to db...');
});


// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/', usersRouter);
app.use('/', gameRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
