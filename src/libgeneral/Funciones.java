/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import libdb.IngresaDatosDB;
import matecore.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

/**
 *
 * @author mgiannini
 */
public class Funciones {

    /**
     * Conjunto de acciones para la creacion de un agente en la maquina virtual.
     *
     * @param conn Objeto WebSocket.
     * @param extension Numero de la extension.
     * @param username Nombre de usuario del Agente.
     * @param idagente Numero de ID del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param grupoColasAgente Mapeo de Colas - Agentes.
     * @throws SQLException Tipo de Excepcion -
     * @throws java.io.IOException -
     * @throws java.lang.ClassNotFoundException -
     */
    public static void agenteLoguea(WebSocket conn, String extension, String username, String idagente, AgenteMapeo grupoAgentes, ColaMapeo grupoColasAgente) throws SQLException, IOException, ClassNotFoundException {
        Logger.getLogger(Funciones.class.getName()).log(Level.DEBUG, "Operador: "+username+" | agenteLoguea | Inicia logueo del agente en cache.");
        Agente nuevoAgente = new Agente();
        Boolean grabacion = false;
        ArrayList<String> colasNumero = new ArrayList<>();
        ConcurrentHashMap<String, List<String>> colas = libdb.Agentes.agenteObtieneColas(idagente);

        colas.forEach((k, v) -> {
            grupoColasAgente.agrega_StrColaNum_ObjWebSocket(k, conn);
            colasNumero.add(k);
            Logger.getLogger(Funciones.class.getName()).log(Level.DEBUG, "Operador: "+username+" | agenteLoguea | Carga cola: "+k+" en la cache del agente.");
        });

        nuevoAgente.setListadoPausas(libdb.Agentes.agenteObtienePausas(idagente));
        String[] datos = libdb.Agentes.agenteObtieneGrabacionSectorDepto(idagente);
        
        if (datos[0].equals("1")) {
            grabacion = true;
        }
        
        nuevoAgente.setAgenteGrabaSaliente(grabacion);
        nuevoAgente.setAgenteSector(datos[1]);
        nuevoAgente.setAgenteDepto(datos[2]);
        nuevoAgente.setAgenteExtension(extension);
        nuevoAgente.setAgenteUsuario(username);
        nuevoAgente.setIdAgente(idagente);
        nuevoAgente.setAgenteStrWS(conn.toString());
        nuevoAgente.setAgenteObjWS(conn);
        nuevoAgente.setColasMapaAsignar(colas);
        nuevoAgente.setColasNumeroAsignar(colasNumero);
        nuevoAgente.setAgenteFechaLogueo(System.currentTimeMillis());

        grupoAgentes.setMapa_StrIdAgente_ObjAgente(idagente, nuevoAgente);
        grupoAgentes.agrega_StrUsername_ObjAgente(username, nuevoAgente);
        grupoAgentes.agrega_StrWebSocket_StrUsername(conn.toString(), username);
        grupoAgentes.agrega_StrExtension_StrWebSocket(extension, conn.toString());
        Logger.getLogger(Funciones.class.getName()).log(Level.DEBUG, "Operador: "+username+" | agenteLoguea | Finaliza creacion del agente en cache.");
    }

    /**
     * Conjunto de acciones para la eliminacion de un agente de la maquina
     * virtual.
     *
     * @param username Nombre de usuario del Agente.
     * @param extension Numero de la extension.
     * @param grupoAgentes Mapeo de Agentes.
     * @param grupoColas Mapeo de Colas.
     */
    public static void agenteDesloguea(String username, String extension, AgenteMapeo grupoAgentes, ColaMapeo grupoColas) {
        List<String> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(username).getAgenteColasNumero();
        WebSocket conn = grupoAgentes.obtiene_StrUsername_ObjWebSocket(username);
        grupoColas.elimina_ArrayWebSocket_ArrayColas(conn, colas);
        grupoAgentes.elimina_StrUsername_ObjAgente(username);
        grupoAgentes.elimina_StrWebSocket_StrUsername(conn.toString());
        grupoAgentes.elimina_StrExtension_StrWebSocket(extension);
        Logger.getLogger(Funciones.class.getName()).log(Level.DEBUG, "Operador: "+username+" | agenteDesloguea | Se elimina el agente de la cache de MATE.");
    }
    
