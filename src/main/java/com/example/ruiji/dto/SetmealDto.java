package com.example.ruiji.dto;

import com.example.ruiji.pojo.Setmeal;
import com.example.ruiji.pojo.SetmealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 套餐展示/保存 DTO
 *
 * @author ocx
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SetmealDto extends Setmeal {

    /**
     * 分类名称（用于列表展示）
     */
    private String categoryName;

    /**
     * 套餐关联菜品明细（用于新增/修改/详情复显）
     */
    private List<SetmealDish> setmealDishes;
}

