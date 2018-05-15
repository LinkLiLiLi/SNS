package com.lxinet.jeesns.service.picture.impl;

import com.lxinet.jeesns.core.consts.AppTag;
import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.enums.MessageType;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.dao.picture.IPictureDao;
import com.lxinet.jeesns.model.member.Member;
import com.lxinet.jeesns.model.picture.Picture;
import com.lxinet.jeesns.service.member.IMessageService;
import com.lxinet.jeesns.service.picture.IPictureFavorService;
import com.lxinet.jeesns.service.picture.IPictureService;
import com.lxinet.jeesns.common.utils.PictureUtil;
import com.lxinet.jeesns.core.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 * @author zchuanzhao
 * @date 2017/3/7
 */
@Service
public class PictureServiceImpl implements IPictureService {
    @Resource
    private IPictureDao pictureDao;
    @Resource
    private IPictureFavorService pictureFavorService;
    @Resource
    private IMessageService messageService;

    @Override
    public List<Picture> find(Integer foreignId) {
        return pictureDao.find(foreignId);
    }

    @Override
    public Picture findById(Integer pictureId,int loginMemberId) {
        return pictureDao.findById(pictureId,loginMemberId);
    }

    @Override
    public ResponseModel<Picture> listByPage(Page page,int loginMemberId) {
        List<Picture> list = pictureDao.listByPage(page,loginMemberId);
        ResponseModel model = new ResponseModel(0, page);
        model.setData(list);
        return model;
    }

    @Override
    public ResponseModel<Picture> listByAlbum(Page page, Integer pictureAlbumId,int loginMemberId) {
        List<Picture> list = pictureDao.listByAlbum(page,pictureAlbumId,loginMemberId);
        ResponseModel model = new ResponseModel(0, page);
        model.setData(list);
        return model;
    }

    @Override
    public int deleteByForeignId(HttpServletRequest request, Integer foreignId) {
        List<Picture> pictures = this.find(foreignId);
        PictureUtil.delete(request,pictures);
        return pictureDao.deleteByForeignId(foreignId);
    }

    @Override
    public ResponseModel delete(HttpServletRequest request, Integer pictureId) {
        Picture picture = this.findById(pictureId,0);
        PictureUtil.delete(request,picture);
        if(pictureDao.delete(pictureId) == 1){
            return new ResponseModel(1,"删除成功");
        }
        return new ResponseModel(-1,"删除失败");
    }

    @Override
    public int save(Picture picture) {
        return pictureDao.save(picture);
    }

    @Override
    public int update(Integer foreignId, String ids,String description) {
        if(StringUtils.isNotEmpty(ids)){
            String[] idsArr = ids.split(",");
            return pictureDao.update(foreignId, idsArr,description);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseModel favor(Member loginMember, int pictureId) {
        String message;
        ResponseModel<Integer> responseModel;
        Picture picture = this.findById(pictureId,loginMember.getId());
        if(pictureFavorService.find(pictureId,loginMember.getId()) == null){
            //增加
            pictureDao.favor(pictureId,1);
            picture.setFavorCount(picture.getFavorCount() + 1);
            pictureFavorService.save(pictureId,loginMember.getId());
            message = "点赞成功";
            responseModel = new ResponseModel(0,message);
            //点赞之后发送系统信息
            messageService.diggDeal(loginMember.getId(),picture.getMemberId(), AppTag.PICTURE, MessageType.PICTURE_ZAN,pictureId);
        }else {
            //减少
            pictureDao.favor(pictureId,-1);
            picture.setFavorCount(picture.getFavorCount() - 1);
            pictureFavorService.delete(pictureId,loginMember.getId());
            message = "取消赞成功";
            responseModel = new ResponseModel(1,message);
        }
        responseModel.setData(picture.getFavorCount());
        return responseModel;
    }
}
