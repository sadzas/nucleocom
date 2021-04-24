/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

/**
 *
 * @author mgiannini
 */
import libdb.Agentes;
import libdb.Colas;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import libgeneral.AgenteMapeo;
import matecore.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class AccionesMG {
    /**
     * Acciones a ejecutar para el logueo del Agente.
     *
     * @param agenteExtension Extension del Agente.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void conectaAgente(String agenteExtension, String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
        eventos.agenteLoguea(agenteUsuario, agenteExtension, colas, false);
    }

    /**
     * Acciones a ejecutar para el deslogueo del Agente.
     *
     * @param agenteExtension Extension del Agente.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void desconectaAgente(String agenteExtension, String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        Logger.getLogger(AccionesMG.class.getName()).log(Level.DEBUG, "Agente: "+agenteUsuario+" | Se inicia la desconexion del agente.");
        Main.extensionChequeoDesconexion(agenteUsuario, agenteExtension);
        ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
        if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado().equals("Activo")) {
            // Primero lo despauso.
            Logger.getLogger(AccionesMG.class.getName()).log(Level.DEBUG, "Agente: "+agenteUsuario+" | Se despausa el agente..");
            eventos.agenteDespausa(agenteUsuario, agenteExtension, colas);
        }
        eventos.agenteDesloguea(agenteUsuario, agenteExtension, colas);
    }

    /**
     * Acciones a ejecutar frente a un cierre inesperado del Agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void cierreInesperadoAgente(String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        String extension = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension();
        Main.extensionChequeoDesconexion(agenteUsuario, extension);
        ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
        if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado().equals("Activo")) {
            eventos.agenteDespausa(agenteUsuario, extension, colas);
        }
        eventos.agenteDesloguea(agenteUsuario, extension, colas);
    }

    /**
     * Acciones a ejecutar frente a una pausa del Agente.
     *
     * @param codigo Codigo de pausa del Agente.
     * @param agenteExtension Extension del Agente.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void pausaAgente(String codigo, String agenteExtension, String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        Logger.getLogger(AccionesMG.class.getName()).log(Level.DEBUG, "Agente: "+agenteUsuario+" | Pausa: "+codigo+" - Inicia la pausa del agente.");
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().getPausaNombre().equals("AutoPausa")) {
            Logger.getLogger(AccionesMG.class.getName()).log(Level.DEBUG, "Agente: "+agenteUsuario+" | Se quita la AutoPausa existente.");
            despausaAgente(agenteExtension, agenteUsuario,grupoAgentes, eventos);
        }
        ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
        eventos.agentePausa(agenteUsuario, agenteExtension, colas, codigo);
    }
    
    /**
     * Acciones a ejecutar frente a una despausa del Agente.
     *
     * @param agenteExtension Extension del Agente.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void despausaAgente(String agenteExtension, String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        Logger.getLogger(AccionesMG.class.getName()).log(Level.DEBUG, "Agente: "+agenteUsuario+" | Inicia la despausa del agente.");
        ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
        eventos.agenteDespausa(agenteUsuario, agenteExtension, colas);
    }
    
    /**
     * Acciones a ejecutar en el intento de logueo de un agente por parte del supervisor.
     * 
     * @param supervisorUsuario Nombre de usuario del supervisor.
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param idCola Numero de cola.
     * @param tipo Tipo de logueo que se pretende (temporal / permanente).
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     * @throws IOException -
     * @throws SQLException -
     * @throws ClassNotFoundException -
     */
    public static void supervisorLogueaAgente(String supervisorUsuario, String agenteUsuario, String idCola, String tipo, AgenteMapeo grupoAgentes, LibManagerEvents eventos) throws IOException, SQLException, ClassNotFoundException {
        Boolean pausado = false;
        if (tipo.equals("2")) { // El logueo es permanente
            Agentes.agenteAgregaCola(grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getIdAgente(), idCola);
        }
        ConcurrentHashMap<String, List<String>> cola = Colas.colaObtieneContexto(idCola);
                
        grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setColasNumeroAgregar(idCola);
        grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setColasMapaAgregar(cola);
        if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado().equals("Activo")) {
            pausado = true;
        }
        grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteAccionSupervisor(true);
        Logger.getLogger(AccionesMG.class.getName()).log(Level.INFO, "Supervisor: " + supervisorUsuario + " | Agrega Agente: " + agenteUsuario + " a la cola: " + idCola);
        eventos.agenteLoguea(agenteUsuario, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension(), cola, pausado);     
    }

    /**
     * Acciones a ejecutar frente a una pausa de Agente efectuada por un
     * supervisor.
     *
     * @param supervisorUsuario Nombre de Usuario del Supervisor.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param codigo Tipo de Pausa seleccionada.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void supervisorPausaAgente(String supervisorUsuario, String agenteUsuario, String codigo, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado().equals("Activo")) {
            String extension = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension();
            ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
            eventos.agentePausa(agenteUsuario, extension, colas, codigo);
            Logger.getLogger(AccionesMG.class.getName()).log(Level.INFO, "Supervisor: " + supervisorUsuario + " | Pausa Agente: " + agenteUsuario);
        } else {
            Main.supervisorEnviaMensajeSimple(supervisorUsuario, "Solo puede pausarse un agente en estado Activo.");
        }
    }
    
    /**
     * Acciones a ejecutar frente a una despausa de Agente efectuada por un
     * supervisor.
     *
     * @param supervisorUsuario Nombre de Usuario del Supervisor.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void supervisorDespausaAgente(String supervisorUsuario, String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado().equals("Activo")) {
            ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
            eventos.agenteDespausa(agenteUsuario, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension(), colas);
            Logger.getLogger(AccionesMG.class.getName()).log(Level.INFO, "Supervisor: " + supervisorUsuario + " | Despausa Agente: " + agenteUsuario);
        } else {
            Main.supervisorEnviaMensajeSimple(supervisorUsuario, "El agente no se encuentra pausado.");
        }
    }

    /**
     * Acciones a ejecutar frente a un deslogueo de Agente efectuado por un
     * supervisor.
     *
     * @param supervisorUsuario Nombre de Usuario del Supervisor.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Evento del Manager.
     */
    public static void supervisorDeslogueaAgente(String supervisorUsuario, String agenteUsuario, AgenteMapeo grupoAgentes, LibManagerEvents eventos) {
        ConcurrentHashMap<String, List<String>> colas = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa();
        String extension = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension();
        Main.extensionChequeoDesconexion(agenteUsuario, extension);
        if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado().equals("Activo")) {
            supervisorDespausaAgente(supervisorUsuario, agenteUsuario, grupoAgentes, eventos);
        }
        eventos.agenteDesloguea(agenteUsuario, extension, colas);
        Logger.getLogger(AccionesMG.class.getName()).log(Level.INFO, "Supervisor: " + supervisorUsuario + " | Desloguea Agente: " + agenteUsuario);
    }
    
    /**
     * Modifica el valor del penalty de un agente / cola.
     * No solo envia el valor al manager, tambien lo modifica de la DB.
     * 
     * @param supervisorUsuario Nombre de Usuario del Supervisor.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param idCola Numero de Cola.
     * @param penalty Valor del Penalty.
     * @param grupoAgentes Mapeo de Agentes.
     * @param eventos Eventos del Manager.
     * @throws SQLException -
     * @throws IOException -
     * @throws ClassNotFoundException -
     */
    public static void supervisorPenaltyAgente(String supervisorUsuario, String agenteUsuario, String idCola, String penalty, AgenteMapeo grupoAgentes, LibManagerEvents eventos) throws SQLException, IOException, ClassNotFoundException {
        List<String> colaDatos = new ArrayList<String>();
        colaDatos.add(grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getContexto(idCola));
        colaDatos.add(penalty);
        libdb.Agentes.agenteCambiaPenalty(grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getIdAgente(), idCola, penalty);
        grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setColasMapaActualizar(idCola, colaDatos);
        eventos.agenteCambiaPenalty(grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension(), idCola, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getContexto(idCola), penalty);
        Main.supervisorEnviaPenalty(supervisorUsuario, agenteUsuario);
    }
}
