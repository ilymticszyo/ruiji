package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ruiji.common.BaseContext;
import com.example.ruiji.common.Res;
import com.example.ruiji.dto.DishDto;
import com.example.ruiji.dto.OrdersDto;
import com.example.ruiji.pojo.OrderDetail;
import com.example.ruiji.pojo.Orders;
import com.example.ruiji.service.OrderDetailService;
import com.example.ruiji.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器
 *
 * @author lizhiwei
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;


    @PostMapping("/submit")
    public Res<String> submit(@RequestBody Orders orders) {

        ordersService.submit(orders);
        return Res.success("下单完成");
    }

    /**
     * 用户端分页查询订单
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 订单分页数据
     * @author lizhiwei
     */
    @GetMapping("/userPage")
    public Res<Page<OrdersDto>> userPage(@RequestParam Integer page, @RequestParam Integer pageSize) {
        if (page == null || page < 1 || pageSize == null || pageSize < 1) {
            return Res.error("分页参数不合法");
        }
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            return Res.error("用户未登录");
        }

        Page<Orders> orderPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(Orders::getUserId, currentUserId);
        orderQueryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(orderPage, orderQueryWrapper);

        Page<OrdersDto> orderDtoPage = new Page<>();
        BeanUtils.copyProperties(orderPage, orderDtoPage, "records");

        List<Orders> orderList = orderPage.getRecords();
        List<OrdersDto> orderDtoList = new ArrayList<>();
        if (orderList == null || orderList.isEmpty()) {
            orderDtoPage.setRecords(orderDtoList);
            return Res.success(orderDtoPage);
        }

        for (Orders order : orderList) {
            if (order == null) {
                continue;
            }
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(order, ordersDto);

            LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(OrderDetail::getOrderId, order.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(detailQueryWrapper);
            ordersDto.setOrderDetails(orderDetailList);

            orderDtoList.add(ordersDto);
        }

        orderDtoPage.setRecords(orderDtoList);
        return Res.success(orderDtoPage);

    }

    /**
     * 再来一单
     *
     * @param orders 订单入参（需要包含历史订单ID）
     * @return 操作结果
     * @author lizhiwei
     */
    @PostMapping("/again")
    public Res<String> again(@RequestBody Orders orders) {
        if (orders == null || orders.getId() == null) {
            return Res.error("订单ID不能为空");
        }
        ordersService.again(orders);
        return Res.success("再来一单成功");
    }

    @GetMapping("/page")
    public Res<Page> page(@RequestParam Integer page, @RequestParam Integer pageSize,@RequestParam(required = false) String number) {
        Page<Orders> orderPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.like(number != null,Orders::getNumber,number);
        ordersService.page(orderPage,objectLambdaQueryWrapper);
        

        return Res.success(orderPage);
    }


    @PutMapping
    public Res<String> put(@RequestBody Orders orders){
        Long id = orders.getId();
        LambdaQueryWrapper<Orders> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(Orders::getId,id);
        ordersService.updateById(orders);
        return Res.success("派送成功");
    }
}
