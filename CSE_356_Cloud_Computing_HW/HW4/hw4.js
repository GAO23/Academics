var port = process.env.PORT || 80;
var express = require('express');
var path = require('path');
var bodyParser = require('body-parser');

        app = express();
        app.use(bodyParser.json());
        app.use(bodyParser.urlencoded({ extended: true }));

        app.post('/listen', function (req, res) {
            console.log("(/listen) Keys: " + req.body.keys);
            this.listen(req.body.keys.toString().split(','), res);
        });

        app.post('/speak', function (req, res) {
            res.sendStatus(200);
            this.speak(req.body.key.toString(), req.body.msg.toString());
        });





     function listen(keys, res) {
        var amqp = require('amqplib/callback_api');
        amqp.connect('amqp://localhost', function (err, conn) {
            conn.createChannel(function (err, ch) {
                var exchange = 'hw4';

                ch.assertQueue('', { exclusive: true }, function (err, q) {

                    keys.forEach(function (key) {
                        ch.bindQueue(q.queue, exchange, key);
                    });

                    ch.consume(q.queue, function (msg) {
                        console.log(" [x] Consume %s: '%s'", msg.fields.routingKey, msg.content.toString());

                        res.status(200).json({
                            msg: msg.content.toString()
                        });

                        conn.close();
                    }, { noAck: true });
                });
            });
        });
    }


    function speak(key, msg) {
        var amqp = require('amqplib/callback_api');

        amqp.connect('amqp://localhost', function (err, conn) {
            conn.createChannel(function (err, ch) {
                var ex = 'hw3';

                ch.publish(ex, key, new Buffer(msg));
                console.log(" [x] Sent %s: '%s'", key, msg);
                setTimeout(function() { conn.close();}, 500);
            });
        });
    }



app.listen(port, function () {
    console.log("Staring on port: " + port)
})
