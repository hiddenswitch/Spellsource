/**
 * @type {import('next').NextConfig}
 */
const nextConfig = {



  webpack: (config) => {
    // camelCase style names from css modules
    // https://stackoverflow.com/questions/74038400/convert-css-module-kebab-case-class-names-to-camelcase-in-next-js
    config.module.rules
      .find(({oneOf}) => !!oneOf).oneOf
      .filter(({use}) => JSON.stringify(use)?.includes('css-loader'))
      .reduce((acc, {use}) => acc.concat(use), [])
      .forEach(({options}) => {
        if (options.modules) {
          options.modules.exportLocalsConvention = 'camelCase';
        }
      });

    config.resolve.fallback = { fs: false, path: false };
    config.experiments.topLevelAwait = true;

    return config;
  },
}

module.exports = nextConfig
