import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/shop',
    children: [
      {
        path: 'shop',
        component: () => import('@/views/shop/ShopView.vue'),
      },
      {
        path: 'dish',
        component: () => import('@/views/dish/DishView.vue'),
      },
      {
        path: 'order',
        component: () => import('@/views/order/OrderView.vue'),
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) return '/login'
  if (to.meta.guest && auth.isLoggedIn) return '/'
})

export default router
