package com.lxinet.jeesns.web.front;

import com.lxinet.jeesns.common.utils.MemberUtil;
import com.lxinet.jeesns.core.annotation.Before;
import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.exception.NotLoginException;
import com.lxinet.jeesns.core.exception.ParamException;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.core.utils.*;
import com.lxinet.jeesns.interceptor.UserLoginInterceptor;
import com.lxinet.jeesns.model.member.Member;
import com.lxinet.jeesns.model.picture.Picture;
import com.lxinet.jeesns.model.picture.PictureAlbum;
import com.lxinet.jeesns.service.member.IMemberService;
import com.lxinet.jeesns.service.picture.IPictureAlbumService;
import com.lxinet.jeesns.service.picture.IPictureCommentService;
import com.lxinet.jeesns.service.picture.IPictureService;
import com.lxinet.jeesns.web.common.BaseController;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 *
 * 
 * 
 */
@Controller("frontPictureController")
@RequestMapping("/")
public class PictureController extends BaseController {
    private static final String MEMBER_FTL_PATH = "/member/";
    @Resource
    private IPictureAlbumService pictureAlbumService;
    @Resource
    private IPictureService pictureService;
    @Resource
    private IPictureCommentService pictureCommentService;
    @Resource
    private IMemberService memberService;
    @Resource
    private JeesnsConfig jeesnsConfig;

