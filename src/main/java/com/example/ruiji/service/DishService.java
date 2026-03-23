package com.example.ruiji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruiji.dto.DishDto;
import com.example.ruiji.pojo.Dish;

/**
 * 菜品服务接口
 *
 * @author ocx
 */
public interface DishService extends IService<Dish> {

    /**
     * 新增菜品（包含口味）
     *
     * @param dishDto 菜品 DTO
     * @return 是否成功
     */
    boolean saveWithFlavors(DishDto dishDto);

    /**
     * 修改菜品（包含口味）
     *
     * @param dishDto 菜品 DTO
     * @return 是否成功
     */
    boolean updateWithFlavors(DishDto dishDto);

    /**
     * 查询菜品详情（包含口味）
     *
     * @param id 菜品ID
     * @return 菜品 DTO
     */
    DishDto getDtoById(Long id);
}

