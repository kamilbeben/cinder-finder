
// https://nuxtjs.org/deployments/pm2/

module.exports = {
  apps: [
    {
      name: 'CinderFinder',
      exec_mode: 'cluster',
      instances: 'max',
      script: './node_modules/nuxt/bin/nuxt.js',
      args: 'start'
    }
  ]
}