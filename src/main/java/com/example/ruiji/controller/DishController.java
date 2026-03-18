package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruiji.common.Res;
import com.example.ruiji.dto.DishDto;
import com.example.ruiji.pojo.Category;
import com.example.ruiji.pojo.Dish;
import com.example.ruiji.service.CategoryService;
import com.example.ruiji.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品分页查询
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param name     菜品名称（可选，模糊查询）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Res<Page<DishDto>> page(@RequestParam int page,
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

        Page<DishDto> dtoPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        if (records == null || records.isEmpty()) {
            dtoPage.setRecords(List.of());
            return Res.success(dtoPage);
        }

        Set<Long> categoryIds = records.stream()
                .map(Dish::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap;
        if (categoryIds.isEmpty()) {
            categoryNameMap = Map.of();
        } else {
            categoryNameMap = categoryService.listByIds(categoryIds).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        }

        List<DishDto> dtoRecords = records.stream()
                .filter(Objects::nonNull)
                .map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(Objects.requireNonNull(dish), dishDto);
            if (dish.getCategoryId() != null) {
                dishDto.setCategoryName(categoryNameMap.get(dish.getCategoryId()));
            }
            return dishDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoRecords);
        return Res.success(dtoPage);
    }

    /**
     * 新增菜品
     *
     * @param dishDto 菜品对象（含口味）
     * @return 结果信息
     */
    @PostMapping
    public Res<String> save(@RequestBody DishDto dishDto) {
        if (dishDto == null) {
            return Res.error("菜品信息不能为空");
        }
        boolean result = dishService.saveWithFlavors(dishDto);
        return result ? Res.success("新增菜品成功") : Res.error("新增菜品失败");
    }

    /**
     * 修改菜品
     *
     * @param dishDto 菜品对象（含口味）
     * @return 结果信息
     */
    @PutMapping
    public Res<String> update(@RequestBody DishDto dishDto) {
        if (dishDto == null || dishDto.getId() == null) {
            return Res.error("菜品信息或主键不能为空");
        }
        boolean result = dishService.updateWithFlavors(dishDto);
        return result ? Res.success("修改菜品成功") : Res.error("修改菜品失败");
    }

    /**
     * 根据主键 ID 查询菜品分类
     *
     * @param id 主键
     * @return 菜品详情
     */
    @GetMapping("/{id}")
    public Res<DishDto> getById(@PathVariable Long id) {
        if (id == null) {
            return Res.error("菜品ID不能为空");
        }
        DishDto dishDto = dishService.getDtoById(id);
        if (dishDto == null) {
            return Res.error("菜品不存在或已被删除");
        }
        return Res.success(dishDto);
    }

    /**
     * 根据主键 ID 删除菜品（逻辑删除）
     *
     * @param ids 主键集合
     * @return 结果信息
     */
    @DeleteMapping
    public Res<String> delete(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Res.error("菜品ID不能为空");
        }

        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids)
                .eq(Dish::getIsDeleted, 0)
                .set(Dish::getIsDeleted, 1);

        boolean result = dishService.update(updateWrapper);
        return result ? Res.success("删除菜品成功") : Res.error("删除菜品失败");
    }

    @PostMapping("/status/{status}")
    public Res<String> status(@PathVariable Integer status, @RequestParam("ids") List<Long> ids) {
        if (status == null || (status != 0 && status != 1)) {
            return Res.error("菜品状态参数非法");
        }
        if (ids == null || ids.isEmpty()) {
            return Res.error("菜品ID不能为空");
        }

        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids)
                .eq(Dish::getIsDeleted, 0)
                .set(Dish::getStatus, status);

        boolean result = dishService.update(updateWrapper);
        if (!result) {
            return Res.error("菜品状态修改失败");
        }
        return status == 0 ? Res.success("菜品停售成功") : Res.success("菜品起售成功");
    }
}

