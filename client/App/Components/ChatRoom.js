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
import {GiftedChat, Bubble} from 'react-native-gifted-chat';
import Translator from '../Translator';

import Icon from 'react-native-vector-icons/FontAwesome';

const languages = {
  en: 'English',
  fr: 'French',
  ru: 'Russian',
  tlh: 'Klingon',
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
  yua: 'Yucatec: Maya'
}
const {height, width} = Dimensions.get('window');
var translationIds = 9999;
export default class Example extends React.Component {
  constructor(props) {
    super(props);

    this._onSendCb = this.onSend.bind(this);
    this._renderBubbleCb = this.renderBubble.bind(this);

    this.state = {
      language: 'en',
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
    // Save original text before translation
    var _originalText = message.text;
    Translator.translate(message.text, 'en', this.state.language).then(
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

  onSend(messages) {
    this.addNewMessages(messages);

    this.onReceivedMessage(messages[0]);
  }

  render() {
    return (
      <View style={{flex: 1, flexDirection: 'row'}}>
        <GiftedChat
          messages={this.state.messages}
          onSend={this._onSendCb}
          renderBubble={this._renderBubbleCb}
          user={{
            _id: 1,
          }}
        />
        <View style={{position: 'absolute', right: 10, top: 10}}>
          <Icon.Button name="cog" backgroundColor="#333" iconStyle={{marginRight: 0}} onPress={() => this.setState({showLanguageChooser: true})} />
        </View>
        <Modal
          animationType={'slide'}
          visible={this.state.showLanguageChooser}
          transparent={true}
          onRequestClose={() => this.setState({showLanguageChooser: false})}
        >
          <View key={'overlay'} style={{position: 'absolute', top: 0, left: 0, height: height, width: width, backgroundColor: '#000', opacity: 0.7}} />
          <View style={{backgroundColor: '#FFF', padding: 15}}>
            <Text style={{textAlign: 'center', fontSize: 16}}>Choose your language:</Text>
            <Picker
              selectedValue={this.state.language}
              onValueChange={lang => this.setState({language: lang})}>
              {
                _.map(languages, (label, code) => <Picker.Item key={code} label={label} value={code} />)
              }
            </Picker>
            <View style={{flex: 0, alignItems: 'center', justifyContent: 'center'}}>
              <TouchableHighlight onPress={() => this.setState({showLanguageChooser: false})}>
                <View style={{backgroundColor: '#d9dce0', padding: 10, overflow: 'hidden'}}>
                  <Text>Close</Text>
                </View>
              </TouchableHighlight>
            </View>
          </View>
        </Modal>
      </View>
    );
  }
}