window.onload = function() {

    var messages = [];
    var socket = io.connect('http://178.5.164.68:7331');
    var field = document.getElementById("field");
    var sendButton = document.getElementById("send");
    var content = document.getElementById("content");
    var name = document.getElementById("name");

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
                socket.emit('adduser', { uid: data.uid });
            }

            content.innerHTML = html;
        } else {
            console.log("There is a problem:", data);
        }
    });

    socket.on('updateroom', function (data) {
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
        for(var i = 1; i < Object.keys(data).length; ++i) {
            html += '<a href="#" class="list-group-item">' +  Object.keys(data)[i] + '</a>';
        }
        $("#rooms").html(html);
    });

    sendButton.onclick = function() {
        if(name.value == "") {
            alert("Please type your name!");
        } else {
            var text = field.value;
            socket.emit('send', { message: text, username: name.value });
            //socket.emit('ping');
            field.value = "";
        }
    };
}