$( document ).ready(function() {

    var messages = [];
    var socket = io.connect('http://localhost:3000/');

    /** HELPER FUNCTIONS **/

    var creatAlert = function(message, alertLevel) {
        $("#messages").append('<div class="alert ' + alertLevel + '">' + message + ' <button type="button" class="close" data-dismiss="alert">&times;</button></div>');
    };

    /** CLIENT   https://github.com/LearnBoost/socket.io/wiki/Exposed-events **/

    // "connect" is emitted when the socket connected successfully
    socket.socket.on('connect', function () {
        creatAlert('Connected.', 'alert-success');
     });

     // disconnect" is emitted when the socket disconnected
    socket.socket.on('disconnect', function () {
        creatAlert('Disconnected.', 'alert-danger');
     });

     // "connect_failed" is emitted when socket.io fails to establish a connection to the server and has no more transports to fallback to
    socket.socket.on('connect_failed', function () {
        creatAlert('Connect Failed.', 'alert-danger');
     });

     // "error" is emitted when an error occurs and it cannot be handled by the other event types.
    socket.socket.on('error', function () {
        creatAlert('Connection error.', 'alert-danger');
     });

     // "reconnect_failed" is emitted when socket.io fails to re-establish a working connection after the connection was dropped
    socket.socket.on('reconnect_failed', function () {
        creatAlert('Reconnection failed.', 'alert-danger');
     });

     // "reconnect" is emitted when socket.io successfully reconnected to the server.
    socket.socket.on('reconnect', function () {
        creatAlert('Reconnected.', 'alert-success');
     });

     // "reconnecting" is emitted when the socket is attempting to reconnect with the server.
    socket.socket.on('reconnecting', function () {
        creatAlert('Reconnecting...', 'alert-info');
     });

    /** RESPONSES **/

    socket.on('message', function (data) {

        console.log("Response:", data);

        if(data.message) {
            messages.push(data);
            var html = '';
            for(var i=0; i<messages.length; i++) {
                html += '<b>' + (messages[i].username ? messages[i].username : 'Server') + ': </b>';
                html += messages[i].message + '<br />';

            }
            if(data.uid) {
                $("#name").val(data.uid);
                socket.emit('add-user', { uid: data.uid });
            }

            $("#content").html(html);
            $("#content").animate({ scrollTop: $("#content")[0].scrollHeight}, 200);
        } else {
            console.log("There is a problem:", data);
        }
    });

    socket.on('update-room', function (data) {
        if(data)
            console.log(data);
        if(data.Queue) {
            var html = '';
            for(var player in data.Queue) {
                html += '<a href="#" class="list-group-item">' +  data.Queue[player] + '</a>';
            }
            $("#queue").html(html);
        }
        html = '';
        for(var i = 1; i < _.size(data); ++i) {
            html += '<a href="#" class="list-group-item">' +  Object.keys(data)[i] + '<span class="badge badge-info pull-right">' + _.size(data[Object.keys(data)[i]]) + '</span></a>';
        }
        $("#rooms").html(html);
    });

    /** REQUESTS **/

    $("#send").click(function() {

        if($("#name").val() == "") {
            alert("Please type your name!");
        } else {
            socket.emit('send', { message:  $("#field").val(), username: $("#name").val() });
        }
    });

    $("#create-game").click( function() {
        socket.emit('event', { "game-event" : "create-game" });
    });

    $("#start-game").click( function() {
        socket.emit('event', { "game-event" : "start-game" });
    });

    $("#pause-game").click( function() {
        socket.emit('event', { "game-event" : "pause-game" });
    });

    $("#resume-game").click( function() {
        socket.emit('event', { "game-event" : "resume-game" });
    });

    $("#end-game").click( function() {
        socket.emit('event', { "game-event" : "end-game" });
    });

    $("#move-units").click( function() {
        socket.emit('event', { "game-event" : "move-units", source : 1, dest : 2, amount  : 50 });
    });

    $("#level-up").click( function() {
        socket.emit('event', { "game-event" : "level-up", skill : 1 });
    });

    $("#special-1").click( function() {
        socket.emit('event', { "game-event" : "special-1", dest: 1 });
    });

    $("#special-2").click( function() {
        socket.emit('event', { "game-event" : "special-2", dest: 1 });
    });

    $("#special-3").click( function() {
        socket.emit('event', { "game-event" : "special-3", dest: 1 });
    });

    $("#buy").click( function() {
        socket.emit('event', { "game-event" : "buy", item : 1 });
    });
});