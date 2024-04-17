// next.config.js
const TerserPlugin = require('terser-webpack-plugin')

module.exports = {
    future: {
        webpack5: true,
    },
    webpack: function (config, options) {
        Object.assign(config.optimization, {
            //entry: [
            //    './index.js',
            //    '../temporal/lib/worker.js'
            //],
            minimize: false,
            minimizer: [
                new TerserPlugin({
                    terserOptions: {
                        keep_fnames: true, // don't strip funciton names in production
                    },
                })
            ],
        });


        Object.assign(config, {
            //entry: ['./index.js','../temporal/lib/worker.js'],
        });


        return config;
    },
};

