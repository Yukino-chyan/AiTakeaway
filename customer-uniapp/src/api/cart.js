import http from '@/utils/request'

export const getCartList = () => http.get('/cart/list')
export const addToCart = data => http.post('/cart/add', data)
export const updateCartQuantity = data => http.put('/cart/update-quantity', data)
export const removeCartItem = cartId => http.delete(`/cart/${cartId}`)
export const clearCart = () => http.delete('/cart/clear')
export const createOrderFromCart = merchantId => http.post('/cart/create-order', { merchantId })
