package com.example.ruiji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruiji.dto.DishDto;
import com.example.ruiji.mapper.DishMapper;
import com.example.ruiji.pojo.Dish;
import com.example.ruiji.pojo.DishFlavor;
import com.example.ruiji.service.DishFlavorService;
import com.example.ruiji.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 菜品服务实现
 *
 * @author lizhiwei
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithFlavors(DishDto dishDto) {
        if (dishDto == null) {
            return false;
        }
        // 逻辑删除标志默认 0（未删除）
        dishDto.setIsDeleted(0);

        boolean dishSaved = this.save(dishDto);
        if (!dishSaved || dishDto.getId() == null) {
            return false;
        }

        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors == null || flavors.isEmpty()) {
            return true;
        }

        List<DishFlavor> toSave = flavors.stream()
                .filter(Objects::nonNull)
                .map(f -> {
                    DishFlavor nf = new DishFlavor();
                    nf.setName(f.getName());
                    nf.setValue(f.getValue());
                    nf.setDishId(dishDto.getId());
                    nf.setIsDeleted(0);
                    return nf;
                })
                .collect(Collectors.toList());

        return dishFlavorService.saveBatch(toSave);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithFlavors(DishDto dishDto) {
        if (dishDto == null || dishDto.getId() == null) {
            return false;
        }

        Dish dbDish = this.getById(dishDto.getId());
        if (dbDish == null || dbDish.getIsDeleted() != 0) {
            return false;
        }

        boolean dishUpdated = this.updateById(dishDto);
        if (!dishUpdated) {
            return false;
        }

        // 先将旧口味逻辑删除，再插入新口味（简单可靠，避免差异合并复杂度）
        LambdaUpdateWrapper<DishFlavor> delWrapper = new LambdaUpdateWrapper<>();
        delWrapper.eq(DishFlavor::getDishId, dishDto.getId())
                .eq(DishFlavor::getIsDeleted, 0)
                .set(DishFlavor::getIsDeleted, 1);
        dishFlavorService.update(delWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors == null || flavors.isEmpty()) {
            return true;
        }

        List<DishFlavor> toSave = flavors.stream()
                .filter(Objects::nonNull)
                .map(f -> {
                    DishFlavor nf = new DishFlavor();
                    nf.setName(f.getName());
                    nf.setValue(f.getValue());
                    nf.setDishId(dishDto.getId());
                    nf.setIsDeleted(0);
                    return nf;
                })
                .collect(Collectors.toList());

        return dishFlavorService.saveBatch(toSave);
    }

    @Override
    public DishDto getDtoById(Long id) {
        if (id == null) {
            return null;
        }
        Dish dish = this.getById(id);
        if (dish == null || dish.getIsDeleted() != 0) {
            return null;
        }

        DishDto dto = new DishDto();
        BeanUtils.copyProperties(dish, dto);

        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(DishFlavor::getDishId, id)
                .eq(DishFlavor::getIsDeleted, 0);
        dto.setFlavors(dishFlavorService.list(qw));
        return dto;
    }
}

