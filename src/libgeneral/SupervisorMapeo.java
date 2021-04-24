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
 * Mapeo de los diferentes atributos de un Supervisor.
 *
 * Posee 2 HashMap: 
 * 
 * mapa_StrWebSocket_ObjSupervisor
 *
 * mapa_StrUsername_StrWebSocket
 *
 * @author mgiannini
 */
public class SupervisorMapeo {

    private ConcurrentHashMap<String, Supervisor> mapa_StrWebSocket_ObjSupervisor = null;
    private ConcurrentHashMap<String, String> mapa_StrUsername_StrWebSocket = null;

    public SupervisorMapeo() {
        this.mapa_StrWebSocket_ObjSupervisor = new ConcurrentHashMap<>();
        this.mapa_StrUsername_StrWebSocket = new ConcurrentHashMap<>();
    }

    public void agrega_StrWebSocket_ObjSupervisor(String key, Supervisor value) {
        this.mapa_StrWebSocket_ObjSupervisor.put(key, value);
    }

    public void agrega_StrUsername_StrWebSocket(String key, String value) {
        this.mapa_StrUsername_StrWebSocket.put(key, value);
    }

    /**
     * Retorna el objeto Supervisor.
     * 
     * @param key String WebSocket.
     * @return Objeto Supervisor
     */
    public synchronized Supervisor obtiene_StrWebSocket_ObjSupervisor(String key) {
        Supervisor retval = null;
        if (this.mapa_StrWebSocket_ObjSupervisor.containsKey(key)) {
            retval = this.mapa_StrWebSocket_ObjSupervisor.get(key);
        }
        return retval;
    }

    /**
     *
     * @return Retorna la totalidad de los WebSocket.
     */
    public synchronized List<WebSocket> obtiene_ListWebSockets() {
        List<WebSocket> retval = new ArrayList<>();
        mapa_StrWebSocket_ObjSupervisor.forEach((k, v) -> {
            retval.add(v.getSupervisorObjWS());
        });
        return retval;
    }

    /**
     * Retorna el String del WebSocket.
     * 
     * @param key Nombre de usuario del Supervisor
     * @return String WebSocket.
     */
    public String obtiene_StrUsername_StrWebSocket(String key) {
        String retval = null;
        if (this.mapa_StrUsername_StrWebSocket.containsKey(key)) {
            retval = this.mapa_StrUsername_StrWebSocket.get(key);
        }
        return retval;
    }

    /**
     * Retorna el objeto WebSocket del Supervior.
     * 
     * @param key String WebSoscket del Supervisor.
     * @return Objeto WebSocket.
     */
    public WebSocket obtiene_StrWebSocket_ObjWebSocket(String key) {
        WebSocket retval = null;
        if (this.mapa_StrWebSocket_ObjSupervisor.containsKey(key)) {
            retval = this.mapa_StrWebSocket_ObjSupervisor.get(key).getSupervisorObjWS();
        }
        return retval;
    }

    /**
     * Elimina el Objeto Supervisor y el mapeo del Usuario al String WebSocket.
     * 
     * @param key String WebSocket del Supervisor.
     */
    public void elimina_StrWebSocket_ObjSupervisor(String key) {
        if (this.mapa_StrWebSocket_ObjSupervisor.containsKey(key)) {
            String username = this.mapa_StrWebSocket_ObjSupervisor.get(key).getSupervisorUsuario();
            this.mapa_StrUsername_StrWebSocket.remove(username);
            this.mapa_StrWebSocket_ObjSupervisor.remove(key);
        }
    }
}
