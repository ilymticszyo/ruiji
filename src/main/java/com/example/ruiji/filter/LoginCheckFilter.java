package com.example.ruiji.filter;

import com.alibaba.fastjson.JSON;
import com.example.ruiji.common.BaseContext;
import com.example.ruiji.common.Res;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher; 

import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 不需要登录校验的路径（Ant 风格通配符） */
    private static final String[] NO_NEED_PATHS = {
            "/employee/login",
            "/employee/logout",
            "/backend/page/login/login.html",
            "/backend/plugins/**",
            "/backend/styles/**",
            "/backend/js/**",
            "/backend/api/**",
            "/backend/images/**",
            "/backend/data/**",
            "/backend/favicon.ico",
            "/front/**"
    };
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1、获取本次请求的URI
        String requestURI = httpRequest.getRequestURI();
        log.info("拦截到请求: {}", requestURI);

        // 2、判断本次请求是否需要处理
        // 不需要处理的路径：登录、登出、登录页、静态资源、前端页面
        if (checkPathNoNeedProcess(requestURI)) {
            // 3、如果不需要处理，则直接放行
            chain.doFilter(request, response);
            return;
        }

        // 4、判断登录状态，如果已登录，则直接放行
        Object employee = httpRequest.getSession().getAttribute("employee");
        if (employee != null) {
            // 将当前登录用户id存入ThreadLocal
            BaseContext.setCurrentId((Long) employee);
            chain.doFilter(request, response);
            return;
        }
        

        // 5、如果未登录则返回未登录结果
        if (PATH_MATCHER.match("/employee/**", requestURI)) {
            // 接口请求：返回 JSON 格式的未登录结果（前端 request.js 会拦截 NOTLOGIN 并跳转登录页）
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.getWriter().write(JSON.toJSONString(Res.error("NOTLOGIN")));
        } else {
            // 页面请求：重定向到登录页
            httpResponse.sendRedirect("/backend/page/login/login.html");
        }
    }

    /**
     * 判断请求路径是否不需要登录校验（直接放行）
     * 使用 AntPathMatcher 进行通配符匹配
     */
    private boolean checkPathNoNeedProcess(String requestURI) {
        for (String pattern : NO_NEED_PATHS) {
            if (PATH_MATCHER.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
 