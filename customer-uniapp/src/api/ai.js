import http from '@/utils/request'

export const sendAiMessage = message => http.post('/ai/chat', { message })
