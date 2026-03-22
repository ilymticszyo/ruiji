package com.example.ruiji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruiji.dto.SetmealDto;
import com.example.ruiji.mapper.SetmealMapper;
import com.example.ruiji.pojo.Setmeal;
import com.example.ruiji.pojo.SetmealDish;
import com.example.ruiji.service.SetmealDishService;
import com.example.ruiji.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 套餐服务实现
 *
 * @author lizhiwei
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithDishes(SetmealDto setmealDto) {
        if (setmealDto == null) {
            return false;
        }
        setmealDto.setIsDeleted(0);
        boolean saved = this.save(setmealDto);
        if (!saved || setmealDto.getId() == null) {
            return false;
        }

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return true;
        }

        List<SetmealDish> toSave = setmealDishes.stream()
                .filter(Objects::nonNull)
                .map(sd -> {
                    SetmealDish nsd = new SetmealDish();
                    nsd.setSetmealId(setmealDto.getId());
                    nsd.setDishId(sd.getDishId());
                    nsd.setName(sd.getName());
                    nsd.setPrice(sd.getPrice());
                    nsd.setCopies(sd.getCopies());
                    nsd.setSort(sd.getSort());
                    nsd.setIsDeleted(0);
                    return nsd;
                })
                .collect(Collectors.toList());

        return setmealDishService.saveBatch(toSave);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithDishes(SetmealDto setmealDto) {
        if (setmealDto == null || setmealDto.getId() == null) {
            return false;
        }

        Setmeal db = this.getById(setmealDto.getId());
        if (db == null || db.getIsDeleted() != 0) {
            return false;
        }

        boolean updated = this.updateById(setmealDto);
        if (!updated) {
            return false;
        }

        // 先将旧关联逻辑删除，再插入新关联（避免差异对比复杂度）
        LambdaUpdateWrapper<SetmealDish> delWrapper = new LambdaUpdateWrapper<>();
        delWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId())
                .eq(SetmealDish::getIsDeleted, 0)
                .set(SetmealDish::getIsDeleted, 1);
        setmealDishService.update(delWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return true;
        }

        List<SetmealDish> toSave = setmealDishes.stream()
                .filter(Objects::nonNull)
                .map(sd -> {
                    SetmealDish nsd = new SetmealDish();
                    nsd.setSetmealId(setmealDto.getId());
                    nsd.setDishId(sd.getDishId());
                    nsd.setName(sd.getName());
                    nsd.setPrice(sd.getPrice());
                    nsd.setCopies(sd.getCopies());
                    nsd.setSort(sd.getSort());
                    nsd.setIsDeleted(0);
                    return nsd;
                })
                .collect(Collectors.toList());

        return setmealDishService.saveBatch(toSave);
    }

    @Override
    public SetmealDto getDtoById(Long id) {
        if (id == null) {
            return null;
        }
        Setmeal setmeal = this.getById(id);
        if (setmeal == null || setmeal.getIsDeleted() != 0) {
            return null;
        }

        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, dto);

        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(SetmealDish::getSetmealId, id)
                .eq(SetmealDish::getIsDeleted, 0)
                .orderByAsc(SetmealDish::getSort);
        dto.setSetmealDishes(setmealDishService.list(qw));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeWithDishes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 启用中的套餐不允许删除
        LambdaQueryWrapper<Setmeal> onSaleQw = new LambdaQueryWrapper<>();
        onSaleQw.in(Setmeal::getId, ids)
                .eq(Setmeal::getIsDeleted, 0)
                .eq(Setmeal::getStatus, 1);
        long onSaleCount = this.count(onSaleQw);
        if (onSaleCount > 0) {
            throw new RuntimeException("启用中的套餐不可删除，请先停售");
        }

        LambdaUpdateWrapper<Setmeal> delSetmeal = new LambdaUpdateWrapper<>();
        delSetmeal.in(Setmeal::getId, ids)
                .eq(Setmeal::getIsDeleted, 0)
                .set(Setmeal::getIsDeleted, 1);
        boolean setmealUpdated = this.update(delSetmeal);

        LambdaUpdateWrapper<SetmealDish> delRel = new LambdaUpdateWrapper<>();
        delRel.in(SetmealDish::getSetmealId, ids)
                .eq(SetmealDish::getIsDeleted, 0)
                .set(SetmealDish::getIsDeleted, 1);
        setmealDishService.update(delRel);

        return setmealUpdated;
    }
}

