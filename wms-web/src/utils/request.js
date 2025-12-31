import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
    baseURL: 'http://localhost:8090', // 根据你的后端端口修改
    timeout: 10000 // 10秒超时
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        // 根据你的后端返回格式调整
        if (res.code === 200) {
            return res.data || res
        } else {
            // 业务错误
            ElMessage({
                message: res.msg || '请求失败',
                type: 'error',
                duration: 3000
            })
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    error => {
        // HTTP错误
        let message = '请求失败'

        if (error.response) {
            switch (error.response.status) {
                case 400:
                    message = '请求错误'
                    break
                case 401:
                    message = '未授权，请重新登录'
                    // 清除token并跳转到登录页
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    localStorage.removeItem('userType')
                    window.location.href = '/login'
                    break
                case 403:
                    message = '拒绝访问'
                    break
                case 404:
                    message = '请求地址不存在'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                default:
                    message = `连接错误 ${error.response.status}`
            }
        } else if (error.request) {
            message = '网络连接失败，请检查网络'
        } else {
            message = error.message
        }

        ElMessage({
            message: message,
            type: 'error',
            duration: 3000
        })

        return Promise.reject(error)
    }
)

export default request