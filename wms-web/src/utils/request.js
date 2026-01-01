// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
    baseURL: 'http://localhost:8999', // 后端端口：8999
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 打印请求信息便于调试
        console.log('🚀 发送请求:', {
            url: config.baseURL + config.url,
            method: config.method,
            params: config.params,
            data: config.data
        })

        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    },
    (error) => {
        console.error('❌ 请求拦截器错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        console.log('✅ 收到响应:', {
            url: response.config.url,
            status: response.status,
            data: response.data
        })

        // 直接返回响应数据
        return response.data
    },
    (error) => {
        console.error('❌ 响应错误详情:', {
            message: error.message,
            code: error.code,
            config: error.config,
            response: error.response
        })

        let message = '请求失败'

        // 网络错误
        if (error.code === 'ERR_NETWORK') {
            message = '网络连接失败，请检查：\n1. 后端服务是否启动\n2. 网络连接是否正常\n3. 端口号是否正确'
        }
        // 超时错误
        else if (error.code === 'ECONNABORTED') {
            message = '请求超时，请稍后重试'
        }
        // HTTP错误
        else if (error.response) {
            const status = error.response.status
            const data = error.response.data

            switch (status) {
                case 400:
                    message = data.msg || '请求参数错误'
                    break
                case 401:
                    message = '登录已过期，请重新登录'
                    // 清除本地存储
                    localStorage.removeItem('token')
                    localStorage.removeItem('user')
                    // 跳转到登录页
                    if (window.location.pathname !== '/login') {
                        window.location.href = '/login'
                    }
                    break
                case 403:
                    message = '没有权限访问该资源'
                    break
                case 404:
                    message = `请求的接口不存在: ${error.config.url}`
                    break
                case 500:
                    message = '服务器内部错误，请联系管理员'
                    break
                default:
                    message = data.msg || `请求失败，状态码: ${status}`
            }
        }
        // 请求发送失败
        else if (error.request) {
            message = '请求发送失败，请检查网络连接'
        }
        // 其他错误
        else {
            message = error.message
        }

        // 显示错误消息
        ElMessage({
            message: message,
            type: 'error',
            duration: 5000,
            showClose: true
        })

        return Promise.reject(error)
    }
)

export default request