import {EventEmitter} from 'events';
import Base64 from 'base-64';

export default class WS extends EventEmitter {
  constructor() {
    super();

    this._ws = null;
  }

  send(message) {
    if (this._ws) {
      var wsMessage = JSON.stringify({
        path: '/dispatch',
        destination: 'all',
        body: Base64.encode(unescape(encodeURIComponent(JSON.stringify(message))))
      })
      this._ws.send(wsMessage);
    }
  }

  onReceive(wsMessage) {
    var message = JSON.parse(decodeURIComponent(escape(Base64.decode(JSON.parse(wsMessage).body))));
    this.emit('message', message);
  }

  connect(locationUrl) {
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