/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.util.ArrayList;
import java.util.List;
import org.java_websocket.WebSocket;

/**
 *
 * @author mgiannini
 */
public class Cola {

    private String colaNombre;
    private List<WebSocket> colaListadoObjWS;
    private int colaValorCA;                        // Carga de Agentes
    private int colaValorLLE;                       // Llamadas en Espera
    private int colaValorSLA;                       // SLA
    private int colaSeteoSLA;
    private int colaSeteoSCO;
    private int colaSeteoRSLA;                      // Rango SLA
    private boolean colaSeteoPOPUP;                 // Posee POPUP o NO.
    private boolean colaSeteoGRB;                   // Graba o no Graba
    private int colaPausa;                          // WrapUp Time
    private String colaSector;                      // Informo el sector asociado a la cola.
    private String colaDepto;                        // Informo el depto asociado a la cola.
    
    public Cola() {
        colaNombre = "";
        colaListadoObjWS = new ArrayList<>();
        colaValorCA = 0;
        colaValorLLE = 0;
        colaValorSLA = 0;
        colaSeteoSLA = 60;
        colaSeteoSCO = 10;
        colaSeteoRSLA = 45;
        colaSeteoPOPUP = false;
        colaSeteoGRB = false;
        colaPausa = 0;
        colaSector = "";
        colaDepto = "";
    }

    public void setColaNombre(String nombre) {
        this.colaNombre = nombre;
    }
    
    public void setColaListadoObjWS(List<WebSocket> conn) {
        this.colaListadoObjWS = conn;
    }
    
    public void setColaValorCA(int agentes) {
        this.colaValorCA = agentes;
    }
    
    public void setColaValorLLE(int llamadas) {
        this.colaValorLLE = llamadas;
    }
    
    public void setColaValorSLA(int nivel) {
        this.colaValorSLA = nivel;
    }
    
    public void setColaSeteoSLA(int colaSeteoSLA) {
        this.colaSeteoSLA = colaSeteoSLA;
    }
    
    public void setColaSeteoSCO(int colaSeteoSCO) {
        this.colaSeteoSCO = colaSeteoSCO;
    }
    
    public void setColaSeteoRSLA(int colaSeteoRSLA) {
        this.colaSeteoRSLA = colaSeteoRSLA;
    }

    public void setColaSeteoPOPUP(boolean colaSeteoPOPUP) {
        this.colaSeteoPOPUP = colaSeteoPOPUP;
    }

    public void setColaSeteoGRB(boolean colaSeteoGRB) {
        this.colaSeteoGRB = colaSeteoGRB;
    }
    
    public void setColaPausa(int colaPausa) {
        this.colaPausa = colaPausa;
    }
    
    public void setColaSector(String colaSector) {
        this.colaSector = colaSector;
    }
    
    public void setColaDepto(String colaDepto) {
        this.colaDepto = colaDepto;
    }
    
    public String getColaNombre() {
        return colaNombre;
    }
    
    public List<WebSocket> getColaListadoObjWS() {
        return colaListadoObjWS;
    }
    
    public int getColaValorCA() {
        return colaValorCA;
    }
    
    public int getColaValorLLE() {
        return colaValorLLE;
    }
    
    public int getColaValorSLA() {
        return colaValorSLA;
    }
    
    public int getColaSeteoSLA() {
        return colaSeteoSLA;
    }
    
    public int getColaSeteoSCO() {
        return colaSeteoSCO;
    }
    
    public int getColaSeteoRSLA() {
        return colaSeteoRSLA;
    }

    public boolean isColaSeteoPOPUP() {
        return colaSeteoPOPUP;
    }

    public boolean isColaSeteoGRB() {
        return colaSeteoGRB;
    }
    
    public int getColaPausa() {
        return colaPausa;
    }
    
    public String getColaSector() {
        return colaSector;
    }
    
    public String getColaDepto() {
        return colaDepto;
    }
}
