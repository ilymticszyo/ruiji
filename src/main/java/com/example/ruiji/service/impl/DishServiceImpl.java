package com.example.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruiji.mapper.DishMapper;
import com.example.ruiji.pojo.Dish;
import com.example.ruiji.service.DishService;
import org.springframework.stereotype.Service;

/**
 * 菜品服务实现
 *
 * @author lizhiwei
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

}

