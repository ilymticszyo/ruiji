package com.example.ruiji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruiji.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 *
 * @author ocx
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