    /**
     * Conjunto de acciones para la creacion de un supervisor en la maquina
     * virtual.
     *
     * @param conn Objeto WebSocket.
     * @param username Nombre de usuario del Supervisor.
     * @param idColas Array de los idColas que supervisa.
     * @param grupoSupervisores Mapeo Supervisores.
     * @param grupoColasSupervisores Mapeo Colas - Supervisores.
     */
    public synchronized static void supervisorLoguea(WebSocket conn, String username, String idColas, SupervisorMapeo grupoSupervisores, ColaMapeo grupoColasSupervisores) {
        Supervisor nuevoSupervisor = new Supervisor();
        List<String> colas = new ArrayList<>(Arrays.asList(idColas.split(",")));

        nuevoSupervisor.setSupervisorUsuario(username);
        nuevoSupervisor.setSupervisorObjWS(conn);
        nuevoSupervisor.setSupervisorStrWS(conn.toString());
        nuevoSupervisor.setSupervisorColas(colas);
        nuevoSupervisor.setSupervisorFechaLogueo(new Date());

        grupoSupervisores.agrega_StrWebSocket_ObjSupervisor(conn.toString(), nuevoSupervisor);
        grupoSupervisores.agrega_StrUsername_StrWebSocket(username, conn.toString());
        
        colas.forEach((idCola) -> {
            grupoColasSupervisores.agrega_StrColaNum_ObjWebSocket(idCola, conn);
        });
    }

