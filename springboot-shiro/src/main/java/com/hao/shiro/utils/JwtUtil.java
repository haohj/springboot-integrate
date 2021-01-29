package com.hao.shiro.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {
    public static String sign(String username, String password) {
        Algorithm algorithm = Algorithm.HMAC256(password);

        String currentTimeMillis = String.valueOf(System.currentTimeMillis());

        Date date = new Date(System.currentTimeMillis() + 60 * 1000L);

        String token = JWT.create()
                .withClaim("account", username)
                .withClaim("currentTimeMillis", currentTimeMillis)
                .withExpiresAt(date)
                .sign(algorithm);
        return token;
    }

    public static String getUsername(String token) {
        String username = JWT.decode(token).getClaim("account").asString();
        return username;
    }

    public static boolean verify(String cacheToken, String username, String password) {
        try {
            // 根据密码生成JWT效验器
            Algorithm algorithm = Algorithm.HMAC256(password);
            JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
            // 效验TOKEN
            DecodedJWT jwt = verifier.verify(cacheToken);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
