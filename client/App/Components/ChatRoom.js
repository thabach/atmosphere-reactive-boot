import React from 'react';
import {
  View,
  Dimensions,
  Picker,
  Modal,
  TouchableOpacity,
  Text,
  TextInput
} from 'react-native';
import _ from 'lodash';
import uuid from 'uuid';

import ModalPicker from 'react-native-modal-picker';
import {GiftedChat, Bubble} from 'react-native-gifted-chat';
import GoogleTranslator from '../Translators/GoogleTranslator';
import BingTranslator from '../Translators/BingTranslator';

import WS from '../ws';

import Icon from 'react-native-vector-icons/FontAwesome';

const TRANSLATORS = {
  none: 'None',
  google: 'Google',
  bing: 'Bing'
}

const LANGUAGES = {
  'google': {
    en: 'English',
    fr: 'French',
    ru: 'Russian',
    be: 'Belarusian'
  },
  'bing': {
    en: 'English',
    fr: 'French',
    ru: 'Russian',
    tlh: 'Klingon'
  }
};

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

    this._onMessageCb = this.onReceivedMessage.bind(this);
    this._ws = new WS();
    this._ws.addListener('message', this._onMessageCb);

    this._userId = uuid.v4();

    this.state = {
      showLanguageChooser: false,
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
      messages: GiftedChat.append(this.state.messages, messages)
    });
  }

  onReceivedMessage(message) {
    console.log('received message', message);
    if (message.userId !== this._userId) {
      // Save original text before translation
      if (this._translator === null || (this._language === 'none' || message.language === this._language)) {
        setTimeout(() => {
          var data = {
            _id: translationIds++,
            text: message.text,
            createdAt: new Date(message.clock),
            user: {
              _id: message.userId,
              language: message.language,
              name: 'Translator'
            }
          }
          this.addNewMessages([data]);
        }, 1);
      } else {
        var _originalText = message.text;
        this._translator.translate(message.text, message.language, this._language).then(
          translated => {
            var data = {
              _id: translationIds++,
              text: translated ? translated : _originalText,
              createdAt: new Date(message.clock),
              user: {
                _id: message.userId,
                language: message.language,
                name: 'Translator'
              }
            }

            console.log(data);
            this.addNewMessages([data]);
          },
          err => {
            console.log('error', err);
          }
        ).catch((err) => {

            console.log('caught error', err);
        });
      }
    }
  }

  onSend(messages) {
    this.addNewMessages(messages);

    if (this._ws && messages[0]) {
      var message = {
        userId: messages[0].user._id,
        avatarUrl: '',
        firstName: '',
        lastName: '',
        language: this._language,
        text: messages[0].text,
        clock: messages[0].createdAt
      }
      this._ws.send(message);
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

  render() {
    return (
      <View style={{flex: 1}}>
        <GiftedChat
          messages={this.state.messages}
          isAnimated={false}
          onSend={this._onSendCb}
          renderBubble={this._renderBubbleCb}
          renderActions={() => <LanguageChooser onChange={this._setLanguage.bind(this)} />}
          user={{
            _id: this._userId
          }}
        />

        <TouchableOpacity style={{position: 'absolute', top: 0, right: 0, padding: 10}} onPress={() => this.setState({showSettings: true})}>
          <Icon name="cog" size={25} />
        </TouchableOpacity>

        <Modal
          animationType={"fade"}
          transparent={true}
          visible={this.state.showSettings === true}
          onRequestClose={() => this.setState({showSettings: false})}
        >
          <View style={{position: 'absolute', top: 0, left: 0, width, height, backgroundColor: '#000', opacity: 0.7}} />
          <View style={{margin: 20, flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center'}}>
            <View style={{padding: 20, flex: 1, backgroundColor: '#FFF'}}>
              <Text style={{fontWeight: 'bold', textAlign: 'center'}}>Server Location:</Text>
              <TextInput
                autoFocus={true}
                style={{height: 40, marginTop: 10, fontSize: 14, borderColor: '#ccc', borderWidth: 1, padding: 10, textAlign: 'center'}}
                onChangeText={(serverLocation) => this.setState({serverLocation})}
                defaultValue={this._serverLocation ? this._serverLocation : 'ws://'}
                value={this.state.serverLocation}
              />
              <View style={{flexDirection: 'row', justifyContent: 'center', marginTop: 10}}>
                <TouchableOpacity onPress={() => {
                  //this._serverLocation = this.state.serverLocation;
                  this._ws.connect(this._serverLocation);
                  this.setState({showSettings: false});
                }}>
                  <View style={{flex: 0, backgroundColor: '#99CCFF', borderRadius: 3, paddingTop: 10, paddingBottom: 10, paddingLeft: 20, paddingRight: 20}}>
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