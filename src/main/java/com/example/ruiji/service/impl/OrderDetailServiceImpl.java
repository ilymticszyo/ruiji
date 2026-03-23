package com.example.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruiji.mapper.OrderDetailMapper;
import com.example.ruiji.pojo.OrderDetail;
import com.example.ruiji.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * 订单明细 Service 实现
 *
 * @author ocx
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
