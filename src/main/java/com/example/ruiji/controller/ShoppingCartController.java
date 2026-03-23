package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ruiji.common.Res;
import com.example.ruiji.common.BaseContext;
import com.example.ruiji.pojo.ShoppingCart;
import com.example.ruiji.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车控制器
 *
 * @author ocx
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查询当前用户购物车列表
     *
     * @return 购物车数据
     */
    @GetMapping("/list")
    public Res<List<ShoppingCart>> list() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            return Res.error("未获取到当前用户信息");
        }
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        return Res.success(shoppingCartService.list(queryWrapper));
    }

    /**
     * 添加商品到购物车
     *
     * @param shoppingCart 购物车入参
     * @return 购物车项
     */
    @PostMapping("/add")
    public Res<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        if (shoppingCart == null) {
            return Res.error("请求参数不能为空");
        }
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            return Res.error("未获取到当前用户信息");
        }
        if (shoppingCart.getDishId() == null && shoppingCart.getSetmealId() == null) {
            return Res.error("菜品ID和套餐ID不能同时为空");
        }

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        queryWrapper.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one != null) {
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
            return Res.success(one);
        }

        shoppingCart.setUserId(currentId);
        shoppingCart.setNumber(1);
        shoppingCartService.save(shoppingCart);
        return Res.success(shoppingCart);
    }

    /**
     * 减少购物车商品数量
     *
     * @param shoppingCart 购物车入参
     * @return 购物车项
     */
    @PostMapping("/sub")
    public Res<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        if (shoppingCart == null) {
            return Res.error("请求参数不能为空");
        }
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            return Res.error("未获取到当前用户信息");
        }
        if (shoppingCart.getDishId() == null && shoppingCart.getSetmealId() == null) {
            return Res.error("菜品ID和套餐ID不能同时为空");
        }

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        queryWrapper.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one == null) {
            return Res.error("购物车中不存在该商品");
        }

        int newNum = one.getNumber() - 1;
        if (newNum <= 0) {
            shoppingCartService.removeById(one.getId());
            one.setNumber(0);
            return Res.success(one);
        }

        one.setNumber(newNum);
        shoppingCartService.updateById(one);
        return Res.success(one);
    }

    /**
     * 清空当前用户购物车
     *
     * @return 操作结果
     */
    @DeleteMapping("/clean")
    public Res<String> clean() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            return Res.error("未获取到当前用户信息");
        }
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(queryWrapper);
        return Res.success("清空购物车成功");
    }
}
