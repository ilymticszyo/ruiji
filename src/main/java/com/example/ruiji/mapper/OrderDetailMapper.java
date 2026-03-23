package com.example.ruiji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ruiji.pojo.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper
 *
 * @author ocx
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
