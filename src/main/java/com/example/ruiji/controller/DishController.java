package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.Dish;
import com.example.ruiji.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜品管理接口
 *
 * 参考 Employee 和 Category 的风格，
 * 提供分页查询、新增、修改、删除等基础能力。
 *
 * @author lizhiwei
 */
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品分页查询
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param name     菜品名称（可选，模糊查询）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Res<Page<Dish>> page(@RequestParam int page,
                                @RequestParam int pageSize,
                                @RequestParam(required = false) String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 名称模糊查询
        queryWrapper.like(name != null && !name.isEmpty(), Dish::getName, name);
        // 逻辑未删除（0 未删除，1 已删除）
        queryWrapper.eq(Dish::getIsDeleted, 0);
        // 更新时间倒序
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);
        return Res.success(pageInfo);
    }

    /**
     * 新增菜品
     *
     * @param dish 菜品对象
     * @return 结果信息
     */
    @PostMapping
    public Res<String> save(@RequestBody Dish dish) {
        if (dish == null) {
            return Res.error("菜品信息不能为空");
        }
        // 逻辑删除标志默认 0（未删除）
        dish.setIsDeleted(0);
        boolean result = dishService.save(dish);
        return result ? Res.success("新增菜品成功") : Res.error("新增菜品失败");
    }

    /**
     * 修改菜品
     *
     * @param dish 菜品对象
     * @return 结果信息
     */
    @PutMapping
    public Res<String> update(@RequestBody Dish dish) {
        if (dish == null || dish.getId() == null) {
            return Res.error("菜品信息或主键不能为空");
        }
        boolean result = dishService.updateById(dish);
        return result ? Res.success("修改菜品成功") : Res.error("修改菜品失败");
    }

    /**
     * 根据主键 ID 查询菜品
     *
     * @param id 主键
     * @return 菜品详情
     */
    @GetMapping("/{id}")
    public Res<Dish> getById(@PathVariable Long id) {
        if (id == null) {
            return Res.error("菜品ID不能为空");
        }
        Dish dish = dishService.getById(id);
        if (dish == null || dish.getIsDeleted() != 0) {
            return Res.error("菜品不存在或已被删除");
        }
        return Res.success(dish);
    }

    /**
     * 根据主键 ID 删除菜品（逻辑删除）
     *
     * @param id 主键
     * @return 结果信息
     */
    @DeleteMapping
    public Res<String> delete(@RequestParam Long id) {
        if (id == null) {
            return Res.error("菜品ID不能为空");
        }
        Dish dish = dishService.getById(id);
        if (dish == null || dish.getIsDeleted() != 0) {
            return Res.error("菜品不存在或已被删除");
        }
        dish.setIsDeleted(1);
        boolean result = dishService.updateById(dish);
        return result ? Res.success("删除菜品成功") : Res.error("删除菜品失败");
    }
}

