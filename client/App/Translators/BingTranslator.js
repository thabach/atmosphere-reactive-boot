import Promise from 'bluebird';
import Translator from '.';

export default class BingTranslator extends Translator {
  constructor(props) {
    super(props);

    setInterval(() => this.generateApiKey(), 480000); // Regenerate the API key every 8 minutes
  }

  generateApiKey() {
    const options = {
      method: 'POST',
      headers: {
        'Ocp-Apim-Subscription-Key': '0b1706a555e844d5b340c73749f8d51e',
        'Content-Type': 'application/json',
        'Accept': 'application/jwt'
      }
    };
    fetch('https://api.cognitive.microsoft.com/sts/v1.0/issueToken', options).then(
      res => res.text(),
      err => console.log('bing translate :: fetch error', err)
    ).then(
      token => {
        console.log('bing translate :: token', token);
        this._token = token
      },
      err => console.log('bing translate :: token error', err)
    ).catch(err => console.log('bing translate :: token caught error', err));
  }

  translate(text, fromLanguage, toLanguage) {
    return new Promise((resolve, reject) => {
      const options = {
        method: 'GET',
        headers: {
          'Accept': 'application/xml',
          'Authorization': 'Bearer ' + this._token,
          'Content-Type': 'application/json'
        }
      };

      var endpoint = 'https://api.microsofttranslator.com/v2/http.svc/Translate?text=' + text + '&from=' + fromLanguage + '&to=' + toLanguage;
      fetch(endpoint, options).then(res => {
        return res.text()
      }).then(
        text => {
          var matches = /^<string[^>]*>(.*)<\/string>$/.exec(text);
          resolve(matches[1]);
        },
        err => reject(err)
      ).catch(err => reject(err));
    });
  }
}