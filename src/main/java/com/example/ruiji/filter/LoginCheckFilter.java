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
            // 前端“用户登录态”相关接口：验证码/登录本身需要放行
            "/user/login",
            "/user/sendMsg",
            "/user/loginout",
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
        if (requestURI == null) {
            // 理论上不会出现 null；这里做保护，避免匹配器/判空的类型安全告警
            chain.doFilter(request, response);
            return;
        }
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

        // 5、如果未登录员工，则尝试使用前端用户登录态（session 中存入 phone）
        Object phone = httpRequest.getSession().getAttribute("phone");
        if (phone != null) {
            // 前端用户仅允许查询类请求，避免把后台管理的写操作暴露出来
            String method = httpRequest.getMethod();
            boolean isQueryRequest = method != null && ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method));
            if (!isQueryRequest) {
                httpResponse.setContentType("application/json;charset=utf-8");
                httpResponse.getWriter().write(JSON.toJSONString(Res.error("NOTLOGIN")));
                return;
            }
            // 可选：如果前面也写了 userId，则继续为 MyBatis-Plus 的审计字段提供 currentId
            Object userId = httpRequest.getSession().getAttribute("userId");
            if (userId instanceof Long) {
                BaseContext.setCurrentId((Long) userId);
            }
            chain.doFilter(request, response);
            return;
        }

        // 6、如果仍未登录，则返回未登录结果
        if (PATH_MATCHER.match("/employee/**", requestURI)) {
            // 员工接口请求：返回 JSON 格式的未登录结果
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.getWriter().write(JSON.toJSONString(Res.error("NOTLOGIN")));
            return;
        }

        // 后台页面请求：重定向到后台登录页
        if (requestURI != null && requestURI.startsWith("/backend/page/")) {
            httpResponse.sendRedirect("/backend/page/login/login.html");
            return;
        }

        // 前端 API 请求：返回 JSON（前端 request.js 会拦截 NOTLOGIN 并跳转到前端登录页）
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.getWriter().write(JSON.toJSONString(Res.error("NOTLOGIN")));
    }

    /**
     * 判断请求路径是否不需要登录校验（直接放行）
     * 使用 AntPathMatcher 进行通配符匹配
     */
    private boolean checkPathNoNeedProcess(String requestURI) {
        if (requestURI == null) {
            return false;
        }
        String safeRequestURI = requestURI;
        for (String pattern : NO_NEED_PATHS) {
            if (pattern == null) {
                continue;
            }
            if (PATH_MATCHER.match(pattern, safeRequestURI)) {
                return true;
            }
        }
        return false;
    }
}
 