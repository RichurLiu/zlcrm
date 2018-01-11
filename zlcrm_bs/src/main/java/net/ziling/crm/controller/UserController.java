package net.ziling.crm.controller;

import net.ziling.crm.common.util.AdminResultVo;
import net.ziling.crm.common.util.ArgumentsValidator;
import net.ziling.crm.common.util.ResultVo;
import net.ziling.crm.common.util.UUIDTools;
import net.ziling.crm.common.wrap.AddResult;
import net.ziling.crm.common.wrap.LoginResult;
import net.ziling.crm.common.wrap.UserPermision;
import net.ziling.crm.entity.BaseUser;
import net.ziling.crm.entity.Role;
import net.ziling.crm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Description:
 * 前端控制器
 *
 * @author huaxin
 * @create 2018/01/08 19:54
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private final static String PRE_STR_OF_USERID = "a";

    @Autowired
    UserService userService;

    @RequestMapping("/login")
    @ResponseBody
    public AdminResultVo login(String username, String password, HttpSession session, HttpServletRequest request) {
        AdminResultVo resultVo = new AdminResultVo();
        BaseUser user = null;

        //验证登录参数不能为空
        if (ArgumentsValidator.checkUsernameAndPasswordNotNull(username, password)) {
            user = userService.loginByUsernameAndPassword(username, password);
        }
        //如果登录失败，设置状态码，然后返回
        if (user == null) {
            resultVo.setCode(LoginResult.USER_NOT_EXIST.getValue());
            resultVo.setMsg(LoginResult.USER_NOT_EXIST.getMsg());
            return resultVo;
        }

        //用户的密码错误
        if (user.getPassword() == null) {
            resultVo.setCode(LoginResult.PASSWORD_ERROR.getValue());
            resultVo.setMsg(LoginResult.PASSWORD_ERROR.getMsg());
            return resultVo;
        }

        //用户状态判断
        //end 用户状态判断

        //获取当前登录的管理员的权限
        Role role = userService.getUserRole(user.getUserId());
        resultVo.setAdminDatas("permission", role.getRoleId());

        List<BaseUser> adminLists;
        //如果是超级管理员，获取所有的一般管理员信息
        if (role.getRoleId().equals(UserPermision.SADMIN.getValue())) {
            adminLists = userService.getAllAdmin();
            resultVo.setAdminDatas("adminLists", adminLists);
        }

        //设置登录状态信息
        session.setAttribute("user", user);

        //登录成功之后的封装的参数
        resultVo.setCode(LoginResult.SUCCESS.getValue());
        resultVo.setMsg(LoginResult.SUCCESS.getMsg());
        resultVo.setAdminDatas("userId", user.getUserId());
        resultVo.setAdminDatas("username", user.getUsername());

        return resultVo;
    }

    @RequestMapping("/addAdmin")
    @ResponseBody
    public ResultVo addAdmin(String userId, String username, String password, String permission, HttpSession session, HttpServletRequest request) {
        ResultVo resultVo = new ResultVo();
        BaseUser user = null;

        //验证登录参数不能为空
        if (!ArgumentsValidator.checkUsernameAndPasswordNotNull(username, password)) {
            resultVo.setStatus_code(AddResult.FIELD_NULL.getValue());
            resultVo.setMsg(AddResult.FIELD_NULL.getMsg());
            return resultVo;
        }

        //验证用户名不能重复
        if (userService.getUserByUsername(username) != null) {
            resultVo.setStatus_code(AddResult.DOUBLE_USERNAME.getValue());
            resultVo.setMsg(AddResult.DOUBLE_USERNAME.getMsg());
            return resultVo;
        }

        if (userId == null) {
            userId = PRE_STR_OF_USERID + UUIDTools.getUUIDByTime_M();
        }

        //添加管理员用户信息
        user = new BaseUser(userId, username, password);

        Role role = new Role();
        role.setRoleId(permission);
        if (userService.addAdminUserAndRole(user, role) < 0) {
            resultVo.setStatus_code(AddResult.FAILED_IN_INSERT.getValue());
            resultVo.setMsg(AddResult.FAILED_IN_INSERT.getMsg());
            return resultVo;
        }

        resultVo.setStatus_code(AddResult.SUCCESS.getValue());
        resultVo.setMsg(AddResult.SUCCESS.getMsg());
        resultVo.setData(user);
        return resultVo;
    }

    @RequestMapping("/getAdmin")
    @ResponseBody
    public AdminResultVo getAdmin(String username, HttpSession session, HttpServletRequest request) {
        AdminResultVo resultVo = new AdminResultVo();
        BaseUser adminUser = null;

        //验证参数的信息是否不为空
        if (username == null || username.trim().length() <= 0) {
            resultVo.setCode(AddResult.FIELD_NULL.getValue());
            resultVo.setMsg(AddResult.FIELD_NULL.getMsg());
            return  resultVo;
        }

        adminUser = userService.getUserByUsername(username);

        //如果没有找到该管理员用户的话
        if (adminUser == null || adminUser.getUsername() ==null || adminUser.getUsername().trim().length() <=0) {
            resultVo.setCode(LoginResult.USER_NOT_EXIST.getValue());
            resultVo.setMsg(LoginResult.USER_NOT_EXIST.getMsg());
            return  resultVo;
        }

        //获取该管理员用户的权限信息
        Role role = userService.getUserRole(adminUser.getUserId());

        resultVo.setCode(AddResult.SUCCESS.getValue());
        resultVo.setMsg(AddResult.SUCCESS.getMsg());
        resultVo.setAdminDatas("userId", adminUser.getUserId());
        resultVo.setAdminDatas("username", adminUser.getUsername());
        resultVo.setAdminDatas("password", adminUser.getPassword());
        resultVo.setAdminDatas("permission", role.getRoleId());

        return resultVo;
    }

    @RequestMapping("/listAllAdmin")
    @ResponseBody
    public AdminResultVo listAllAdmin(String username, HttpSession session, HttpServletRequest request) {
        AdminResultVo resultVo = new AdminResultVo();
        BaseUser user = null;

        //验证参数
        if (username == null || username.trim().length() <=0 ) {
            resultVo.setCode(AddResult.FIELD_NULL.getValue());
            resultVo.setMsg(AddResult.FIELD_NULL.getMsg());
            return resultVo;
        }

        //获取username对应的管理员用户的信息
        user = userService.getUserByUsername(username);

        //获取当前登录的管理员的权限
        Role role = userService.getUserRole(user.getUserId());
        if (!role.getRoleId().equals("*")) {
            resultVo.setCode(AddResult.NOT_PERMISSION.getValue());
            resultVo.setMsg(AddResult.NOT_PERMISSION.getMsg());
            return resultVo;
        }

        List<BaseUser> adminLists;
        //如果是超级管理员，获取所有的一般管理员信息
        adminLists = userService.getAllAdmin();
        resultVo.setAdminDatas("adminLists", adminLists);
        resultVo.setCode(AddResult.SUCCESS.getValue());
        resultVo.setMsg(AddResult.SUCCESS.getMsg());

        return resultVo;
    }


}