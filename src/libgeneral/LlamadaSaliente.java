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
public class LlamadaSaliente {
    private String idAgente;
    private String agenteExtension;
    private String agenteUsuario;
    private String numeroDestino;
    private String canal;
    private long fechaDiscado;
    private long fechaConexion;
    private boolean graba;
    private boolean atendida;
    private boolean isOpen;
    private LlamadaHold llamadaHold;
    
    public LlamadaSaliente() {
        idAgente = "";
        agenteExtension = "";
        agenteUsuario = "";
        numeroDestino = "";
        canal = "";
        fechaDiscado = 0;
        fechaConexion = 0;
        graba = false;
        atendida = false;
        isOpen = false;
        llamadaHold = new LlamadaHold();
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
    
    public void setAgenteExtension(String agenteExtension) {
        this.agenteExtension = agenteExtension;
    }
    
    public void setAgenteUsuario(String agenteUsuario) {
        this.agenteUsuario = agenteUsuario;
    }

    public void setNumeroDestino(String numeroDestino) {
        this.numeroDestino = numeroDestino;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public void setFechaDiscado(long fechaDiscado) {
        this.fechaDiscado = fechaDiscado;
    }

    public void setFechaConexion(long fechaConexion) {
        this.fechaConexion = fechaConexion;
    }
    
    public void setGraba(boolean graba) {
        this.graba = graba;
    }
    
    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }
    
    public void setLlamadaHold(LlamadaHold llamadaHold) {
        this.llamadaHold = llamadaHold;
    }

    public String getIdAgente() {
        return idAgente;
    }
    
    public String getAgenteExtension() {
        return agenteExtension;
    }
    
    public String getAgenteUsuario() {
        return agenteUsuario;
    }

    public String getNumeroDestino() {
        return numeroDestino;
    }

    public String getCanal() {
        return canal;
    }

    public long getFechaDiscado() {
        return fechaDiscado;
    }

    public long getFechaConexion() {
        return fechaConexion;
    }
    
    public boolean isGraba() {
        return graba;
    }
    
    public boolean isAtendida() {
        return atendida;
    }

    public LlamadaHold getLlamadaHold() {
        return llamadaHold;
    }
}
