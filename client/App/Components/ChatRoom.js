import React from 'react';
import {
  View,
  Dimensions,
  Picker,
  Modal,
  TouchableOpacity,
  Text,
  TextInput,
  Platform
} from 'react-native';
import _ from 'lodash';
import uuid from 'uuid';

import ModalPicker from 'react-native-modal-picker';
import {GiftedChat, Bubble, InputToolbar} from 'react-native-gifted-chat';


import GoogleTranslator from '../Translators/GoogleTranslator';
import BingTranslator from '../Translators/BingTranslator';

import WS from '../ws';

import Icon from 'react-native-vector-icons/FontAwesome';

let index = 0;
const data = [
  { key: index++, section: true, label: 'Select Your Language:' },
  { key: index++, value: 'none', label: 'Do not translate messages' },
  { key: index++, value: 'en', label: 'English' },
  { key: index++, value: 'fr', label: 'French' },
  { key: index++, value: 'ru', label: 'Russian' },
  { key: index++, value: 'be', label: 'Belarusian' },
  { key: index++, value: 'tlh', label: 'Klingon' },
];

const {height, width} = Dimensions.get('window');
var translationIds = 9999;

// Android tweak
class AndroidGiftedChat extends GiftedChat {
  onKeyboardWillShow(e) {}
}
var MyGiftedChat = GiftedChat;
if (Platform.OS === 'android') {
  MyGiftedChat = AndroidGiftedChat;
}

class LanguageChooser extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      language: null
    };
  }

  render() {
    return (
      <View style={{alignItems: 'center', justifyContent: 'center', height: 45}}>
        <ModalPicker
          onChange={lang => {
            this.setState({language: lang.value === 'none' ? null : lang.value});
            this.props.onChange(lang.value);
          }}
          data={data}>
          <View style={{paddingTop: 12, paddingLeft: 10, paddingBottom: 12}}>
            {this.state.language ? <Text style={{fontWeight: 'bold', color: '#003366'}}>{this.state.language.toUpperCase()}</Text> : <Icon name="globe" size={21} color="#003366" />}
          </View>
        </ModalPicker>
      </View>
    );
  }
}

export default class ChatRoom extends React.Component {
  constructor(props) {
    super(props);

    this._onSendCb = this.onSend.bind(this);
    this._renderBubbleCb = this.renderBubble.bind(this);

    this._googleTranslator = new GoogleTranslator();
    this._bingTranslator = new BingTranslator();

    this._translator = null;
    this._serverLocation = 'ws://10.0.1.2:8080';
    this._language = 'none';

    // Setup and add WebSocket listeners
    this._onMessageCb = this.onReceivedMessage.bind(this);
    this._onTypingCb = this.onReceivedTyping.bind(this);
    this._ws = new WS();
    this._ws.addListener('message', this._onMessageCb);
    this._ws.addListener('typing', this._onTypingCb);

    this._userId = uuid.v4();

    this._isTyping = false;
    this._typingUsers = {};

    this.state = {
      showLanguageChooser: false,
      firstName: 'Jeremie',
      lastName: 'Papillon',
      messages: []
    };
  }

  renderBubble(props) {
    return (
      <Bubble
        {...props}
        wrapperStyle={{
          left: {
            backgroundColor: '#d9dce0',
            borderRadius: 5
          },
          right: {
            backgroundColor: '#7695C4',
            borderRadius: 5
          }
        }}
      />
    );
  }

  addNewMessages(messages = []) {
    this.setState({
      messages: MyGiftedChat.append(this.state.messages, messages)
    });
  }

  _getTranslatedMessage(message, translation) {
    return {
      _id: message._id,
      text: translation ? translation : message.text,
      createdAt: new Date(message.clock),
      user: {
        _id: message.userId,
        language: message.language,
        name: message.firstName + ' ' + message.lastName
      }
    }
  }

  onReceivedMessage(message) {
    // Prepare message to be added
    var addMessage = (message, translation) => {
      var data = this._getTranslatedMessage(message, translation);
      this.addNewMessages([data]);
    }

    if (message.userId !== this._userId) {
      if (this._translator === null || (this._language === 'none' || message.language === this._language)) {
        setTimeout(() => addMessage(message), 1);
      } else {
        var _originalText = message.text;
        this._translator.translate(message.text, message.language, this._language).then(
          translated => addMessage(message, translated),
          err => console.log('chatroom :: error', err)
        ).catch((err) => console.log('chatroom :: caught error', err));
      }
    }
  }

  onReceivedTyping(user) {
    if (user.userId === this._userId) {
      return;
    }

    var msg = ' ';
    if (user.isTyping) {
      this._typingUsers[user.userId] = user;
    } else {
      this._typingUsers[user.userId] = null;
      delete this._typingUsers[user.userId];
    }

    var users = _.values(this._typingUsers);
    if (users.length > 0) {
      msg = users[0].firstName + ' ' + users[0].lastName;
      if (users.length > 1) {
        msg += ' and ' + (users.length - 1) + ' other' + (users.length === 2 ? ' is' : 's are') + ' typing...';
      } else {
        msg += ' is writing...';
      }
    }

    this.setState({
      isTypingText: msg
    });
  }

