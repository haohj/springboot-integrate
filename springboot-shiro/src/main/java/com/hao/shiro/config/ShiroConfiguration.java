package com.hao.shiro.config;

import com.hao.shiro.filter.TokenFilter;
import com.hao.shiro.utils.RedisUtil;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * shiro配置
 */
@Configuration
public class ShiroConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ShiroConfiguration.class);

    @Bean
    public EhCacheManager getEhCacheManager() {
        EhCacheManager em = new EhCacheManager();
        em.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return em;
    }


    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }


    /**
     * 加密器：这样一来数据库就可以是密文存储，为了演示我就不开启了
     *
     * @return HashedCredentialsMatcher
     */
//    @Bean
//    public HashedCredentialsMatcher hashedCredentialsMatcher() {
//        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
//        //散列算法:这里使用MD5算法;
//        hashedCredentialsMatcher.setHashAlgorithmName("md5");
//        //散列的次数，比如散列两次，相当于 md5(md5(""));
//        hashedCredentialsMatcher.setHashIterations(2);
//        return hashedCredentialsMatcher;
//    }
    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator autoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        autoProxyCreator.setProxyTargetClass(true);
        return autoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(
            DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(AuthRealm shiroRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        securityManager.setRealm(shiroRealm);

        //关闭shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        // <!-- 用户授权/认证信息Cache, 采用EhCache 缓存 -->
        securityManager.setCacheManager(getEhCacheManager());
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager securityManager, RedisUtil redisUtil) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 添加jwt过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new TokenFilter(redisUtil));
//        filterMap.put("logout", new SystemLogoutFilter(jedisUtils));
        shiroFilter.setFilters(filterMap);

        //动态配置拦截器注入
        Map<String, String> filterRuleMap = new HashMap<>(16);
        List<Map<String, String>> perms = this.getShiroFilterProperties().getPerms();
        perms.forEach(perm -> filterRuleMap.put(perm.get("key"), perm.get("value")));

        shiroFilter.setFilterChainDefinitionMap(filterRuleMap);
        return shiroFilter;
    }

    @Bean
    public ShiroFilterProperties getShiroFilterProperties() {
        return new ShiroFilterProperties();
    }

    /**
     * 加载shiroFilter权限控制规则（从数据库读取然后配置）
     */
    private void loadShiroFilterChain(ShiroFilterFactoryBean shiroFilterFactoryBean) {
        /////////////////////// 下面这些规则配置最好配置到配置文件中 ///////////////////////
        // TODO 重中之重啊，过滤顺序一定要根据自己需要排序
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 需要验证的写 authc 不需要的写 anon
        filterChainDefinitionMap.put("/resource/**", "anon");
        filterChainDefinitionMap.put("/install", "anon");
        filterChainDefinitionMap.put("/hello", "anon");
        // anon：它对应的过滤器里面是空的,什么都没做
        log.info("##################从数据库读取权限规则，加载到shiroFilter中##################");

        // 不用注解也可以通过 API 方式加载权限规则
        Map<String, String> permissions = new LinkedHashMap<>();
        permissions.put("/users/find", "perms[user:find]");
        filterChainDefinitionMap.putAll(permissions);
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    }
}
