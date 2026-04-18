import http from '@/utils/request'

export const getMyOrders = () => http.get('/order/my-list')
export const getOrderDetail = id => http.get(`/order/${id}`)
export const cancelOrder = id => http.put(`/order/${id}/cancel`)
