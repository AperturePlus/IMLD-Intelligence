import { createRouter, createWebHistory } from 'vue-router'
import { authGuard } from './guards/authGuard'
import { routes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(authGuard)

export default router
