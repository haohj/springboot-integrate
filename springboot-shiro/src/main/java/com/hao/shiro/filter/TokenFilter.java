package com.hao.shiro.filter;

import com.hao.shiro.config.DBCache;
import com.hao.shiro.utils.JwtUtil;
import com.hao.shiro.utils.RedisUtil;
import com.hao.shiro.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author haohj
 */
@Component
public class TokenFilter implements Filter {
    @Autowired
    RedisUtil redisUtil;
    private String[] excludedUris;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        excludedUris = filterConfig.getInitParameter("exclusions").split(",");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String requestURI = this.getRequestURI(req);
        if (this.isExcludedUri(requestURI)) {
            //不过滤走
            System.out.println("===不进过滤器");
            filterChain.doFilter(servletRequest, servletResponse);
        }else {
            //这里是过滤方法
            System.out.println("===进了过滤器");

            //从header中获取token
            String token = req.getHeader("token");
            if (null == token) {
                throw new RuntimeException("illegal request，token is necessary!");
            }
            //解析token获取用户名
            String username = JwtUtil.getUsername(token);
            //根据用户名获取用户实体，在实际开发中从redis取
            User user = DBCache.USERS_CACHE.get(username);
            if (null == user) {
                throw new RuntimeException("illegal request，token is Invalid!");
            }
            //校验token是否失效，自动续期
            if (!refreshToken(token, username, user.getPassword())) {
                throw new RuntimeException("illegal request，token is expired!");
            }

            filterChain.doFilter(servletRequest,servletResponse);
        }
    }

    private boolean refreshToken(String token, String username, String password) {
        String tokenKey = "user:token" + token;
        String cacheToken = String.valueOf(redisUtil.get(tokenKey));
        if (StringUtils.isNotEmpty(cacheToken)) {
            // 校验token有效性，注意需要校验的是缓存中的token
            if (!JwtUtil.verify(cacheToken, username, password)) {
                String newToken = JwtUtil.sign(username, password);
                // 设置超时时间
                redisUtil.set(tokenKey, newToken);
                redisUtil.expire(tokenKey, 60 * 2);
            }
            return true;
        }
        return false;
    }

    private boolean isExcludedUri(String uri) {
        if (excludedUris == null || excludedUris.length <= 0) {
            return false;
        }
        for (String ex : excludedUris) {
            uri = uri.trim();
            ex = ex.trim();
            if (uri.toLowerCase().matches(ex.toLowerCase().replace("*",".*"))) {
                return true;
            }
        }
        return false;
    }

    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
