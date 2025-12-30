package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper  // 添加这个注解
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    // 不需要自定义 selectList 和 insert 方法
    // BaseMapper 已经提供了这些方法
}