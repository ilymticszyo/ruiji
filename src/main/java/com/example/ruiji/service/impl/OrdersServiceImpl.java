package com.example.ruiji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruiji.common.BaseContext;
import com.example.ruiji.mapper.OrdersMapper;
import com.example.ruiji.pojo.*;
import com.example.ruiji.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 订单 Service 实现
 *
 * @author ocx
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;
    /**
     * 提交订单（支付）
     *
     * @param orders 订单入参
     * @author ocx
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Orders orders) {
        if (orders == null) {
            throw new RuntimeException("订单数据不能为空");
        }

        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }

        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId, currentUserId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartWrapper);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        Long addressBookId = orders.getAddressBookId();
        if (addressBookId == null) {
            throw new RuntimeException("请选择收货地址");
        }

        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null || Integer.valueOf(1).equals(addressBook.getIsDeleted())) {
            throw new RuntimeException("收货地址不存在");
        }

        if (addressBook.getUserId() == null || !currentUserId.equals(addressBook.getUserId())) {
            throw new RuntimeException("收货地址不属于当前用户");
        }
        User byId = userService.getById(currentUserId);
        orders.setId(IdWorker.getId());
        orders.setUserName(byId.getName());
        orders.setUserId(currentUserId);
        orders.setStatus(2);
        orders.setNumber(String.valueOf(IdWorker.getId()));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(buildFullAddress(addressBook));
        orders.setAmount(BigDecimal.valueOf(calculateTotalAmount(shoppingCartList).get()));

        this.save(orders);

        Long orderId = orders.getId();
        if (orderId == null) {
            throw new RuntimeException("订单创建失败");
        }

        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(cartItem -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(cartItem.getName());
            orderDetail.setDishId(cartItem.getDishId());
            orderDetail.setSetmealId(cartItem.getSetmealId());
            orderDetail.setDishFlavor(cartItem.getDishFlavor());
            orderDetail.setNumber(cartItem.getNumber());
            orderDetail.setAmount(cartItem.getAmount());
            orderDetail.setImage(cartItem.getImage());
            orderDetail.setOrderId(orderId);
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailService.saveBatch(orderDetailList);

        shoppingCartService.remove(shoppingCartWrapper);
    }

    /**
     * 再来一单
     *
     * @param orders 订单入参（需要包含历史订单ID）
     * @author lizhiwei
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void again(Orders orders) {
        if (orders == null || orders.getId() == null) {
            throw new RuntimeException("订单ID不能为空");
        }

        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders historyOrder = this.getById(orders.getId());
        if (historyOrder == null) {
            throw new RuntimeException("历史订单不存在");
        }
        if (historyOrder.getUserId() == null || !currentUserId.equals(historyOrder.getUserId())) {
            throw new RuntimeException("无权限操作该订单");
        }

        LambdaQueryWrapper<OrderDetail> orderDetailQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailQueryWrapper.eq(OrderDetail::getOrderId, historyOrder.getId());
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailQueryWrapper);
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new RuntimeException("历史订单明细为空");
        }

        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId, currentUserId);
        shoppingCartService.remove(shoppingCartWrapper);

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(currentUserId);
            shoppingCart.setName(orderDetail.getName());
            shoppingCart.setDishId(orderDetail.getDishId());
            shoppingCart.setSetmealId(orderDetail.getSetmealId());
            shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            shoppingCart.setNumber(orderDetail.getNumber());
            shoppingCart.setAmount(orderDetail.getAmount());
            shoppingCart.setImage(orderDetail.getImage());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartService.saveBatch(shoppingCartList);
    }

    /**
     * 拼接完整地址
     *
     * @param addressBook 地址簿信息
     * @return 完整地址
     * @author ocx
     */
    private String buildFullAddress(AddressBook addressBook) {
        StringBuilder fullAddress = new StringBuilder();
        if (addressBook.getProvinceName() != null) {
            fullAddress.append(addressBook.getProvinceName());
        }
        if (addressBook.getCityName() != null) {
            fullAddress.append(addressBook.getCityName());
        }
        if (addressBook.getDistrictName() != null) {
            fullAddress.append(addressBook.getDistrictName());
        }
        if (addressBook.getDetail() != null) {
            fullAddress.append(addressBook.getDetail());
        }
        return fullAddress.toString();
    }

    /**
     * 计算订单总金额
     *
     * @param shoppingCartList 购物车数据
     * @return 订单金额
     * @author ocx
     */
    private AtomicInteger calculateTotalAmount(List<ShoppingCart> shoppingCartList) {
        AtomicInteger totalAmount = new AtomicInteger(0);
        //BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCart cartItem : shoppingCartList) {
            if (cartItem.getAmount() == null || cartItem.getNumber() == null) {
                continue;
            }
            int i = cartItem.getAmount().multiply(new BigDecimal(cartItem.getNumber())).intValue();
            totalAmount.addAndGet(i);
        }
        return totalAmount;
    }
}
