/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase para el acceso a la tabla agentes de la DB central.
 *
 * @author mgiannini
 */
public class Agentes {

    

    /**
     * Se obtienen las colas y los contextos de cada agente. Se utiliza para la
     * creacion del agente en la conexion inicial. Adicionalmente, se agregan
     * las colas al mapeo de colas.
     *
     * @param idAgente Numero de ID del Agente.
     * @return Retorna las colas del Agente.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static ConcurrentHashMap<String, List<String>> agenteObtieneColas(String idAgente) throws IOException, SQLException, ClassNotFoundException {
        List<String> listado;
        ConcurrentHashMap<String, List<String>> colas = new ConcurrentHashMap<>();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT "
                    + "colas.idcola, "
                    + "contextos.contextonombre,"
                    + "agentescolas.penalty "
                    + "FROM colas "
                    + "INNER JOIN contextos ON contextos.idcontexto = colas.idcontexto "
                    + "INNER JOIN agentescolas ON agentescolas.idcola = colas.idcola "
                    + "AND agentescolas.idagente = " + idAgente + "");
            while (consulta.next()) {
                listado = new ArrayList<String>();
                listado.add(consulta.getString(2));
                listado.add(consulta.getString(3));
                colas.put(consulta.getString(1), listado);
            }
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "colaObtieneContexto. Error: ", db);
        }
        return colas;
    }

    /**
     * Se obtienen el idpausa junto a su descripcion. Se utiliza en la creacion
     * del agente en la conexion inicial.
     *
     * @param idAgente Numero de ID del Agente.
     * @return Retorna las pausas del Agente.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static ConcurrentHashMap<String, String> agenteObtienePausas(String idAgente) throws IOException, SQLException, ClassNotFoundException {
        ConcurrentHashMap<String, String> pausas = new ConcurrentHashMap<>();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT "
                    + "DISTINCT(pausas.idpausa), "
                    + "pausas.pausadescripcion "
                    + "FROM pausas "
                    + "INNER JOIN sectorespausas ON sectorespausas.idpausa = pausas.idpausa "
                    + "WHERE sectorespausas.idsector IN ("
                    + "SELECT idsector FROM colas WHERE idcola IN ("
                    + "SELECT idcola FROM agentescolas WHERE idagente = '" + idAgente + "'))");
            while (consulta.next()) {
                pausas.put(consulta.getString(1), consulta.getString(2));
            }
            pausas.put("99", "AutoPausa");
            pausas.put("100", "Supervisor");
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "agenteObtienePausas. Error: ", db);
        }
        return pausas;
    }

    /**
     * Funcion Boolean para saber si se deben grabar o no las llamadas salientes
     * del agente.
     *
     * @param idAgente Numero de ID del Agente.
     * @return Retorna un boolean de grabacion del Agente.
     * @throws java.sql.SQLException -
     * @throws java.io.IOException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static String[] agenteObtieneGrabacionSectorDepto(String idAgente) throws SQLException, IOException, ClassNotFoundException {
        String[] retorno = new String[]{};
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT "
                    + "agentegrabacion, "
                    + "idsector, "
                    + "iddepartamento "
                    + "FROM agentes "
                    + "WHERE idagente =" + idAgente + "");
            while (consulta.next()) {
                retorno = new String[]{consulta.getString(1), consulta.getString(2), consulta.getString(3)};
            }
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "agenteObtieneGrabacionSectorDepto.", db);
        }
        return retorno;
    }

    /**
     * Limpia la tabla agentes de la DB central.
     *
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static void agenteLimpiaDB() throws IOException, SQLException, ClassNotFoundException {
        Logger.getLogger("").log(Level.DEBUG, "agenteLimpiaDB | Actualiza la DB en el inicio de MATE. Se vuelve a 0 la marca temporal de logueo de los agentes.");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "UPDATE agentes SET agentelogueado = 0")) {
            stmt.executeUpdate();
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "agenteLimpiaDB | Query UPDATE en la tabla agentes de la DB.", db);
        }
    }

    /**
     * Obtiene los agentes del sistema.
     *
     * @return Retorna la totalidad de los agentes del sistema.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static List<String> agenteObtieneListado() throws IOException, SQLException, ClassNotFoundException {
        List<String> agentes = new ArrayList<>();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT agenteusuario "
                    + "FROM agentes;");
            while (consulta.next()) {
                agentes.add(consulta.getString(1));
            }
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "agenteObtieneListado. Error: ", db);
        }
        return agentes;
    }

    /**
     * Agrega un valor agente/cola.
     *
     * @param idAgente Numero de ID del agente.
     * @param idCola Numero de Cola.
     * @throws IOException -
     * @throws SQLException -
     * @throws ClassNotFoundException -
     */
    public static void agenteAgregaCola(String idAgente, String idCola) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agentescolas "
                + "(idagente, idcola) "
                + "VALUES "
                + "(" + idAgente + ", " + idCola + ");")) {
            stmt.executeUpdate();
        } catch (SQLException db) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "agenteAgregaCola. Error: ", db);
        }
    }

    /**
     * Modifica el valor de la prioridad del agente para la cola indicada.
     *
     * @param idAgente Numero de ID del Agente.
     * @param idCola Numero de Cola.
     * @param penalty Prioridad en la atencion del agente.
     * @throws SQLException -
     * @throws IOException -
     * @throws ClassNotFoundException -
     */
    public static void agenteCambiaPenalty(String idAgente, String idCola, String penalty) throws SQLException, IOException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "UPDATE agentescolas SET "
                + "penalty = " + penalty + " "
                + "WHERE idAgente = " + idAgente + " "
                + "AND idCola = " + idCola + "")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(Agentes.class.getName()).log(Level.ERROR, "colaObtieneDatos. Error: ", e);
        }
    }
}
