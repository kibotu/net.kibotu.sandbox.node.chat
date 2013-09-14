process.env.NODE_ENV = 'development';

var express = require('express');
var routes = require('./routes');
var user = require('./routes/user');
var chat = require('./routes/chat');
var admin = require('./routes/admin');
var http = require('http');
var path = require('path');
var hat = require('hat');
var _ = require('underscore');

var server = express();

// development
server.configure('development', function(){
    server.set('port', process.env.PORT || 80);
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
    io.set('close timeout', 60 * 15); // 15 min
    io.set('log level', 3);
    io.enable('browser client minification');  // send minified client
    //io.enable('browser client etag');          // apply etag caching logic based on version number
    //io.enable('browser client gzip');          // gzip the file
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

// http://psitsmike.com/2011/10/node-js-and-socket-io-multiroom-chat-tutorial/
var updateRooms = function() {
    _.each(users, function(socket) {
        socket.emit("update-room", rooms);
    });
};

io.sockets.on('connect', function (){
    console.info('successfully established a working connection');
});

io.sockets.on('connection', function (socket) {

    socket.emit('message', { message: 'Welcome !', uid: hat() });

    socket.on("message", function(data) {
        console.log("message " + JSON.stringify(data));
        socket.broadcast.to(rooms[0]).emit('message', data);
        socket.emit('message', data);
    });

    socket.on("event", function(data) {
        console.log("event " + JSON.stringify(data))  // server console
        socket.emit('message', { message: JSON.stringify(data) }); // own message (for acknowledging action)
        socket.broadcast.to(socket.room).emit('message', { username : socket.uid, message: JSON.stringify(data) }); // real broadcast to other listener in room

        if(data['game-event'] == "create-game"){
            joinOrCreateNewGame(socket);
            updateRooms();
        }
    });

    socket.on("add-user", function(data) {
        var room = Object.keys(rooms)[0];
        socket.uid = data.uid;
        socket.room = room;
        users[data.uid] = socket;
        rooms[room].push(data.uid);
        socket.join(room);
        socket.emit('message', { message: 'You have connected to ' + room });
        socket.broadcast.to(room.key).emit('message', { message : data.uid + ' has connected to this room' });
        updateRooms();
    });

    socket.on('send', function (data) {
        io.sockets.emit('message', data);
        console.log("Response:", data);
    });

    socket.on('error', function (reason){
        console.error('Unable to connect Socket.IO ' +  reason);
        socket.destroy();
    });

    socket.on('disconnect', function(){
        console.log("disconnect: " + socket.uid + " from " + socket.room);
        rooms[socket.room].splice(socket.uid, 1);
        updateRooms();
        socket.emit('message', { message: 'You have disconnected from ' + socket.room });
        socket.broadcast.to(socket.room.key).emit('message', { message : socket.uid + ' has disconnected.' });
        socket.leave(socket.room);
    });

    socket.on('uncaughtException', function (err) {
        console.error('Caught exception: ' + err.stack);
    });
});

// # serving the flash policy file
net = require("net");

net.createServer(function(socket) {
    //just added
    socket.on("error", function() {
        console.log("Caught flash policy server socket error: ");
        console.log(err.stack);
    });

    socket.write('<?xml version="1.0"?>\n');
    socket.write('<!DOCTYPE cross-domain-policy SYSTEM "http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd">\n');
    socket.write("<cross-domain-policy>\n");
    socket.write('<allow-access-from domain="*" to-ports="*"/>\n');
    socket.write('</cross-domain-policy>\n');
    socket.end();
}).listen(843);