// composeApp/webpack.config.d/sqljs.js
;(function (config) {
  config.resolve = config.resolve || {};
  config.resolve.fallback = Object.assign({}, config.resolve.fallback, {
    fs: false,
    path: false,
  });
})(config);
