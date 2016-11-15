import React, { Component } from 'react';
import { StatusBar } from 'react-native';

import ChatRoom from './Components/ChatRoom';

export default class App extends Component {
  componentWillMount() {
    StatusBar.setHidden(true);
  }

  render() {
    return (
      <ChatRoom />
    );
  }
}
