require("./sass/embed-main.scss");
var common = require("./prod-common.js");

// TODO: find a way to refer to embed-main.scss
var styleUrl = "/public/embedded.css"

common.com.olegych.scastie.client.ScastieEmbedded = 
  new common.clientOpt.scastie.ScastieEmbedded(common.defaultServerUrl, styleUrl)

module.exports = {
  com: common.com
};