    /**
     * Conjunto de acciones para la modificacion de un supervisor de la maquina
     * virtual.
     *
     * @param conn Objeto WebSocket.
     * @param username Nombre de usuario del Supervisor.
     * @param idColas Array de los idColas que supervisa.
     * @param grupoSupervisores Mapeo Supervisores.
     * @param grupoColasSupervisores Mapeo Colas - Supervisores.
     */
    public synchronized static void supervisorModifica(WebSocket conn, String username, String idColas, SupervisorMapeo grupoSupervisores, ColaMapeo grupoColasSupervisores) {
        //List<String> colasActuales = grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).getColas();
        grupoColasSupervisores.elimina_ArrayWebSocket_ArrayColas(conn, grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).getSupervisorColas());
        List<String> colas = new ArrayList<>(Arrays.asList(idColas.split(",")));
        grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).setSupervisorColas(colas);
        
        colas.forEach((idCola) -> {
            grupoColasSupervisores.agrega_StrColaNum_ObjWebSocket(idCola, conn);
        });
    }

    /**
     * Conjunto de acciones para la eliminacion de un supervisor de la maquina
     * virtual. Al desconectarse el Supervisor, se debe eliminar el objeto
     * WebSocket de las colas asociadas al mismo. Ademas, se elimina el objeto
     * Supervisor.
     *
     * @param conn Objeto WebSocket.
     * @param grupoSupervisores Mapeo Supervisores.
     * @param grupoColasSupervisores Mapeo Colas - Supervisores.
     */
    public static void supervisorDesloguea(WebSocket conn, SupervisorMapeo grupoSupervisores, ColaMapeo grupoColasSupervisores) {
        //List<String> colas = grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).getColas();
        grupoColasSupervisores.elimina_ArrayWebSocket_ArrayColas(conn, grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).getSupervisorColas());
        grupoSupervisores.elimina_StrWebSocket_ObjSupervisor(conn.toString());
    }

    /**
     * Metodo para la consulta de operaciones de Supervisores sobre los agentes.
     * 
     * @param codigo Codigo unico de operacion.
     * @param supervisor Codigo del supervisor.
     * @param agente Agente sobre el que se desea realizar la operacion.
     * @param colaNum ID unico de cola de atencion.
     * @param grupoAgentes Grupo de Agentes.
     * @param grupoColasAgentes Grupo de colas de Agentes.
     */
    public static void supervisorConsultaOperacion(String codigo, String supervisor, String agente, String colaNum, AgenteMapeo grupoAgentes, ColaMapeo grupoColasAgentes) {
        switch (codigo) {
            case "10": // (Intento de logueo de Agente)
                if (grupoAgentes.obtiene_StrUsername_ObjAgente(agente).getAgenteColasNumero().contains(colaNum)) {
                    Main.supervisorEnviaMensajeSimple(supervisor, "El agente ya se encuentra en la cola: " + grupoColasAgentes.obtiene_StrColaNum_StrColaNom(colaNum) + ".");
                } else {
                    Main.supervisorEnviaMensajeComplejo(supervisor, colaNum, grupoColasAgentes.obtiene_StrColaNum_StrColaNom(colaNum), agente);
                }
                break;
            case "11": {
                try {
                    // Codigo para envio de datos para el grafico del agente.
                    Main.supervisorEnviaStatsAgente(supervisor, grupoAgentes.obtiene_StrUsername_ObjAgente(agente).getAgenteExtension(), libdb.IngresaDatosDB.agenteObtieneHablaPausaHold(grupoAgentes.obtiene_StrUsername_ObjAgente(agente).getIdAgente()), libdb.IngresaDatosDB.agenteObtieneLlamadasAtendidas(grupoAgentes.obtiene_StrUsername_ObjAgente(agente).getIdAgente()));
                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    org.apache.log4j.Logger.getLogger(Main.class.getName()).log(org.apache.log4j.Level.ERROR, "supervisorConsultaOperacion. Error: ", ex);
                }
            }
            break;
            case "12":
                Main.supervisorEnviaInfoAgente(supervisor, agente);
                break;
            default:
                System.out.println("Llego el codigo" + codigo);
                break;
        }
    }

    /**
     * Seteo de campo para la tabla de SLA.
     *
     * @param segundos Cantidad de segundos
     * @return Retorna los campos de la BD donde impactar los datos.
     */
    public static String obtieneCampoImpacto(long segundos) {
        if (segundos >= 0 && segundos <= 5) {
            return "a0005";
        } else if (segundos >= 06 && segundos <= 10) {
            return "a0610";
        } else if (segundos >= 11 && segundos <= 20) {
            return "a1120";
        } else if (segundos >= 21 && segundos <= 30) {
            return "a2130";
        } else if (segundos >= 31 && segundos <= 40) {
            return "a3140";
        } else if (segundos >= 41 && segundos <= 50) {
            return "a4150";
        } else if (segundos >= 51 && segundos <= 60) {
            return "a5160";
        } else if (segundos >= 61 && segundos <= 70) {
            return "a6170";
        } else if (segundos >= 71 && segundos <= 80) {
            return "a7180";
        } else if (segundos >= 81 && segundos <= 90) {
            return "a8190";
        } else if (segundos >= 91 && segundos <= 100) {
            return "a91100";
        } else if (segundos >= 101 && segundos <= 110) {
            return "a101110";
        } else if (segundos >= 111 && segundos <= 120) {
            return "a111120";
        } else if (segundos >= 121 && segundos <= 150) {
            return "a121150";
        } else if (segundos >= 151 && segundos <= 180) {
            return "a151180";
        } else if (segundos >= 181 && segundos <= 210) {
            return "a181210";
        } else if (segundos >= 211 && segundos <= 240) {
            return "a211240";
        } else if (segundos >= 241 && segundos <= 270) {
            return "a241270";
        } else if (segundos >= 271 && segundos <= 300) {
            return "a271300";
        } else {
            return "a301";
        }
    }

    /**
     * Seteo de campos para la obtencion del SLA.
     *
     * @param segundos Cantidad de segundos.
     * @return Retorna los campos que se deben sumar.
     */
    public static String obtieneSumatoriaDeCamposParaSLA(int segundos) {
        switch (segundos) {
            case 5:
                return "SUM(a0005)";
            case 10:
                return "SUM(a0005) + SUM(a0610)";
            case 20:
                return "SUM(a0005) + SUM(a0610) + SUM(a1120)";
            case 30:
                return "SUM(a0005) + SUM(a0610) + SUM(a1120) + SUM(a2130)";
            case 40:
                return "SUM(a0005) + SUM(a0610) + SUM(a1120) + SUM(a2130) + SUM(a3140)";
            case 50:
                return "SUM(a0005) + SUM(a0610) + SUM(a1120) + SUM(a2130) + SUM(a3140) + SUM(a4150)";
            case 60:
                return "SUM(a0005) + SUM(a0610) + SUM(a1120) + SUM(a2130) + SUM(a3140) + SUM(a4150) + SUM(a5160)";
            default:
                return "";
        }
    }
    
    /**
     * Obtengo la descripcion del estado devuelto de la extension
     * 
     * @param extensionEstado Codigo unico del estado de la extension.
     * @return Retorno una descripcion del código.
     */
    public static String obtieneDescripcionEstado(int extensionEstado) {
        /*
        Status - The numeric device state status of the queue member.
            0 - AST_DEVICE_UNKNOWN
            1 - AST_DEVICE_NOT_INUSE
            2 - AST_DEVICE_INUSE
            3 - AST_DEVICE_BUSY
            4 - AST_DEVICE_INVALID
            5 - AST_DEVICE_UNAVAILABLE
            6 - AST_DEVICE_RINGING
            7 - AST_DEVICE_RINGINUSE
            8 - AST_DEVICE_ONHOLD
        */
        
        switch (extensionEstado) {
            case 0:
                return "Error";
            case 1:
                return "Activo";
            case 2:
                return "Activo";
            case 3:
                return "Activo";
            case 4:
                return "Error";
            case 5:
                return "Error";
            case 6:
                return "Activo";
            case 7:
                return "Activo";
            case 8:
                return "Activo";
            default:
                return "";
        }
    }
    
    /**
     * Metodo para procesar la trazabilidad de las llamadas.
     * 
     * @param conjunto Cola que contienen todos los eventos de cada llamada.
     * @param linkedid ID unico de la llamada.
     */
    public synchronized static void procesoTrazabilidad(ConcurrentLinkedDeque<LlamadaColaQueue> conjunto, String linkedid) throws IOException {
        for (Iterator<LlamadaColaQueue> it = conjunto.iterator(); it.hasNext();) {
            LlamadaColaQueue d = it.next();
            IngresaDatosDB.metodosTrazables(d.getIdAgente(), d.getEvento(), linkedid, d.getFechaInicio(), d.getOrigen(), d.getDestino(), d.getCanal());
        }
    }
    
}
