
/**
 * Module dependencies.
 */

process.env.NODE_ENV = 'development';

var express = require('express');
var routes = require('./routes');
var user = require('./routes/user');
var chat = require('./routes/chat');
var http = require('http');
var path = require('path');
// serving the flash policy file
var net = require("net");
var io;

var app = express();

// development
app.configure('development', function(){
    app.set('port', process.env.PORT || 7331);
    app.set('views', __dirname + '/views');
    app.set('view engine', 'jade');
    app.use(express.errorHandler());
    app.locals.pretty = true;
    app.use(express.favicon());
    app.use(express.logger('dev'));
    app.use(express.bodyParser());
    app.use(express.methodOverride());
    app.use(app.router);
    app.use(express.static(path.join(__dirname, 'public')));
});

app.get('/', routes.index);
app.get('/users', user.list);
app.get('/chat', chat.list);

// socket io
io = require('socket.io').listen(app.listen(app.get('port')));

//io.disable('heartbeats');
io.configure( function() {
    io.set('close timeout', 60*60*24); // 24h time out
});

io.sockets.on('connection', function (socket) {
    socket.emit('message', { message: 'welcome to the chat' });
    socket.on('send', function (data) {
        io.sockets.emit('message', data);
        console.log("Response:", data);
    });

    socket.on('ping', function (data) {
        io.sockets.emit('pong', {"hallo": "welt"});
        console.log("ping => pong");
    });

    //just added
    socket.on("error", function(err) {
        console.log("Caught flash policy server socket error: ");
        console.log(err.stack);
    });
});