import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/trips',
    children: [
      {
        path: 'trips',
        name: 'Trips',
        component: () => import('@/views/Trips.vue'),
        meta: { title: '我的旅程', requiresAuth: true }
      },
      {
        path: 'trips/create',
        name: 'CreateTrip',
        component: () => import('@/views/TripForm.vue'),
        meta: { title: '创建旅程', requiresAuth: true }
      },
      {
        path: 'trips/:id/edit',
        name: 'EditTrip',
        component: () => import('@/views/TripForm.vue'),
        meta: { title: '编辑旅程', requiresAuth: true }
      },
      {
        path: 'trips/:id',
        name: 'TripDetail',
        component: () => import('@/views/TripDetail.vue'),
        meta: { title: '旅程详情', requiresAuth: true }
      },
      {
        path: 'trips/:tripId/journals/create',
        name: 'CreateJournal',
        component: () => import('@/views/JournalForm.vue'),
        meta: { title: '写日记', requiresAuth: true }
      },
      {
        path: 'trips/:tripId/journals/:id/edit',
        name: 'EditJournal',
        component: () => import('@/views/JournalForm.vue'),
        meta: { title: '编辑日记', requiresAuth: true }
      },
      {
        path: 'trips/:id/review',
        name: 'TripReview',
        component: () => import('@/views/TripReview.vue'),
        meta: { title: '旅程回顾', requiresAuth: true }
      },
      {
        path: 'shared/:token',
        name: 'SharedTrip',
        component: () => import('@/views/SharedTrip.vue'),
        meta: { title: '分享的旅程', requiresAuth: false }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/Tags.vue'),
        meta: { title: '标签管理', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 旅行日记` : '旅行日记'
  
  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && isAuthenticated) {
    next({ name: 'Trips' })
  } else {
    next()
  }
})

export default router
