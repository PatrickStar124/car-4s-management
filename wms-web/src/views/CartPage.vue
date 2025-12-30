<template>
  <div class="cart-page">
    <header class="header">
      <h1>🛒 我的购物车</h1>
      <div class="header-actions">
        <button @click="goHome" class="home-btn">返回首页</button>
        <button @click="goToBookList" class="booklist-btn">继续购物</button>
        <div class="cart-summary" v-if="userStore.user">
          共 {{ cartStore.cartTotalQuantity }} 件商品
        </div>
        <div class="user-info" v-if="userStore.user">
          <span>{{ userStore.user.name }}</span>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
      </div>
    </header>

    <div v-if="!userStore.user" class="not-logged-in">
      <div class="login-prompt">
        <h3>请先登录</h3>
        <p>登录后查看和管理购物车</p>
        <div class="prompt-actions">
          <button @click="goToLogin" class="login-btn">立即登录</button>
          <button @click="goHome" class="continue-btn">继续浏览</button>
        </div>
      </div>
    </div>

    <div v-else>
      <div v-if="cartStore.loading" class="loading">加载购物车...</div>

      <div v-else-if="cartStore.cartList.length === 0" class="empty-cart">
        <div class="empty-icon">🛒</div>
        <h3>购物车空空如也</h3>
        <p>快去挑选心仪的图书吧</p>
        <div class="empty-actions">
          <button @click="goToBookList" class="browse-btn">浏览图书</button>
          <button @click="goHome" class="home-btn">返回首页</button>
        </div>
      </div>

      <div v-else class="cart-content">
        <div class="cart-items">
          <div class="cart-header">
            <h3>商品列表</h3>
            <button @click="clearCart" class="clear-btn" :disabled="clearingAll">
              {{ clearingAll ? '清空中...' : '清空购物车' }}
            </button>
          </div>

          <div class="items-list">
            <CartItem
                v-for="item in cartStore.cartList"
                :key="item.id"
                :item="item"
                @remove="handleRemove"
            />
          </div>
        </div>

        <div class="cart-sidebar">
          <div class="order-summary">
            <h3>订单摘要</h3>
            <div class="summary-item">
              <span>商品数量</span>
              <span>{{ cartStore.cartTotalQuantity }} 件</span>
            </div>
            <div class="summary-item">
              <span>商品总价</span>
              <span class="total-price">¥{{ cartStore.cartTotalPrice }}</span>
            </div>
            <div class="summary-item discount">
              <span>优惠折扣</span>
              <span>-¥0.00</span>
            </div>
            <div class="summary-item final">
              <span>应付金额</span>
              <span class="final-price">¥{{ cartStore.cartTotalPrice }}</span>
            </div>

            <button
                class="checkout-btn"
                @click="checkout"
                :disabled="checkoutProcessing || cartStore.cartList.length === 0"
            >
              {{ checkoutProcessing ? '结算中...' : `去结算 (¥${cartStore.cartTotalPrice})` }}
            </button>

            <div class="secure-checkout">
              <span>🔒 安全支付保障</span>
            </div>
          </div>

          <div class="cart-tips">
            <h4>购物须知</h4>
            <ul>
              <li>购物车商品最多保存30天</li>
              <li>结算前请确认商品信息</li>
              <li>支持在线支付和货到付款</li>
              <li>有任何问题请联系客服</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import CartItem from '@/components/CartItem.vue'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'

