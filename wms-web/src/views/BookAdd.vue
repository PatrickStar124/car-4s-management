<template>
  <div class="book-add">
    <header class="header">
      <h1>📖 新增书籍</h1>
      <div class="header-actions">
        <button @click="goBack" class="back-btn">返回图书列表</button>
        <div class="user-info" v-if="user">
          <span>当前登录：{{ user.name }}</span>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
      </div>
    </header>

    <div class="form-container">
      <form class="book-form" @submit.prevent="submitBook">
        <!-- 图书封面上传 -->
        <div class="form-item">
          <label class="form-label">图书封面：</label>
          <div class="upload-container">
            <div class="preview-img" v-if="imagePreview">
              <img :src="imagePreview" alt="封面预览" />
              <button type="button" class="remove-img" @click="clearImage">移除</button>
            </div>
            <div class="upload-btn" v-else @click="$refs.fileInput.click()">
              <input
                  type="file"
                  accept="image/*"
                  ref="fileInput"
                  @change="handleImageUpload"
                  style="display: none"
              />
              <span>点击上传封面</span>
            </div>
            <p class="tips">支持jpg/png格式，建议尺寸200x300</p>
          </div>
        </div>

        <!-- 书名 -->
        <div class="form-item">
          <label class="form-label">图书名称：</label>
          <input
              type="text"
              v-model="bookForm.name"
              class="form-input"
              placeholder="请输入图书名称"
              required
          />
        </div>

        <!-- 作者 -->
        <div class="form-item">
          <label class="form-label">作者：</label>
          <input
              type="text"
              v-model="bookForm.author"
              class="form-input"
              placeholder="请输入作者名称"
              required
          />
        </div>

        <!-- 价格 -->
        <div class="form-item">
          <label class="form-label">价格（元）：</label>
          <input
              type="number"
              v-model.number="bookForm.price"
              class="form-input"
              placeholder="请输入图书价格"
              min="0.01"
              step="0.01"
              required
          />
        </div>

        <!-- 库存 -->
        <div class="form-item">
          <label class="form-label">库存：</label>
          <input
              type="number"
              v-model.number="bookForm.stock"
              class="form-input"
              placeholder="请输入库存数量"
              min="0"
              required
          />
        </div>

        <!-- 描述：保留模板，传参时自动忽略（后端无对应属性） -->
        <div class="form-item">
          <label class="form-label">图书描述：</label>
          <textarea
              v-model="bookForm.description"
              class="form-textarea"
              placeholder="请输入图书简介（选填）"
              rows="4"
          ></textarea>
        </div>

        <!-- 提交按钮 -->
        <div class="form-submit">
          <button
              type="submit"
              class="submit-btn"
              :disabled="isSubmitting"
          >
            {{ isSubmitting ? '提交中...' : '新增书籍' }}
          </button>
          <button
              type="reset"
              class="reset-btn"
              @click="resetForm"
          >
            重置表单
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'BookAdd',
  data() {
    return {
      user: null,
      bookForm: {
        name: '',
        author: '',
        price: 0,
        stock: 0,
        description: '',
        image: ''
      },
      imagePreview: '',
      isSubmitting: false,
      file: null
    }
  },
  created() {
    this.loadUser()
  },
  methods: {
    loadUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          this.user = JSON.parse(userStr)
        } catch (e) {
          console.error('解析用户信息失败:', e)
          this.$router.push('/login')
        }
      } else {
        this.$router.push('/login')
      }
    },

    handleImageUpload(e) {
      const file = e.target.files[0]
      if (!file) return

      const acceptTypes = ['image/jpeg', 'image/png', 'image/jpg']
      if (!acceptTypes.includes(file.type)) {
        alert('仅支持jpg/png格式图片')
        return
      }

      if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过2M')
        return
      }

      this.file = file
      this.imagePreview = URL.createObjectURL(file)
    },

    clearImage() {
      this.imagePreview = ''
      this.file = null
      this.bookForm.image = ''
      this.$refs.fileInput.value = ''
    },

    resetForm() {
      this.bookForm = {
        name: '',
        author: '',
        price: 0,
        stock: 0,
        description: '',
        image: ''
      }
      this.clearImage()
    },

    async submitBook() {
      if (!this.bookForm.name || !this.bookForm.author || !this.bookForm.price || !this.bookForm.stock) {
        alert('请填写必填字段（名称/作者/价格/库存）')
        return
      }

      this.isSubmitting = true
      try {
        let imageUrl = ''
        if (this.file) {
          const formData = new FormData()
          formData.append('file', this.file)

          const uploadRes = await axios.post('http://localhost:8090/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })

          if (uploadRes.data.code !== 200) {
            alert('封面上传失败：' + uploadRes.data.msg)
            this.isSubmitting = false
            return
          }
          imageUrl = uploadRes.data.data
          console.log('图片上传成功，地址：', imageUrl)
        }

        const bookRes = await axios.post('http://localhost:8090/book/save', {
          name: this.bookForm.name,
          author: this.bookForm.author,
          price: this.bookForm.price,
          stock: this.bookForm.stock,
          imageUrl: imageUrl
        })

        console.log('书籍新增请求返回结果：', bookRes.data)
        if (bookRes.data.code === 200) {
          alert('书籍新增成功！')
          this.resetForm()
          // 关键修改：带刷新参数跳转，强制更新图书列表
          this.$router.push({ path: '/booklist', query: { refresh: Date.now() } })
        } else {
          alert('新增失败：' + bookRes.data.msg)
        }
      } catch (error) {
        console.error('新增书籍失败详情:', error)
        if (error.message.includes('Network Error')) {
          alert('后端服务未启动，请检查后端端口是否为8090')
        } else if (error.response) {
          alert('接口请求失败：' + (error.response.data.msg || '未知错误'))
        } else {
          alert('操作失败，请稍后重试')
        }
      } finally {
        this.isSubmitting = false
      }
    },

    goBack() {
      this.$router.push('/booklist')
    },

    logout() {
      localStorage.removeItem('user')
      this.user = null
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.book-add {
  max-width: 800px;
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

.back-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn {
  padding: 8px 16px;
  background: #f5222d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.book-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-input, .form-textarea {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  resize: vertical;
}

.upload-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-img {
  position: relative;
  width: 200px;
  height: 300px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}

.preview-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  background: rgba(255,255,255,0.8);
  color: #ff4d4f;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.upload-btn {
  width: 200px;
  height: 300px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.upload-btn input {
  display: none !important;
}

.tips {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.form-submit {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.submit-btn {
  padding: 10px 30px;
  background: #52c41a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.submit-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.reset-btn {
  padding: 10px 30px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
</style>