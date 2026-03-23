package com.example.ruiji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruiji.dto.SetmealDto;
import com.example.ruiji.pojo.Setmeal;

import java.util.List;

/**
 * 套餐服务接口
 *
 * @author ocx
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐（包含关联菜品）
     *
     * @param setmealDto 套餐 DTO
     * @return 是否成功
     */
    boolean saveWithDishes(SetmealDto setmealDto);

    /**
     * 修改套餐（包含关联菜品）
     *
     * @param setmealDto 套餐 DTO
     * @return 是否成功
     */
    boolean updateWithDishes(SetmealDto setmealDto);

    /**
     * 查询套餐详情（包含关联菜品）
     *
     * @param id 套餐ID
     * @return 套餐 DTO
     */
    SetmealDto getDtoById(Long id);

    /**
     * 删除套餐（逻辑删除）并清理关联
     *
     * @param ids 套餐ID集合
     * @return 是否成功
     */
    boolean removeWithDishes(List<Long> ids);
}

