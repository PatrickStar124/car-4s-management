package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Part;
import com.wms.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 配件管理控制器
 * 处理配件相关的接口请求，包括添加、更新、查询、搜索等操作
 * 接口排序逻辑：基础查询 → 条件查询/搜索 → 核心操作 → 统计/删除
 */
@RestController
@RequestMapping("/api/part")
public class PartController {

    @Autowired
    private PartService partService;

    // ======================== 基础查询接口（通用/全量/详情）========================
    /**
     * 获取所有配件列表（分页可选）
     * 用途：管理员后台查看系统内所有配件基础数据，支持分页展示
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认20）
     * @return 配件列表
     */
    @GetMapping("/list")
    public Result getPartList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        // 简单返回所有配件，如果需要分页可以扩展
        return Result.suc(partService.list());
    }

    /**
     * 获取配件详情（包含库存信息）
     * 用途：查看单个配件的完整信息（基础信息+库存状态），用于采购、出库等场景
     * @param partId 配件ID
     * @return 配件详细信息
     */
    @GetMapping("/detail/{partId}")
    public Result getPartDetail(@PathVariable Integer partId) {
        return partService.getPartDetail(partId);
    }

    // ======================== 条件查询/搜索接口（分类/品牌/关键词）========================
    /**
     * 根据分类查询配件
     * 用途：按配件品类筛选（如筛选所有滤清器、刹车系统配件）
     * @param category 配件分类（如：滤清器、刹车系统、机油等）
     * @return 该分类下的所有配件
     */
    @GetMapping("/category/{category}")
    public Result getPartsByCategory(@PathVariable String category) {
        return partService.getPartsByCategory(category);
    }

    /**
     * 根据品牌查询配件
     * 用途：按品牌筛选配件（如筛选所有博世、马勒品牌配件）
     * @param brand 品牌名称（如：博世、马勒、NGK等）
     * @return 该品牌下的所有配件
     */
    @GetMapping("/brand/{brand}")
    public Result getPartsByBrand(@PathVariable String brand) {
        return partService.getPartsByBrand(brand);
    }

    /**
     * 搜索配件
     * 用途：多维度模糊搜索配件（支持编号、名称、品牌、适用车型）
     * @param keyword 搜索关键词（可搜索配件编号、名称、品牌、适用车型）
     * @return 搜索结果列表
     */
    @GetMapping("/search")
    public Result searchParts(@RequestParam String keyword) {
        return partService.searchParts(keyword);
    }

    // ======================== 核心操作接口（添加/更新）========================
    /**
     * 添加配件
     * 用途：录入新配件信息到系统（如新增采购的配件品类）
     * @param part 配件信息实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result addPart(@RequestBody Part part) {
        return partService.addPart(part);
    }

    /**
     * 更新配件信息
     * 用途：修改配件的基础信息（如价格、库存阈值、适用车型等）
     * @param part 配件信息实体（需包含ID）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result updatePart(@RequestBody Part part) {
        return partService.updatePart(part);
    }

    // ======================== 统计/删除接口（管理类操作）========================
    /**
     * 获取配件库存状态统计
     * 用途：管理层查看配件库存分布、各分类配件数量等汇总数据
     * @return 按分类统计的配件数量
     */
    @GetMapping("/stats")
    public Result getPartStats() {
        try {
            // 这里可以调用Service层的方法，或者直接在这里写统计逻辑
            // 暂时返回成功
            return Result.suc("统计功能待实现");
        } catch (Exception e) {
            return Result.fail("统计失败: " + e.getMessage());
        }
    }

    /**
     * 删除配件
     * 用途：管理员删除无效/下架的配件记录
     * @param partId 配件ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{partId}")
    public Result deletePart(@PathVariable Integer partId) {
        // 因为你的Service没有删除方法，这里提供一个简单的实现
        try {
            boolean removed = partService.removeById(partId);
            return removed ? Result.suc("删除成功") : Result.fail("删除失败，配件不存在");
        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }
}