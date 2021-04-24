/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
/**
 *
 * @author mgiannini
 */
public class IntegracionSugar {
    public static String obtengoUrlSugar(String uniqueid) throws IOException, SQLException, ClassNotFoundException {
        String urlsugar = "";
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT urlsugar "
                    + "FROM integracion_sugar "
                    + "WHERE uniqueidsugar = '" + uniqueid + "' ORDER BY idsugar DESC");
            while (consulta.next()) {
                urlsugar = consulta.getString(1);
            }
        } catch (SQLException db) {
            Logger.getLogger(IntegracionSugar.class.getName()).log(Level.ERROR, "obtengoUrlSugar", db);
        }
        return urlsugar;
    }
}