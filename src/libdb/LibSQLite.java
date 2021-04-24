/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libdb;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import matecore.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author mgiannini
 */
public class LibSQLite {

    private static Connection connectSQLite;
    File file = new File(Main.getDBSQLiteName());

    public LibSQLite crearDB() {
        try {
            if (file.exists()) {
                Logger.getLogger(LibSQLite.class.getName()).log(Level.INFO, ".:SQLite:. .:Crea DB Local:.");
                file.delete();
            }
            Class.forName("org.sqlite.JDBC");
            connectSQLite = DriverManager.getConnection("jdbc:sqlite:" + file);
            sqliteCreaTablas();
            connectSQLite.close();
        } catch (ClassNotFoundException | SQLException e) {
            Logger.getLogger(LibSQLite.class.getName()).log(Level.FATAL, "Conexion SQLite Fallida: " + e);
        }
        return this;
    }

    public LibSQLite conectar() {
        try {
            Class.forName("org.sqlite.JDBC");
            connectSQLite = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (ClassNotFoundException | SQLException e) {
            Logger.getLogger(LibSQLite.class.getName()).log(Level.FATAL, "Conexion SQLite Fallida: " + e);
        }
        return this;
    }

    public LibSQLite cerrar() {
        try {
            connectSQLite.close();
        } catch (SQLException e) {
            Logger.getLogger(LibSQLite.class.getName()).log(Level.FATAL, "cerrar. Error: ", e);
        }
        return this;
    }

    public boolean ejecutar(String sql) {
        try {
            Statement sentencia = getConexion().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            sentencia.executeUpdate(sql);
        } catch (SQLException e) {
            Logger.getLogger(LibSQLite.class.getName()).log(Level.ERROR, "ejecutar. Error: ", e);
            return false;
        }
        return true;
    }
    
    public ResultSet consultar(String sql) {
        ResultSet resultado = null;
        try {
            Statement sentencia = getConexion().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultado = sentencia.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(LibSQLite.class.getName()).log(Level.ERROR, "Fallo consulta a la DB. Query: " + sql);
        }
        return resultado;
    }

    public static void sqliteCreaTablas() {
        try {
            LibSQLite conectar = new LibSQLite();
            conectar.ejecutar("CREATE TABLE `atendidas` (`fecha` varchar(100) NOT NULL,`idcola` SMALLINT NOT NULL,`a0005` SMALLINT DEFAULT 0,`a0610` SMALLINT DEFAULT 0,`a1120` SMALLINT DEFAULT 0,`a2130` SMALLINT DEFAULT 0,`a3140` SMALLINT DEFAULT 0,`a4150` SMALLINT DEFAULT 0,`a5160` SMALLINT DEFAULT 0,`a6170` SMALLINT DEFAULT 0,`a7180` SMALLINT DEFAULT 0,`a8190` SMALLINT DEFAULT 0,`a91100` SMALLINT DEFAULT 0,`a101110` SMALLINT DEFAULT 0,`a111120` SMALLINT DEFAULT 0,`a121150` SMALLINT DEFAULT 0,`a151180` SMALLINT DEFAULT 0,`a181210` SMALLINT DEFAULT 0,`a211240` SMALLINT DEFAULT 0,`a241270` SMALLINT DEFAULT 0,`a271300` SMALLINT DEFAULT 0,`a301` SMALLINT DEFAULT 0,`total` SMALLINT DEFAULT 0, PRIMARY KEY(`fecha`,`idcola`))");
            conectar.ejecutar("CREATE TABLE `perdidas` (`fecha` varchar(100) NOT NULL,`idcola` SMALLINT NOT NULL,`a0005` SMALLINT DEFAULT 0,`a0610` SMALLINT DEFAULT 0,`a1120` SMALLINT DEFAULT 0,`a2130` SMALLINT DEFAULT 0,`a3140` SMALLINT DEFAULT 0,`a4150` SMALLINT DEFAULT 0,`a5160` SMALLINT DEFAULT 0,`a6170` SMALLINT DEFAULT 0,`a7180` SMALLINT DEFAULT 0,`a8190` SMALLINT DEFAULT 0,`a91100` SMALLINT DEFAULT 0,`a101110` SMALLINT DEFAULT 0,`a111120` SMALLINT DEFAULT 0,`a121150` SMALLINT DEFAULT 0,`a151180` SMALLINT DEFAULT 0,`a181210` SMALLINT DEFAULT 0,`a211240` SMALLINT DEFAULT 0,`a241270` SMALLINT DEFAULT 0,`a271300` SMALLINT DEFAULT 0,`a301` SMALLINT DEFAULT 0,`total` SMALLINT DEFAULT 0, PRIMARY KEY(`fecha`,`idcola`))");
            conectar.cerrar();
            Logger.getLogger(LibSQLite.class.getName()).log(Level.INFO, ".:SQLite:. .:Se generan las tablas:.");
        } catch (Exception e) {
            Logger.getLogger(LibSQLite.class.getName()).log(Level.FATAL, "sqliteCreaTablas. Error: ", e);
        }
    }

    public Connection getConexion() {
        return connectSQLite;
    }
}
