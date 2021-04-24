/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author mgiannini
 */
public class ReporteSco {
    
    
    
    /**
     * Obtiene las llamadas perdidas cortas (SCO) para el calculo de SLA en tiempo real.
     * 
     * @param idCola Numero de Cola.
     * @param camposSCO Sumatoria de los campos que indican los segundos para obtener el SLA en tiempo real.
     * @param rangoSLA Rango de tiempo hacia atras para obtener el valor SLA en tiempo real.
     * @return Entero que indica la cantidad de llamadas perdidas.
     */
    public synchronized static int obtienePerdidas(String idCola, String camposSCO, int rangoSLA) {
        LibSQLite DB = new LibSQLite().conectar();
        int retorno = 0;
        try {
            ResultSet consulta = DB.consultar("SELECT SUM(total) - (" + camposSCO + ") FROM perdidas WHERE idcola = " + idCola + " AND fecha <= datetime('now','-" + rangoSLA + " minutes')");

            while (consulta.next()) {
                retorno = consulta.getInt(1);
            }
        } catch (SQLException db) {
            Logger.getLogger(ReporteSco.class.getName()).log(Level.ERROR, "obtienePerdidas. Error: ", db);
        } finally {
            DB.cerrar();
        }
        return retorno;
    }
}
