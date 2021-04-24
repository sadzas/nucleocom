/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

/**
 *
 * @author mgiannini
 */
public class LlamadaTransferencia {
    String idAgente;
    String agenteUsuario;
    long fechaAtencion;
    String origen;
    boolean isOpen;
    
    public LlamadaTransferencia() {
        idAgente = "";
        agenteUsuario = "";
        fechaAtencion = 0;
        origen = "";
        isOpen = false;
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

    public void setFechaAtencion(long fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getIdAgente() {
        return idAgente;
    }

    public String getAgenteUsuario() {
        return agenteUsuario;
    }

    public long getFechaAtencion() {
        return fechaAtencion;
    }

    public String getOrigen() {
        return origen;
    }
    
    public boolean isIsOpen() {
        return isOpen;
    }
}