export default {
  name: 'CartPage',
  components: {
    CartItem
  },
  setup() {
    const cartStore = useCartStore()
    const userStore = useUserStore()
    const { cartList, loading, cartTotalQuantity, cartTotalPrice } = storeToRefs(cartStore)
    const { user } = storeToRefs(userStore)

    return {
      cartStore,
      userStore,
      cartList,
      loading,
      cartTotalQuantity,
      cartTotalPrice,
      user
    }
  },
  data() {
    return {
      clearingAll: false,
      checkoutProcessing: false
    }
  },
  created() {
    if (this.userStore.user) {
      this.cartStore.initCartList(this.userStore.user.id)
    }
  },
  methods: {
    handleRemove(removedItem) {
      this.cartStore.cartList = this.cartStore.cartList.filter(item => item.id !== removedItem.id)
      this.removeCartItem(removedItem)
    },
    async removeCartItem(item) {
      try {
        await axios.delete('http://localhost:8090/cart/remove', {
          params: {
            userId: this.userStore.user.id,
            bookId: item.bookId
          }
        })
      } catch (error) {
        console.error('删除购物车商品失败:', error)
        alert('删除商品失败，请稍后重试')
        this.cartStore.initCartList(this.userStore.user.id)
      }
    },
    async clearCart() {
      if (!confirm('确定要清空购物车吗？')) return

      this.clearingAll = true
      try {
        for (const item of [...this.cartStore.cartList]) {
          await axios.delete('http://localhost:8090/cart/remove', {
            params: {
              userId: this.userStore.user.id,
              bookId: item.bookId
            }
          })
        }
        this.cartStore.clearCart()
        alert('购物车已清空')
      } catch (error) {
        console.error('清空购物车失败:', error)
        alert('清空购物车失败，请稍后重试')
        this.cartStore.initCartList(this.userStore.user.id)
      } finally {
        this.clearingAll = false
      }
    },
    async checkout() {
      // 验证用户登录
      if (!this.userStore.user) {
        alert('请先登录');
        this.goToLogin();
        return;
      }

      // 验证购物车不为空
      if (this.cartStore.cartList.length === 0) {
        alert('购物车为空');
        return;
      }

      // 检查库存（前端验证）
      const outOfStockItems = this.cartStore.cartList.filter(item =>
          item.quantity > (item.stock || 0)
      );

      if (outOfStockItems.length > 0) {
        const itemNames = outOfStockItems.map(item =>
            `• ${item.bookName}（库存：${item.stock}本，需要：${item.quantity}本）`
        ).join('\n');

        alert(`⚠️ 库存不足，无法结算：\n${itemNames}\n\n请调整数量或移除商品后重试。`);
        return;
      }

      // 显示结算详情
      const checkoutDetails = this.cartStore.cartList.map(item =>
          `${item.bookName} × ${item.quantity} = ¥${(item.price * item.quantity).toFixed(2)}`
      ).join('\n');

      const confirmMessage = `💰 确定要结算以下商品吗？\n\n${checkoutDetails}\n\n📦 总计 ${this.cartStore.cartTotalQuantity} 件商品\n💵 应付金额：¥${this.cartStore.cartTotalPrice}`;

      if (!confirm(confirmMessage)) {
        return;
      }

      // 禁用结算按钮，显示处理中
      this.checkoutProcessing = true;

      try {
        const result = await this.cartStore.checkout(this.userStore.user.id);

        if (result.success) {
          // 结算成功，显示详细结果
          let successDetails = '';
          if (result.data && result.data.checkoutItems) {
            successDetails = result.data.checkoutItems.map(item =>
                `${item.bookName} × ${item.quantity} = ¥${item.total}`
            ).join('\n');
          } else {
            successDetails = '成功结算所有商品';
          }

          const successMessage = `✅ ${result.message}\n\n${successDetails}\n\n📦 已结算 ${result.data?.totalQuantity || this.cartStore.cartTotalQuantity} 件商品\n💵 实付金额：¥${result.data?.totalPrice || this.cartStore.cartTotalPrice}`;

          alert(successMessage);

          // 自动刷新页面数据
          await this.cartStore.initCartList(this.userStore.user.id);
        }
      } catch (error) {
        console.error('结算失败:', error);

        // 根据错误类型显示不同提示
        if (error.response && error.response.data) {
          const errorData = error.response.data;
          if (errorData.code === 400 || (errorData.msg && errorData.msg.includes('库存'))) {
            alert(`❌ 结算失败：${errorData.msg}\n\n请调整商品数量后重试。`);
          } else {
            alert(`❌ 结算失败：${errorData.msg}`);
          }
        } else if (error.message) {
          alert(`❌ 结算失败：${error.message}`);
        } else {
          alert('❌ 网络错误，请稍后重试');
        }

        // 重新加载购物车数据
        await this.cartStore.initCartList(this.userStore.user.id);
      } finally {
        this.checkoutProcessing = false;
      }
    },
    goHome() {
      this.$router.push('/')
    },
    goToBookList() {
      this.$router.push('/booklist')
    },
    goToLogin() {
      this.$router.push('/login')
    },
    logout() {
      this.userStore.clearUser()
      this.cartStore.clearCart()
      alert('已退出登录')
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.cart-page {
  max-width: 1400px;
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
  gap: 15px;
  align-items: center;
}

.user-info {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-left: 10px;
}

.cart-summary {
  padding: 6px 12px;
  background: #f0f7ff;
  color: #1890ff;
  border-radius: 15px;
  font-size: 14px;
  margin-right: 10px;
}

.home-btn, .booklist-btn, .login-btn, .continue-btn, .logout-btn,
.browse-btn, .clear-btn, .checkout-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.home-btn {
  background: #1890ff;
  color: white;
}

.booklist-btn {
  background: #52c41a;
  color: white;
}

.login-btn {
  background: #1890ff;
  color: white;
}

.continue-btn {
  background: #f5f5f5;
  color: #666;
}

.logout-btn {
  background: #f5222d;
  color: white;
}

.browse-btn {
  background: #1890ff;
  color: white;
  padding: 10px 20px;
}

.clear-btn {
  background: #ff4d4f;
  color: white;
}

.clear-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.checkout-btn {
  width: 100%;
  padding: 14px;
  background: #52c41a;
  color: white;
  font-size: 16px;
  font-weight: 500;
  margin-top: 20px;
}

.checkout-btn:hover {
  background: #73d13d;
}

.checkout-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  opacity: 0.6;
}

.not-logged-in {
  padding: 40px;
  background: white;
  border-radius: 8px;
  text-align: center;
}

.login-prompt {
  max-width: 400px;
  margin: 0 auto;
}

.login-prompt h3 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #333;
}

.login-prompt p {
  color: #666;
  margin-bottom: 25px;
}

.prompt-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.empty-cart {
  padding: 60px 40px;
  background: white;
  border-radius: 8px;
  text-align: center;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-cart h3 {
  font-size: 22px;
  margin-bottom: 10px;
  color: #333;
}

.empty-cart p {
  color: #666;
  margin-bottom: 30px;
}

.empty-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #999;
  background: white;
  border-radius: 8px;
}

.cart-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 30px;
}

.cart-items {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.cart-header h3 {
  font-size: 18px;
  color: #333;
}

.items-list {
  min-height: 200px;
}

.cart-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.order-summary {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.order-summary h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #666;
}

.summary-item.discount {
  color: #52c41a;
}

.summary-item.final {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  font-weight: 600;
  color: #333;
}

.total-price, .final-price {
  font-weight: bold;
  color: #ff4d4f;
}

.final-price {
  font-size: 20px;
}

.secure-checkout {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  color: #52c41a;
  font-size: 12px;
}

.cart-tips {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.cart-tips h4 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}

.cart-tips ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.cart-tips li {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  padding-left: 15px;
  position: relative;
}

.cart-tips li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #1890ff;
}
</style>