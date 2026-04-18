import http from '@/utils/request'

export const getMerchantList = () => http.get('/merchant/list')
export const searchMerchant = keyword => http.get('/merchant/search', { params: { keyword } })
export const getMerchantDetail = id => http.get(`/merchant/detail/${id}`)
