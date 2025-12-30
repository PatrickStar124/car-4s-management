// PartService.java
package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.common.Result;
import com.wms.entity.Part;

import java.util.List;

public interface PartService extends IService<Part> {

    /**
     * 添加配件
     */
    Result addPart(Part part);

    /**
     * 更新配件信息
     */
    Result updatePart(Part part);

    /**
     * 根据分类查询配件
     */
    Result getPartsByCategory(String category);

    /**
     * 根据品牌查询配件
     */
    Result getPartsByBrand(String brand);

    /**
     * 搜索配件（名称或编号）
     */
    Result searchParts(String keyword);

    /**
     * 获取配件详情（包含库存信息）
     */
    Result getPartDetail(Integer partId);
}