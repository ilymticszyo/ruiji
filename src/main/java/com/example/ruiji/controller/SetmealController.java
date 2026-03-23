package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruiji.common.Res;
import com.example.ruiji.dto.SetmealDto;
import com.example.ruiji.pojo.Category;
import com.example.ruiji.pojo.Setmeal;
import com.example.ruiji.pojo.SetmealDish;
import com.example.ruiji.service.CategoryService;
import com.example.ruiji.service.SetmealDishService;
import com.example.ruiji.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 套餐管理接口
 *
 * 对齐前端接口：
 * - GET  /setmeal/page
 * - POST /setmeal
 * - PUT  /setmeal
 * - DELETE /setmeal?ids=1,2,3
 * - POST /setmeal/status/{status}?ids=1,2
 * - GET  /setmeal/{id}
 * - GET  /setmeal/list?categoryId=xx&status=1
 * - GET  /setmeal/dish/{id}
 *
 * @author ocx
 */
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 套餐分页查询
     */
    @GetMapping("/page")
    public Res<Page<SetmealDto>> page(@RequestParam int page,
                                      @RequestParam int pageSize,
                                      @RequestParam(required = false) String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.like(name != null && !name.isEmpty(), Setmeal::getName, name)
                .eq(Setmeal::getIsDeleted, 0)
                .orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, qw);

        Page<SetmealDto> dtoPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();
        if (records == null || records.isEmpty()) {
            dtoPage.setRecords(List.of());
            return Res.success(dtoPage);
        }

        Set<Long> categoryIds = records.stream()
                .map(Setmeal::getCategoryId)
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

        List<SetmealDto> dtoRecords = records.stream()
                .filter(Objects::nonNull)
                .map(sm -> {
                    SetmealDto dto = new SetmealDto();
                    BeanUtils.copyProperties(Objects.requireNonNull(sm), dto);
                    if (sm.getCategoryId() != null) {
                        dto.setCategoryName(categoryNameMap.get(sm.getCategoryId()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoRecords);
        return Res.success(dtoPage);
    }

    /**
     * 新增套餐（含关联菜品）
     */
    @PostMapping
    public Res<String> save(@RequestBody SetmealDto setmealDto) {
        if (setmealDto == null) {
            return Res.error("套餐信息不能为空");
        }
        boolean result = setmealService.saveWithDishes(setmealDto);
        return result ? Res.success("新增套餐成功") : Res.error("新增套餐失败");
    }

    /**
     * 修改套餐（含关联菜品）
     */
    @PutMapping
    public Res<String> update(@RequestBody SetmealDto setmealDto) {
        if (setmealDto == null || setmealDto.getId() == null) {
            return Res.error("套餐信息或主键不能为空");
        }
        boolean result = setmealService.updateWithDishes(setmealDto);
        return result ? Res.success("修改套餐成功") : Res.error("修改套餐失败");
    }

    /**
     * 查询套餐详情（用于后台编辑回显）
     */
    @GetMapping("/{id}")
    public Res<SetmealDto> getById(@PathVariable Long id) {
        if (id == null) {
            return Res.error("套餐ID不能为空");
        }
        SetmealDto dto = setmealService.getDtoById(id);
        if (dto == null) {
            return Res.error("套餐不存在或已被删除");
        }
        return Res.success(dto);
    }

    /**
     * 删除套餐（逻辑删除）
     */
    @DeleteMapping
    public Res<String> delete(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Res.error("套餐ID不能为空");
        }
        boolean result = setmealService.removeWithDishes(ids);
        return result ? Res.success("删除套餐成功") : Res.error("删除套餐失败");
    }

    /**
     * 批量启用/停用
     */
    @PostMapping("/status/{status}")
    public Res<String> status(@PathVariable Integer status, @RequestParam("ids") List<Long> ids) {
        if (status == null || (status != 0 && status != 1)) {
            return Res.error("套餐状态参数非法");
        }
        if (ids == null || ids.isEmpty()) {
            return Res.error("套餐ID不能为空");
        }

        LambdaUpdateWrapper<Setmeal> uw = new LambdaUpdateWrapper<>();
        uw.in(Setmeal::getId, ids)
                .eq(Setmeal::getIsDeleted, 0)
                .set(Setmeal::getStatus, status);

        boolean ok = setmealService.update(uw);
        if (!ok) {
            return Res.error("套餐状态修改失败");
        }
        return status == 0 ? Res.success("套餐停售成功") : Res.success("套餐起售成功");
    }

    /**
     * 前台：按分类查询套餐列表
     * 对应接口：GET /setmeal/list?categoryId=xx&status=1
     */
    @GetMapping("/list")
    public Res<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        if (setmeal != null && setmeal.getCategoryId() != null) {
            qw.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        }
        if (setmeal != null && setmeal.getStatus() != null) {
            qw.eq(Setmeal::getStatus, setmeal.getStatus());
        }
        qw.eq(Setmeal::getIsDeleted, 0)
                .orderByDesc(Setmeal::getUpdateTime);
        return Res.success(setmealService.list(qw));
    }

    /**
     * 前台：查询套餐下的菜品明细
     * 对应接口：GET /setmeal/dish/{id}
     */
    @GetMapping("/dish/{id}")
    public Res<List<SetmealDish>> dish(@PathVariable Long id) {
        if (id == null) {
            return Res.error("套餐ID不能为空");
        }
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(SetmealDish::getSetmealId, id)
                .eq(SetmealDish::getIsDeleted, 0)
                .orderByAsc(SetmealDish::getSort);
        return Res.success(setmealDishService.list(qw));
    }
}

