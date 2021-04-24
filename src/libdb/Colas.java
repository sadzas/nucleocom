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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase para el acceso a la tabla Colas en la DB central.
 *
 * @author mgiannini
 */
public class Colas {

    /**
     * Obtiene datos relevantes de una cola. Devuelve un Array de String
     * (String[]) con los siguientes datos: Tipo de integracion, Si graba o no,
     * Nombre de la cola, SLA, SCO y Rango SLA.
     *
     * @param idCola Numero de la Cola.
     * @return Retorna los datos de la Cola.
     * @throws java.sql.SQLException -
     * @throws java.io.IOException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static String[] colaObtieneDatos(String idCola) throws SQLException, IOException, ClassNotFoundException {
        String[] retorno = new String[]{};
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {

            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT "
                    + "integraciones.integracionnombre, "
                    + "colas.colagrabacion, "
                    + "colas.colanombre, "
                    + "colas.colasla, "
                    + "colas.colasco, "
                    + "colas.rangosla, "
                    + "colas.colapausa, "
                    + "colas.idsector, "
                    + "sectores.iddepartamento "
                    + "FROM integraciones "
                    + "INNER JOIN colas ON colas.idintegracion = integraciones.idintegracion "
                    + "INNER JOIN sectores ON colas.idsector = sectores.idsector "
                    + "WHERE colas.idcola = '" + idCola + "'");
            while (consulta.next()) {
                retorno = new String[]{consulta.getString(1), consulta.getString(2), consulta.getString(3), consulta.getString(4), consulta.getString(5), consulta.getString(6), consulta.getString(7), consulta.getString(8), consulta.getString(9)};
            }
        } catch (SQLException e) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "colaObtieneDatos. Error: ", e);
        }
        return retorno;
    }

    /**
     * Obtiene datos relevantes de la colas colas y agentes que se encuentran
     * ligados. Devuelve un HashMap (String, List[String]) con los siguientes
     * datos: IdCola, Contexto, penalty para la cola / agente.
     *
     * @param idCola Numero de la Cola.
     * @return Retorna un HashMap de la siguiente forma: (idCola, "contexto"/0).
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static ConcurrentHashMap<String, List<String>> colaObtieneContexto(String idCola) throws IOException, SQLException, ClassNotFoundException {
        List<String> temp = new ArrayList<String>();
        ConcurrentHashMap<String, List<String>> colas = new ConcurrentHashMap<>();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT contextos.contextonombre "
                    + "FROM contextos "
                    + "INNER JOIN colas ON colas.idcontexto = contextos.idcontexto "
                    + "WHERE colas.idcola = " + idCola + "");
            while (consulta.next()) {
                temp.add(consulta.getString(1));
                temp.add("0");
                colas.put(idCola, temp);
            }
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "colaObtieneContexto. Error: ", db);
        }
        return colas;
    }
}
