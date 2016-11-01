import React from 'react';
import {
  View,
  Dimensions,
  Picker,
  Modal,
  TouchableHighlight,
  Text
} from 'react-native';
import _ from 'lodash';
import ModalPicker from 'react-native-modal-picker';
import {GiftedChat, Bubble} from 'react-native-gifted-chat';
import GoogleTranslator from '../Translators/GoogleTranslator';
import BingTranslator from '../Translators/BingTranslator';

import Icon from 'react-native-vector-icons/FontAwesome';

const TRANSLATORS = {
  none: 'None',
  google: 'Google',
  bing: 'Bing'
}

const LANGUAGES = {
  en: 'English',
  fr: 'French',
  ru: 'Russian',
  be: 'Belarusian'
  /*tlh: 'Klingon',
  af: 'Afrikaans',
  ar: 'Arabic',
  'bs-Latn': 'Bosnian: (Latin)',
  bg: 'Bulgarian',
  ca: 'Catalan',
  'zh-CHS': 'Chinese: Simplified',
  'zh-CHT': 'Chinese: Traditional',
  hr: 'Croatian',
  cs: 'Czech',
  da: 'Danish',
  nl: 'Dutch',
  et: 'Estonian',
  fi: 'Finnish',
  de: 'German',
  el: 'Greek',
  ht: 'Haitian: Creole',
  he: 'Hebrew',
  hi: 'Hindi',
  mww: 'Hmong: Daw',
  hu: 'Hungarian',
  id: 'Indonesian',
  it: 'Italian',
  ja: 'Japanese',
  sw: 'Kiswahili',
  ko: 'Korean',
  lv: 'Latvian',
  lt: 'Lithuanian',
  ms: 'Malay',
  mt: 'Maltese',
  no: 'Norwegian',
  fa: 'Persian',
  pl: 'Polish',
  pt: 'Portuguese',
  otq: 'Querétaro: Otomi',
  ro: 'Romanian',
  'sr-Cyrl': 'Serbian',
  sk: 'Slovak',
  sl: 'Slovenian',
  es: 'Spanish',
  sv: 'Swedish',
  th: 'Thai',
  tr: 'Turkish',
  uk: 'Ukrainian',
  ur: 'Urdu',
  vi: 'Vietnamese',
  cy: 'Welsh',
  yua: 'Yucatec: Maya'*/
}
const {height, width} = Dimensions.get('window');
var translationIds = 9999;
export default class Example extends React.Component {
  constructor(props) {
    super(props);

    this._onSendCb = this.onSend.bind(this);
    this._renderBubbleCb = this.renderBubble.bind(this);

    this._googleTranslator = new GoogleTranslator();
    this._bingTranslator = new BingTranslator();

    this._translator = null;

    this.state = {
      language: 'none',
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
    console.log(messages);
    this.setState({
      messages: GiftedChat.append(this.state.messages, messages)
    });
  }

  onReceivedMessage(message) {
    // Save original text before translation
    if (this._translator === null) {
      setTimeout(() => {
        var data = {
          _id: translationIds++,
          text: message.text,
          createdAt: new Date(),
          user: {
            _id: 99,
            name: 'Translator'
          }
        }
        this.addNewMessages([data]);
      }, 1);
    } else {
      var _originalText = message.text;
      this._translator.translate(message.text, 'en', this.state.language).then(
        translated => {
          var message = {
            _id: translationIds++,
            text: translated ? translated : _originalText,
            createdAt: new Date(),
            user: {
              _id: 99,
              name: 'Translator'
            }
          }
          this.addNewMessages([message]);
        },
        err => {}
      ).catch(() => {});
    }
  }

  onSend(messages) {
    this.addNewMessages(messages);

    this.onReceivedMessage(messages[0]);
  }

  render() {
    return (
      <View style={{flex: 1, flexDirection: 'column'}}>
      <View style={{height: 40, width}}>
        <View style={{flex: 1, flexDirection: 'row', alignItems: 'center'}}>
          <View style={{flex: 1}}>
            <Text
              style={{}}
              editable={false}
            >Translator:</Text>
            <ModalPicker
              onChange={translator => {
                console.log(translator);
                if (translator.value === 'google') {
                  this._translator = this._googleTranslator;
                } else if (translator.value === 'bing') {
                  this._translator = this._bingTranslator;
                } else {
                  this._translator = null;
                }
                this.setState({translator: translator.value});
              }}
              initValue={TRANSLATORS[this.state.translator]}
              value={this.state.translator}
              data={_.map(TRANSLATORS, (translator, key) => {
                return {
                  label: translator,
                  value: key,
                  key: key
                };
              })}>
                <Text
                  style={{textAlign: 'center', color: '#333', borderWidth:1, borderColor:'#ccc', padding:5, height:30}}
                  editable={false}
                >{TRANSLATORS[this.state.translator]}</Text>
            </ModalPicker>
          </View>
          <View style={{flex: 1}}>
            <Text
              style={{}}
              editable={false}
            >Language:</Text>
            <ModalPicker
              onChange={lang => this.setState({language: lang.value})}
              initValue={LANGUAGES[this.state.language]}
              value={this.state.language}
              data={_.map(LANGUAGES, (lang, key) => {
                return {
                  label: lang,
                  value: key,
                  key: key
                };
              })}>
                <Text
                  style={{textAlign: 'center', color: '#333', borderWidth:1, borderColor:'#ccc', padding:5, height:30}}
                  editable={false}
                >{LANGUAGES[this.state.language]}</Text>
              </ModalPicker>
            </View>
          </View>
        </View>
        <View style={{width, height: height - 40}}>
          <View style={{flex: 1, flexDirection: 'row'}}>
            <GiftedChat
              messages={this.state.messages}
              onSend={this._onSendCb}
              renderBubble={this._renderBubbleCb}
              user={{
                _id: 1,
              }}
            />
          </View>
        </View>
      </View>
    );
  }
}