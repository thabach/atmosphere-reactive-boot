import Promise from 'bluebird';

export default class Translator {
  constructor() {
    this._token = null;
    this.generateApiKey();
  }

  generateApiKey() {
    this._token = null;
  }

  translate(text, fromLanguage, toLanguage) {
    return new Promise((resolve, reject) => {
      resolve(text);
    });
  }
}