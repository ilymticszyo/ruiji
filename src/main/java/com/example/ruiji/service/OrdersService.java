package com.example.ruiji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ruiji.pojo.Orders;

/**
 * 订单 Service
 *
 * @author ocx
 */
public interface OrdersService extends IService<Orders> {
    /**
     * 提交订单（支付）
     *
     * @param orders 订单入参
     * @author ocx
     */
    void submit(Orders orders);

    /**
     * 再来一单
     *
     * @param orders 订单入参（需要包含历史订单ID）
     * @author lizhiwei
     */
    void again(Orders orders);
}
