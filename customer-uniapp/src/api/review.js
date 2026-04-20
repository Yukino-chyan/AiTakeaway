import http from '@/utils/request'

export const submitReview = (data) => http.post('/review', data)
export const getMerchantReviews = (merchantId) => http.get(`/review/merchant/${merchantId}`)
export const getOrderReview = (orderId) => http.get(`/review/order/${orderId}`)
export const getMerchantRating = (merchantId) => http.get(`/review/merchant/${merchantId}/rating`)
