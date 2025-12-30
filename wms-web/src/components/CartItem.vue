<template>
  <div class="cart-item">
    <div class="item-left">
      <div class="item-image">
        <!-- 使用 v-if 避免默认图片404 -->
        <img
            v-if="item.image && item.image !== '' && item.image !== '/default-book.jpg'"
            :src="getImageUrl(item.image)"
            :alt="item.bookName"
            loading="lazy"
            @error="handleImageError"
        />
        <div v-else class="image-placeholder">
          📚
          <span class="placeholder-text">暂无图片</span>
        </div>
      </div>
      <div class="item-info">
        <h4 class="item-title">{{ item.bookName || '未知图书' }}</h4>
        <p class="item-author" v-if="item.author">作者：{{ item.author }}</p>
        <div class="item-price">
          ¥{{ item.price ? item.price.toFixed(2) : '0.00' }}
        </div>
        <div class="item-quantity">
          数量：{{ item.quantity }} 件
        </div>
        <div class="item-stock" v-if="item.stock !== undefined">
          库存：{{ item.stock }} 本
        </div>
      </div>
    </div>

    <div class="item-right">
      <button class="btn-remove" @click="removeFromCart" :disabled="isRemoving">
        {{ isRemoving ? '移除中...' : '移除' }}
      </button>
    </div>

    <!-- 调试信息（开发阶段显示） -->
    <div v-if="false" class="debug-info" style="font-size: 10px; color: #999;">
      图片字段: {{ item.image }}
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CartItem',
  props: {
    item: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      isRemoving: false,
      imageError: false
    }
  },
  methods: {
    getImageUrl(url) {
      // 如果 url 为空或无效，返回在线占位图
      if (!url || url === 'null' || url === 'undefined' || url.trim() === '' || url === '/default-book.jpg') {
        return 'https://via.placeholder.com/80x100?text=No+Image';
      }

      // 如果是完整 URL，直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }

      // 相对路径，添加基础 URL
      const baseUrl = 'http://localhost:8090';

      // 确保路径格式正确
      if (url.startsWith('/uploads/') || url.startsWith('/images/') || url.startsWith('/static/')) {
        return baseUrl + url;
      }

      // 如果只是文件名，添加默认路径前缀
      if (!url.includes('/')) {
        return baseUrl + '/uploads/' + url;
      }

      // 确保路径以 / 开头
      return baseUrl + (url.startsWith('/') ? url : '/' + url);
    },

    handleImageError(event) {
      console.log('图片加载失败，使用默认图片:', this.item.image);
      event.target.src = 'https://via.placeholder.com/80x100?text=No+Image';
    },

    async removeFromCart() {
      const userStr = localStorage.getItem('user')
      if (!userStr) {
        alert('请先登录')
        return
      }

      let user
      try {
        user = JSON.parse(userStr)
      } catch (e) {
        alert('用户信息错误')
        return
      }

      this.isRemoving = true
      try {
        const response = await axios.delete('http://localhost:8090/cart/remove', {
          params: {
            userId: user.id,
            bookId: this.item.bookId
          }
        })

        if (response.data.code === 200) {
          this.$emit('remove', this.item)
          alert('已移除商品')
        } else {
          alert(response.data.msg || '移除失败')
        }
      } catch (error) {
        console.error('移除失败:', error)
        alert('网络错误，请稍后重试')
      } finally {
        this.isRemoving = false
      }
    }
  }
}
</script>

<style scoped>
.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
  transition: all 0.3s ease;
}

.cart-item:hover {
  background: #fafafa;
}

.item-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.item-image {
  width: 80px;
  height: 100px;
  margin-right: 16px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 4px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 图片占位符样式 */
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #666;
  border-radius: 4px;
  border: 1px dashed #ddd;
}

.placeholder-text {
  font-size: 10px;
  margin-top: 5px;
  color: #999;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-author {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.item-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
  margin-bottom: 4px;
}

.item-quantity, .item-stock {
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}

.item-right {
  margin-left: 16px;
}

.btn-remove {
  padding: 6px 16px;
  background: transparent;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-remove:hover {
  background: #ff4d4f;
  color: white;
}

.btn-remove:disabled {
  background: #f5f5f5;
  color: #d9d9d9;
  border-color: #d9d9d9;
  cursor: not-allowed;
}
</style>