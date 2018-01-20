package net.ziling.crm.service.impl;

import net.ziling.crm.common.util.UUIDTools;
import net.ziling.crm.common.wrap.DeleteResult;
import net.ziling.crm.common.wrap.GetUserResult;
import net.ziling.crm.common.wrap.UpdateUserResult;
import net.ziling.crm.common.wrap.UserStatus;
import net.ziling.crm.dao.*;
import net.ziling.crm.entity.*;
import net.ziling.crm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * 用户服务接口UserService的实现部分
 *
 * @author huaxin
 * @create 2018/01/09 16:20
 */
@Service
public class UserServiceImpl implements UserService {

    private final static String DEFAULT_USERNAME = "sadmin";
    private final static String DEFAULT_PASSWORD = "sadmin";

    @Autowired
    private BaseUserMapper baseUserMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private DutyMapper dutyMapper;
    @Autowired
    private UserDutyMapper userDutyMapper;
    @Autowired
    private UserProjectMapper userProjectMapper;

    @Override
    public BaseUser loginByUsernameAndPassword(String username, String password) {
        BaseUser user;

        user = baseUserMapper.selectByUsername(username);

        //没有找到该用户的信息
        if (user == null || (user.getUsername() == null) || (user.getUsername().length() <= 0)) {
            return null;
        }

        //如果用户找到了，匹配用户的密码
        if (!user.getPassword().equals(password)) {
            user.setPassword(null);
            return user;
        }

        return user;
    }

    @Override
    public Role getUserRole(String userId) {
        System.out.println("userId:" + userId);
        UserRole userRole = userRoleMapper.getUserRoleByUserId(userId);
        return roleMapper.selectByPrimaryKey(userRole.getRoleId());
    }

    @Override
    public List<BaseUser> getAllAdmin() {
        return baseUserMapper.getAllAdmin();
    }

    @Override
    public BaseUser getUserByUsername(String username) {
        if (username == null || username.trim().length() <= 0) {
            return null;
        }
        return baseUserMapper.selectByUsername(username);
    }

