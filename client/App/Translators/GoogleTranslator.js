import Promise from 'bluebird';
import Translator from '.';

var decodeHtmlEntity = function(str) {
  return str.replace(/&#(\d+);/g, function(match, dec) {
    return String.fromCharCode(dec);
  });
};

export default class GoogleTranslator extends Translator {
  generateApiKey() {
    this._token = 'AIzaSyCIQ2j6ykaOGyoP6h9GbPL2baLWnGWRN50';
  }

  translate(text, fromLanguage, toLanguage) {
    return new Promise((resolve, reject) => {
      const options = {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      };

      var endpoint = 'https://www.googleapis.com/language/translate/v2?q=' + text + '&target=' + toLanguage + '&source=' + fromLanguage + '&key=' + this._token;
      fetch(endpoint, options).then(res => {
        return res.json()
      }).then(
        response => {
          if (response && response.data && response.data.translations && response.data.translations.length > 0) {
            resolve(decodeHtmlEntity(response.data.translations[0].translatedText));
          } else {
            reject();
          }
        },
        err => {
          console.log('google translate :: error', err);
          reject(err);
        }
      ).catch(err => {
        console.log('google translate :: caught error', err);
        reject(err)
      });
    });
  }
}