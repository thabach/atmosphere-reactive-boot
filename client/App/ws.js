import {EventEmitter} from 'events';
import Base64 from 'base-64';

export default class WS extends EventEmitter {
  constructor() {
    super();

    this._ws = null;
  }

  send(path, message) {
    if (this._ws) {
      var wsMessage = JSON.stringify({
        path: path,
        destination: 'all',
        body: Base64.encode(unescape(encodeURIComponent(JSON.stringify(message))))
      })

      // Send to server
      this._ws.send(wsMessage);
    }
  }

  onReceive(wsMessage) {
    var jsonResponse = JSON.parse(wsMessage);
    var message = JSON.parse(decodeURIComponent(escape(Base64.decode(jsonResponse.body))));
    if (jsonResponse.path === '/dispatch') {
      this.emit('message', message);
    } else if (jsonResponse.path === '/typing') {
      this.emit('typing', message);
    }
  }

  connect(locationUrl) {
    if (this._ws) {
      // Make sure to close current connection before connecting to new location URL
      this._ws.close();
    }

    this._ws = new WebSocket(locationUrl);

    this._ws.onopen = () => {
      // Websocket is connected!
    };

    this._ws.onmessage = event => {
      // Message received, yay!
      this.onReceive(event.data);
    };

    this._ws.onerror = event => {
      // Oops!
      console.log('ws :: error', event.message);
    };

    this._ws.onclose = event => {
      // Yep, WebSocket has been closed
    };
  }
}