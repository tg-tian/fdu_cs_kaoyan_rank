package fdu.kaoyanrank.constant;

public class UserConstants {

    /**
     * 账号状态：禁用
     */
    public static final int DISABLED = 0;

    /**
     * 账号状态：启用
     */
    public static final int ACTIVE = 1;

    public static class Role {
        /**
         * 角色：普通用户
         */
        public static final String USER = "USER";

        /**
         * 角色：管理员
         */
        public static final String ADMIN = "ADMIN";
    }
}
