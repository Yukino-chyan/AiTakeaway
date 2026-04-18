import Request from 'luch-request'

const http = new Request({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

http.interceptors.request.use(config => {
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = { ...config.header, Authorization: `Bearer ${token}` }
  }
  return config
})

http.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code !== 200) {
      uni.showToast({ title: data.message || '请求失败', icon: 'none' })
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  err => {
    if (err.statusCode === 401) {
      uni.removeStorageSync('token')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('role')
      uni.reLaunch({ url: '/pages/login/login' })
      uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
    } else {
      uni.showToast({ title: '网络异常', icon: 'none' })
    }
    return Promise.reject(err)
  }
)

export default http
