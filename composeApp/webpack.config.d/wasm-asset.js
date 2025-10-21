// composeApp/webpack.config.d/wasm-asset.js
;(function (config) {
  // Убедимся, что .wasm не пытается грузиться как модуль, а копируется как статический ресурс
  config.module = config.module || {}
  config.module.rules = (config.module.rules || []).concat([
    { test: /\.wasm$/, type: 'asset/resource' }
  ])
})(config)
