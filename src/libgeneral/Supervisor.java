/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libgeneral;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.java_websocket.WebSocket;

/**
 *
 * @author mgiannini
 */
public class Supervisor
{
    private String supervisorUsuario;               // supervisorUsuario del Supervisor
    private String supervisorStrWS;                 // WebSocket del Supervisor
    private WebSocket supervisorObjWS;              // Objeto WebSocket
    private List<String> supervisorColas;           // Colas del agente
    private Date supervisorFechaLogueo;             // Fecha y Hora del logueo del Supervisor

    //Constructor
    public Supervisor() {
        supervisorUsuario = "";
        supervisorStrWS = "";
        supervisorColas = new ArrayList<>();
        supervisorFechaLogueo = new Date();
    }
    //Cierre del constructor

    public void setSupervisorUsuario(String supervisorUsuario) {
        this.supervisorUsuario = supervisorUsuario;
    }

    public void setSupervisorStrWS(String supervisorStrWS) {
        this.supervisorStrWS = supervisorStrWS;
    }

    public void setSupervisorObjWS(WebSocket supervisorObjWS) {
        this.supervisorObjWS = supervisorObjWS;
    }

    public void setSupervisorColas(List<String> supervisorColas) {
        this.supervisorColas = supervisorColas;
    }
    
    public void setSupervisorFechaLogueo(Date supervisorFechaLogueo) {
        this.supervisorFechaLogueo = supervisorFechaLogueo;
    }

    public String getSupervisorUsuario() {
        return supervisorUsuario;
    }

    public String getSupervisorStrWS() {
        return supervisorStrWS;
    }

    public WebSocket getSupervisorObjWS() {
        return supervisorObjWS;
    }

    public List<String> getSupervisorColas() {
        return supervisorColas;
    }
    
    public Date getSupervisorFechaLogueo() {
        return supervisorFechaLogueo;
    }
}
