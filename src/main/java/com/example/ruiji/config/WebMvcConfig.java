package com.example.ruiji.config;

import com.example.ruiji.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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

    // 基于自定义 JacksonObjectMapper 的消息转换器
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 使用自定义的 JacksonObjectMapper 进行 JSON 序列化和反序列化
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将自定义的消息转换器放在首位，保证优先使用
        converters.add(0, messageConverter);
    }

    // 其他方法如 addViewControllers、configureMessageConverters 等
}

