angular.module("kafkatail")
	.service('socket', [ function() {

    var socket;

    function supported() { return window.WebSocket || window.MozWebSocket; }
    function newWebSocket(uri) { return window.WebSocket ?
								 new WebSocket(uri) : new MozWebSocket(uri);
							   }
    function createSocket(uri, callback) {
        if(supported()) {
            window.socket = socket = newWebSocket(uri);
            socket.onmessage = function(e) {
				callback(e);
            };
            socket.onopen = function(e) {
                debug('connection open');
            };
            socket.onclose = function(e) {
                debug('connection closed');
            };
        } else {
            alert("your browser does not support web sockets. try chrome.");
        }
    }
    function debug(msg) { console.debug(msg); }
    function isOpen() { return socket ?
						socket.readyState == (window.WebSocket ? WebSocket.OPEN : MozWebSocket.OPEN) : false;
					  }
    function send(message) {
        if(!supported()) { return; }
        if(isOpen()) {
            socket.send(message);
        } else {
            alert("socket is not open");
        }
    }
    function closeSocket() { if(socket) { socket.close(); } }
    function  openSocket() {
        if(isOpen()) {
            alert('socket already open');
            return;
        }
        createSocket(host);
    }

	return {
		create: function(host, callback) { createSocket(host, callback);},
		toggleConnection: function() { if(isOpen()) { closeSocket(); } else { openSocket(); } },
		send: function(msg) {send(msg);}
	};

}]);
