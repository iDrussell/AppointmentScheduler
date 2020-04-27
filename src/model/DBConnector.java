/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author dalton
 */
public class DBConnector {
    private static final String DBNAME = "Testing";
    private static final String DBURI = "jdbc:mysql://127.0.0.1/" + DBNAME +"?serverTimezone=UTC";
    private static final String DBUSERNAME = "root";
    private static final String DBPASSWORD = "TestPassword!!";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static Connection conn;
    
    public static void dbConnect() throws ClassNotFoundException, SQLException {
        
        Class.forName(DRIVER);
        conn = DriverManager.getConnection(DBURI, DBUSERNAME, DBPASSWORD);

        System.out.println("Connection successful");
    }
    
    public static void dbClose() throws ClassNotFoundException, SQLException {
        conn.close();
        System.out.println("Connection closed");
    }
    
    public static Connection getConnection() {
        return conn;
    }
}
