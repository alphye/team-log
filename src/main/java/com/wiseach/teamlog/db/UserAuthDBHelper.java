package com.wiseach.teamlog.db;

import com.mysql.jdbc.StringUtils;
import com.wiseach.teamlog.Constants;
import com.wiseach.teamlog.model.User;
import com.wiseach.teamlog.model.UserInfo;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.util.List;
import java.util.UUID;

/**
 * User: Arlen Tan
 * 12-8-10 下午2:27
 */
public class UserAuthDBHelper {
    public static final String ADMIN_USER="admin";
    public static String createFirstUser(String email){
       return createUser(ADMIN_USER,email);
    }

    public static String createUser(String name,String email) {
        if (StringUtils.isNullOrEmpty(name)) {
            name = email.replaceAll(Constants.AT_STRING,Constants.MINUS_STRING);
            name = name.replaceAll(Constants.BACK_SLASH_STRING+Constants.DOT_STRING,Constants.MINUS_STRING);
        }

        String uuidStr;
        uuidStr = PublicDBHelper.getStringData("select activateUUID as \"activateUUID\" from user where username=? and email=?",name,email);
        if (StringUtils.isNullOrEmpty(uuidStr)) {
            UUID uuid = UUID.randomUUID();
            uuidStr = uuid.toString();
            PublicDBHelper.exec("insert into user(username,email,activateUUID,createTime,disabled) values (?,?,?,DATEADD(DAY,2,current_timestamp()),false)",name,email, uuidStr);
            PublicDBHelper.exec("insert into userInfo(id) select id from user where username=?",name);
        }

        return uuidStr;
    }

    public static boolean firstUserCreated() {
        return PublicDBHelper.exist("select count(*) from user where username=?",ADMIN_USER);
    }

    public static boolean needActivate(String uuid) {
        return PublicDBHelper.exist("select count(*) from user where activateUUID=?",uuid);
    }

    public static boolean activateUser(String uuid, String newPassword) {
        return PublicDBHelper.exec("update user set password = concat(id,?),activateUUID = null where activateUUID=?",newPassword,uuid)>0;
    }

    public static boolean changePassword(String name, String newPassword) {
        return PublicDBHelper.exec("update user set password = concat(id,?) where username=? or email=?",newPassword,name,name)>0;
    }

    public static boolean userPasswordMatched(String name, String password) {
        return PublicDBHelper.exist("select count(*) from user where (username=? or email=?) and password = concat(id,?)", name, name, password);
    }

    public static boolean userDisabled(String name) {
        return PublicDBHelper.exist("select count(*) from user where disabled = true and (username=? or email=?)",name,name);
    }

    public static User getAuthUserByName(String username) {
        return PublicDBHelper.query("select id as \"id\",password as \"password\",username as \"username\" from user where username=? or email=?",new BeanHandler<User>(User.class),username,username);
    }

    public static void saveLoginKey(String key,Long userId,String username,String ip,Integer expired) {
        if (PublicDBHelper.exist("select count(*) from onlineUser where cookieKey=?",key)) return;
        PublicDBHelper.exec("insert into onlineUser(cookieKey,userId,username,ip,expiredTime) values(?,?,?,?,DATEADD(DAY,?,current_timestamp()))",key,userId,username,ip,expired);
    }

    public static User getUserByCookieKey(String cookieKey) {

        Long onlineUserId = PublicDBHelper.getLongData("select userId as \"userId\" from onlineUser where cookieKey=?",cookieKey);
        if (onlineUserId>0) {
            return PublicDBHelper.query("select id as \"id\",username as \"username\" from user where id=?",new BeanHandler<User>(User.class),onlineUserId);
        } else {
            return null;
        }
    }

    public static void clearExpiredUser() {
        PublicDBHelper.exec("delete FROM onlineUser where expiredTime < CURRENT_TIMESTAMP()");
    }

    public static void removeOnlineUser(String cookieKey) {
        PublicDBHelper.exec("delete from onlineUser where cookieKey=?",cookieKey);
    }

    public static UserInfo getProfile(Long userId) {
        return PublicDBHelper.query("select u.id as \"id\",u.username as \"username\",u.password as \"password\",u.email as \"email\",ui.description as \"description\",ui.mobile as \"mobile\",ui.note as \"note\",ui.qq as \"qq\",ui.telephone as \"telephone\" from user u inner join userInfo ui on(u.id = ui.id) where u.id = ?",new BeanHandler<UserInfo>(UserInfo.class),userId);
    }

    public static void updateProfile(UserInfo userInfo) {
        PublicDBHelper.exec("update user set username=? where id=?",userInfo.getUsername(),userInfo.getId());
        PublicDBHelper.exec("update userInfo set description=?,mobile=?,note=?,qq=?,telephone=? where id = ?",userInfo.getDescription(),
                userInfo.getMobile(),userInfo.getNote(),userInfo.getQq(),userInfo.getTelephone(),userInfo.getId());
    }

    public static void removeExpiredUUID() {
        // remove all not activated user.
        PublicDBHelper.exec("delete from userInfo where id in (select id from user where activateUUID is not null and createTime<CURRENT_TIMESTAMP())");
        PublicDBHelper.exec("delete from user where activateUUID is not null and createTime<CURRENT_TIMESTAMP()");
        // clear all unreset user password.
        PublicDBHelper.exec("update user set resetUUID = null where resetUUID is not null");
    }

    public static boolean needResetPassword(String uuid) {
        return PublicDBHelper.exist("select count(*) from user where resetUUID=?",uuid);
    }

    public static boolean resetPassword(String uuid, String newPassword) {
        return PublicDBHelper.exec("update user set password = concat(id,?),resetUUID = null where resetUUID=?",newPassword,uuid)>0;
    }

    public static boolean emailHasRegistered(String resetEmail) {
        return PublicDBHelper.exist("select count(*) from user where email=?",resetEmail);
    }

    public static String updateResetUUID(String resetEmail) {
        UUID uuid = UUID.randomUUID();

        String uuidStr = uuid.toString();
        PublicDBHelper.exec("update user set resetUUID=? where email=?", uuidStr,resetEmail);

        return uuidStr;
    }

    public static String getAdminEmail() {
        return PublicDBHelper.getStringData("select email as \"email\" from user where username=?",ADMIN_USER);
    }

    public static void updateUserAvatar(Long userId, String filename) {
        PublicDBHelper.exec("update userInfo set avatar=? where id=?",filename,userId);
    }

    public static String getUserAvatar(Long userId) {
        return PublicDBHelper.getStringData("select avatar as \"avatar\" from userInfo where id = ?",userId);
    }

    public static List<User> getUserList() {
        // todo: implement pager.
        return PublicDBHelper.query("select u.id as \"id\", u.username as \"username\" , u.email as \"email\", u.password as \"password\", u.activateUUID as \"activateUUID\", u.resetUUID as \"resetUUID\", u.createTime as \"createTime\", u.disabled as \"disabled\" , "
        		+ "ui.avatar as \"avatar\" from user u inner join userInfo ui on (u.id = ui.id)",new BeanListHandler<User>(User.class));
    }

    public static boolean usernameUsed(String username) {
        return PublicDBHelper.exist("select count(*) from user where username = ?",username);
    }

    public static void updateUserState(Long userId, Boolean disabled) {
        PublicDBHelper.exec("update user set disabled=? where id = ?",disabled,userId);
    }
}
