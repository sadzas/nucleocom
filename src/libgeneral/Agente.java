/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.WebSocket;

/**
 *
 * @author mgiannini
 */
public class Agente {

    private String idAgente;                                                // ID de la DB del agente
    private String agenteUsuario;                                           // Username del Agente
    private String agenteStrWS;                                             // WebSocket del Agente
    private String agenteExtension;                                         // Extension del Agente
    private WebSocket agenteObjWS;                                          // Objeto WebSocket
    private AgenteObjetoPausa agenteObjetoPausa;                            // Objeto Pausa con el detalle actual del estado del agente.
    private int agenteEstadoFlag;                                           // Valor numerico del agenteEstado
    private String agenteEstado;                                            // Estado del Agente => Activo / TipoPausa
    private String agenteActividad;                                         // Actividad del Agente => Libre / Hablando / Ringueando / Espera
    private List<String> agenteColasNumero;                                 // Colas del Agente
    private ConcurrentHashMap<String, List<String>> agenteColasMapa = null; // Colas del Agente
    private ConcurrentHashMap<String, String> agenteListadoPausa = null;    // Pausas del Agente
    private long agenteFechaLogueo;                                         // TimeStamp de la Fecha y Hora del logueo del Agente
    private long agenteFechaUltAct;                                         // TimeStamp de la ultima llamada del Agente o hacia el agente
    private long agenteFechaUltEst;                                         // TimeStamp del ultimo agenteEstado del Agente.
    private String agenteCanalLlamada;                                      // Canal actual del agente (debe vaciarse al cortar la llamada)
    private String agenteColaLlamada;                                       // Cola por la que está recibiendo la llamada
    private String agenteNumeroHablando;                                    // Numero origen / destino con quien esta hablando el agente.
    private boolean agenteGrabaSaliente;                                    // Indica si se graban las llamadas salientes del agente.
    private boolean agenteAccionSupervisor;                                 // Indica si el supervisor esta realizando una accion sobre el agente.
    private long agenteFechaAlarma;                                         // TimeStamp de la alarma para aviso del agente.
    private String agenteSector;                                            // ID unico del Sector
    private String agenteDepto;                                             // ID unico del Depto

    //Constructor
    public Agente() {
        idAgente = "";
        agenteUsuario = "";
        agenteStrWS = "";
        agenteExtension = "";
        agenteObjetoPausa = new AgenteObjetoPausa();
        agenteEstadoFlag = 0;
        agenteEstado = "";
        agenteActividad = "Libre";
        agenteColasNumero = new ArrayList<>();
        agenteColasMapa = new ConcurrentHashMap<>();
        agenteListadoPausa = new ConcurrentHashMap<>();
        agenteFechaLogueo = 0;
        agenteFechaUltAct = 0;
        agenteFechaUltEst = 0;
        agenteCanalLlamada = "";
        agenteColaLlamada = "-";
        agenteNumeroHablando = "-";
        agenteGrabaSaliente = false;
        agenteAccionSupervisor = false;
        agenteFechaAlarma = 0;
        agenteSector = "";
        agenteDepto = "";
    }
    //Cierre del constructor

    public void setIdAgente(String idAgente) {
        this.idAgente = idAgente;
    }
    
    public void setAgenteUsuario(String agenteUsuario) {
        this.agenteUsuario = agenteUsuario;
    }

    public void setAgenteStrWS(String agenteStrWS) {
        this.agenteStrWS = agenteStrWS;
    }

    public void setAgenteExtension(String agenteExtension) {
        this.agenteExtension = agenteExtension;
    }

    public void setAgenteObjWS(WebSocket agenteObjWS) {
        this.agenteObjWS = agenteObjWS;
    }
    
    /**
     * Setea las caracteristicas del objeto Pausa Actual.
     * 
     * @param agenteObjetoPausa Objeto Pausa Actual
     */
    public void setAgenteObjetoPausa(AgenteObjetoPausa agenteObjetoPausa) {
        this.agenteObjetoPausa = agenteObjetoPausa;
    }
    
    public void setAgenteEstadoFlag(int agenteEstadoFlag) {
        this.agenteEstadoFlag = agenteEstadoFlag;
    }

    public void setAgenteEstado(String agenteEstado) {
        this.agenteEstado = agenteEstado;
    }

    public void setAgenteActividad(String agenteActividad) {
        this.agenteActividad = agenteActividad;
    }
    
    /**
     * Funcion que agrega una Nueva cola al listado del agente.
     * Se utiliza cuando un Supervisor desea agregar un agente de forma dinamica.
     * 
     * @param idCola Nueva cola para agregar.
     */
    public void setColasNumeroAgregar(String idCola) {
        this.agenteColasNumero.add(idCola);
    }
    
    public void setColasNumeroAsignar(List<String> colasNumero) {
        this.agenteColasNumero = colasNumero;
    }
    
