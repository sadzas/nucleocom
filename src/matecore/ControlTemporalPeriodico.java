/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matecore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase para la ejecucion periodica de funciones.
 * 
 * @author mgiannini
 */
public class ControlTemporalPeriodico {

    private final ScheduledExecutorService chequeaTiempoAgente = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService monitoreaSupervisor = Executors.newScheduledThreadPool(3);

    /**
     * Ejecuta la funcion agenteExpulsaPorInactivo cada 30 minutos (1800 segundos).
     * 
     */
    public void agenteChequeaActividad() {
        final Runnable agenteActividad = () -> {
            try {
                Main.agenteExpulsaPorInactivo();
            } catch (Exception e) {
                Logger.getLogger(ControlTemporalPeriodico.class.getName()).log(Level.ERROR, "agenteChequeaActividad", e);
            }
        };
        final ScheduledFuture<?> endHandle = chequeaTiempoAgente.scheduleAtFixedRate(agenteActividad, 1800, 1800, SECONDS);
    }
    
    /**
     * Envia la informacion de las colas a los agentes y supervisores.
     * 
     */
    public void colaMonitoreo() {
        final Runnable colaMonitor = () -> {
            try {
                Main.supervisorInfoColas();
                Main.supervisorInfoAgentes();
                Main.agenteInfoColas();
            } catch (Exception e) {
                Logger.getLogger(ControlTemporalPeriodico.class.getName()).log(Level.ERROR, "colaMonitoreo", e);
            }
        };
        final ScheduledFuture<?> endHandle = monitoreaSupervisor.scheduleAtFixedRate(colaMonitor, 10, 10, SECONDS);
    }     
}
