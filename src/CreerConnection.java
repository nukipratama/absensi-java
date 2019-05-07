import java.sql.DriverManager;
import java.sql.SQLException;

public class CreerConnection {

    static String driver = "com.mysql.jdbc.Driver";
    static String DB_username = "nukidb";
    static String DB_password = "nukidb";
    static String DB_URL = "jdbc:mysql://85.10.205.173:3307/nukipratama";

    public static void main(String[] args) {
        try {
            Class.forName(driver);
            java.sql.Connection con = DriverManager.getConnection(DB_URL, DB_username, DB_password);
            System.out.println("Success");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Exception : " + e);
        }
    }
}