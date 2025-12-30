// src/store/index.js
import { createStore } from 'vuex'

export default createStore({
    state: {
        menu: [
            {
                menuclick: 'AppMain',
                menuname: '用户管理',
                menuicon: 'el-icon-user-solid'
            }
        ]
    },
    mutations: {
        setMenu(state, menu) {
            state.menu = menu
        }
    },
    actions: {
        updateMenu({ commit }, menu) {
            commit('setMenu', menu)
        }
    },
    getters: {
        getMenu: state => state.menu
    }
})