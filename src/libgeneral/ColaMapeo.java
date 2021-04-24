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
 * Posee 1 HashMap: 
 * 
 * mapa_StrColaNum_ObjCola
 *
 * @author mgiannini
 */
public class ColaMapeo {

    private ConcurrentHashMap<String, Cola> mapa_StrColaNum_ObjCola = null;

    public ColaMapeo() {
        this.mapa_StrColaNum_ObjCola = new ConcurrentHashMap<>();
    }
    
    /**
     * Agrega un nuevo Objeto Cola en estado vacio.
     * No consulto si el numero de cola existe ya que Asterisk solo permite una cola por numero.
     * Ademas, este proceso SOLO debe correr al inicio de MATE.
     * 
     * @param key Numero de Cola.
     */
    public void agrega_StrColaNum_ObjCola(String key) {
        this.mapa_StrColaNum_ObjCola.put(key, new Cola());
    }
    
    /**
     * Agrega el nombre de la cola al KEY Cola Numero. Primero consulta si
     * existe el KEY Cola Numero. Si existe, se agrega el nombre de la cola. Si
     * no existe, primero se crea la KEY y el objeto cola.
     *
     * @param key Numero de la Cola.
     * @param value Nombre de la Cola.
     */
    public synchronized void agrega_StrColaNum_StrColaNom(String key, String value) {
        if (!this.mapa_StrColaNum_ObjCola.containsKey(key)) {
            this.mapa_StrColaNum_ObjCola.put(key, new Cola());
        }
        this.mapa_StrColaNum_ObjCola.get(key).setColaNombre(value);
    }
    
    /**
     * Agrega un WebSocket al objeto Cola. Primero consulta si existe el KEY
     * Cola Numero. Si existe, el objeto cola ya fue creado y por lo tanto, se
     * agrega el objeto WebSocket al Array del objeto Cola. 
     *
     * @param key Numero de Cola.
     * @param value Objeto WebSocket del Agente que pertenece a la Cola.
     */
    public synchronized void agrega_StrColaNum_ObjWebSocket(String key, WebSocket value) {
        if (this.mapa_StrColaNum_ObjCola.containsKey(key)) {
            this.mapa_StrColaNum_ObjCola.get(key).getColaListadoObjWS().add(value);
        }
    }

    /**
     * Obtengo el nombre de la cola a traves de su numero.
     *
     * @param key Numero de Cola.
     * @return Retorna el Nombre de la Cola.
     */
    public synchronized String obtiene_StrColaNum_StrColaNom(String key) {
        if (this.mapa_StrColaNum_ObjCola.containsKey(key)) {
            return this.mapa_StrColaNum_ObjCola.get(key).getColaNombre();
        }
        return "";
    }

    /**
     * Obtiene el objeto Cola a traves de su numero de Cola.
     *
     * @param key Numero de Cola.
     * @return Retorna el Objeto Cola.
     */
    public synchronized Cola obtiene_ColaNum_ObjCola(String key) {
        if (this.mapa_StrColaNum_ObjCola.containsKey(key)) {
            return this.mapa_StrColaNum_ObjCola.get(key);
        }
        return new Cola();
    }

    /**
     * Obtengo el Array de los WbSockets asociados a la cola.
     *
     * @param key Numero de Cola.
     * @return Retorna el Total de Objetos Websockets asociados a la Cola.
     */
    public synchronized List<WebSocket> obtiene_StrColaNum_ArrayWebSocket(String key) {
        if (this.mapa_StrColaNum_ObjCola.containsKey(key)) {
            return this.mapa_StrColaNum_ObjCola.get(key).getColaListadoObjWS();
        }
        return new ArrayList<WebSocket>();
    }

    /**
     * Funcion para eliminar el objeto WebSocket de las colas asociadas. La
     * funcion se usa tanto para Agentes como para Supervisores. Se envian como
     * parametros el objeto WebSocket y el array de las colas.
     *
     * @param value Objeto WebSocket.
     * @param colas Listado de las colas.
     */
    public synchronized void elimina_ArrayWebSocket_ArrayColas(WebSocket value, List<String> colas) {
        colas.forEach((idCola) -> {
            if (this.mapa_StrColaNum_ObjCola.containsKey(idCola)) {
                this.mapa_StrColaNum_ObjCola.get(idCola).getColaListadoObjWS().remove(value);
            }
        });
    }
    
    /**
     * 
     * @return Retorna la totalidad de las Colas.
     */
    public List<String> obtiene_NumerosCola() {
        List<String> retval = new ArrayList<String>();
        mapa_StrColaNum_ObjCola.keySet().forEach((key) -> {
            retval.add(key);
        });
        return retval;
    }
    
    /**
     * Funcion para saber si la cola existe. Se utiliza el KEY Cola Numero.
     *
     * @param key Numero de Cola.
     * @return Retorna TRUE en caso de que la cola exista.
     */
    public boolean obtiene_ExistenciaCola(String key) {
        return this.mapa_StrColaNum_ObjCola.containsKey(key);
    }
}
