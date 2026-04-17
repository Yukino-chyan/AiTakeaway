import request from './request'

export const getMerchantOrders = status =>
  request.get('/order/merchant-list', { params: status != null ? { status } : {} })
export const getOrderDetail = id => request.get(`/order/${id}`)
export const confirmOrder = id => request.put(`/order/${id}/confirm`)
export const deliverOrder = id => request.put(`/order/${id}/deliver`)
export const completeOrder = id => request.put(`/order/${id}/complete`)
