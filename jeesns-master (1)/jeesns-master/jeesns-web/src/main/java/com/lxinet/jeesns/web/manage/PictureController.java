package com.lxinet.jeesns.web.manage;

import com.lxinet.jeesns.core.annotation.Before;
import com.lxinet.jeesns.core.consts.AppTag;
import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.interceptor.AdminLoginInterceptor;
import com.lxinet.jeesns.service.picture.IPictureService;
import com.lxinet.jeesns.service.system.ITagService;
import com.lxinet.jeesns.web.common.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 *
 * 
 * 
 */
@Controller
@RequestMapping("/${managePath}/picture")
@Before(AdminLoginInterceptor.class)
public class PictureController extends BaseController{
    private static final String MANAGE_FTL_PATH = "/manage/picture/";
    @Resource
    private IPictureService pictureService;
    @Resource
    private ITagService tagService;

    @RequestMapping("/tagList")
    public String tagList(Model model){
        Page page = new Page(request);
        ResponseModel responseModel = tagService.listByPage(page, AppTag.PICTURE);
        model.addAttribute("model",responseModel);
        return MANAGE_FTL_PATH + "tagList";
    }


    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){
        Page page = new Page(request);
        ResponseModel responseModel = pictureService.listByPage(page,0);
        model.addAttribute("model",responseModel);
        return MANAGE_FTL_PATH + "list";
    }

    @RequestMapping(value = "/delete/{pictureId}",method = RequestMethod.GET)
    @ResponseBody
    public Object delete(@PathVariable("pictureId") Integer pictureId){
        return pictureService.delete(request,pictureId);
    }

}
