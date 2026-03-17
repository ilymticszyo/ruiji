package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.DishFlavor;
import com.example.ruiji.service.DishFlavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品口味管理接口
 *
 * 一般实际项目中会和 Dish 联合处理，
 * 这里提供基础单表能力，方便后续按需扩展。
 *
 * @author lizhiwei
 */
@RestController
@RequestMapping("/dishFlavor")
public class DishFlavorController {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 根据菜品 ID 查询对应口味列表（仅未删除）
     *
     * @param dishId 菜品 ID
     * @return 口味列表
     */
    @GetMapping("/list")
    public Res<List<DishFlavor>> listByDishId(@RequestParam Long dishId) {
        if (dishId == null) {
            return Res.error("菜品ID不能为空");
        }
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        queryWrapper.eq(DishFlavor::getIsDeleted, 0);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        return Res.success(list);
    }

    /**
     * 新增口味
     *
     * @param dishFlavor 口味实体
     * @return 结果信息
     */
    @PostMapping
    public Res<String> save(@RequestBody DishFlavor dishFlavor) {
        if (dishFlavor == null) {
            return Res.error("口味信息不能为空");
        }
        if (dishFlavor.getIsDeleted() == null) {
            dishFlavor.setIsDeleted(0);
        }
        boolean result = dishFlavorService.save(dishFlavor);
        return result ? Res.success("新增口味成功") : Res.error("新增口味失败");
    }

    /**
     * 修改口味
     *
     * @param dishFlavor 口味实体
     * @return 结果信息
     */
    @PutMapping
    public Res<String> update(@RequestBody DishFlavor dishFlavor) {
        if (dishFlavor == null || dishFlavor.getId() == null) {
            return Res.error("口味信息或主键不能为空");
        }
        boolean result = dishFlavorService.updateById(dishFlavor);
        return result ? Res.success("修改口味成功") : Res.error("修改口味失败");
    }

    /**
     * 逻辑删除口味
     *
     * @param id 主键
     * @return 结果信息
     */
    @DeleteMapping
    public Res<String> delete(@RequestParam Long id) {
        if (id == null) {
            return Res.error("口味ID不能为空");
        }
        DishFlavor flavor = dishFlavorService.getById(id);
        if (flavor == null || flavor.getIsDeleted() != 0) {
            return Res.error("口味不存在或已删除");
        }
        flavor.setIsDeleted(1);
        boolean result = dishFlavorService.updateById(flavor);
        return result ? Res.success("删除口味成功") : Res.error("删除口味失败");
    }
}

