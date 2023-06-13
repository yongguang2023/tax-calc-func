package com.yg.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author wangx
 * @version 1.0
 * @date 2021/3/3 17:45
 */
@Slf4j
public class JdbcConnectionUtils {
    public static boolean testMysqlConnection(String url, String userName, String pwd) {
        boolean ifCanConn = true;
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.setLoginTimeout(1);
            conn = DriverManager.getConnection(url, userName, pwd);
        } catch (Exception e) {
            log.error("testMysqlConnection -> pwd :{}", pwd);
            ifCanConn = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {

                }
            }
        }
        return ifCanConn;
    }
}
