import Promise from 'bluebird';

class Translator {
  constructor() {
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
      err => console.log('Error: ', err)
    ).then(
      token => {
        this._token = token},
      err => console.log('Error2: ', err)
    ).catch(err => console.log('Error caught: ', err));
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

var translator = new Translator();

export default translator;