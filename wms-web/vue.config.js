const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    // 新增代理配置：转发/api开头的请求到后端8090
    devServer: {
        port: 8081, // 前端端口（保持不变）
        proxy: {
            '/api': {
                target: 'http://localhost:8090', // 后端地址
                changeOrigin: true, // 开启跨域转发
                pathRewrite: { '^/api': '' } // 转发时去掉/api前缀（后端接口还是/register）
            }
        }
    }
})