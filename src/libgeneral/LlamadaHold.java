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
public class LlamadaHold {
    private long fechaInicio;
    private long acumuladoLlamada;
    private long acumuladoAgente;
    private boolean isOpen;
    
    public LlamadaHold() {
        fechaInicio = 0;
        acumuladoLlamada = 0;
        acumuladoAgente = 0;
        isOpen = false;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public long getAcumuladoLlamada() {
        return acumuladoLlamada;
    }
    
    public long getAcumuladoAgente() {
        return acumuladoAgente;
    }

    public boolean isIsOpen() {
        return isOpen;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
        isOpen = true;
    }

    public void setAcumuladoLlamada(long acumuladoLlamada) {
        this.acumuladoLlamada += acumuladoLlamada;
    }
    
    public void setAcumuladoAgente(long acumuladoAgente) {
        this.acumuladoAgente += acumuladoAgente;
    }
    
    public void setAcumuloHold(long fechaFinaliza) {
        long hold = (fechaFinaliza - fechaInicio) / 1000;
        setAcumuladoLlamada(hold);
        setAcumuladoAgente(hold);
        isOpen = false;
    }
    
    public void setAcumuloAgenteReset() {
        this.acumuladoAgente = 0;
    }
    
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
}
