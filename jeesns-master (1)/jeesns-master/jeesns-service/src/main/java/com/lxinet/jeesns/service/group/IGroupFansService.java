package com.lxinet.jeesns.service.group;

import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.model.group.GroupFans;
import com.lxinet.jeesns.model.member.Member;
import org.apache.ibatis.annotations.Param;


/**
 * Created by zchuanzhao on 16/12/26.
 */
public interface IGroupFansService {

    ResponseModel save(Member loginMember, Integer groupId);

    ResponseModel delete(Member loginMember, Integer groupId);

    ResponseModel listByPage(Page page, Integer groupId);

    GroupFans findByMemberAndGroup(@Param("groupId") Integer groupId, @Param("memberId") Integer memberId);

    /**
     * 获取用户关注的群组列表
     * @param page
     * @param memberId
     * @return
     */
    ResponseModel listByMember(Page page, Integer memberId);
}
