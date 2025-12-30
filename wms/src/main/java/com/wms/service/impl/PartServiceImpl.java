// PartServiceImpl.java
package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.Result;
import com.wms.entity.Inventory;
import com.wms.entity.Part;
import com.wms.mapper.InventoryMapper;
import com.wms.mapper.PartMapper;
import com.wms.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartServiceImpl extends ServiceImpl<PartMapper, Part> implements PartService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public Result addPart(Part part) {
        try {
            // 1. 验证必填字段
            if (part.getPartNo() == null || part.getPartNo().trim().isEmpty()) {
                return Result.fail("配件编号不能为空");
            }
            if (part.getPartName() == null || part.getPartName().trim().isEmpty()) {
                return Result.fail("配件名称不能为空");
            }

            // 2. 检查配件编号是否重复
            LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Part::getPartNo, part.getPartNo());
            if (this.count(wrapper) > 0) {
                return Result.fail("配件编号已存在");
            }

            // 3. 保存配件
            boolean saved = this.save(part);

            // 4. 自动创建库存记录
            if (saved && part.getId() != null) {
                Inventory inventory = new Inventory();
                inventory.setPartId(part.getId());
                inventory.setStockQuantity(0);
                inventory.setSafeStock(10); // 默认安全库存10个
                inventory.setLockedQuantity(0);
                inventoryMapper.insert(inventory);
            }

            return saved ? Result.suc("配件添加成功", part) : Result.fail("添加失败");
        } catch (Exception e) {
            return Result.fail("添加配件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result updatePart(Part part) {
        try {
            if (part.getId() == null) {
                return Result.fail("配件ID不能为空");
            }

            Part existing = this.getById(part.getId());
            if (existing == null) {
                return Result.fail("配件不存在");
            }

            // 如果更新了配件编号，检查是否重复
            if (part.getPartNo() != null && !part.getPartNo().equals(existing.getPartNo())) {
                LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Part::getPartNo, part.getPartNo())
                        .ne(Part::getId, part.getId());
                if (this.count(wrapper) > 0) {
                    return Result.fail("配件编号已存在");
                }
            }

            boolean updated = this.updateById(part);
            return updated ? Result.suc("配件更新成功", part) : Result.fail("更新失败");
        } catch (Exception e) {
            return Result.fail("更新配件失败: " + e.getMessage());
        }
    }

    @Override
    public Result getPartsByCategory(String category) {
        try {
            LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Part::getCategory, category)
                    .orderByAsc(Part::getPartName);

            List<Part> parts = this.list(wrapper);
            return Result.suc(parts);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getPartsByBrand(String brand) {
        try {
            LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Part::getBrand, brand)
                    .orderByAsc(Part::getPartName);

            List<Part> parts = this.list(wrapper);
            return Result.suc(parts);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result searchParts(String keyword) {
        try {
            LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(Part::getPartNo, keyword)
                    .or()
                    .like(Part::getPartName, keyword)
                    .or()
                    .like(Part::getBrand, keyword)
                    .or()
                    .like(Part::getModel, keyword)
                    .orderByAsc(Part::getPartName);

            List<Part> parts = this.list(wrapper);

            // 关联库存信息
            parts.forEach(part -> {
                if (part.getId() != null) {
                    LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
                    inventoryWrapper.eq(Inventory::getPartId, part.getId());
                    Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
                    part.setInventory(inventory);
                }
            });

            return Result.suc(parts);
        } catch (Exception e) {
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    @Override
    public Result getPartDetail(Integer partId) {
        try {
            Part part = this.getById(partId);
            if (part == null) {
                return Result.fail("配件不存在");
            }

            // 查询库存信息
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Inventory::getPartId, partId);
            Inventory inventory = inventoryMapper.selectOne(wrapper);

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("part", part);
            result.put("inventory", inventory);

            return Result.suc(result);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }
}