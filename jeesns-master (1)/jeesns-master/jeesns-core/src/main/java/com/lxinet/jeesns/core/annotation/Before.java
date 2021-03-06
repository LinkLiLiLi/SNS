package com.lxinet.jeesns.core.annotation;

import com.lxinet.jeesns.interceptor.JeesnsInterceptor;

import java.lang.annotation.*;

/**
 * ly2016/11/26.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Before {
    Class<? extends JeesnsInterceptor> value();
}
