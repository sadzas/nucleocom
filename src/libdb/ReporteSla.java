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
public class ReporteSla {
    
    /**
     * Obtiene las llamadas atendidas SLA.
     * 
     * @param idCola Numero de COla.
     * @param camposSLA Sumatoria de los campos que indican los segundos para obtener el SLA en tiempo real.
     * @param rangoSLA Rango de tiempo hacia atras para obtener el valor SLA en tiempo real.
     * @return Array de 2 valores: El valor SLA y la totalidad de llamadas atendidas en el rango especificado.
     */
    public synchronized static int[] obtieneAtendidas(String idCola, String camposSLA, int rangoSLA) {
        LibSQLite DB = new LibSQLite().conectar();
        int retorno[] = new int[]{0,0};
        try {
            ResultSet consulta = DB.consultar("SELECT (" + camposSLA + "), SUM(total) FROM atendidas WHERE idcola = " + idCola + " AND fecha <= datetime('now','-" + rangoSLA + " minutes')");
            while (consulta.next()) {
                retorno[0] = consulta.getInt(1);
                retorno[1] = consulta.getInt(2);
            }
        } catch (SQLException db) {
            Logger.getLogger(ReporteSla.class.getName()).log(Level.ERROR, "obtieneAtendidas. Error: ", db);
        } finally {
            DB.cerrar();
        }
        return retorno;
    }
}