    /**
     * Funcion que agrega una nueva cola al mapeo del agente.
     * Se utiliza cuando un supervisor desea agregar un agente de forma dinamica.
     * 
     * @param colasMapa Contiene el mapeo del idCola y Contexto que se desea agregar.
     */
    public void setColasMapaAgregar(ConcurrentHashMap<String, List<String>> colasMapa) {
        this.agenteColasMapa.putAll(colasMapa);
    }
        
    public void setColasMapaAsignar(ConcurrentHashMap<String, List<String>> colasMapa) {
        this.agenteColasMapa = colasMapa;
    }
    
    public void setColasMapaActualizar(String key, List<String> colasMapa) {
        this.agenteColasMapa.put(key, colasMapa);
    }

    public void setListadoPausas(ConcurrentHashMap<String, String> pausas) {
        this.agenteListadoPausa = pausas;
    }

    public void setAgenteFechaLogueo(long agenteFechaLogueo) {
        this.agenteFechaLogueo = agenteFechaLogueo;
    }
    
    public void setAgenteFechaUltAct(long agenteFechaUltAct) {
        this.agenteFechaUltAct = agenteFechaUltAct;
    }
    
    public void setAgenteFechaUltEst(long agenteFechaUltEst) {
        this.agenteFechaUltEst = agenteFechaUltEst;
    }
    
    public void setAgenteCanalLlamada(String agenteCanalLlamada) {
        this.agenteCanalLlamada = agenteCanalLlamada;
    }
    
    public void setAgenteColaLlamada(String agenteColaLlamada) {
        this.agenteColaLlamada = agenteColaLlamada;
    }
    
    public void setAgenteNumeroHablando(String numeroHablando) {
        this.agenteNumeroHablando = numeroHablando;
    }

    public void setAgenteGrabaSaliente(boolean agenteGrabaSaliente) {
        this.agenteGrabaSaliente = agenteGrabaSaliente;
    }

    public void setAgenteAccionSupervisor(boolean agenteAccionSupervisor) {
        this.agenteAccionSupervisor = agenteAccionSupervisor;
    }

    public void setAgenteFechaAlarma(long agenteFechaAlarma) {
        this.agenteFechaAlarma = agenteFechaAlarma;
    }
    
    public void setAgenteSector(String agenteSector) {
        this.agenteSector = agenteSector;
    }
    
    public void setAgenteDepto(String agenteDepto) {
        this.agenteDepto = agenteDepto;
    }
    
    public String getIdAgente() {
        return idAgente;
    }
    
    public String getAgenteUsuario() {
        return agenteUsuario;
    }

    public String getAgenteStrWS() {
        return agenteStrWS;
    }

    public String getAgenteExtension() {
        return agenteExtension;
    }

    public WebSocket getAgenteObjWS() {
        return agenteObjWS;
    }
    
    public AgenteObjetoPausa getAgenteObjetoPausa() {
        return agenteObjetoPausa;
    }
    
    public int getAgenteEstadoFlag() {
        return agenteEstadoFlag;
    }

    public String getAgenteEstado() {
        return agenteEstado;
    }

    public String getAgenteActividad() {
        return agenteActividad;
    }
    
    public List<String> getAgenteColasNumero() {
        return agenteColasNumero;
    }
        
    public ConcurrentHashMap<String, List<String>> getAgenteColasMapa() {
        return agenteColasMapa;
    }
    
    public String getContexto(String key) {
        String retval = null;
        if (this.agenteColasMapa.containsKey(key)) {
            retval = this.agenteColasMapa.get(key).get(0);
        }
        return retval;
    }

    public String getListadoPausas(String key) {
        String retval = null;
        if (this.agenteListadoPausa.containsKey(key)) {
            retval = this.agenteListadoPausa.get(key);
        }
        return retval;
    }

    public long getAgenteFechaLogueo() {
        return agenteFechaLogueo;
    }
    
    public long getAgenteFechaUltAct() {
        return agenteFechaUltAct;
    }
    
    public long getAgenteFechaUltEst() {
        return agenteFechaUltEst;
    }
    
    public String getAgenteCanalLlamada() {
        return agenteCanalLlamada;
    }

    public String getAgenteColaLlamada() {
        return agenteColaLlamada;
    }
    
    public String getAgenteNumeroHablando() {
        return agenteNumeroHablando;
    }

    public boolean isAgenteGrabaSaliente() {
        return agenteGrabaSaliente;
    }

    public boolean isAgenteAccionSupervisor() {
        return agenteAccionSupervisor;
    }

    public long getAgenteFechaAlarma() {
        return agenteFechaAlarma;
    }
    
    public String getAgenteSector() {
        return agenteSector;
    }
    
    public String getAgenteDepto() {
        return agenteDepto;
    }
}
