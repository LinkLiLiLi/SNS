package com.lxinet.jeesns.web.front;

import com.lxinet.jeesns.common.utils.MemberUtil;
import com.lxinet.jeesns.interceptor.UserLoginInterceptor;
import com.lxinet.jeesns.core.annotation.Before;
import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.core.utils.*;
import com.lxinet.jeesns.web.common.BaseController;
import com.lxinet.jeesns.model.member.Member;
import com.lxinet.jeesns.service.member.IMemberService;
import com.lxinet.jeesns.service.member.IMessageService;
import com.lxinet.jeesns.model.system.ActionLog;
import com.lxinet.jeesns.service.system.IActionLogService;
import com.lxinet.jeesns.service.system.IConfigService;
//import com.lxinet.jeesns.modules.weibo.service.IWeiboService;
import com.lxinet.jeesns.common.utils.ConfigUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 
 */
@Controller("memberIndexController")
@RequestMapping("/member")
public class MemberController extends BaseController {
    private static final String MEMBER_FTL_PATH = "/member/";
    @Resource
    private IMemberService memberService;
    @Resource
    private IConfigService configService;
    @Resource
    private IActionLogService actionLogService;
    @Resource
    private IMessageService messageService;
    @Resource
    private JeesnsConfig jeesnsConfig;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login(Model model,@RequestParam(value = "redirectUrl",required = false,defaultValue = "") String redirectUrl){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember != null){
            return "redirect:/member/";
        }
        model.addAttribute("redirectUrl",redirectUrl);
        return MEMBER_FTL_PATH + "/login";
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel<Member> login(Member member,@RequestParam(value = "redirectUrl",required = false,defaultValue = "") String redirectUrl){
        ResponseModel responseModel = memberService.login(member,request);
        if (StringUtils.isNotEmpty(redirectUrl) && responseModel.getCode() >= 0){
            responseModel.setCode(3);
            responseModel.setUrl(redirectUrl);
        }
        return responseModel;
    }

    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String register(){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember != null){
            return "redirect:/member/";
        }
        return MEMBER_FTL_PATH + "/register";
    }

    @RequestMapping(value = "/register",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel register(Member member,String repassword){
        Map<String,String> config = configService.getConfigToMap();
        if("0".equals(config.get(ConfigUtil.MEMBER_REGISTER_OPEN))){
            return new ResponseModel(-1,"注册功能已关闭");
        }
        if(member == null){
            return new ResponseModel(-1,"参数错误");
        }
        if(member.getName().length() < 6){
            return new ResponseModel(-1,"用户名长度最少6位");
        }
        if(!StringUtils.checkNickname(member.getName())){
            return new ResponseModel(-1,"用户名只能由中文、字母、数字、下划线(_)或者短横线(-)组成");
        }
        if(!StringUtils.isEmail(member.getEmail())){
            return new ResponseModel(-1,"邮箱格式错误");
        }
        if(member.getPassword().length() < 6){
            return new ResponseModel(-1,"密码长度最少6位");
        }
        if(!member.getPassword().equals(repassword)){
            return new ResponseModel(-1,"两次密码输入不一致");
        }
        return memberService.register(member,request);
    }

    @RequestMapping(value = "/active",method = RequestMethod.GET)
    public String active(){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            return "redirect:/member/login";
        }
        return MEMBER_FTL_PATH + "/active";
    }

    @RequestMapping(value = "/active",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel active(String randomCode){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            return new ResponseModel(-1,"请先登录");
        }
        return memberService.active(loginMember,randomCode,request);
    }

    @RequestMapping(value = "/sendEmailActiveValidCode",method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel sendEmailActiveValidCode(){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            return new ResponseModel(-1,"请先登录");
        }
        return memberService.sendEmailActiveValidCode(loginMember, request);
    }

    @RequestMapping(value = "/forgetpwd",method = RequestMethod.GET)
    public String forgetpwd(){
        return MEMBER_FTL_PATH + "/forgetpwd";
    }

    @RequestMapping(value = "/forgetpwd",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel forgetpwd(String name,String email){
        return memberService.forgetpwd(name, email, request);
    }

    @RequestMapping(value = "/resetpwd",method = RequestMethod.GET)
    public String resetpwd(String email,String token,Model model){
        model.addAttribute("email",email);
        model.addAttribute("token",token);
        return MEMBER_FTL_PATH + "/resetpwd";
    }

    @RequestMapping(value = "/resetpwd",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel resetpwd(String email,String token,String password,String repassword){
        if(StringUtils.isEmpty(password)){
            return new ResponseModel(-1,"新密码不能为空");
        }
        if(password.length() < 6){
            return new ResponseModel(-1,"密码不能少于6个字符");
        }
        if(!password.equals(repassword)){
            return new ResponseModel(-1,"两次密码输入不一致");
        }
        return memberService.resetpwd(email,token,password,request);
    }


    @RequestMapping(value = "/",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String index(Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        int loginMemberId = loginMember == null ? 0 : loginMember.getId();
        Page page = new Page(request);
        ResponseModel<ActionLog> list = actionLogService.memberActionLog(page,loginMemberId);
        model.addAttribute("actionLogModel",list);
        return MEMBER_FTL_PATH + "index";
    }


    @RequestMapping(value = "/editInfo",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String editInfo(){
        return MEMBER_FTL_PATH + "editInfo";
    }

    @RequestMapping(value = "/editBaseInfo",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel editBaseInfo(String name,String sex,String introduce){
        Member loginMember = MemberUtil.getLoginMember(request);
        return memberService.editBaseInfo(loginMember,name,sex,introduce);
    }

    @RequestMapping(value = "/editOtherInfo",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel editOtherInfo(String birthday,String qq,String wechat,String contactPhone,
                                       String contactEmail,String website){
        Member loginMember = MemberUtil.getLoginMember(request);
        return memberService.editOtherInfo(loginMember,birthday,qq,wechat,contactPhone,contactEmail,website);
    }

    @RequestMapping(value = "/avatar",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String avatar(){
        return MEMBER_FTL_PATH + "avatar";
    }

    @RequestMapping(value = "/password",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String password(){
        return MEMBER_FTL_PATH + "password";
    }

    @RequestMapping(value = "/password",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel password(String oldPassword, String newPassword, String renewPassword){
        if(StringUtils.isEmpty(oldPassword)){
            return new ResponseModel(-1,"旧密码不能为空");
        }
        if(StringUtils.isEmpty(newPassword)){
            return new ResponseModel(-1,"新密码不能为空");
        }
        if(!newPassword.equals(renewPassword)){
            return new ResponseModel(-1,"两次密码输入不一致");
        }
        Member loginMember = MemberUtil.getLoginMember(request);
        return memberService.changepwd(loginMember,oldPassword,newPassword);
    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String logout(){
        MemberUtil.setLoginMember(request,null);
        return "redirect:/member/login";
    }

    /**
     * 关注、取消关注
     * @param followWhoId
     * @return
     */
    @RequestMapping(value = "/follows/{followWhoId}",method = RequestMethod.GET)
    @ResponseBody
    public Object follows(@PathVariable(value = "followWhoId") Integer followWhoId){
        Member loginMember = MemberUtil.getLoginMember(request);
        return memberService.follows(loginMember,followWhoId);
    }

    /**
     * 查询是否已关注该用户
     * @param followWhoId
     * @return
     */
    @RequestMapping(value = "/isFollowed/{followWhoId}",method = RequestMethod.GET)
    @ResponseBody
    public Object isFollowed(@PathVariable(value = "followWhoId") Integer followWhoId){
        Member loginMember = MemberUtil.getLoginMember(request);
        return memberService.isFollowed(loginMember,followWhoId);
    }


    /**
     * 获取该登录会员的收件信息
     * @param memberId
     * @return
     */
    @RequestMapping(value = "/message",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String message(@RequestParam(value = "mid",required = false,defaultValue = "-1") Integer memberId,Model model){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        Integer loginMemberId = -1;
        if(loginMember != null){
            loginMemberId = loginMember.getId();
        }
        //获取联系人
//        ResponseModel contactMembers = messageService.listContactMembers(page, loginMemberId);
//        获取联系人
//        ResponseModel contactMembers = messageService.listContactMembers(page, memberId, loginMemberId);
//        model.addAttribute("model", contactMembers);
        return MEMBER_FTL_PATH + "message";
    }

    /**
     * 获取联系人
     * @return
     */
    @RequestMapping(value = "/listContactMembers",method = RequestMethod.GET)
    @ResponseBody
    public Object listContactMembers(){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        if (loginMember != null){
            //获取联系人
            ResponseModel contactMembers = memberService.listContactMembers(page, loginMember.getId());
            return contactMembers;
        }
        return null;
    }

    /**
     * 获取聊天记录
     * @param memberId
     * @return
     */
    @RequestMapping(value = "/messageRecords/{memberId}",method = RequestMethod.GET)
    @ResponseBody
    public Object messageRecords(@PathVariable("memberId") Integer memberId){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        if (loginMember != null){
            //获取聊天记录
            ResponseModel messageRecords = messageService.messageRecords(page, memberId, loginMember.getId());
            return messageRecords;
        }
        return null;
    }

    /**
     * 发送信息窗口
     * @param memberId
     * @param model
     * @return
     */
    @RequestMapping(value = "/sendMessageBox",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String sendMessageBox(@RequestParam(value = "mid") Integer memberId,Model model){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model, -1008, Const.INDEX_ERROR_FTL_PATH);
        }
        if(memberId == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model, -1000, Const.INDEX_ERROR_FTL_PATH);
        }
        Member findMember= memberService.findById(memberId);
        if(findMember == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model, -1005, Const.INDEX_ERROR_FTL_PATH);
        }
        model.addAttribute("member", findMember);
        return MEMBER_FTL_PATH + "sendMessageBox";
    }

    /**
     * 发送信息
     * @param content
     * @param memberId
     * @return
     */
    @RequestMapping(value = "/sendMessage",method = RequestMethod.POST)
    @ResponseBody
    public Object sendMessage(String content,Integer memberId){
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            return new ResponseModel(-1,"请先登录");
        }
        if(memberId == null){
            return new ResponseModel(-1,"请选择发送对象");
        }
        Member findMember= memberService.findById(memberId);
        if(findMember == null){
            return new ResponseModel(-1,"会员不存在");
        }
        return messageService.sentMsg(loginMember.getId(), memberId, content);
    }

    /**
     * 系统信息
     * @param model
     * @return
     */
    @RequestMapping(value = "/systemMessage",method = RequestMethod.GET)
    @Before(UserLoginInterceptor.class)
    public String systemMessage(Model model){
        Page page = new Page(request);
        Member loginMember = MemberUtil.getLoginMember(request);
        if(loginMember == null){
            return jeesnsConfig.getFrontTemplate() + ErrorUtil.error(model, -1008, Const.INDEX_ERROR_FTL_PATH);
        }
        ResponseModel messageModel = messageService.systemMessage(page, loginMember.getId(),request.getContextPath());
        model.addAttribute("messageModel",messageModel);
        return MEMBER_FTL_PATH + "systemMessage";
    }
}
