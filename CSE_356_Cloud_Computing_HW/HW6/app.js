// app.js
var express = require('express');
var path = require('path');
var bodyParser = require('body-parser');
var fs = require('fs');
var mime = require('mime-types')
var logger = require('morgan');
var cassandra = require('cassandra-driver');
var async = require('async');

//Connect to the cluster
var client = new cassandra.Client({ contactPoints: ['127.0.0.1'], localDataCenter: 'datacenter1', keyspace: 'hw6' });

// multer
var multer = require('multer');
var multipart = multer({ dest: 'uploads/' });

//create express app
var app = express();
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.post('/deposit', multipart.single('contents'), function (req, res) {
  // Use query markers (?) and parameters
  const query = 'INSERT INTO imgs (filename, contents, path) VALUES (?,?,?)';
  var fileBin = fs.readFileSync(req.file.path);
  const params = [req.body.filename, fileBin, req.file.path];
  // Set the prepare flag in the query options

  client.execute(query, params, { prepare: true })
    .then(result => console.log('Uploaded ' + params[0]))
    .catch(err => console.log(err.message));
  res.sendStatus(200);
})

app.get('/retrieve', multipart.single('contents'), function (req, res) {
  console.log("Retrieve: " + req.query.filename);

  // Use query markers (?) and parameters
  const query = 'SELECT path FROM imgs WHERE filename=?';
  const params = [req.query.filename];
  // Set the prepare flag in the query options
  var image;
  client.execute(query, params, { prepare: true }, function (err, result) {
    if (result.rows.length > 0) {
      console.log(params[0] + " is " + mime.lookup(params[0]));
      image = result.rows[0].path;

      res.writeHead(200, {
        'Content-Type': mime.lookup(params[0])
      });
      var readStream = fs.createReadStream(image.toString());
      // We replaced all the event handlers with a simple call to readStream.pipe()
      readStream.pipe(res);
    } else console.log("No results");
  });
})

module.exports = app;
