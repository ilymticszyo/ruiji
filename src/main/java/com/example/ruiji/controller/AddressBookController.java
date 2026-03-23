package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.AddressBook;
import com.example.ruiji.service.AddressBookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;


    //新增地址   记得关联session中存的用户信息
    @PostMapping
    public Res<String> add(@RequestBody AddressBook addressBook, HttpSession session){
        Long uId = (Long) session.getAttribute("user");
        if (uId == null) {
            return Res.error("用户未登录");
        }
        addressBook.setUserId(uId);
        addressBookService.save(addressBook);
        return Res.success("保存地址成功");
    }


    //设置默认地址
    @PutMapping("/default")
    public Res<String> setDefault(@RequestBody AddressBook addressBook,HttpSession session){
        Long user = (Long) session.getAttribute("user");
        if (user == null) {
            return Res.error("用户未登录");
        }
        LambdaUpdateWrapper<AddressBook> qw = new LambdaUpdateWrapper<>();
        // set is_default = 0
        qw.set(AddressBook::getIsDefault,1);
        qw.eq(AddressBook::getUserId,user);// where user_id =?
        addressBookService.update(qw);
        LambdaQueryWrapper<AddressBook> aqw = new LambdaQueryWrapper<>();
        addressBook.setIsDefault(1);
        aqw.eq(AddressBook::getId,addressBook.getId());
        addressBookService.updateById(addressBook);
        return Res.success("设置成功");
    }


    //根据ID查地址
    @GetMapping("/{id}")
    public Res<AddressBook> getAddress(@PathVariable Long id){
        AddressBook byId = addressBookService.getById(id);
        return Res.success(byId);
    }

    //查询默认地址
    @GetMapping("/default")
    public Res<AddressBook> defaultAdd(HttpSession session){
        Long user = (Long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> aqw = new LambdaQueryWrapper<>();
        aqw.eq(AddressBook::getUserId,user).eq(AddressBook::getIsDefault,1);
        AddressBook one = addressBookService.getOne(aqw);
        return Res.success(one);
    }


    //查询指定用户的全部地址
    @GetMapping("/list")
    public Res<List<AddressBook>> getList(HttpSession session){
        Long user = (Long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> aqw = new LambdaQueryWrapper<>();
        aqw.eq(AddressBook::getUserId,user);
        List<AddressBook> list = addressBookService.list(aqw);
        return Res.success(list);
    }

    //删除地址  一个或者多个
    @DeleteMapping
    public Res<String> delete(@RequestParam List<Long> ids){
        addressBookService.removeByIds(ids);
        return Res.success("删除成功");
    }

    //修改地址
    @PutMapping
    public Res<String> update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return Res.success("修改成功");
    }




}
