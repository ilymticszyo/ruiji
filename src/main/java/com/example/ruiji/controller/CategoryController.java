package com.example.ruiji.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.Category;
import com.example.ruiji.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public Res<Page> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        return Res.success(pageInfo);
    }

    @PostMapping
    public Res<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return Res.success("新增分类成功");
    }

    @PutMapping
    public Res<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Res.success("修改分类成功");
    }

    @DeleteMapping
    public Res<String> delete( Long id) {
        categoryService.removeById(id);
        return Res.success("删除分类成功");
    }

    /**
     * 根据类型查询分类列表
     * 对应接口：GET /category/list?type=1
     *
     * @param category 分类查询条件（只关心 type）
     * @return 分类列表
     */
    @GetMapping("/list")
    public Res<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null && category.getType() != null) {
            queryWrapper.eq(Category::getType, category.getType());
        }
        queryWrapper.orderByAsc(Category::getSort)
                .orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return Res.success(list);
    }
}
