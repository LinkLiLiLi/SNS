package com.lxinet.jeesns.service.system;

import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.model.system.ActionLog;

/**
 * ly2017/2/14.
 */
public interface IActionLogService {

    ResponseModel<ActionLog> listByPage(Page page, Integer memberId);

    ResponseModel<ActionLog> memberActionLog(Page page, Integer memberId);

    ActionLog findById(Integer id);

    void save(String actionIp,Integer memberId, Integer actionId);

    void save(String actionIp,Integer memberId, Integer actionId,String remark);

    void save(String actionIp,Integer memberId, Integer actionId,String remark, Integer type, Integer foreignId);

}
