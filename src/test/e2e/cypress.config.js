const { defineConfig } = require('cypress')

module.exports = defineConfig(
  {
    allowCypressEnv: false,
    e2e: {
      setupNodeEvents(on) {
        require('cypress-terminal-report/src/installLogsPrinter')(
          on, { }
        )
      },
      baseUrl: "http://127.0.0.1:8080",
      specPattern: "cypress/**/*.spec.js",
      video: false
    }
  }
)

