package com.lxinet.jeesns.dao.common;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作基类接口
 * ly2016/9/26.
 * @param <T>
 */
public interface IBaseDao<T> {

    T findById(@Param("id") Integer id);

    int save(T entity);

    int update(T entity);

    int delete(@Param("id") Integer id);

    List<T> allList();
}
