package com.example.ruiji.dto;

import com.example.ruiji.pojo.Dish;
import com.example.ruiji.pojo.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜品分页展示 DTO
 *
 * @author ocx
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DishDto extends Dish {

    /**
     * 菜品分类名称（用于列表展示）
     */
    private String categoryName;

    /**
     * 菜品口味列表（用于新增/修改/详情复显）
     */
    private List<DishFlavor> flavors;
}

