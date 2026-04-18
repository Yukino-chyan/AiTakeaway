import http from '@/utils/request'

export const getDishList = merchantId => http.get(`/dish/list/${merchantId}`)
