package com.hao.shiro.controller;

import com.auth0.jwt.JWT;
import com.hao.shiro.utils.JwtUtil;
import com.hao.shiro.utils.RedisUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    RedisUtil redisUtil;

    @GetMapping(value = "/hello")
    public String hello() {
        log.info("不登录也可以访问...");
        return "hello...";
    }

    @GetMapping(value = "/index")
    public String index() {
        log.info("登陆成功了...");
        return "index";
    }

    @GetMapping(value = "/denied")
    public String denied() {
        log.info("小伙子权限不足,别无谓挣扎了...");
        return "denied...";
    }

    @GetMapping(value = "/login")
    public String login(String username, String password) {
        // 想要得到 SecurityUtils.getSubject() 的对象．．访问地址必须跟 shiro 的拦截地址内．不然后会报空指针
        Subject sub = SecurityUtils.getSubject();
        // 用户输入的账号和密码,,存到UsernamePasswordToken对象中..然后由shiro内部认证对比,
        // 认证执行者交由 com.battcn.config.AuthRealm 中 doGetAuthenticationInfo 处理
        // 当以上认证成功后会向下执行,认证失败会抛出异常
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
        try {
            sub.login(usernamePasswordToken);

            //生成token并设置过期时间为一分钟
            String token = JwtUtil.sign(username, password);
            System.out.println("token:" + token);
            String claim = JWT.decode(token).getClaim("account").asString();
            System.out.println(claim);

            //将token缓存到redis并设置过期时间为token过期时间的2倍
            String tokenKey = "user:token" + token;
            redisUtil.set(tokenKey, token);
            redisUtil.expire(tokenKey, 60 * 2);
        } catch (UnknownAccountException e) {
            log.error("对用户[{}]进行登录验证,验证未通过,用户不存在", username);
            usernamePasswordToken.clear();
            return "UnknownAccountException";
        } catch (LockedAccountException lae) {
            log.error("对用户[{}]进行登录验证,验证未通过,账户已锁定", username);
            usernamePasswordToken.clear();
            return "LockedAccountException";
        } catch (ExcessiveAttemptsException e) {
            log.error("对用户[{}]进行登录验证,验证未通过,错误次数过多", username);
            usernamePasswordToken.clear();
            return "ExcessiveAttemptsException";
        } catch (AuthenticationException e) {
            log.error("对用户[{}]进行登录验证,验证未通过,堆栈轨迹如下", username, e);
            usernamePasswordToken.clear();
            return "AuthenticationException";
        }
        return "success";
    }
}