  _onTypingStop() {
    this.sendUserIsTyping(false);
    this._isTyping = false;
  }

  resetTypingTimer() {
    clearTimeout(this._typingTimer);
    this._typingTimer = setTimeout(this._onTypingStop.bind(this), 2000);
  }

  sendUserIsTyping(isTyping) {
    if (!this._isTyping) {
      this._isTyping = isTyping;
      this._ws.send('/typing', {
        userId: this._userId,
        firstName: this.state.firstName,
        lastName: this.state.lastName,
        isTyping
      });
    }
    this.resetTypingTimer();
  }

  onSend(messages) {
    this.addNewMessages(messages);

    if (this._ws && messages[0]) {
      this._isTyping = false;
      this.sendUserIsTyping(false);
      var message = {
        userId: messages[0].user._id,
        avatarUrl: '',
        firstName: this.state.firstName,
        lastName: this.state.lastName,
        language: this._language,
        text: messages[0].text,
        clock: messages[0].createdAt
      }
      this._ws.send('/dispatch', message);
    }
  }

  _setLanguage(language) {
    if (language === 'none') {
      this._translator = null;
    } else if (language === 'tlh') {
      this._translator = this._bingTranslator;
    } else {
      this._translator = this._googleTranslator;
    }

    this._language = language;
  }

  componentWillMount() {
    if (this._serverLocation) {
      this._ws.connect(this._serverLocation);
    }
  }

  render() {
    return (
      <View style={{width, height}}>
        <MyGiftedChat
          bottomOffset={0}
          messages={this.state.messages}
          isAnimated={true}
          onSend={this._onSendCb}
          renderBubble={this._renderBubbleCb}
          renderActions={() => <LanguageChooser onChange={this._setLanguage.bind(this)} />}
          renderChatFooter={() => <Text style={{fontSize: 13, color: '#222', padding: 5}}>{this.state.isTypingText}</Text>}
          renderInputToolbar={(inputToolbarProps) => (
            <InputToolbar {...inputToolbarProps} onChange={(e) => {
              inputToolbarProps.onChange(e);
              this.sendUserIsTyping(true);
            }} style={{marginTop: 20}} />
          )}
          user={{
            _id: this._userId
          }}
        />

        <TouchableOpacity style={{position: 'absolute', top: 0, right: 0, padding: 10, backgroundColor: 'transparent'}} onPress={() => this.setState({showSettings: true})}>
          <Icon name="cog" size={25} />
        </TouchableOpacity>

        <Modal
          animationType={"fade"}
          transparent={true}
          visible={this.state.showSettings === true}
          onRequestClose={() => this.setState({showSettings: false})}
        >
          <View style={{position: 'absolute', top: 0, left: 0, width, height, backgroundColor: '#000', opacity: 0.7}} />
          <View style={{margin: 20, flex: 1, flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'center'}}>
            <View style={{padding: 20, flex: 1, backgroundColor: '#FFF'}}>
              <Text style={{fontWeight: 'bold', textAlign: 'center'}}>First Name:</Text>
              <TextInput
                autoFocus={true}
                style={{height: 40, marginTop: 5, marginBottom: 10, fontSize: 14, borderColor: '#ccc', borderWidth: 1, padding: 10, textAlign: 'center'}}
                onChangeText={(firstName) => this.setState({firstName})}
                value={this.state.firstName}
              />
              <Text style={{fontWeight: 'bold', textAlign: 'center'}}>Last Name:</Text>
              <TextInput
                style={{height: 40, marginTop: 5, marginBottom: 10, fontSize: 14, borderColor: '#ccc', borderWidth: 1, padding: 10, textAlign: 'center'}}
                onChangeText={(lastName) => this.setState({lastName})}
                value={this.state.lastName}
              />
              <Text style={{fontWeight: 'bold', textAlign: 'center'}}>Server Location:</Text>
              <TextInput
                style={{height: 40, marginTop: 5, marginBottom: 10, fontSize: 14, borderColor: '#ccc', borderWidth: 1, padding: 10, textAlign: 'center'}}
                onChangeText={(serverLocation) => this.setState({serverLocation})}
                defaultValue={this._serverLocation ? this._serverLocation : 'ws://'}
                value={this.state.serverLocation}
              />
              <View style={{flexDirection: 'row', justifyContent: 'center', marginTop: 10}}>
                <TouchableOpacity onPress={() => {
                  this.setState({showSettings: false});
                }}>
                  <View style={{flex: 0, backgroundColor: '#CCCCCC', borderRadius: 3, paddingTop: 10, paddingBottom: 10, paddingLeft: 20, paddingRight: 20}}>
                    <Text style={{fontSize: 15, textAlign: 'center'}}>Close</Text>
                  </View>
                </TouchableOpacity>
                <TouchableOpacity onPress={() => {
                  this._ws.connect(this._serverLocation);
                  this.setState({showSettings: false});
                }}>
                  <View style={{flex: 0, marginLeft: 10, backgroundColor: '#99CCFF', borderRadius: 3, paddingTop: 10, paddingBottom: 10, paddingLeft: 20, paddingRight: 20}}>
                    <Text style={{fontSize: 15, textAlign: 'center'}}>Save</Text>
                  </View>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </Modal>
      </View>
    );
  }
}