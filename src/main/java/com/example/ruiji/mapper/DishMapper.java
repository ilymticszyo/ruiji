package com.example.ruiji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruiji.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品 Mapper
 *
 * @author lizhiwei
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {

}

