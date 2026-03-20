package com.example.ruiji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ruiji.common.Res;
import com.example.ruiji.pojo.User;
import com.example.ruiji.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public Res<String> getCode(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            //TODO   发送验证码
            Random random = new Random();
            String code = String.valueOf(random.nextInt(900000)+100000);
            System.out.println(code);

            session.setAttribute("code",code);
            return Res.success("发送短信成功");
        }

        //String code = RandomUtil.randomNumbers(6);
       // AliyunSmsUtil.sendSms(phone, code);
        //session.setAttribute(phone, code);
      //  redisTemplate.opsForValue().set("code:" + phone, code, 5, TimeUnit.MINUTES);
        return Res.success("发送短信失败");
    }

    @PostMapping("/login")
    public Res<User> login(@RequestBody Map<String,String> map, HttpSession session){

        String phone = map.get("phone");
        String code = map.get("code");
        Object codeInSession = session.getAttribute("code");
        if (code != null && code.equals(codeInSession)){
            //数据库不存在这个手机号表示是新用户，进行注册
            LambdaQueryWrapper<User> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            objectLambdaQueryWrapper.eq(User::getPhone,phone);
            User one = userService.getOne(objectLambdaQueryWrapper);
            if (one == null){
                one = new User();
                one.setPhone(phone);
               userService.save(one);
            }
            // 登录成功后，把前端“用户登录态”写入 Session
            // 供 LoginCheckFilter 识别，从而允许前端访问 /category 等业务接口
            session.setAttribute("phone", phone);
            if (one.getId() != null) {
                session.setAttribute("userId", one.getId());
            }
            return Res.success(one);
        }
        return Res.error("登陆失败");
    }

    @PostMapping("/loginout")
    public Res<String> loginout(HttpSession session){
        session.removeAttribute("phone");
        return Res.success("退出登录成功");
    }
}
