/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Esta clase es solo para las llamadas entrantes a traves de una cola de
 * atencion.
 *
 * @author mgiannini
 */
public class LlamadaColaMapeo {

    private ConcurrentHashMap<String, LlamadaCola> mapa_StrLinkedid_ObjLlamada = null;

    public LlamadaColaMapeo() {
        this.mapa_StrLinkedid_ObjLlamada = new ConcurrentHashMap<>();
    }

    public void agrega_StrLinkedid_ObjLlamada(String key, LlamadaCola value) {
        this.mapa_StrLinkedid_ObjLlamada.put(key, value);
    }

    public LlamadaCola obtiene_StrLinkedid_ObjLlamada(String key) {
        LlamadaCola retval = null;
        if (this.mapa_StrLinkedid_ObjLlamada.containsKey(key)) {
            retval = this.mapa_StrLinkedid_ObjLlamada.get(key);
        }
        return retval;
    }

    public void elimina_StrLinkedid_ObjLlamada(String key) {
        if (this.mapa_StrLinkedid_ObjLlamada.containsKey(key)) {
            this.mapa_StrLinkedid_ObjLlamada.remove(key);
        }
    }

    public boolean chequea_StrLinkedid_ObjLlamada(String key) {
        return this.mapa_StrLinkedid_ObjLlamada.containsKey(key);
    }

    public synchronized void obtieneListadoLlamadas() {
        mapa_StrLinkedid_ObjLlamada.forEach((k, v) -> {
            System.out.println(v.getCanal());
        });
    }
}
