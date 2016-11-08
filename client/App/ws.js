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
      this._ws.close();
    }

    console.log('ws :: connect', locationUrl);
    this._ws = new WebSocket(locationUrl);

    this._ws.onopen = () => {
      console.log('ws :: connected');
    };

    this._ws.onmessage = event => {
      console.log('ws :: onmessage', event.data);
      this.onReceive(event.data);
    };

    this._ws.onerror = event => {
      console.log('ws :: error', event.message);
    };

    this._ws.onclose = event => {
      console.log('ws :: onclose', event.code, event.reason);
    };
  }
}