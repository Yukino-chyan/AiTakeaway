import request from './request'

export const getMyShop = () => request.get('/merchant/my-shop')
export const createShop = data => request.post('/merchant/create', data)
export const updateShop = data => request.put('/merchant/update', data)
export const updateStatus = (id, status) =>
  request.put('/merchant/status', null, { params: { id, status } })
