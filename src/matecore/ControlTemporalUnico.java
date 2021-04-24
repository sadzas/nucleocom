/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matecore;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase para ejecutar funiones en un tiempo determinado.
 *
 * @author mgiannini
 */
public class ControlTemporalUnico {

    /**
     * Metodo para agendar el fin de la AutoPausa (WrapUp Time) 
     * (Pausa generada automaticamente cuando el agente termina una llamada.)
     * 
     * @param operadorUsuario Nombre de usuario del Agente.
     * @param segundos Numero de segundos para el agendado de la funcion.
     */
    public void agenteAgendoFinPausa(String operadorUsuario, int segundos) {
        try {
            TimerTask agendoTarea = new TimerTask() {
                @Override
                public void run() {
                    Main.colaDespausaFinLlamado(operadorUsuario);
                }
            };
            
            Logger.getLogger(ControlTemporalUnico.class.getName()).log(Level.DEBUG, "Operador: "+operadorUsuario+". | agenteAgendoFinPausa | Se agenda el fin de la autopausa en :"+segundos+" segundos");
            Timer delay = new Timer();
            delay.schedule(agendoTarea, segundos*1000);
        } catch (Exception e) {
            Logger.getLogger(ControlTemporalUnico.class.getName()).log(Level.FATAL, "agenteAgendoFinPausa", e);
        }
    };
}