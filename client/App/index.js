import React, { Component } from 'react';
import {
  StyleSheet,
  StatusBar,
  View,
  Dimensions
} from 'react-native';
import ChatRoom from './Components/ChatRoom';

const {height, width} = Dimensions.get('window');
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

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F6F6F7',
  }
});
