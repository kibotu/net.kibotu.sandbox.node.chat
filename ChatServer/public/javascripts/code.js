$( document ).ready(function() {

    $("[rel='tooltip']").tooltip();

    var messages = [];
    var socket = io.connect('http://172.16.3.13:3000/');

    /** HELPER FUNCTIONS **/

    var createAlert = function(text, alertLevel) {
        //$("#message-box").append('<div class="alert ' + alertLevel + '">' + text + ' <button type="button" class="close" data-dismiss="alert">&times;</button></div>');
    };

    /** CLIENT   https://github.com/LearnBoost/socket.io/wiki/Exposed-events **/

    // "connect" is emitted when the socket connected successfully
    socket.socket.on('connect', function () {
        createAlert('Connected.', 'alert-success');
     });

     // disconnect" is emitted when the socket disconnected
    socket.socket.on('disconnect', function () {
        createAlert('Disconnected.', 'alert-danger');
     });

     // "connect_failed" is emitted when socket.io fails to establish a connection to the server and has no more transports to fallback to
    socket.socket.on('connect_failed', function () {
        createAlert('Connect Failed.', 'alert-danger');
     });

     // "error" is emitted when an error occurs and it cannot be handled by the other event types.
    socket.socket.on('error', function () {
        createAlert('Connection error.', 'alert-danger');
     });

     // "reconnect_failed" is emitted when socket.io fails to re-establish a working connection after the connection was dropped
    socket.socket.on('reconnect_failed', function () {
        createAlert('Reconnection failed.', 'alert-danger');
     });

     // "reconnect" is emitted when socket.io successfully reconnected to the server.
    socket.socket.on('reconnect', function () {
        createAlert('Reconnected.', 'alert-success');
     });

     // "reconnecting" is emitted when the socket is attempting to reconnect with the server.
    socket.socket.on('reconnecting', function () {
        createAlert('Reconnecting...', 'alert-info');
     });

    /** RESPONSES **/

    socket.on('message', function (data) {

        console.log("Response:", data);

        if(data.message) {
            messages.push(data);
            var html = '';
            for(var i=0; i < messages.length; ++i) {
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
        if(!data)
            console.log("empty data: " + data);
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

        var name = $("#name").val();

        if(name == "") {
            alert("Please type your name!");
        } else {
            var field = $("#field");
            socket.emit('send', { message:  field.val(), username: name });
            field.val("");
        }
    });

    $("#join-game_type_1vs1").click( function() {
        socket.emit('event', { "game-event" : "join", "game-type" : "1vs1" });
    });

    $("#join-game_type_2vs2").click( function() {
        socket.emit('event', { "game-event" : "join", "game-type" : "2vs2" });
    });

    $("#join-game_type_custom_1vs1").click( function() {
        socket.emit('event', { "game-event" : "join", "game-type" : "custom_1vs1" });
    });

    $("#join-game_type_custom_2vs2").click( function() {
        socket.emit('event', { "game-event" : "join", "game-type" : "custom_2vs2" });
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

    /** SERVER EVENTS **/
    $("#game-data").click( function() {
        socket.emit('event', { "game-event" : "game-data", planets : [ { id : 0, position : [10, 10, 0] }, { id : 0, position : [10, 10, 0] }, { id : 1, position : [100, 100, 0] }], player : [{  }]  });
    });

});