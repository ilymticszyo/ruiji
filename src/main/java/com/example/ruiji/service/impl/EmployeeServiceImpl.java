package com.example.ruiji.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ruiji.mapper.EmployeeMapper;
import com.example.ruiji.pojo.Employee;
import com.example.ruiji.service.EmployeeService;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {


}
