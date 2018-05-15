package com.lxinet.jeesns.service.common;

import java.util.List;

/**
 * ly16/9/29.
 */
public interface IBaseService<T> {

    T get(Integer id);

    int save(T entity);

    int update(T entity);

    int delete(Integer id);

    List allList();
}
