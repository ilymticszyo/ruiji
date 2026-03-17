package com.example.ruiji.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.Category;
import com.example.ruiji.service.CategoryService;

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
}
