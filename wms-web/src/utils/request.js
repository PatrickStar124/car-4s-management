import axios from 'axios'
import { ElMessage } from 'element-plus'
import store from '@/store'

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

        // 从store获取token
        const token = store.state.user.token
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

        const result = response.data

        // 处理文件流等特殊响应
        const contentType = response.headers['content-type']
        if (contentType && (contentType.includes('application/octet-stream') ||
            contentType.includes('application/vnd.ms-excel'))) {
            return response
        }

        // 根据后端Result格式处理
        if (result && typeof result === 'object') {
            // 如果后端返回了标准的Result格式
            if (result.code === 200 || result.code === 0) {
                return result.data !== undefined ? result.data : result
            } else if (result.code === 401) {
                // 未授权，清除用户状态
                store.dispatch('user/logout')
                ElMessage.error(result.message || '登录已过期，请重新登录')

                // 避免重复跳转
                if (window.location.pathname !== '/login') {
                    setTimeout(() => {
                        window.location.href = '/login'
                    }, 1500)
                }
                return Promise.reject(new Error(result.message || '未授权'))
            } else {
                // 其他业务错误
                const errorMsg = result.message || result.msg || '操作失败'
                ElMessage.error(errorMsg)
                return Promise.reject(new Error(errorMsg))
            }
        }

        // 如果后端返回的不是标准格式，直接返回
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
                    message = data?.message || data?.msg || '请求参数错误'
                    break
                case 401:
                    message = '登录已过期，请重新登录'
                    // 清除用户状态
                    store.dispatch('user/logout')
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
                    message = data?.message || data?.msg || '服务器内部错误，请联系管理员'
                    break
                default:
                    message = data?.message || data?.msg || `请求失败，状态码: ${status}`
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