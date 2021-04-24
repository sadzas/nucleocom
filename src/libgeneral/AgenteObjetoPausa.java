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
public class AgenteObjetoPausa {
    private String pausaNombre;
    private String pausaCola;               // Por el momento no es USABLE ya que no discrimino por cola de atencion. Pienso modificarlo en el futuro.
    private long fechaInicio;
    private int pausaProductiva;            // 0 = No productiva / 1 = Productiva
    
    public AgenteObjetoPausa() {
        pausaNombre = "";
        pausaCola = "";
        fechaInicio = 0;
        pausaProductiva = 0;
    
    }

    public void setPausaNombre(String pausaNombre) {
        this.pausaNombre = pausaNombre;
    }

    public void setPausaCola(String pausaCola) {
        this.pausaCola = pausaCola;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setPausaProductiva(int pausaProductiva) {
        this.pausaProductiva = pausaProductiva;
    }
    
    public String getPausaNombre() {
        return pausaNombre;
    }

    public String getPausaCola() {
        return pausaCola;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public int getPausaProductiva() {
        return pausaProductiva;
    }
}


