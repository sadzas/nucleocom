/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

/**
 * Mapeo de los diferentes atributos de un agente.
 *
 * Posee 3 HashMap: 
 * 
 * mapa_StrAgenteUsuario_ObjAgente
 *
 * mapa_StrWebSocket_StrAgenteUsuario
 *
 * mapa_StrAgenteExtension_StrWebSocket
 *
 * @author mgiannini
 */
public class AgenteMapeo {

    private ConcurrentHashMap<String, Agente> mapa_StrAgenteUsuario_ObjAgente = null;
    private ConcurrentHashMap<String, String> mapa_StrWebSocket_StrAgenteUsuario = null;
    private ConcurrentHashMap<String, String> mapa_StrAgenteExtension_StrWebSocket = null;
    private ConcurrentHashMap<String, Agente> mapa_StrIdAgente_ObjAgente = null;


    public AgenteMapeo() {
        this.mapa_StrAgenteUsuario_ObjAgente = new ConcurrentHashMap<>();
        this.mapa_StrWebSocket_StrAgenteUsuario = new ConcurrentHashMap<>();
        this.mapa_StrAgenteExtension_StrWebSocket = new ConcurrentHashMap<>();
        this.mapa_StrIdAgente_ObjAgente = new ConcurrentHashMap<>();
    }

    public Agente getMapa_StrIdAgente_ObjAgente(String key) {
        Agente retval = null;
        if (this.mapa_StrIdAgente_ObjAgente.containsKey(key)) {
            retval = this.mapa_StrIdAgente_ObjAgente.get(key);
        }
        return retval;
    }

    public void setMapa_StrIdAgente_ObjAgente(String key, Agente value) {
        this.mapa_StrIdAgente_ObjAgente.put(key, value);
    }

    

    /**
     * Agrega un objeto Agente a traves de su String Username.
     *
     * @param key Nombre de usuario del Agente.
     * @param value Objeto Agente.
     */
    public void agrega_StrUsername_ObjAgente(String key, Agente value) {
        this.mapa_StrAgenteUsuario_ObjAgente.put(key, value);
    }

    /**
     * Agrega un String Username a traves de su String WebSocket.
     *
     * @param key String del WebSocket.
     * @param value Nombre de usuario del Agente.
     */
    public void agrega_StrWebSocket_StrUsername(String key, String value) {
        this.mapa_StrWebSocket_StrAgenteUsuario.put(key, value);
    }

    /**
     * Agrega un String WebSocket a traves de su String Extension.
     *
     * @param key String de la Extension.
     * @param value String del WebSocket.
     */
    public void agrega_StrExtension_StrWebSocket(String key, String value) {
        this.mapa_StrAgenteExtension_StrWebSocket.put(key, value);
    }

    /**
     * Elimina el mapeo String Username - Objeto Agente.
     *
     * @param key Nombre de usuario del Agente.
     */
    public void elimina_StrUsername_ObjAgente(String key) {
        if (this.mapa_StrAgenteUsuario_ObjAgente.containsKey(key)) {
            this.mapa_StrAgenteUsuario_ObjAgente.remove(key);
        }
    }

    /**
     * Elimina el mapeo String WebSocket - String Username.
     *
     * @param key String WebSocket.
     */
    public void elimina_StrWebSocket_StrUsername(String key) {
        if (this.mapa_StrWebSocket_StrAgenteUsuario.containsKey(key)) {
            this.mapa_StrWebSocket_StrAgenteUsuario.remove(key);
        }
    }

    /**
     * Elimina el mapeo String Extension - String WebSocket.
     *
     * @param key String de la extension.
     */
    public void elimina_StrExtension_StrWebSocket(String key) {
        if (this.mapa_StrAgenteExtension_StrWebSocket.containsKey(key)) {
            this.mapa_StrAgenteExtension_StrWebSocket.remove(key);
        }
    }

