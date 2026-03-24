package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.Dish;
import com.example.ruiji.pojo.OrderDetail;
import com.example.ruiji.pojo.Orders;
import com.example.ruiji.pojo.Setmeal;
import com.example.ruiji.service.DishService;
import com.example.ruiji.service.OrderDetailService;
import com.example.ruiji.service.OrdersService;
import com.example.ruiji.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统计分析控制器
 *
 * @author lizhiwei
 */
@Slf4j
@RestController
@RequestMapping("/statics")
public class StatisticsController {

    private static final Integer ORDER_STATUS_FINISHED = 4;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 总营业额（万元）
     *
     * @return 总营业额
     * @author lizhiwei
     */
    @GetMapping("/total")
    public Res<BigDecimal> total() {
        List<Orders> finishedOrders = listFinishedOrders();
        if (finishedOrders.isEmpty()) {
            return Res.success(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        BigDecimal totalAmount = finishedOrders.stream()
                .map(Orders::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Res.success(toWan(totalAmount));
    }

    /**
     * 菜系营收占比
     *
     * @return 图表数据
     * @author lizhiwei
     */
    @GetMapping("/dishCash")
    public Res<List<Map<String, Object>>> dishCash() {
        return Res.success(buildDishCashData());
    }

    /**
     * 套餐营收占比
     *
     * @return 图表数据
     * @author lizhiwei
     */
    @GetMapping("/setmealCash")
    public Res<List<Map<String, Object>>> setmealCash() {
        return Res.success(buildSetmealCashData());
    }

    /**
     * 菜系销量占比
     *
     * @return 图表数据
     * @author lizhiwei
     */
    @GetMapping("/dishNum")
    public Res<List<Map<String, Object>>> dishNum() {
        return Res.success(buildDishNumData());
    }

    /**
     * 套餐销量占比
     *
     * @return 图表数据
     * @author lizhiwei
     */
    @GetMapping("/setmealNum")
    public Res<List<Map<String, Object>>> setmealNum() {
        return Res.success(buildSetmealNumData());
    }

    /**
     * 近一年月营业额趋势（万元）
     *
     * @return 月份与营业额
     * @author lizhiwei
     */
    @GetMapping("/cashPerMonth")
    public Res<Map<String, List<Object>>> cashPerMonth() {
        List<Orders> finishedOrders = listFinishedOrders();
        Map<YearMonth, BigDecimal> monthCashMap = new HashMap<>();
        for (Orders order : finishedOrders) {
            if (order == null || order.getCheckoutTime() == null || order.getAmount() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(order.getCheckoutTime());
            monthCashMap.put(month, monthCashMap.getOrDefault(month, BigDecimal.ZERO).add(order.getAmount()));
        }

        List<Object> monthList = new ArrayList<>();
        List<Object> priceList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            monthList.add(month.format(formatter));
            BigDecimal monthAmount = monthCashMap.getOrDefault(month, BigDecimal.ZERO);
            priceList.add(toWan(monthAmount));
        }

        Map<String, List<Object>> result = new HashMap<>(2);
        result.put("month", monthList);
        result.put("price", priceList);
        return Res.success(result);
    }

    private List<Orders> listFinishedOrders() {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, ORDER_STATUS_FINISHED);
        return ordersService.list(wrapper);
    }

    private List<Map<String, Object>> buildDishCashData() {
        List<OrderDetail> details = listOrderDetailsForFinishedOrders();
        if (details.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> dishNameMap = dishService.list(new LambdaQueryWrapper<Dish>().eq(Dish::getIsDeleted, 0))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Dish::getId, Dish::getName, (v1, v2) -> v1));
        Map<String, BigDecimal> sumByDish = new LinkedHashMap<>();
        for (OrderDetail detail : details) {
            if (detail == null || detail.getDishId() == null) {
                continue;
            }
            String dishName = dishNameMap.get(detail.getDishId());
            if (dishName == null || dishName.isEmpty()) {
                continue;
            }
            BigDecimal rowAmount = getRowAmount(detail);
            sumByDish.put(dishName, sumByDish.getOrDefault(dishName, BigDecimal.ZERO).add(rowAmount));
        }
        return convertAmountMapToChartData(sumByDish);
    }

    private List<Map<String, Object>> buildSetmealCashData() {
        List<OrderDetail> details = listOrderDetailsForFinishedOrders();
        if (details.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> setmealNameMap = setmealService.list(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getIsDeleted, 0))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Setmeal::getId, Setmeal::getName, (v1, v2) -> v1));
        Map<String, BigDecimal> sumBySetmeal = new LinkedHashMap<>();
        for (OrderDetail detail : details) {
            if (detail == null || detail.getSetmealId() == null) {
                continue;
            }
            String setmealName = setmealNameMap.get(detail.getSetmealId());
            if (setmealName == null || setmealName.isEmpty()) {
                continue;
            }
            BigDecimal rowAmount = getRowAmount(detail);
            sumBySetmeal.put(setmealName, sumBySetmeal.getOrDefault(setmealName, BigDecimal.ZERO).add(rowAmount));
        }
        return convertAmountMapToChartData(sumBySetmeal);
    }

    private List<Map<String, Object>> buildDishNumData() {
        List<OrderDetail> details = listOrderDetailsForFinishedOrders();
        if (details.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> dishNameMap = dishService.list(new LambdaQueryWrapper<Dish>().eq(Dish::getIsDeleted, 0))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Dish::getId, Dish::getName, (v1, v2) -> v1));
        Map<String, Integer> countByDish = new LinkedHashMap<>();
        for (OrderDetail detail : details) {
            if (detail == null || detail.getDishId() == null || detail.getNumber() == null) {
                continue;
            }
            String dishName = dishNameMap.get(detail.getDishId());
            if (dishName == null || dishName.isEmpty()) {
                continue;
            }
            countByDish.put(dishName, countByDish.getOrDefault(dishName, 0) + detail.getNumber());
        }
        return convertCountMapToChartData(countByDish);
    }

    private List<Map<String, Object>> buildSetmealNumData() {
        List<OrderDetail> details = listOrderDetailsForFinishedOrders();
        if (details.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> setmealNameMap = setmealService.list(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getIsDeleted, 0))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Setmeal::getId, Setmeal::getName, (v1, v2) -> v1));
        Map<String, Integer> countBySetmeal = new LinkedHashMap<>();
        for (OrderDetail detail : details) {
            if (detail == null || detail.getSetmealId() == null || detail.getNumber() == null) {
                continue;
            }
            String setmealName = setmealNameMap.get(detail.getSetmealId());
            if (setmealName == null || setmealName.isEmpty()) {
                continue;
            }
            countBySetmeal.put(setmealName, countBySetmeal.getOrDefault(setmealName, 0) + detail.getNumber());
        }
        return convertCountMapToChartData(countBySetmeal);
    }

    private List<OrderDetail> listOrderDetailsForFinishedOrders() {
        List<Orders> finishedOrders = listFinishedOrders();
        if (finishedOrders.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> orderIds = finishedOrders.stream()
                .map(Orders::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(OrderDetail::getOrderId, orderIds);
        return orderDetailService.list(detailWrapper);
    }

    private List<Map<String, Object>> convertAmountMapToChartData(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : source.entrySet()) {
            Map<String, Object> item = new HashMap<>(2);
            item.put("name", entry.getKey());
            item.put("value", toWan(entry.getValue()));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> convertCountMapToChartData(Map<String, Integer> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            Map<String, Object> item = new HashMap<>(2);
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            result.add(item);
        }
        return result;
    }

    private BigDecimal getRowAmount(OrderDetail detail) {
        if (detail == null || detail.getAmount() == null || detail.getNumber() == null) {
            return BigDecimal.ZERO;
        }
        return detail.getAmount().multiply(BigDecimal.valueOf(detail.getNumber()));
    }

    private BigDecimal toWan(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
    }
}
