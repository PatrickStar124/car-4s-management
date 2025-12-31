const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        port: 8081,
        proxy: {
            '/api': {
                target: 'http://localhost:8999', // 后端地址
                changeOrigin: true // 开启跨域
                // 注意：这里不要加 pathRewrite
            }
        }
    }
})