    @RequestMapping(value = "/picture/album/{memberId}",method = RequestMethod.GET)
    public String album(Model model,@PathVariable("memberId") Integer memberId){
        Member findMember = memberService.findById(memberId);
        if (findMember == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1005, Const.INDEX_ERROR_FTL_PATH);
        }
        ResponseModel responseModel = pictureAlbumService.listByMember(memberId);
        model.addAttribute("model",responseModel);
        model.addAttribute("member",findMember);
        return jeesnsConfig.getFrontTemplate() + "/picture/album";
    }

    @RequestMapping(value = "/member/picture/album",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String album(Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        ResponseModel responseModel = pictureAlbumService.listByMember(loginMember.getId());
        model.addAttribute("model",responseModel);
        return MEMBER_FTL_PATH + "/picture/album";
    }

    @RequestMapping(value = "/member/picture/addAlbum",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String addAlbum(Model model){
        return MEMBER_FTL_PATH + "/picture/addAlbum";
    }

    @RequestMapping(value = "/member/picture/saveAlbum",method = RequestMethod.POST)
    @Before(UserLoginInterceptor.class)
    @ResponseBody
    public ResponseModel saveAlbum(PictureAlbum pictureAlbum){
        if (StringUtils.isEmpty(pictureAlbum.getName())){
            return new ResponseModel(-1,"相册名称不能为空");
        }
        Member loginMember = MemberUtil.getLoginMember(request);
        pictureAlbum.setMemberId(loginMember.getId());
        ResponseModel responseModel = pictureAlbumService.save(pictureAlbum);
        if (responseModel.getCode() == 0){
            responseModel.setCode(3);
        }
        return responseModel;
    }

    @RequestMapping(value = "/picture/list/{memberId}-{albumId}",method = RequestMethod.GET)
    public String indexList(Model model,@PathVariable("memberId") Integer memberId,@PathVariable("albumId") Integer albumId){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        PictureAlbum pictureAlbum = pictureAlbumService.findById(albumId);
        if (pictureAlbum == null || memberId.intValue() != pictureAlbum.getMemberId().intValue()){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1010, Const.INDEX_ERROR_FTL_PATH);
        }
        if (pictureAlbum.getJuri() != 0){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1012, Const.INDEX_ERROR_FTL_PATH);
        }
        ResponseModel responseModel = pictureService.listByAlbum(page,albumId,loginMemberId);
        model.addAttribute("model",responseModel);
        model.addAttribute("pictureAlbum",pictureAlbum);
        return jeesnsConfig.getFrontTemplate() + "/picture/list";
    }

    @RequestMapping(value = "/member/picture/list/{memberId}-{albumId}",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String list(Model model,@PathVariable("memberId") Integer memberId,@PathVariable("albumId") Integer albumId){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        if (loginMemberId != memberId){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1001, Const.INDEX_ERROR_FTL_PATH);
        }
        PictureAlbum pictureAlbum = pictureAlbumService.findById(albumId);
        if (pictureAlbum == null || memberId.intValue() != pictureAlbum.getMemberId().intValue()){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1010, Const.INDEX_ERROR_FTL_PATH);
        }
        ResponseModel responseModel = pictureService.listByAlbum(page,albumId,loginMemberId);
        model.addAttribute("model",responseModel);
        model.addAttribute("pictureAlbum",pictureAlbum);
        return MEMBER_FTL_PATH + "/picture/list";
    }

    @RequestMapping(value = {"/picture","/picture/"},method = RequestMethod.GET)
    public Object index(Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        Page page = new Page(1,20);
        ResponseModel responseModel = pictureService.listByPage(page,loginMemberId);
        model.addAttribute("model",responseModel);
        return jeesnsConfig.getFrontTemplate() + "/picture/index";
    }

    @RequestMapping(value = "/picture/indexData",method = RequestMethod.GET)
    @ResponseBody
    public Object indexData(){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        ResponseModel responseModel = pictureService.listByPage(page,loginMemberId);
        return responseModel;
    }

    @RequestMapping(value = "/picture/detail/{pictureId}",method = RequestMethod.GET)
    public Object detail(Model model,@PathVariable("pictureId") Integer pictureId) throws NotLoginException {
        Member loginMember = MemberUtil.getLoginMember(request);
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        Picture picture = pictureService.findById(pictureId,loginMemberId);
        if (picture == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1011, Const.INDEX_ERROR_FTL_PATH);
        }
        model.addAttribute("picture",picture);
        return jeesnsConfig.getFrontTemplate() + "/picture/detail";
    }


    @RequestMapping(value="/picture/comment/{pictureId}",method = RequestMethod.POST)
    @ResponseBody
    public Object comment(@PathVariable("pictureId") Integer pictureId, String content) throws NotLoginException {
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            throw new NotLoginException();
        }
        if(StringUtils.isEmpty(content)){
            return new ResponseModel(-1,"内容不能为空");
        }
        if(content.length() > 500){
            return new ResponseModel(-1,"评论内容不能超过500长度");
        }
        return pictureCommentService.save(loginMember,content,pictureId);
    }

    @RequestMapping(value="/picture/commentList/{pictureId}.json",method = RequestMethod.GET)
    @ResponseBody
    public Object commentList(@PathVariable("pictureId") Integer pictureId){
        Page page = new Page(request);
        if(pictureId == null){
            pictureId = 0;
        }
        return pictureCommentService.listByPicture(page,pictureId);
    }

    @RequestMapping(value="/picture/favor/{pictureId}",method = RequestMethod.GET)
    @ResponseBody
    public Object favor(@PathVariable("pictureId") Integer pictureId) throws NotLoginException, ParamException {
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            throw new NotLoginException();
        }
        if(pictureId == null) {
            throw new ParamException();
        }
        return pictureService.favor(loginMember,pictureId);
    }

    @RequestMapping(value="/member/picture/uploadPic")
    @Before(UserLoginInterceptor.class)
    public String uploadPic(Model model,Integer albumId) {
        PictureAlbum pictureAlbum = pictureAlbumService.findById(albumId);
        Member loginMember = MemberUtil.getLoginMember(request);
        if (pictureAlbum == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1010, Const.INDEX_ERROR_FTL_PATH);
        }
        if (pictureAlbum.getMemberId().intValue() != loginMember.getId().intValue()){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model,-1001, Const.INDEX_ERROR_FTL_PATH);
        }
        model.addAttribute("albumId",albumId);
        return MEMBER_FTL_PATH + "/picture/uploadPic";
    }

    @RequestMapping(value="/member/picture/uploadPic/{albumId}")
    @ResponseBody
    public Object uploadPic(@RequestParam(value = "file", required = false) MultipartFile file, @PathVariable("albumId") Integer albumId) {
        Member loginMember = MemberUtil.getLoginMember(request);
        if (loginMember == null){
            return new ResponseModel(-1,"请先登录");
        }
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."),fileName.length());
        if(suffix == null || (!".png".equals(suffix.toLowerCase()) && !".jpg".equals(suffix.toLowerCase()) && !".gif".equals(suffix.toLowerCase()) && !".jpeg".equals(suffix.toLowerCase()) && !".bmp".equals(suffix.toLowerCase()))) {
            return new ResponseModel(-1,"格式不支持");
        }
        String newFileName = UUID.randomUUID() + suffix;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String ymd = sdf.format(new Date());
        String path = Const.UPLOAD_PATH + "/images/"+ymd+"/";
        String savePath = request.getServletContext().getRealPath(path);
        File baseFile = new File(savePath);
        File targetFile = new File(baseFile, newFileName);

        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        //保存
        try {
            file.transferTo(targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PictureAlbum pictureAlbum = pictureAlbumService.findById(albumId);
            if (pictureAlbum == null){
                return new ResponseModel(-1,"相册不存在");
            }

            BufferedImage sourceImg = ImageIO.read(new FileInputStream(targetFile));
            Picture picture = new Picture();
            picture.setWidth(sourceImg.getWidth());
            picture.setHeight(sourceImg.getHeight());
            picture.setMd5(DigestUtils.md5Hex(new FileInputStream(targetFile)));
            //生成缩略图和小图片
            new ImageUtil().dealImage(targetFile);
            picture.setPath(path + newFileName);
            picture.setThumbnailPath(path + ImageUtil.THUMB_DEFAULT_PREVFIX + newFileName);
            picture.setSmallPath(path + ImageUtil.SMALL_DEFAULT_PREVFIX + newFileName);
            picture.setType(0);
            picture.setMemberId(loginMember.getId());
            picture.setAlbumId(pictureAlbum.getId());
            pictureService.save(picture);
            if (Const.DEFAULT_PICTURE_COVER.equals(pictureAlbum.getCover())){
                pictureAlbum.setCover(picture.getSmallPath());
                pictureAlbumService.update(pictureAlbum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseModel(0,"上传成功");
    }
}
