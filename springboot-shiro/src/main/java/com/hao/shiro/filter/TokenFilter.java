package com.hao.shiro.filter;

import com.hao.shiro.config.DBCache;
import com.hao.shiro.utils.JwtUtil;
import com.hao.shiro.utils.RedisUtil;
import com.hao.shiro.vo.JwtToken;
import com.hao.shiro.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author haohj
 */
public class TokenFilter extends BasicHttpAuthenticationFilter {
    private RedisUtil redisUtil;

    public TokenFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 检测Header里Authorization字段
     * 判断是否登录
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader("token");
        return authorization != null;
    }

    /**
     * 登录验证
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("token");

        JwtToken token = new JwtToken(authorization);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        getSubject(request, response).login(token);

        String username = JwtUtil.getUsername(authorization);
        //根据用户名获取用户实体，在实际开发中从redis取
        User user = DBCache.USERS_CACHE.get(username);
        if (null == user) {
            throw new RuntimeException("illegal request，token is Invalid!");
        }

        //检查是否需要更换token，需要则重新颁发
        //校验token是否失效，自动续期
        if (!refreshToken(authorization, username, user.getPassword())) {
            throw new RuntimeException("illegal request，token is expired!");
        }

        // 如果没有抛出异常则代表登入成功，返回true
        return true;
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
}
