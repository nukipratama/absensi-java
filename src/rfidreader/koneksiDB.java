package rfidreader;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class koneksiDB {

    public Connection koneksi;
    public Statement stm;
    public ResultSet rs = null;

    public koneksiDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            koneksi = DriverManager.getConnection("jdbc:mysql://85.10.205.173:3307/nukipratama", "nukidb", "nukidb");
//            koneksi = DriverManager.getConnection("jdbc:mysql://localhost/rfiddb","root","");
            stm = koneksi.createStatement();
            System.out.println("sukses");
        } catch (SQLException e) {
            System.out.println("gagal");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(koneksiDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ResultSet DATA(String SQLString) {
        try {
            rs = stm.executeQuery(SQLString);
        } catch (SQLException e) {
        }
        return rs;
    }

}
