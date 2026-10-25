import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import LoginView from '../views/login/LoginView.vue';
import ChatView from '../views/chat/ChatView.vue';
import AdminView from '../views/admin/AdminView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/chat'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (!to.meta.requiresAuth) {
    return true;
  }
  if (!authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'chat' };
  }
  return true;
});

router.afterEach((to) => {
  const authStore = useAuthStore();
  if (to.name === 'login' && authStore.isAuthenticated) {
    router.replace(authStore.isAdmin ? '/admin' : '/chat');
  }
});

export default router;
