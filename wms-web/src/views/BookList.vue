<template>
  <div class="book-list">
    <header class="header">
      <h1>📚 全部图书</h1>
      <div class="header-actions">
        <button v-if="user" @click="goToBookAdd" class="add-book-btn">+ 新增书籍</button>

        <button @click="goHome" class="home-btn">返回首页</button>
        <button @click="goToCart" class="cart-btn">🛒 购物车</button>
        <div class="user-info" v-if="user">
          <span>欢迎，{{ user.name }}</span>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
        <div class="user-info" v-else>
          <button @click="goToLogin" class="login-btn">登录</button>
          <button @click="goToRegister" class="register-btn">注册</button>
        </div>
      </div>
    </header>

    <div class="book-filter">
      <input
          v-model="searchKeyword"
          placeholder="搜索图书名称或作者..."
          class="search-input"
          @input="filterBooks"
      />
      <button @click="refreshBooks" class="refresh-btn">刷新</button>
    </div>

    <!-- 调试信息面板 -->
    <div class="debug-panel" v-if="showDebug">
      <h4>调试信息</h4>
      <div class="debug-content">
        <div>图书总数: {{ books.length }}</div>
        <div>过滤后: {{ filteredBooks.length }}</div>
        <div v-if="books.length > 0">
          <div>第一本图书字段:</div>
          <pre>{{ JSON.stringify(getFirstBookFields(), null, 2) }}</pre>
        </div>
      </div>
      <button @click="toggleDebug" class="debug-toggle">隐藏调试</button>
    </div>

    <div class="content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="filteredBooks.length === 0" class="empty">
        {{ searchKeyword ? '没有找到相关图书' : '暂无图书' }}
        <div v-if="!searchKeyword && books.length === 0">
          <p>可能的原因：</p>
          <ul>
            <li>1. 后端服务未启动（端口8090）</li>
            <li>2. 数据库中没有图书数据</li>
            <li>3. 网络连接问题</li>
          </ul>
          <button @click="testBackendConnection" class="test-btn">测试后端连接</button>
        </div>
      </div>
      <div v-else class="books-grid">
        <BookCard
            v-for="book in filteredBooks"
            :key="book.id"
            :book="book"
            @cart-updated="handleCartUpdate"
        />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import BookCard from '@/components/BookCard.vue'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'
import { watch } from 'vue'
import { useRoute } from 'vue-router'

