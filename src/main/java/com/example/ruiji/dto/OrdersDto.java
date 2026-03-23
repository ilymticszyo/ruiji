package com.example.ruiji.dto;

import com.example.ruiji.pojo.OrderDetail;
import com.example.ruiji.pojo.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单扩展数据传输对象
 *
 * @author lizhiwei
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersDto extends Orders {
    /**
     * 订单明细列表
     */
    private List<OrderDetail> orderDetails;
}
