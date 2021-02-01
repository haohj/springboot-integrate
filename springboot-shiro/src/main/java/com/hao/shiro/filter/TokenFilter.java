package com.hao.shiro.filter;

import com.hao.shiro.config.DBCache;
import com.hao.shiro.utils.RedisUtil;
import com.hao.shiro.utils.TokenUtil;
import com.hao.shiro.vo.JwtToken;
import com.hao.shiro.vo.Result;
import com.hao.shiro.vo.User;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author haohj
 */
public class TokenFilter extends BasicHttpAuthenticationFilter {
    private static Logger logger = LoggerFactory.getLogger(TokenFilter.class);
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
        Claims claims = TokenUtil.getTokenBody(authorization);
        User user = DBCache.USERS_CACHE.get(claims.getSubject());
        if (null == user) {
            throw new RuntimeException("illegal request，token is Invalid!");
        }

        //检查是否需要更换token，需要则重新颁发
        //校验token是否失效，自动续期
        if (!refreshToken(authorization, user.getUsername(), user.getPassword())) {
            throw new RuntimeException("illegal request，token is expired!");
        }

        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }

    private boolean refreshToken(String token, String username, String password) {
        String tokenKey = "user:token" + token;
        String cacheToken = String.valueOf(redisUtil.get(tokenKey));
        if (StringUtils.isNotEmpty(cacheToken)) {
            try {
                TokenUtil.getTokenBody(token);
            } catch (Exception e) {
                try {
                    TokenUtil.getTokenBody(cacheToken);
                    String newToken = TokenUtil.getToken(username, password, null);
                    System.out.println("token自动续期");
                    // 设置超时时间
                    redisUtil.set(tokenKey, newToken);
                    redisUtil.expire(tokenKey, 6 * 60 * 2);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 是否允许访问
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            try {
                this.executeLogin(request, response);
                return true;
            } catch (Exception e) {
                String msg = e.getMessage();
                this.response401(response, msg);
            }
        }
        return false;
    }

    /**
     * 重写 onAccessDenied 方法，避免父类中调用再次executeLogin
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
//        logger.info("调用onAccessDenied拒绝访问");
        this.sendChallenge(request, response);
        return false;
    }

    /**
     * 401非法请求
     *
     * @param resp
     * @param msg
     */
    private void response401(ServletResponse resp, String msg) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = httpServletResponse.getWriter();

            Result result = new Result();
            result.setResult(false);
            result.setCode(401);
            result.setMessage(msg);
            out.append(result.toString());
        } catch (IOException e) {
            logger.error("返回Response信息出现IOException异常:" + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