export default {
  name: 'BookList',
  components: {
    BookCard
  },
  setup() {
    const cartStore = useCartStore()
    const userStore = useUserStore()
    const { user } = storeToRefs(userStore)
    const route = useRoute()

    // 监听用户状态变化，登录后自动初始化购物车
    watch(
        user,
        (newUser) => {
          if (newUser) {
            console.log('用户已登录，初始化购物车，用户ID:', newUser.id)
            cartStore.initCartList(newUser.id)
          }
        },
        { immediate: true }
    )

    return {
      cartStore,
      userStore,
      user,
      route
    }
  },
  data() {
    return {
      books: [],
      filteredBooks: [],
      loading: true,
      searchKeyword: '',
      showDebug: false // 控制调试信息显示
    }
  },
  created() {
    this.fetchBooks()
    // 监听路由刷新参数，重新获取图书列表
    this.routeWatcher = watch(
        () => this.route.query.refresh,
        () => {
          console.log('检测到路由刷新参数，重新获取图书列表')
          this.fetchBooks()
        }
    )
  },
  beforeUnmount() {
    // 销毁监听器，防止内存泄漏
    if (this.routeWatcher) {
      this.routeWatcher()
    }
  },
  methods: {
    async fetchBooks() {
      this.loading = true
      console.log('开始获取图书列表...')

      try {
        const response = await axios.get('http://localhost:8090/book/list')

        console.log('图书列表接口返回数据:', response.data)

        if (response.data.code === 200) {
          // 处理图书数据，确保字段完整
          this.books = response.data.data.map(book => {
            console.log('单个图书对象:', book)

            // 标准化图书数据，确保所有必要字段都有值
            return {
              id: book.id || 0,
              name: book.name || book.bookName || '未知图书',
              author: book.author || '未知作者',
              price: book.price || 0,
              stock: book.stock || 0,
              imageUrl: book.imageUrl || book.image || book.img || book.cover || '',
              description: book.description || '暂无描述'
            }
          })

          console.log('处理后的图书列表:', this.books)

          if (this.books.length > 0) {
            console.log('第一本图书详情:')
            console.log('- ID:', this.books[0].id)
            console.log('- 书名:', this.books[0].name)
            console.log('- 作者:', this.books[0].author)
            console.log('- 价格:', this.books[0].price)
            console.log('- 库存:', this.books[0].stock)
            console.log('- 图片URL:', this.books[0].imageUrl)
            console.log('- 完整图片URL:', this.getFullImageUrl(this.books[0].imageUrl))
          }

          this.filteredBooks = [...this.books]
        } else {
          console.error('获取图书失败，错误码:', response.data.code, '消息:', response.data.msg)
          alert('获取图书失败: ' + response.data.msg)
        }
      } catch (error) {
        console.error('获取图书失败:', error)

        if (error.code === 'ERR_NETWORK') {
          alert('网络错误: 请确保后端服务正在运行（端口8090）')
        } else if (error.response) {
          alert('接口请求失败: ' + (error.response.data?.msg || error.message))
        } else {
          alert('操作失败，请稍后重试: ' + error.message)
        }
      } finally {
        this.loading = false
      }
    },

    // 获取完整图片URL
    getFullImageUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      return 'http://localhost:8090' + url
    },

    // 获取第一本图书的字段信息（用于调试）
    getFirstBookFields() {
      if (this.books.length === 0) return {}
      const book = this.books[0]
      return {
        id: book.id,
        name: book.name,
        author: book.author,
        price: book.price,
        stock: book.stock,
        imageUrl: book.imageUrl,
        hasDescription: !!book.description
      }
    },

    // 测试后端连接
    async testBackendConnection() {
      try {
        console.log('测试后端连接...')
        const response = await axios.get('http://localhost:8090/book/list', { timeout: 5000 })
        console.log('后端连接测试结果:', response.status)
        if (response.status === 200) {
          alert('后端连接正常！返回数据: ' + JSON.stringify(response.data))
          this.fetchBooks() // 重新获取数据
        }
      } catch (error) {
        console.error('后端连接测试失败:', error)
        alert('后端连接失败: ' + error.message)
      }
    },

    filterBooks() {
      if (!this.searchKeyword.trim()) {
        this.filteredBooks = [...this.books]
        return
      }

      const keyword = this.searchKeyword.toLowerCase()
      console.log('搜索关键词:', keyword)

      this.filteredBooks = this.books.filter(book => {
        const nameMatch = book.name && book.name.toLowerCase().includes(keyword)
        const authorMatch = book.author && book.author.toLowerCase().includes(keyword)

        if (nameMatch || authorMatch) {
          console.log('匹配到图书:', book.name, '作者:', book.author)
        }

        return nameMatch || authorMatch
      })

      console.log('过滤后图书数量:', this.filteredBooks.length)
    },

    refreshBooks() {
      console.log('刷新图书列表...')
      this.searchKeyword = ''
      this.fetchBooks()
    },

    toggleDebug() {
      this.showDebug = !this.showDebug
    },

    handleCartUpdate() {
      console.log('购物车已更新')
    },

    goHome() {
      this.$router.push('/')
    },

    goToCart() {
      if (!this.user) {
        alert('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push('/cart')
    },

    goToLogin() {
      this.$router.push('/login')
    },

    goToRegister() {
      this.$router.push('/register')
    },

    logout() {
      this.userStore.clearUser()
      this.cartStore.clearCart()
      alert('已退出登录')
      this.$router.push('/login')
    },

    goToBookAdd() {
      this.$router.push('/bookadd')
    }
  }
}
</script>

<style scoped>
.book-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.add-book-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  background: #52c41a;
  color: white;
}

.user-info {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-left: 10px;
}

.home-btn, .cart-btn, .login-btn, .register-btn, .logout-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.home-btn {
  background: #1890ff;
  color: white;
}

.cart-btn {
  background: #faad14;
  color: white;
}

.login-btn {
  background: #52c41a;
  color: white;
}

.register-btn {
  background: #722ed1;
  color: white;
}

.logout-btn {
  background: #f5222d;
  color: white;
}

.book-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.refresh-btn {
  padding: 8px 16px;
  background: #13c2c2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 调试面板样式 */
.debug-panel {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-family: monospace;
  font-size: 12px;
}

.debug-panel h4 {
  margin: 0 0 10px 0;
  color: #495057;
}

.debug-content {
  margin-bottom: 10px;
  max-height: 200px;
  overflow-y: auto;
  background: white;
  padding: 10px;
  border-radius: 4px;
}

.debug-content pre {
  margin: 10px 0;
  padding: 10px;
  background: #f1f3f5;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.debug-toggle, .test-btn {
  padding: 6px 12px;
  font-size: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.debug-toggle {
  background: #6c757d;
  color: white;
}

.test-btn {
  background: #ffc107;
  color: #212529;
  margin-top: 10px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.loading, .empty {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #999;
  background: white;
  border-radius: 8px;
}

.empty ul {
  text-align: left;
  display: inline-block;
  margin: 10px auto;
  padding-left: 20px;
}

.empty li {
  margin-bottom: 5px;
  font-size: 14px;
}
</style>