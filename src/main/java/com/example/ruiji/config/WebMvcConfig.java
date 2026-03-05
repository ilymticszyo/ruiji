package com.example.ruiji.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 重写需要自定义的方法，例如添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 暂时注释掉拦截器，因为MyInterceptor类还未创建
        // registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**");
    }

    // 添加资源处理器
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("静态资源映射配置");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    // 其他方法如 addViewControllers、configureMessageConverters 等
}

