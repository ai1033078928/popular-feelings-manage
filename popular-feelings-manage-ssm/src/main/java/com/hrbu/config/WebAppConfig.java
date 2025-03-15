package com.hrbu.config;

import com.hrbu.component.LoginHandlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {
    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 静态资源 *.css *.js
        // spring boot已经做好了静态资源映射 这里不需要处理
        registry.addInterceptor(new LoginHandlerInterceptor())
                .addPathPatterns("/**")         // 拦截任意多层路径下的任意请求
                .excludePathPatterns("/index", "/", "/user/login", "/user/register", "/user/user", "/root/user")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/fonts/**");
    }
}
