package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.Employee;
import com.example.ruiji.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public Res<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        /*
        1、将页面提交的密码password进行md5加密处理
2、根据页面提交的用户名username查询数据库
3、如果没有查询到则返回登录失败结果
4、密码比对，如果不一致则返回登录失败结果
5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
6、登录成功，将员工id存入Session并返回登录成功结果
         */
        try {
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        String userName = employee.getUsername();
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getUsername, userName);
        Employee one = employeeService.getOne(employeeLambdaQueryWrapper);
        if (one == null) {
            return Res.error("不存在该用户");
        }
        if (!password.equals(one.getPassword())) {
            return Res.error("密码错误");
        }
        if (one.getId() != 1) {
            return Res.error("该员工账号未启用");
        }
        request.getSession().setAttribute("employee", employee.getId());
        return Res.success(employee);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
