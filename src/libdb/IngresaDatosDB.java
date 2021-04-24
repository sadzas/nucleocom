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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase para el acceso a la tabla agenteslog en la DB central.
 *
 * @author mgiannini
 */
public class IngresaDatosDB {
    
    /**
     * Conjunto de metodos para el logueo de agente en la DB.
     * 
     * @param idAgente ID unico de agente.
     * @param agenteExtension Extension que el agente utiliza para loguearse.
     */
    public static void agenteLogueo(String idAgente, String agenteExtension) {
        try {
            metodoParaMarcaTemporalLogueo(idAgente, agenteExtension);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB. ", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso del deslogueo de agente en la DB.
     * 
     * @param idAgente ID unico de Agente.
     * @param fechaInicio Fecha de login del agente.
     * @param fechaFinaliza Fecha del unlogin del agente.
     * @param eventoLogueo ID unico del evento de Logueo. Codigo de Logueo = 1
     * @param agenteExtension Extension del agente.
     */
    public static void agenteDeslogueo(String idAgente, int eventoLogueo, long fechaInicio, long fechaFinaliza, String agenteExtension) {
        try {
            agenteQueueMemberRemove(idAgente, eventoLogueo, fechaInicio, fechaFinaliza, agenteExtension);
            metodoParaMarcaTemporalDeslogueo(idAgente);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso de la marca temporal de deslogueo en la DB.
     * 
     * @param idagente ID unico de agente.
     */
    public static void agenteMarcaExtensionDesconectada(String idagente,long fechainicio, long fechaFinaliza, long tiempoTotal, String extension) {
        try {
            agentesLogExtensionDesconectada(idagente, fechainicio, fechaFinaliza, tiempoTotal, extension);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso de la pausa en la DB.
     * 
     * @param idAgente ID unico de agente.
     * @param fechaInicio Fecha de incio de la Pausa.
     * @param fechaFinaliza Fecha de fin de la Pausa.
     * @param tiempoTotal Tiempo total de la pausa.
     * @param pausaNombre Detalle de la pausa.
     */
    public static void agentePausa(String idAgente, int eventoPausa, long fechaInicio, long fechaFinaliza, long tiempoTotal, String pausaNombre) {
        try {
            agentesLogQueueMemberPause(idAgente, eventoPausa, fechaInicio, fechaFinaliza, tiempoTotal, pausaNombre);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso de datos SLA y en RealTime.
     * 
     * @param idCola ID unico de la cola de atencion.
     * @param fecha Fecha y hora de la atencion.
     * @param campo Segudos en los que se atendio la llamada.
     */
    public static void agenteConnect(String idCola, long fecha, String campo) {
        try {
            agentesConnect(idCola, fecha, campo);
            agentesConnectRT(idCola, fecha, campo);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el evento RINGNOANSWER en la DB.
     * 
     * @param idAgente ID Unico del agente.
     * @param fechaInicio Fecha de inicio del Ring.
     * @param fechaFinaliza Fecha de fin del Ring.
     * @param tiempoTotal Tiempo total del Ring.
     * @param eventoNoAtiende Codigo unico del evento RINGNOANSWER = 3.
     * @param detalle Numero de Cola en caso de entrante. Saliente en case de llamada Saliente.
     * @param data1 Numero origen de la llamada en caso de entrante. Numero destino en caso de llamada Saliente.
     * @param linkedid ID unico de la llamada.
     */
    public static void agenteNoAtiende(String idAgente, int eventoNoAtiende, long fechaInicio, long fechaFinaliza, long tiempoTotal, String detalle, String data1, String linkedid) {
        try {
            agentesLogRingNoAnswer(idAgente, eventoNoAtiende, fechaInicio, fechaFinaliza, tiempoTotal, detalle, data1, linkedid);
            reporteAgenteRingNoAnswer(idAgente, fechaInicio, detalle);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para ingresar a la DB el hold del agente.
     * 
     * @param idAgente ID unico del agente.
     * @param fechaInicio Fecha de inicio del Hold.
     * @param fechaFinaliza Fecha de finalizacion del Hold.
     * @param eventoHold ID unico del evento Hold.
     * @param linkedid ID unico de la llamada.
     */
    public static void agenteHold(String idAgente, long fechaInicio, long fechaFinaliza, int eventoHold, String linkedid) {
        try {
            agentesHold(idAgente, fechaInicio, fechaFinaliza, eventoHold, linkedid);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso a la DB del evento Queue Abandon.
     * 
     * @param idCola ID unico de cola de atencion.
     * @param fechaIngreso Fecha de ingreso de la llamada.
     * @param esperaCola Tiempo (en segundos) de espera de la llamada.
     * @param origen Numero origen de la llamada.
     * @param campo Campo calculado del abandono de la llamada.
     * @param posicionInicio Posicion en la cola de atencion al ingreso de la llamada.
     * @param posicionFinaliza Posicion en la cola de atencion al salir de la llamada.
     * @param linkedid 
     */
    public static void QueueCallerAbandonEvent(String idCola, long fechaIngreso, long esperaCola, String origen, String campo, int posicionInicio, int posicionFinaliza, String linkedid) {
        try {
            colasabandonadas(idCola, fechaIngreso, esperaCola, origen, posicionInicio, posicionFinaliza, linkedid);
            reportegeneralPERDIDAS(idCola, fechaIngreso, esperaCola);
            reportesco(idCola, fechaIngreso, campo);
            perdidasRT(idCola, fechaIngreso, campo);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso de llamadas salientes a la DB.
     * 
     * @param idAgente
     * @param fechaDiscado
     * @param fechaFinaliza
     * @param tiempoTotal
     * @param tiempoEfectivo
     * @param esperaHold
     * @param destino
     * @param estado
     * @param uniqueId 
     */
    public static void agenteSalientes(String idAgente, String fechaDiscado, String fechaFinaliza, long tiempoTotal, long tiempoEfectivo, long esperaHold, String destino, String estado, String uniqueId) {
        try {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.DEBUG, "Linkedid: " + uniqueId + " | idAgente: "+ idAgente +" | agentesSalientes | Ingreso en la DB la llamada saliente.");
            salienteslog(idAgente, fechaDiscado, fechaFinaliza, tiempoTotal, tiempoEfectivo, esperaHold, destino, estado, uniqueId);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para ingresar la llamada finalizada del agente.
     * 
     * @param idAgente ID unico de agente.
     * @param eventoHabla ID unico del evento Habla = 5.
     * @param fechaIngreso Fecha de ingreso de la llamada.
     * @param tiempoRingCola Tiempo de espera en la cola hasta ser atendida.
     * @param fechaInicio Fecha de inicio de la llamada.
     * @param tiempoRingAgente Tiempo de ringueo en el agente hasta ser atendida.
     * @param fechaFinaliza Fecha de finalizacion de la llamada.
     * @param tiempoTotal Tiempo total de la conversacion.
     * @param idCola ID de la cola en caso de ingreso por cola de atencion. Saliente en caso de llamada saliente.
     * @param data1 Numero del extremo de la llamada.
     * @param razon Razon de corte de la llamada.
     * @param holdAcumulado Tiempo de espera acumulado en la llamada.
     * @param linkedid  ID unico de la llamada.
     */
    public static void agentCompleteEvent(String idAgente, int eventoHabla, long fechaIngreso, long tiempoRingCola, long fechaInicio, long tiempoRingAgente, long fechaFinaliza, long tiempoTotal, String idCola, String data1, String razon, long holdAcumulado, String linkedid) {
        try {
            agenteslogHabla(idAgente, eventoHabla, fechaInicio, fechaFinaliza, tiempoTotal, idCola, data1, linkedid);
            reporteAgenteAtendidas(idAgente, idCola, fechaInicio,tiempoTotal, razon, holdAcumulado);
            colasatendidas(idCola, idAgente, fechaIngreso, fechaInicio, fechaFinaliza, data1, tiempoTotal, tiempoRingAgente, tiempoRingCola, holdAcumulado, razon, linkedid);
            reporteGeneralAtendida(idCola, fechaIngreso, tiempoTotal, tiempoRingCola, holdAcumulado);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "Agente: Ejecucion de Ingresos a la DB.", e);
        }
    }
    
    /**
     * Conjunto de metodos para el ingreso de la trazabilidad de las llamadas.
     * 
     * @param idAgente ID unico de agente.
     * @param idEvento ID unico de evento.
     * @param linkedid ID unico de llamada.
     * @param fecha Fecha de inicio del evento.
     * @param origen Numero origen del evento.
     * @param destino Numero destino del evento.
     * @param canal Canal de la llamada.
     * @throws IOException 
     */
    public static void metodosTrazables(String idAgente, int idEvento, String linkedid, long fecha, String origen, String destino, String canal) throws IOException {
        try {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.DEBUG, "Linkedid: " + linkedid + " | idAgente: "+ idAgente +" | metodosTrazables | Ingreso en la DB la trazabilidad de la llamada.");
            llamadaslog(idAgente, idEvento, linkedid, fecha, origen, destino, canal);
        } catch (ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "metodosTrazables", e);
        }
    }
    
    /**
     * Metodos para la carga del log de grabacion
     * 
     * @param idagente ID unico de operador.
     * @param iddepto ID unico de depto.
     * @param idsector ID unico de sector.
     * @param nombreAudio Nombre del audio.
     * @param fecha Fecha de atencion.
     * @param origendestino Origen o destino de la llamada.
     * @param tipo SAL o ID de la cola.
     * @param linkedid  ID unico de la llamada.
     */
    public static void metodosAudioLog(String idagente, String iddepto, String idsector, String nombreAudio, long fecha, String origendestino, String tipo, String linkedid) {
        try {
            audioslog(idagente, iddepto, idsector, nombreAudio, fecha, origendestino, tipo, linkedid);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "metodosAudioLog", e);
        }
    }
    
    /**
     * Funcion que cierra en la DB el logueo de agente.
     *
     * @param idAgente ID del agente.
     * @param fechaFinaliza Fecha del deslogueo del agente.
     * @param fechaInicio Fecha del logueo del agente.
     * @param evento Codigo de logueo = 1
     * @param agenteExtension Extension del agente.
     *
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void agenteQueueMemberRemove(String idAgente, int eventoLogueo, long fechaInicio, long fechaFinaliza, String agenteExtension) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long tiempoTotal = (fechaFinaliza - fechaInicio) / 1000;
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agenteslog "
                + "(idagente, idevento, fecha_inicio, fecha_finaliza, tiempo_total, detalle) "
                + "VALUES "
                + "('" + idAgente + "', '" + eventoLogueo + "', '" + formato.format(fechaInicio) + "', '" + formato.format(fechaFinaliza) + "', '" + tiempoTotal + "', '" + agenteExtension + "')")) {
            stmt.executeUpdate();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "agenteQueueMemberRemove", db);
        }
    }
    
    /**
     * Setea la extension en la que se logueo el Agente. De esta forma, se evita
     * que una extension pueda ser utilizada 2 veces.
     * ESTE METODO DEBE DESAPARECER Y EL CLIENTE WEB DEBE CONSULTAR DIRECTO A LA PBX.
     *
     * @param idagente ID unico de agente.
     * @param agenteExtension Extension desde la que el agente se loguea.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void metodoParaMarcaTemporalLogueo(String idagente, String idExtension) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.DEBUG, "Agente: "+idagente+" | Se ingresa marca temporal en la DB");
        try (PreparedStatement stmt = con.prepareStatement(""
                + "UPDATE agentes SET "
                + "idextension = " + idExtension + ", "
                + "agentelogueado = 1 "
                + "WHERE idagente = " + idagente + "")) {
            stmt.executeUpdate();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.FATAL, "metodoParaMarcaTemporal", db);
        }
    }
    
    /**
     * Elimina el campo "extension" del agente que se esta desconectando.
     * ESTE METODO DEBE DESAPARECER Y EL CLIENTE WEB DEBE CONSULTAR DIRECTO A LA PBX.
     *
     * @param idagente ID unico del agente.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void metodoParaMarcaTemporalDeslogueo(String idagente) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "UPDATE agentes SET "
                + "agentelogueado = 0 "
                + "WHERE idagente = " + idagente + "")) {
            stmt.executeUpdate();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "metodoParaMarcaTemporal", db);
        }
    }

    /**
     * Funcion que ingresa en la DB la pausa de un agente.
     *
     * @param idAgente ID del agente.
     * @param fechaInicio Fecha del inicio de la pausa.
     * @param fechaFinaliza Fecha del fin de la pausa.
     * @param tiempoTotal Tiempo total pausado.
     * @param detallePausa Motivo de la pausa del agente.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void agentesLogQueueMemberPause(String idAgente, int eventoPausa, long fechaInicio, long fechaFinaliza, long tiempoTotal, String detallePausa) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agenteslog "
                + "(idagente, idevento, fecha_inicio, fecha_finaliza, tiempo_total, detalle, data1) "
                + "VALUES "
                + "(" + idAgente + ", " + eventoPausa + ", '" + formato.format(fechaInicio) + "', '" + formato.format(fechaFinaliza) + "', '" + tiempoTotal + "', '" + detallePausa + "', (SELECT pausaproductiva FROM pausas WHERE pausadescripcion = '" + detallePausa + "' AND iddepartamento = (SELECT iddepartamento FROM agentes WHERE idagente = " + idAgente + ")))")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agentesLogQueueMemberPause", db);
        }
    }

    /**
     * Ingresa un nuevo registro en la Tabla logreportessla. Esta tabla ofrece
     * informacion para las metricas Nivel de Atencion - Atendidas
     *
     * @param campo Campo de segundos de la llamada.
     * @param idCola Numero de Cola.
     * @param fecha Fecha de la llamada.
     * @throws java.sql.SQLException -
     * @throws java.io.IOException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void agentesConnect(String idCola, long fecha, String campo) throws SQLException, IOException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO reportesla "
                + "(fecha, idcola, " + campo + ", total) "
                + "VALUES "
                + "('" + formato.format(fecha).replaceFirst("[\\s\\S]{0,4}$", "0:00") + "'," + idCola + ",'1', '1') "
                + "ON DUPLICATE KEY UPDATE " + campo + "=" + campo + "+1, total = total + 1;")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agenteConnect", db);
        }
    }
    
    /**
     * Ingresa una llamada Atendida en la base local SQLite.
     * 
     * @param campo La cantidad de segundos que ringueo la llamada.
     * @param idCola Numero de Cola.
     * @param fecha Fecha (yyyy-MM-dd HH:mm) de la llamda atendida.
     */
    private static void agentesConnectRT(String idCola, long fecha, String campo) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:m0:00");
        LibSQLite DB = new LibSQLite().conectar();
        try {
            DB.ejecutar("UPDATE atendidas SET " + campo + " = " + campo + " + 1, total = total + 1 WHERE fecha = '" + formato.format(fecha) + "' AND idcola = " + idCola + ";");
            DB.ejecutar("INSERT OR IGNORE INTO atendidas (fecha, idcola, " + campo + ", total) VALUES ('" + formato.format(fecha) + "', " + idCola + ", 1, 1);");
        } catch (Exception db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agentesConnectRT", db);
        } finally {
            DB.cerrar();
        }
    }
    
    /**
     * Funcion que ingresa en la DB el hold de un agente.
     *
     * @param idAgente ID del agente.
     * @param fechaInicio Fecha de inicio del Hold.
     * @param fechaFin Fecha de finalizacion del Hold.
     * @param tiempoTotal Duracion del Hold.
     * @param eventoHold ID unico del evento Hold.
     * @param linkedid ID unico de la llamada.
     * saliente.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void agentesHold(String idAgente, long fechaInicio, long fechaFinaliza, int eventoHold, String linkedid) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long tiempoTotal = (fechaFinaliza - fechaInicio) / 1000;
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agenteslog "
                + "(idagente, idevento, fecha_inicio, fecha_finaliza, tiempo_total, linkedid) "
                + "VALUES "
                + "(" + idAgente + ", '" + eventoHold + "', '" + formato.format(fechaInicio) + "', '" +  formato.format(fechaFinaliza) + "', '" + tiempoTotal + "', '" + linkedid + "')")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agenteHold. ", db);
        }
    }
    
    /**
     * Ingresa en la tabla colaslog una nueva llamada perdida.
     *
     * @param idCola Numero de COla.
     * @param fechaIngreso Fecha de Inicio de la Llamada perdida.
     * @param origen Numero de origen de la llamada.
     * @param esperaTotal Espera total de la llamada.
     * @param linkedId ID unico de la llamada.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void colasabandonadas(String idCola, long fechaIngreso, long esperaTotal, String origen, int posicionInicio, int posicionFinaliza, String linkedId) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO colasperdidas "
                + "(idcola, fecha_ingreso, espera_cola, origen, posicion_inicio, posicion_finaliza, linkedid) "
                + "VALUES"
                + "(" + idCola + ", '" + formato.format(fechaIngreso) + "', '" + esperaTotal + "', '" + origen + "', '" + posicionInicio + "', '" + posicionFinaliza + "', '" + linkedId + "');")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "llamadaAbandonada", db);
        }
    }
    
    /**
     * Ingresa un nuevo registro o actualizacion (llamada perdida) para la fecha
     * y cola indicadas.
     *
     * @param fechaIngreso Fecha de la llamada abandonada.
     * @param idCola Numero de Cola.
     * @param esperaTotal Tiempo de Espera.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void reportegeneralPERDIDAS(String idCola, long fechaIngreso, long esperaCola) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO reportegeneral "
                + "(logfecha, idcola, perdidas, total, espera_cola) "
                + "VALUES"
                + "('" + formato.format(fechaIngreso).replaceFirst("[\\s\\S]{0,4}$", "0:00") + "', " + idCola + ", 1, 1, " + esperaCola + ") "
                + "ON DUPLICATE KEY UPDATE "
                + "perdidas = perdidas + 1, "
                + "total = total + 1, "
                + "espera_cola = espera_cola + " + esperaCola + ";")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "reportegeneralPERDIDAS. Error: ", db);
        }
    }
    
    /**
     * Ingresa un nuevo registro de llamada saliente.
     * 
     * @param idAgente
     * @param fechaDiscado
     * @param fechaFinaliza
     * @param tiempoTotal
     * @param esperaHold
     * @param destino
     * @param estado
     * @param uniqueId
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public static void salienteslog(String idAgente, String fechaDiscado, String fechaFinaliza, long tiempoTotal, long tiempoEfectivo, long esperaHold, String destino, String estado, String uniqueId) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO salienteslog "
                + "(idagente, fecha_discado, fecha_finaliza, tiempo_total, tiempo_conversacion, espera_hold, destino, estado, uniqueid) "
                + "VALUES "
                + "(" + idAgente + ", '" + fechaDiscado + "', '" + fechaFinaliza + "', " + tiempoTotal + ", " + tiempoEfectivo + ", " + esperaHold + ", '" + destino + "', '" + estado + "', '" + uniqueId + "');")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agenteLlamadaSaliente.", db);
        }
    }
    
    /**
     * Ingresa un nuevo registro en la Tabla logreportessco. Esta tabla ofrece
     * informacion para las metricas Nivel de Atencion - Perdidas
     *
     * @param campo Campo de segundos de la llamada.
     * @param idCola Numero de Cola.
     * @param fechaIngreso Fecha de la llamada.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void reportesco(String idCola, long fechaIngreso, String campo) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO reportesco "
                + "(fecha, idcola, " + campo + ", total) "
                + "VALUES "
                + "('" + formato.format(fechaIngreso).replaceFirst("[\\s\\S]{0,4}$", "0:00") + "'," + idCola + ",'1', '1') "
                + "ON DUPLICATE KEY UPDATE " + campo + "=" + campo + "+1, total = total + 1;")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "llamadaPerdida. Error: ", db);
        }
    }
    
    /**
     * Ingresa una llamada perdida en la base local SQLite.
     * 
     * @param campo La cantidad de segundos que ringueo la llamada.
     * @param idCola Numero de cola.
     * @param fechaIngreso Fecha (yyyy-MM-dd HH:mm) de la llamda abandonada.
     */
    private synchronized static void perdidasRT(String idCola, long fechaIngreso, String campo) {
        LibSQLite DB = new LibSQLite().conectar();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:m0:00");
        try {
            DB.ejecutar("UPDATE perdidas SET " + campo + " = " + campo + " + 1, total = total + 1 WHERE fecha = '" + formato.format(fechaIngreso) + "' AND idcola = " + idCola + ";");
            DB.ejecutar("INSERT OR IGNORE INTO perdidas (fecha, idcola, " + campo + ", total) VALUES ('" + formato.format(fechaIngreso) + "', " + idCola + ", 1, 1);");
        } catch (Exception db) {
            Logger.getLogger(ReporteSco.class.getName()).log(Level.ERROR, "llamadaPerdida. Error: ", db);
        } finally {
            DB.cerrar();
        }
    }
    
    /**
     * Ingresa en la tabla colaslog una nueva llamada atendida.
     *
     * @param idCola Numero de COla.
     * @param idAgente Numero de ID de Agente.
     * @param fechaIngreso Fecha de Ingreso de la Llamada Atendida.
     * @param origen Numero de origen de la llamada.
     * @param conversa Tiempo de conversacion.
     * @param espeagente Espera de la llamada en el agente.
     * @param esperaCola Espera total de la llamada hasta ser atendida.
     * @param hold Tiempo de hold en la llamada.
     * @param corte Tipo de corte de la llamada.
     * @param detalle Tipo de origen de la llamada.
     * @param linkedId Link unico de llamada.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void colasatendidas(String idCola, String idAgente, long fechaIngreso, long fechaAtendida, long fechaFinaliza, String origen, long conversacion, long esperaAgente, long esperaCola, long esperaHold, String corteOriginal, String linkedId) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO colasatendidas "
                + "(idcola, idagente, fecha_ingreso, espera_cola, fecha_atendida, espera_agente, fecha_finaliza, conversacion, origen, espera_hold, corte, linkedid) "
                + "VALUES"
                + "(" + idCola + ", " + idAgente + ", '" + formato.format(fechaIngreso) + "', " + esperaCola + ", '" + formato.format(fechaAtendida) + "', " + esperaAgente + ", '" + formato.format(fechaFinaliza) + "', " + conversacion + ", '" + origen + "', " + esperaHold + ", '" + corteOriginal + "', '" + linkedId +"');")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "colaLlamada. Error: ", db);
        }
    }
    
    /**
     * Ingresa un nuevo registro o actualizacion (llamada atendida) para la
     * fecha y cola indicadas.
     *
     * @param idCola Numero de Cola.
     * @param fechaIngreso Fecha de la llamada atendida.
     * @param conversacion Tiempo de Conversacion.
     * @param esperaCola Tiempo de espera en la cola.
     * @param esperaHold Tiempo de hold en la llamada.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private synchronized static void reporteGeneralAtendida(String idCola, long fechaIngreso, long conversacion, long esperaCola, long esperaHold) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO reportegeneral "
                + "(logfecha, idcola, atendidas, total, conversacion, espera_cola, espera_hold) "
                + "VALUES"
                + "('" + formato.format(fechaIngreso).replaceFirst("[\\s\\S]{0,4}$", "0:00") + "', " + idCola + ", 1, 1, " + conversacion + ", " + esperaCola + ", " + esperaHold + ") "
                + "ON DUPLICATE KEY UPDATE "
                + "atendidas = atendidas + 1, "
                + "total = total + 1, "
                + "conversacion = conversacion + " + conversacion + ", "
                + "espera_cola = espera_cola + " + esperaCola + ", "
                + "espera_hold = espera_hold + " + esperaHold + ";")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "reporteGeneralAtendida", db);
        }
    }
    
    /**
     * Funcion que cierra en la DB la atencion de un agente.
     *
     * @param idAgente Numero de ID del agente.
     * @param eventoHabla ID unico del evento Habla = 5.
     * @param fechaInicio Fecha del inicio de atención.
     * @param fechaFinaliza Fecha del fin de la llamada.
     * @param tiempoTotal Tiempo de conversacion.
     * @param detalle idCola o "saliente".
     * @param data1 Origen en caso de llamada por cola o destino en caso de llamada saliente.
     * @param linkedid ID unico de linke.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    private static void agenteslogHabla(String idAgente, int eventoHabla, long fechaAtencion, long fechaFinaliza, long tiempoTotal, String detalle, String data1, String linkedid) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agenteslog "
                + "(idagente, idevento, fecha_inicio, fecha_finaliza, tiempo_total, detalle, data1, linkedid) "
                + "VALUES "
                + "(" + idAgente + ", '" + eventoHabla + "', '" + formato.format(fechaAtencion) + "', '" + formato.format(fechaFinaliza) + "', " + tiempoTotal + ", '" + detalle + "', '" + data1 + "', '" + linkedid + "')")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agentesLogHabla. ", db);
        }
    }

    /**
     * Ingresa datos en la tabla agenteslog cuando un agente no atiende.
     *
     * @param idAgente Numero del ID del agente.
     * @param eventoNoAtiende ID unico del evento No Atiende = 3
     * @param fechaInicio Fecha del inicio del Ringueo.
     * @param fechaFinaliza Fecha del fin evento RINGNOANSWER.
     * @param tiempoTotal Tiempo de Ringueo de la llamada.
     * @param detalle Cola de atención.
     * @param data1 Numero origen de la llamada.
     * @param linkedid ID Link de la llamada.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */                                                                      
    private static void agentesLogRingNoAnswer(String idAgente, int eventoNoAtiende, long fechaInicio, long fechaFinaliza, long tiempoTotal, String detalle, String data1, String linkedid) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agenteslog "
                + " (idagente, "
                + "idevento, "
                + "fecha_inicio, "
                + "fecha_finaliza, "
                + "tiempo_total, "
                + "detalle, "
                + "data1, "
                + "linkedid) "
                + "VALUES"
                + "(" + idAgente + ", '" + eventoNoAtiende + "', '" + formato.format(fechaInicio) + "', '" + formato.format(fechaFinaliza) + "', '" + tiempoTotal + "', '" + detalle + "', '" + data1 + "', '" + linkedid +"');")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agentesLogRingNoAnswer. ", db);
        }
    }

    /**
     * Metodo para insertar la desconexion de la extension.
     * DATO: el 6 es el codigo correspondiente al evento de desconexion.
     * 
     * @param idAgente ID unico de agente.
     * @param fechainicio Fecha de comienzo del evento.
     * @param fechaFinaliza Fecha de finalizacion del event.
     * @param tiempoTotal Tiempo total que dura el evento.
     * @param detalle Extension del operador.
     * @throws IOException
     * @throws SQLException 
     */
    private static void agentesLogExtensionDesconectada(String idAgente, long fechaInicio, long fechaFinaliza, long tiempoTotal, String detalle) throws IOException, SQLException, ClassNotFoundException {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO agenteslog "
                + " (idagente, "
                + "idevento, "
                + "fecha_inicio, "
                + "fecha_finaliza, "
                + "tiempo_total, "
                + "detalle, "
                + "data1, "
                + "linkedid) "
                + "VALUES"
                + "(" + idAgente + ", '6', '" + formato.format(fechaInicio) + "', '" + formato.format(fechaFinaliza) + "', '" + tiempoTotal + "', '" + detalle + "', '', '');")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agentesLogExtensionDesconectada", db);
        }
    }
    
    /**
     * Inserta un nuevo registro de llamada NO atendida en el ReporteAgente.
     *
     * @param idAgente Numero de ID del agente.
     * @param fechaInicio Fecha de la llamada.
     * @param detalle Numero de Cola.
     * @throws IOException -
     * @throws SQLException -
     * @throws ClassNotFoundException -
     */
    private synchronized static void reporteAgenteRingNoAnswer(String idAgente, long fechaInicio, String detalle) throws IOException, SQLException, ClassNotFoundException {
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO reporteagente "
                + "(logfecha, idcola, idagente, noatendidas) "
                + "VALUES"
                + "('" + formato.format(fechaInicio).replaceFirst("[\\s\\S]{0,4}$", "0:00") + "', " + detalle + ", " + idAgente + ", 1) "
                + "ON DUPLICATE KEY UPDATE "
                + "noatendidas = noatendidas + 1;")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "reporteAgenteRingNoAnswer. Error: ", db);
        }
    }
    
    /**
     * Inserta un nuevo registro de llamada atendida en el ReporteAgente.
     *
     * @param idAgente Numero de ID del Agente.
     * @param fechaInicio Fecha de la llamada.
     * @param idCola Numero de Cola.
     * @param razon Razon del corte de la llamada.
     * @param holdAcumulado Tiempo de hold durante la llamada.
     * @param conversacion Tiempo de conversacion total de la llamada.
     * @throws IOException -
     * @throws SQLException -
     * @throws ClassNotFoundException -
     */
    private synchronized static void reporteAgenteAtendidas(String idAgente, String idCola, long fechaInicio, long conversacion, String razon, long holdAcumulado) throws IOException, SQLException, ClassNotFoundException {
        int transfer = 0;
        if (razon.equals("transfer")) {
            transfer = 1;
        }
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO reporteagente "
                + "(logfecha, idcola, idagente, atendidas, transferidas, hold_total, conversacion) "
                + "VALUES"
                + "('" + formato.format(fechaInicio).replaceFirst("[\\s\\S]{0,4}$", "0:00") + "', " + idCola + ", " + idAgente + ", 1, " + transfer + ", " + holdAcumulado + ", " + conversacion + ") "
                + "ON DUPLICATE KEY UPDATE "
                + "atendidas = atendidas + 1, "
                + "transferidas = transferidas + " + transfer + ", "
                + "hold_total = hold_total + " + holdAcumulado + ", "
                + "conversacion = conversacion + " + conversacion + ";")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "reporteAgenteAtendidas. Error: ", db);
        }
    }
    
    /**
     * Obtiene la cantidad de segundos de actividades del agente. Cantidad de
     * segundos hablando, pausado y en hold.
     *
     * @param idAgente Numero de ID del Agente.
     * @return Retorna un Array con los datos: Habla / Pausa / Hold.
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static List<String> agenteObtieneHablaPausaHold(String idAgente) throws IOException, SQLException, ClassNotFoundException {
        List<String> agentes = new ArrayList<>();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT IFNULL(ROUND(SUM(tiempototal)), 0) AS 'Habla' FROM agenteslog WHERE evento = 'HABLA' AND idagente = " + idAgente + " AND fechainicio > CURDATE() "
                    + "UNION ALL "
                    + "SELECT IFNULL(ROUND(SUM(tiempototal)), 0) AS 'Pausa' FROM agenteslog WHERE evento = 'PAUSA' AND idagente = " + idAgente + " AND fechainicio > CURDATE() "
                    + "UNION ALL "
                    + "SELECT IFNULL(ROUND(SUM(tiempototal)), 0) AS 'Hold' FROM agenteslog WHERE evento = 'HOLD' AND idagente = " + idAgente + " AND fechainicio > CURDATE()");
            while (consulta.next()) {
                agentes.add(consulta.getString(1));
            }
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agenteObtieneHablaPausaHold. ", db);
        }
        return agentes;
    }

    /**
     * Obtiene la cantidad de llamadas por hora de un agente.
     *
     * @param idAgente Numero de ID del Agente.
     * @return Retorna un String con datos separados por ",".
     * @throws java.io.IOException -
     * @throws java.sql.SQLException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static String agenteObtieneLlamadasAtendidas(String idAgente) throws IOException, SQLException, ClassNotFoundException {
        List<String> datos = new ArrayList<>();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (Statement stmt = con.createStatement()) {
            ResultSet consulta = stmt.executeQuery(""
                    + "SELECT "
                    + "CONCAT(DATE_FORMAT(fechainicio, '%H'),':00:00'), "
                    + "COUNT(idagenteslog) "
                    + "FROM agenteslog "
                    + "WHERE evento = 'HABLA' "
                    + "AND fechainicio > CURDATE() "
                    + "AND idagente = " + idAgente + " "
                    + "GROUP BY HOUR(fechainicio)");
            while (consulta.next()) {
                datos.add(consulta.getString(1));
                datos.add(consulta.getString(2));
            }
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "agenteObtieneLlamadasAtendidas. ", db);
        }
        String datoscoma = String.join(",", datos);
        return datoscoma;
    }

    /**
     * Inserta un nuevo registro para la trazabilidad de llamadas.
     * 
     * @param idagente ID unico de agente.
     * @param idevento ID unico de evento.
     * @param linkedid ID unico de llamada.
     * @param fecha Fecha de inicio del evento.
     * @param origen Numero de origen del evento.
     * @param destino Numero de destino del evento.
     * @param canal Canal de la llamada.
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public synchronized static void llamadaslog(String idagente, int idevento, String linkedid, long fecha, String origen, String destino, String canal) throws IOException, SQLException, ClassNotFoundException{
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO llamadaslog "
                + "(idagente, idevento, linkedid, fecha, origen, destino, canal) "
                + "VALUES "
                + "(" + idagente + ", '" + idevento + "', '" + linkedid + "', '" + formato.format(fecha) + "', '" + origen + "', '" + destino + "', '" + canal + "')")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "llamadaslog. ", db);
        }
    }
    
    /**
     * Inserta un nuevo registro que identifica el audio de la llamada.
     * 
     * @param idagente ID unico de operador.
     * @param iddepto ID unico de departamento.
     * @param idsector ID unico de sector.
     * @param fecha Fecha del inicio de la llamada.
     * @param origendestino Origen / Destino de la llamada.
     * @param nombre Nombre del audio.
     * @param path Path donde se encuentra el audio.
     * @param tipo Tipo de Audio, ID de Cola o SAL de saliente.
     * @param linkedid ID unico de la llamada.
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    private static void audioslog(String idagente, String iddepto, String idsector, String nombreAudio, long fecha, String origendestino, String tipo, String linkedid) throws IOException, SQLException, ClassNotFoundException{
        SimpleDateFormat formFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formPath = new SimpleDateFormat("yyyy/MM/dd/");
        Date date = new Date();
        Connection con = libdb.LibMySQLCentral.getInstance().ConectaMysql();
        try (PreparedStatement stmt = con.prepareStatement(""
                + "INSERT INTO audioslog "
                + "(idagente, iddepartamento, idsector, fecha, origendestino, nombre, `path`, tipo, linkedid, ubicacion) "
                + "VALUES "
                + "(" + idagente + ", " + iddepto + ", '" + idsector + "', '" + formFecha.format(fecha) + "', '" + origendestino + "', '" + nombreAudio + "', '" + formPath.format(date) + "', '" + tipo + "', '" + linkedid + "', 0)")) {
            stmt.execute();
        } catch (SQLException db) {
            Logger.getLogger(IngresaDatosDB.class.getName()).log(Level.ERROR, "audioslog", db);
        }
    }
    
}
