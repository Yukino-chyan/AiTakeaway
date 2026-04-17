import request from './request'

export const getMyDishList = () => request.get('/dish/my-list')
export const createDish = data => request.post('/dish/create', data)
export const updateDish = data => request.put('/dish/update', data)
export const updateDishStatus = (id, status) =>
  request.put('/dish/status', null, { params: { id, status } })
export const deleteDish = id => request.delete(`/dish/${id}`)
