process.env.NODE_ENV = 'development';

var express = require('express');
var routes = require('./routes');
var user = require('./routes/user');
var chat = require('./routes/chat');
var admin = require('./routes/admin');
var http = require('http');
var path = require('path');
var hat = require('hat');
var net = require('net');
var _ = require('underscore');
var passport = require('passport');

var server = express();

// development
server.configure('development', function(){
    server.set('port', process.env.PORT || 3000);
    server.set('views', __dirname + '/views');
    server.set('view engine', 'jade');
    server.use(express.errorHandler());
    server.locals.pretty = true;
    server.use(express.favicon());
    server.use(express.logger('dev'));
    server.use(express.cookieParser('keyboard unicorn'));
    server.use(express.bodyParser());
    server.use(express.methodOverride());
    server.use(express.session({secret: 'keyboard unicorn', key: 'express.sid'}));
    // server.use(function (req, res) { res.end('<h2>Hello, your session id is ' + req.sessionID + '</h2>');  });
    server.use(server.router);
    server.use(express.static(path.join(__dirname, 'public')));
});

server.get('/', routes.index);
server.get('/users', user.list);
server.get('/chat', chat.list);
server.get('/admin', admin.list);

// socket io
var io = require('socket.io').listen(server.listen(server.get('port'),"0.0.0.0"));

//io.disable('heartbeats');
io.configure( function() {
    io.set('close timeout', 60*15); // 15 min
    io.set('log level', 5);
    io.set('transports', [
          'websocket'
        , 'flashsocket'
        , 'htmlfile'
        , 'xhr-polling'
        , 'jsonp-polling'
    ]);
    io.set('authorization', function (data, accept) {
        accept(null, true);
        console.log("Authorization : " + JSON.stringify(data));
    });
});

// usernames which are currently connected to the chat
var users = {};

// rooms which are currently available in chat
var rooms = {'Queue' : []};

var joinOrCreateNewGame = function(socket) {
    // leave old room
    rooms[socket.room].splice(socket.uid, 1);
    socket.leave(socket.room);
    // join new room
    socket.room = hat();
    rooms[socket.room] = [socket.uid];
    socket.join(socket.room);

    socket.emit('message', { message: 'You have switch to ' + socket.room });
    socket.broadcast.to(socket.room.key).emit('message', { message: socket.uid + ' has switch to ' + socket.room });
};

var updateRooms = function() {
    _.each(users, function(socket) {
        socket.emit("update-room", rooms);
    });
};

io.sockets.on('connect', function (){
    console.info('successfully established a working connection');
});

io.sockets.on('connection', function (socket) {

    socket.emit('message', { message: 'Welcome ' + socket.handshake.sessionID + '!', uid: hat() });

    socket.on("event", function(data) {
        console.log("event " + JSON.stringify(data))  // server console
        socket.emit('message', { message: JSON.stringify(data) }); // own message (for acknowledging action)
        socket.broadcast.to(socket.room).emit('message', { username : socket.uid, message: JSON.stringify(data) }); // real broadcast to other listener in room

        if(data['game-event'] == "create-game"){
            joinOrCreateNewGame(socket);
            updateRooms();
        }
    });

    socket.on("adduser", function(data) {
        var room = Object.keys(rooms)[0];
        socket.uid = data.uid;
        socket.room = room;
        users[data.uid] = socket;
        rooms[room].push(data.uid);
        socket.join(room);
        socket.emit('message', { message: 'You have connected to ' + room });
        socket.broadcast.to(room.key).emit('message', { message : data.uid + ' has connected to this room' });

        // http://psitsmike.com/2011/10/node-js-and-socket-io-multiroom-chat-tutorial/
        updateRooms();
    });

    socket.on('send', function (data) {
        io.sockets.emit('message', data);
        console.log("Response:", data);
    });

    socket.on('ping', function (data) {
        io.sockets.emit('pong', {"hallo": "welt"});
        console.log("ping => pong");
    });

    socket.on('error', function (reason){
        console.error('Unable to connect Socket.IO', reason);
    });

    socket.on('disconnect', function(){
        console.log("disconnect: " + socket.uid + " from " + socket.room);
        rooms[socket.room].splice(socket.uid, 1);
        updateRooms();
        socket.emit('message', { message: 'You have disconnected from ' + socket.room });
        socket.broadcast.to(socket.room.key).emit('message', { message : socket.uid + ' has disconnected.' });
        socket.leave(socket.room);
    });
});