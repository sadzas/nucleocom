/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author mgiannini
 */
public class LlamadaCola {
    private String idAgente;
    private String agenteUsuario;
    private String canal;
    private String cola;
    private String origen;
    private String destino;
    private boolean isOpen;
    private long fechaIngreso;
    private long fechaRingueo;
    private long fechaAtencion;
    private long tiempoRingCola;
    private long tiempoRingAgente;
    private String corteOriginal;
    private boolean popup;
    private boolean graba;
    private String ctiurl;
    private int colaPausa;
    private String colaSector;
    private String colaDepto;
    private LlamadaHold llamadaHold;     // Control del HOLD durante la llamada.
    private ConcurrentLinkedDeque<LlamadaColaQueue> llamadaQueue;

    public LlamadaCola() {
        idAgente = "";
        agenteUsuario = "";
        canal = "";
        cola = "";
        origen = "Desconocido";
        destino = "";
        isOpen = true;
        fechaIngreso = 0;
        fechaRingueo = 0;
        fechaAtencion = 0;
        tiempoRingCola = 0;
        tiempoRingAgente = 0;
        corteOriginal = "";
        popup = false;
        graba = false;
        ctiurl = "";
        colaPausa = 0;
        colaSector = "";
        colaDepto = "";
        llamadaHold = new LlamadaHold();
        llamadaQueue = new ConcurrentLinkedDeque<LlamadaColaQueue>();
    }

    public ConcurrentLinkedDeque<LlamadaColaQueue> getLlamadaQueue() {
        return llamadaQueue;
    }

    public void setLlamadaQueue(ConcurrentLinkedDeque<LlamadaColaQueue> llamadaQueue) {
        this.llamadaQueue = llamadaQueue;
    }

    public boolean isIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void setIdAgente(String idAgente) {
        this.idAgente = idAgente;
    }
    
    public void setAgenteUsuario(String agenteUsuario) {
        this.agenteUsuario = agenteUsuario;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public void setCola(String cola) {
        this.cola = cola;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }
    
    public void setFechaIngreso(long fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
    
    public void setFechaRingueo(long fechaRingueo) {
        this.fechaRingueo = fechaRingueo;
    }

    public void setFechaAtencion(long fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public void setTiempoRingCola(long tiempoRingCola) {
        this.tiempoRingCola = tiempoRingCola;
    }

    public void setTiempoRingAgente(long tiempoRingAgente) {
        this.tiempoRingAgente = tiempoRingAgente;
    }
    
    public void setCorteOriginal(String corteOriginal) {
        this.corteOriginal = corteOriginal;
    }

    public void setPopup(boolean popup) {
        this.popup = popup;
    }

    public void setGraba(boolean graba) {
        this.graba = graba;
    }

    public void setCtiurl(String ctiurl) {
        this.ctiurl = ctiurl;
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
    
    public void setLlamadaHold(LlamadaHold llamadaHold) {
        this.llamadaHold = llamadaHold;
    }
    
    public String getIdAgente() {
        return idAgente;
    }
    
    public String getAgenteUsuario() {
        return agenteUsuario;
    }

    public String getCanal() {
        return canal;
    }

    public String getCola() {
        return cola;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }
    
    public long getFechaIngreso() {
        return fechaIngreso;
    }
    
    public long getFechaRingueo() {
        return fechaRingueo;
    }

    public long getFechaAtencion() {
        return fechaAtencion;
    }

    public long getTiempoRingCola() {
        return tiempoRingCola;
    }

    public long getTiempoRingAgente() {
        return tiempoRingAgente;
    }
    
    public String getCorteOriginal() {
        return corteOriginal;
    }

    public boolean isPopup() {
        return popup;
    }

    public boolean isGraba() {
        return graba;
    }

    public String getCtiurl() {
        return ctiurl;
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
    
    public LlamadaHold getLlamadaHold() {
        return llamadaHold;
    }
}
