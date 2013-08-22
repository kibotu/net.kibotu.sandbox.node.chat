
/**
 * Module dependencies.
 */

process.env.NODE_ENV = 'development';

var express = require('express');
var routes = require('./routes');
var user = require('./routes/user');
var chat = require('./routes/chat')
var admin = require('./routes/admin');
var http = require('http');
var path = require('path');
var hat = require('hat');
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
app.get('/admin', admin.list);

// socket io
io = require('socket.io').listen(app.listen(app.get('port')));

//io.disable('heartbeats');
io.configure( function() {
    io.set('close timeout', 15);
});

// usernames which are currently connected to the chat
var users = {};

// rooms which are currently available in chat
var rooms = {'Queue' : [], 'Game 1' : [], 'Game 2' : []};

// initial connection from a client. socket argument should be used in further communication with the client.
io.sockets.on('connection', function (socket) {

    socket.emit('message', { message: 'Welcome!', uid: hat() });

    socket.on("adduser", function(data) {
        var room = Object.keys(rooms)[0];
        socket.uid = data.uid;
        socket.room = room;
        users[data.uid] = data.uid;
        rooms[room].push(data.uid);
        socket.join(room);
        socket.emit('message', { message: 'You have connected to ' + room });
        socket.broadcast.to(room.key).emit('message', { message : data.uid + ' has connected to this room' });

        // http://psitsmike.com/2011/10/node-js-and-socket-io-multiroom-chat-tutorial/
        socket.emit("updateroom", rooms );
        console.log(data.uid + " joined " + room);
    });

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

    socket.on('disconnect', function(){
        delete rooms[socket.room][socket.uid];
        socket.emit("updateroom", rooms );
        socket.broadcast.to(socket.room.key).emit('message', { message : socket.uid + ' has disconnected.' });
        socket.leave(socket.room);
    });
});

/* CLIENT

// "connect" is emitted when the socket connected successfully
io.socket.on('connect', function () {});

// disconnect" is emitted when the socket disconnected
io.socket.on('disconnect', function () {});

// "connect_failed" is emitted when socket.io fails to establish a connection to the server and has no more transports to fallback to
io.socket.on('connect_failed', function () {});

// "error" is emitted when an error occurs and it cannot be handled by the other event types.
io.socket.on('error', function () {});

// "reconnect_failed" is emitted when socket.io fails to re-establish a working connection after the connection was dropped
io.socket.on('reconnect_failed', function () {});

// "reconnect" is emitted when socket.io successfully reconnected to the server.
io.socket.on('reconnect', function () {});

// "reconnecting" is emitted when the socket is attempting to reconnect with the server.
io.socket.on('reconnecting', function () {});     */