    /**
     * Obtengo el Objeto Agente a traves de su String Username.
     *
     * @param key Nombre de usuario del Agente.
     * @return Retorna el objeto Agente.
     */
    public Agente obtiene_StrUsername_ObjAgente(String key) {
        Agente retval = null;
        if (this.mapa_StrAgenteUsuario_ObjAgente.containsKey(key)) {
            retval = this.mapa_StrAgenteUsuario_ObjAgente.get(key);
        }
        return retval;
    }

    /**
     * Obtengo el String Username a traves del String WebSocket.
     *
     * @param key String del WebSocket.
     * @return Retorna el Nombre de Usuario del agente.
     */
    public String obtiene_StrWebSocket_StrUsername(String key) {
        String retval = null;
        if (this.mapa_StrWebSocket_StrAgenteUsuario.containsKey(key)) {
            retval = this.mapa_StrWebSocket_StrAgenteUsuario.get(key);
        }
        return retval;
    }

    /**
     * Obtengo el Objeto WebSocket a traves del String Username. Esto lo realizo
     * a traves del mapeo Username - Agente. Uno de los atributos del agente es
     * el objeto WebSocket.
     *
     * @param key Nombre de usuario del Agente.
     * @return Retorna el Objeto WebSocket.
     */
    public WebSocket obtiene_StrUsername_ObjWebSocket(String key) {
        WebSocket retval = null;
        if (this.mapa_StrAgenteUsuario_ObjAgente.containsKey(key)) {
            retval = this.mapa_StrAgenteUsuario_ObjAgente.get(key).getAgenteObjWS();
        }
        return retval;
    }

    /**
     * Obtengo el String WebSocket a traves del String Extension.
     *
     * @param key String de la extension.
     * @return Retorna el String WebSocket.
     */
    public String obtiene_StrExtension_StrWebSocket(String key) {
        String retval = null;
        if (this.mapa_StrAgenteExtension_StrWebSocket.containsKey(key)) {
            retval = this.mapa_StrAgenteExtension_StrWebSocket.get(key);
        }
        return retval;
    }

    /**
     * Retorno TRUE si el key Username Existe.
     *
     * @param key Nombre de usuario del Agente.
     * @return Retorna TRUE en case de que el Agente exista.
     */
    public boolean chequea_StrUsername_ObjAgente(String key) {
        return this.mapa_StrAgenteUsuario_ObjAgente.containsKey(key);
    }

    /**
     * Retorna TRUE si el key Extension existe.
     *
     * @param key Numero de extension.
     * @return Retorna TRUE en case de que la extension exista.
     */
    public boolean chequea_StrExtension_StrWebSocket(String key) {
        return this.mapa_StrAgenteExtension_StrWebSocket.containsKey(key);
    }

    /**
     * Retorno la totalidad de los Objetos WebSocket de todos los agentes. El
     * retorno se hace en un array del tipo WebSocket.
     *
     * @return Retorna el total de los WebSockets.
     */
    public synchronized List<WebSocket> obtiene_ListWebSockets() {
        List<WebSocket> retval = new ArrayList<>();
        try {
            mapa_StrAgenteUsuario_ObjAgente.forEach((k, v) -> {
                retval.add(v.getAgenteObjWS());
            });
        } catch (NullPointerException e) {
            Logger.getLogger(AgenteMapeo.class.getName()).log(org.apache.log4j.Level.ERROR, "obtieneArrayWebSockets. Error: ", e);
        }
        return retval;
    }

    /**
     * Retorno la totalidad de los String username de todos los agentes. El
     * retorno se hace en un array del tipo String.
     *
     * @return Retorna la totalidad de los nombres de usuario.
     */
    public synchronized List<String> obtiene_ListUsernames() {
        List<String> retval = new ArrayList<String>();
        try {
            mapa_StrAgenteUsuario_ObjAgente.forEach((k, v) -> {
                retval.add(v.getAgenteUsuario());
            });
        } catch (NullPointerException e) {
            Logger.getLogger(AgenteMapeo.class.getName()).log(org.apache.log4j.Level.ERROR, "obtieneArrayUsernames. Error: ", e);
        }
        return retval;
    }
}