    @Override
    public int addAdminUserAndRole(BaseUser user, Role role) {
        if (user == null) {
            return -1;
        }
        try {
            user.setStatus(UserStatus.ON.toString());
            baseUserMapper.insertSelective(user);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        if (role == null) {
            return -2;
        }
        try {
            /**
             * 说明：
             * 1、每个用户只能有一个role信息
             * 此处添加管理员用户的方式，首先是将BaseUser的信息写入到数据表中
             * 然后，根据userId在user_role表中查找，如果找到了相应用户的的角色信息
             * 这说明已经添加过了，此时只需要修改相关的role_id即可
             */
             UserRole userRole = userRoleMapper.getUserRoleByUserId(user.getUserId());
             if (userRole == null) {
                 userRole = new UserRole();
                 userRole.setId(UUIDTools.getUUIDId());
                 userRole.setUserId(user.getUserId());
                 userRole.setRoleId(role.getRoleId());
                 userRoleMapper.insertSelective(userRole);
             }
             userRole.setRoleId(role.getRoleId());
             userRoleMapper.updateByPrimaryKeySelective(userRole);

            /**
             * 说明：
             * 2、每个用户可以有多个role信息
             * 即每次添加的时候直接添加role即可
             */
//            UserRole userRole = new UserRole();
//            userRole.setId(UUIDTools.getUUIDId());
//            userRole.setRoleId(role.getRoleId());
//            userRole.setUserId(user.getUserId());
//            userRoleMapper.insertSelective(userRole);
        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

    @Override
    public int updateByUserId(BaseUser user, String permission) {
        int res = baseUserMapper.updateUserByUserIdSimple(user);
        System.out.println(res);
        if (res == 0) {
            return res;
        }

        res = userRoleMapper.updateUserRoleByUserId(user.getUserId(), permission);
        System.out.println(res);
        if (res == 0) {
            return -1;
        } else {
            return res;
        }
    }

    @Override
    public int deleteByUserId(String userId) {
        return baseUserMapper.deleteByUserId(userId);
    }

    @Override
    public Map<String, Object> getUserByUserId(String userId) {
        Map<String, Object> resultUser = new HashMap<>();
        String warningMessage = new String("");
        boolean warn;

        //获取基本信息
        BaseUser user = baseUserMapper.selectByUserId(userId);
        if (user == null) {
            resultUser.put("Error", GetUserResult.USER_NOT_EXIST.getValue());
            resultUser.put("ErrorMsg", GetUserResult.USER_NOT_EXIST.getMsg());
            return resultUser;
        }

        //获取dutyId
        List<String> dutyIds = userDutyMapper.selectByUserId(userId);
        //获取proId
        List<String> proIds = userProjectMapper.selectProIdByUserId(userId);

        List<Duty> duties = new ArrayList<>();
        duties.clear();
       
        List<Project> projects = new ArrayList<>();
        projects.clear();

        if(dutyIds.size() == 0){
            warningMessage += GetUserResult.DUTY_NOT_EXIST.getMsg();
        }else {
            warn = false;
            //获取duty信息
            for (String id : dutyIds) {
                Duty t = dutyMapper.selectDutyByDutyId(id);
                if (t != null) {
                    duties.add(t);
                } else {
                    warn = true;
                }
            }
            if (warn) {
                warningMessage += GetUserResult.DUTY_ID_NOT_EXIST.getMsg();
            }
        }

        if(proIds.size() == 0){
            warningMessage += GetUserResult.PROJECT_NOT_EXIST.getMsg();
        }else {
            warn = false;
            //获取project信息
            for (String id : proIds) {
                Project t = projectMapper.selectByPrimaryKey(id);
                if (t != null) {
                    projects.add(t);
                } else {
                    warn = true;
                }
            }
            if (warn) {
                warningMessage += GetUserResult.PROJECT_ID_NOT_EXIST.getMsg();
            }
        }

        resultUser.put("User", user);
        resultUser.put("Duties", duties);
        resultUser.put("Projects", projects);

        resultUser.put("Error", GetUserResult.SUCCESS.getValue());
        if(warningMessage.equals("")) {
            resultUser.put("ErrorMsg", GetUserResult.SUCCESS.getMsg());
        }else{
            resultUser.put("ErrorMsg", warningMessage);
        }

        return resultUser;
    }

    @Override
    public int addBaseUser(BaseUser baseUser) throws Exception {
        try {
            baseUser.setStatus(UserStatus.ON.toString());
            UserDuty userDuty = new UserDuty();
            userDuty.setUserId(baseUser.getUserId());
            userDuty.setDutyId("0");
            userDutyMapper.insertSelective(userDuty);
            return baseUserMapper.insertSelective(baseUser);
        } catch (Exception e) {
            throw e;
        } finally {
            return 0;
        }
    }

    @Override
    public int updateUserInf (BaseUser user){
        if (baseUserMapper.updateByUserId(user) == 0) {
            return -1;
        }
        return 0;
    }

    @Override
    public int updateUserDuty(Duty duty, String userId){
        //获取dutyId
        List<String> dutyIds = userDutyMapper.selectByUserId(userId);
        if(dutyIds.indexOf(duty.getDutyId()) == -1){
            return -1;
        }
        if (dutyMapper.updateDutyById(duty) == 0) {
            return -2;
        }
        return 0;
    }

    @Override
    public int updateUserProject(Project project, String userId){
        //获取proId
        List<String> proIds = userProjectMapper.selectProIdByUserId(userId);
        if(proIds.indexOf(project.getProId()) == -1){
            return -1;
        }
        if (projectMapper.updateByPrimaryKeySelective(project) == 0) {
            return -2;
        }
        return 0;
    }
    @Override
    public int addUserDuty(UserDuty userDuty, Duty duty) {
        if (userDuty.getUserId() == null || userDuty.getDutyId() == null ||
                userDuty.getDutyId().trim().length() <= 0 || userDuty.getUserId().trim().length() <= 0) {
            return 3;
        }

        try {
            dutyMapper.insertSelective(duty);
        }catch (Exception e) {
            e.printStackTrace();
            return 2;
        }

        try {
            userDutyMapper.updateByPrimaryKey(userDuty);
        }catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public int addUserProject(UserProject userProject, Project project) {
        if (userProject.getUserId() == null || userProject.getProId() == null ||
                userProject.getProId().trim().length() <= 0 || userProject.getUserId().trim().length() <= 0) {
            return 3;
        }

        try {
            projectMapper.insertSelective(project);
        }catch (Exception e) {
            e.printStackTrace();
            return 2;
        }

        try {
            userProjectMapper.insertSelective(userProject);
        }catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public List<BaseUser> getAllSelectedUser(Map<String, Object> limits) {

        return baseUserMapper.getAllUser(limits);
    }

    @Override
    public BaseUser judgeUserExist(String userId) {
        return baseUserMapper.selectByUserId(userId);
    }

    @Override
    public DeleteResult deleteProject(String userId, String proId){
        if(userId == null || proId == null) {
            return DeleteResult.LACK_OF_ID;
        }
        if(baseUserMapper.selectByUserId(userId) == null){
            return DeleteResult.USER_NOT_EXIST;
        }
        UserProjectKey userProjectKey = new UserProjectKey();
        userProjectKey.setProId(proId);
        userProjectKey.setUserId(userId);
        if(userProjectMapper.selectByProIdAndUserId(userId, proId) == null){
            return DeleteResult.USER_PROJECT_NOT_EXIST;
        }

        if(projectMapper.selectByPrimaryKey(proId) == null){
            return DeleteResult.ID_PROJECT_NOT_EXIST;
        }

        //删除userProject关联和project
        userProjectMapper.deleteByPrimaryKey(userProjectKey);
        projectMapper.deleteByPrimaryKey(proId);
        return DeleteResult.SUCCESS;
    }
}
