package com.lxinet.jeesns.dao.system;

import com.lxinet.jeesns.dao.common.IBaseDao;
import com.lxinet.jeesns.model.system.Action;
import org.apache.ibatis.annotations.Param;

/**
 * ly2017/2/14.
 */
public interface IActionDao extends IBaseDao<Action> {
    int isenable(@Param("id") Integer id);
}
