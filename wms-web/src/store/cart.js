import { defineStore } from 'pinia'
import axios from 'axios'

export const useCartStore = defineStore('cart', {
    state: () => ({
        cartList: [],
        loading: false,
        checkoutProcessing: false
    }),

    actions: {
        async addToCart(book, userId) {
            if (!book || !userId) return

            const existingItemIndex = this.cartList.findIndex(item => item.bookId === book.id)

            if (existingItemIndex > -1) {
                const existingItem = this.cartList[existingItemIndex]
                if (existingItem.quantity >= book.stock) {
                    alert('已达到最大库存，无法继续添加')
                    return
                }

                try {
                    const response = await axios.post('http://localhost:8090/cart/add', null, {
                        params: {
                            userId: userId,
                            bookId: book.id
                        }
                    })

                    if (response.data.code === 200) {
                        this.cartList[existingItemIndex].quantity += 1
                        alert('购物车数量已更新')
                    } else {
                        alert(response.data.msg || '更新购物车数量失败')
                    }
                } catch (error) {
                    console.error('更新购物车数量失败:', error)
                    alert('网络错误，请稍后重试')
                }
            } else {
                this.loading = true
                try {
                    const response = await axios.post('http://localhost:8090/cart/add', null, {
                        params: {
                            userId: userId,
                            bookId: book.id
                        }
                    })

                    if (response.data.code === 200) {
                        // 尝试多种可能的图片字段名
                        const imageUrl = this.extractImageUrl(book);

                        this.cartList.push({
                            id: response.data.data?.id || Date.now(),
                            bookId: book.id,
                            bookName: book.name || book.bookName || '未知图书',
                            price: book.price || 0,
                            quantity: 1,
                            stock: book.stock || 0,
                            image: imageUrl,
                            author: book.author || ''
                        })

                        console.log('添加到购物车的书籍信息:', this.cartList[this.cartList.length - 1])
                        alert('已加入购物车')
                    } else {
                        alert(response.data.msg || '加入购物车失败')
                    }
                } catch (error) {
                    console.error('加入购物车失败:', error)
                    alert('网络错误，请稍后重试')
                } finally {
                    this.loading = false
                }
            }
        },

        async initCartList(userId) {
            if (!userId) return
            this.loading = true
            try {
                const response = await axios.get('http://localhost:8090/cart/list', {
                    params: { userId: userId }
                })

                console.log('购物车接口返回的完整数据:', response.data)

                if (response.data.code === 200) {
                    // 调试：显示数据结构
                    if (response.data.data && response.data.data.length > 0) {
                        console.log('第一个购物车项的结构:', response.data.data[0])
                        console.log('第一个购物车项的book对象:', response.data.data[0].book)

                        // 分析图片字段
                        const firstItem = response.data.data[0];
                        const book = firstItem.book || {};
                        console.log('book对象的字段:', Object.keys(book));

                        // 查找可能的图片字段
                        const imageFields = this.findImageFields(book);
                        if (imageFields.length > 0) {
                            console.log('找到的图片字段:', imageFields);
                        }
                    }

                    this.cartList = response.data.data.map(item => {
                        console.log('处理单个购物车项:', item)

                        const book = item.book || {}

                        // 提取图片URL（尝试多种字段名）
                        const imageUrl = this.extractImageUrl(book);

                        return {
                            id: item.id,
                            bookId: item.bookId || book.id,
                            bookName: book.name || book.bookName || item.bookName || '未知图书',
                            price: book.price || item.price || 0,
                            quantity: item.quantity || 1,
                            stock: book.stock || item.stock || 0,
                            image: imageUrl, // 使用提取的图片URL
                            author: book.author || item.author || ''
                        }
                    })

                    console.log('处理后的购物车列表:', this.cartList)
                }
            } catch (error) {
                console.error('获取购物车列表失败:', error)
            } finally {
                this.loading = false
            }
        },

        // 新增结算方法
        async checkout(userId) {
            if (!userId) {
                throw new Error('用户未登录');
            }

            if (this.cartList.length === 0) {
                throw new Error('购物车为空');
            }

            this.checkoutProcessing = true;
            try {
                const response = await axios.post('http://localhost:8090/cart/checkout', null, {
                    params: {
                        userId: userId
                    }
                });

                if (response.data.code === 200) {
                    // 结算成功，清空购物车
                    this.cartList = [];
                    console.log('结算成功:', response.data);

                    return {
                        success: true,
                        data: response.data.data,
                        message: response.data.msg
                    };
                } else {
                    throw new Error(response.data.msg || '结算失败');
                }
            } catch (error) {
                console.error('结算失败:', error);
                throw error;
            } finally {
                this.checkoutProcessing = false;
            }
        },

        // 提取图片URL的方法
        extractImageUrl(book) {
            if (!book) return '';

            // 可能的图片字段名（按优先级排序）
            const possibleFields = [
                'coverImage',      // 最常见
                'cover',           // 次常见
                'imageUrl',        // 标准命名
                'imgUrl',          // 简写
                'image',           // 简单命名
                'img',             // 更简写
                'picture',         // 英文
                'thumbnail',       // 缩略图
                'picUrl',          // 图片URL
                'photo',           // 照片
                'coverImageUrl',   // 完整URL
                'bookCover',       // 图书封面
                'bookImage'        // 图书图片
            ];

            // 查找第一个非空的图片字段
            for (const field of possibleFields) {
                if (book[field] && book[field].trim() !== '') {
                    console.log(`找到图片字段 "${field}":`, book[field]);
                    return book[field];
                }
            }

            // 如果没有找到明确的字段，尝试查找包含图片扩展名的字段
            for (const key in book) {
                const value = book[key];
                if (typeof value === 'string' &&
                    (value.includes('.jpg') || value.includes('.png') ||
                        value.includes('.jpeg') || value.includes('.gif') ||
                        value.includes('/uploads/') || value.includes('/images/'))) {
                    console.log(`通过内容识别找到图片字段 "${key}":`, value);
                    return value;
                }
            }

            return ''; // 没有找到图片0
        },

        // 查找图片字段的方法（用于调试）
        findImageFields(obj) {
            const imageFields = [];
            const possibleNames = ['image', 'img', 'cover', 'pic', 'photo', 'thumbnail'];

            for (const key in obj) {
                const value = obj[key];
                if (typeof value === 'string') {
                    // 检查字段名是否包含图片相关关键词
                    if (possibleNames.some(name => key.toLowerCase().includes(name))) {
                        imageFields.push({ field: key, value: value });
                    }
                    // 检查值是否包含图片扩展名
                    else if (value.includes('.jpg') || value.includes('.png') ||
                        value.includes('.jpeg') || value.includes('.gif')) {
                        imageFields.push({ field: key, value: value });
                    }
                }
            }

            return imageFields;
        },

        // 清理购物车
        clearCart() {
            this.cartList = [];
        }
    },

    getters: {
        cartTotalQuantity: state => state.cartList.reduce((total, item) => total + item.quantity, 0),
        cartTotalPrice: state => state.cartList.reduce((total, item) => total + (item.price * item.quantity), 0).toFixed(2)
    }
})