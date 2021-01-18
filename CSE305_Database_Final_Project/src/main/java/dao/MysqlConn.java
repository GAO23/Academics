package dao;

import java.sql.*;




public class MysqlConn {

    private static Connection conn = null;

    public static final String URL = "jdbc:mysql://localhost:3306/Default?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    public static final String PASSWORD  = "rootpasswordgiven";

    public static final String USER = "root";

    private MysqlConn(){}


    // call this first to established a connection
    public static void initalizeConnection(){
        if(conn == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                System.out.println("debug: couldn't connect to database. Encounter this error: " + e.toString());
                e.printStackTrace();
            }
            System.out.println("debug: Connected to database successfully");
            return;
        }
    }


    // this returns the conention to the mysql server
    public static Connection getConnection(){
        return conn;
    }

    public static Statement getStatement(){
        try{
            return conn.createStatement();
        }catch (Exception e){
            System.out.println("debug: couldn't get statement. Encounter this error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }


    // resets the connection
    public static void resetConnection(){
        conn = null;
    }

    // close the connections
    public static void closeConnection(){
        try{
            conn.close();
        }catch (Exception e){
            System.out.println("debug: couldn't close connection. Encounter this error: " + e.toString());
            e.printStackTrace();
        }
    }

    // this is for running the queries
    public static ResultSet runSelectQuery(String query){
        ResultSet result = null;
        try{
            Statement statement = conn.createStatement();
            result = statement.executeQuery(query);
        }catch (Exception e){
            System.out.println("debug: couldn't run select query. Encounter this error: " + e.toString());
            e.printStackTrace();
        }
        return result;
    }

    // this is for running the queries
    public static boolean updateDeleteInsertQuery(String query){
        try{
            Statement statement = conn.createStatement();
            int result = statement.executeUpdate(query);
            return result > 0;
        }catch (Exception e){
            System.out.println("debug: couldn't run update, delete, or insert query. Encounter this error: " + e.toString());
            e.printStackTrace();
        }
        return false;
    }

}
