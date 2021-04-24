/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import matecore.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase que determina el pool de conexiones a la DB central.
 *
 * @author mgiannini
 */
public class LibMySQLCentral {
    private HashMap<String, Connection> openconns = new HashMap<>();
    private static LibMySQLCentral m_conexiones;
    
    private LibMySQLCentral() throws IOException, SQLException {}
 
    public static LibMySQLCentral getInstance() throws IOException, SQLException {
        if (m_conexiones == null) {
            m_conexiones = new LibMySQLCentral();
        }
        return m_conexiones;
    }

    public Connection ConectaMysql() throws SQLException, ClassNotFoundException {

        Connection con = (openconns.get(Main.getDBConexionCentral()));

        if (con == null) {
            con = AbrirConn(Main.getDBConexionCentral(), Main.getDBUsuarioCentral(), Main.getDBPasswordCentral());
            openconns.put(Main.getDBConexionCentral(), con);
        } else {
            try {
                ResultSet rs;
                try (Statement stmt = con.createStatement()) {
                    rs = stmt.executeQuery("select 1");
                    rs.close();
                }
                rs = null;
            } catch (SQLException ex) {
                openconns.remove(Main.getDBConexionCentral());
                con = AbrirConn(Main.getDBConexionCentral(), Main.getDBUsuarioCentral(), Main.getDBPasswordCentral());
                openconns.put(Main.getDBConexionCentral(), con);
                Logger.getLogger(LibMySQLCentral.class.getName()).log(Level.FATAL, "ConectaMysql. Error: ", ex);
            }
        }
        return con;
    }
    
    private Connection AbrirConn(String conexion, String DBUser, String DBPass) throws ClassNotFoundException, SQLException {
        Connection con = null;
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection(conexion, DBUser, DBPass);
        return con;
    }
}
