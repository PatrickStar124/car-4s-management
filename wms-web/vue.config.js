const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,

    // ✅ 添加这一行禁用 ESLint
    lintOnSave: false,

    // ✅ 添加 CSS 配置
    css: {
        loaderOptions: {
            sass: {
                additionalData: `@import "@/assets/global.css";`
            }
        }
    },

    // ✅ 保留原有的 devServer 配置
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