/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.log4j.Logger;
import static java.util.concurrent.TimeUnit.SECONDS;
import libgeneral.Funciones;
import libgeneral.LlamadaCola;
import libgeneral.LlamadaColaMapeo;
import libgeneral.LlamadaColaQueue;
import libgeneral.LlamadaSaliente;
import libgeneral.LlamadaSalienteMapeo;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.ManagerCommunicationException;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.ManagerEventListenerProxy;
import org.asteriskjava.manager.TimeoutException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.action.QueueAddAction;
import org.asteriskjava.manager.action.QueuePauseAction;
import org.asteriskjava.manager.action.QueueRemoveAction;

import org.asteriskjava.manager.event.QueueMemberAddedEvent;
import org.asteriskjava.manager.event.QueueMemberRemovedEvent;
import matecore.Main;
import org.apache.log4j.Level;
import org.asteriskjava.manager.action.MixMonitorAction;
import org.asteriskjava.manager.action.QueuePenaltyAction;
import org.asteriskjava.manager.action.QueueSummaryAction;
import org.asteriskjava.manager.event.AgentCalledEvent;
import org.asteriskjava.manager.event.AgentCompleteEvent;
import org.asteriskjava.manager.event.AgentConnectEvent;
import org.asteriskjava.manager.event.AgentRingNoAnswerEvent;
import org.asteriskjava.manager.event.AttendedTransferEvent;
import org.asteriskjava.manager.event.BlindTransferEvent;
import org.asteriskjava.manager.event.CdrEvent;
import org.asteriskjava.manager.event.ExtensionStatusEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.HoldEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.QueueCallerAbandonEvent;
import org.asteriskjava.manager.event.QueueCallerJoinEvent;
import org.asteriskjava.manager.event.QueueMemberPauseEvent;
import org.asteriskjava.manager.event.QueueSummaryEvent;
import org.asteriskjava.manager.event.UnholdEvent;

/**
 *
 * @author mgiannini
 * 
 * Formato de los LOGS
 * 
 * DEBUG - Agente
 * Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Agente: "+ agente+" | Metodo | Detalle");
 * 
 * DEBUG - Llamada
 * Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ linkedid+" | Operador / Metodo | Detalle");
 * 
 * FATAL
 * Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "Metodo | Detalle | Exception");
 * 
 */
public class LibManagerEvents extends AbstractManagerEventListener implements ManagerEventListener {

    private final ManagerConnection managerConnection;
    private DefaultAsteriskServer asteriskServer;
    private final LlamadaColaMapeo grupoLlamadaCola;
    private final LlamadaSalienteMapeo grupoLlamadaSaliente;
    
    /* Las siguientes variables corresponden a los eventos del agenteslog.
    * Estos mismos codigos se encuentran en la tabla eventos de la DB matecore.
    */
    private final int eventoLogueo = 1;
    private final int eventoPausa = 2;
    private final int eventoNoAtiende = 3;
    private final int eventoHold = 4;
    private final int eventoHabla = 5;
    private final int eventoExtensionDesc = 6;
    
    /* Las siguientes variables corresponden a los eventos para la trazabilidad
    * de las llamadas.    
    */
    private final int join = 0;
    private final int abandon = 1;
    private final int called = 2;
    private final int ringnoanswer = 3;
    private final int connected = 4;
    private final int completed = 5;
    private final int hold = 6;
    private final int unhold = 7;
    private final int attendedtransfer = 8;
    private final int blindtransfer = 9;

    public LibManagerEvents() throws IOException, ManagerCommunicationException, InterruptedException {
        this.grupoLlamadaCola = new LlamadaColaMapeo();
        this.grupoLlamadaSaliente = new LlamadaSalienteMapeo();
        ManagerConnectionFactory factory = new ManagerConnectionFactory(Main.getMNGServidor(), Main.getMNGUsuario(), Main.getMNGPassword());
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException, SQLException {
        managerConnection.addEventListener(new ManagerEventListenerProxy(this));
        this.escuchar();
    }

    private void escuchar() throws ManagerCommunicationException, InterruptedException {
        try {
            managerConnection.login();
            asteriskServer = new DefaultAsteriskServer(managerConnection);
            asteriskServer.initialize();
            managerConnection.registerUserEventClass(DoQueueStatus.class);
            managerConnection.registerUserEventClass(CallBackRequestEvent.class);
        } catch (IOException | IllegalStateException | AuthenticationFailedException | TimeoutException e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "escuchar", e);
        }

        try {
            colasCargaDesdeDBInicio(asteriskServer);
        } catch (Exception e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "escucharQueue", e);
        }
        
        final Runnable datosQueue = () -> {
            try {
                colasCargaDesdeDB(asteriskServer);
            } catch (Exception e) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "runEscucharQueue", e);
            }
        };
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(datosQueue, 20, 20, SECONDS);
    }

    /**
     * Evento del manager para el logueo de Agente.
     *
     * La sentencia IF es debido a que puede que la PBX tenga agentes estaticos
     * o dinamicos. La sentencia IF se puede eliminar en caso de dedicar la PBX
     * exclusivamente a un CALLCenter
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(QueueMemberAddedEvent evento) {
        /*
        Event: QueueMemberAdded
        Privilege: agent,all
        Queue: 201
        MemberName: mgiannini
        Interface: Local/206040@from-internal
        StateInterface: SIP/206040
        Membership: dynamic
        Penalty: 0
        CallsTaken: 0
        LastCall: 0
        LastPause: 0
        InCall: 0
        Status: 5
            Status - The numeric device state status of the queue member.
            0 - AST_DEVICE_UNKNOWN
            1 - AST_DEVICE_NOT_INUSE
            2 - AST_DEVICE_INUSE
            3 - AST_DEVICE_BUSY
            4 - AST_DEVICE_INVALID
            5 - AST_DEVICE_UNAVAILABLE
            6 - AST_DEVICE_RINGING
            7 - AST_DEVICE_RINGINUSE
            8 - AST_DEVICE_ONHOLD
        Paused: 0
        PausedReason: 
        Ringinuse: 0
        Wrapuptime: 0
        */
        if (Main.agenteChequeaExistenciaUsername(evento.getMemberName())) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Agente: "+evento.getMemberName()+" | Asterisk QueueMemberAddedEvent.");
            Main.accionesLogueaAgente(System.currentTimeMillis(), evento.getMemberName(), evento.getStatus());
        }
    }

    /**
     * Evento del manager para el deslogueo de Agentes.
     *
     * El chequeo que realiza (oara la existencia del agente) es debido a que
     * las PBXs tienen colas con miembros dinamicos y estaticos. El chequeo se
     * podria eliminar en caso de que la PBX se utilice SOLO para un CALL
     * CENTER.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(QueueMemberRemovedEvent evento) {
        /*
        Event: QueueMemberRemoved
        Privilege: agent,all
        Queue: 202
        MemberName: mgiannini
        Interface: Local/206040@from-internal
        StateInterface: SIP/206040
        Membership: dynamic
        Penalty: 0
        CallsTaken: 0
        LastCall: 0
        LastPause: 1593793211
        InCall: 0
        Status: 1
        Paused: 0
        PausedReason: 
        Ringinuse: 1
        Wrapuptime: 0
        */
        if (Main.agenteChequeaExistenciaUsername(evento.getMemberName())) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Agente: "+evento.getMemberName()+" | QueueMemberRemovedEvent.");
            Main.accionesDeslogueaAgente(System.currentTimeMillis(), evento.getMemberName(), eventoLogueo);
        }
    }
    
    /**
     * Manejo del Evento CDR
     * 
     * @param evento 
     */
    @Override
    public void handleEvent(CdrEvent evento) {
        /*
        Event: Cdr
        Privilege: cdr,all
        AccountCode: 
        Source: 206399
        Destination: 91569329545
        DestinationContext: from-operadores-atusu
        CallerID: "ATUSU 1783" <206399>
        Channel: SIP/206399-000000c4
        DestinationChannel: SIP/spbx0101lx-00000034
        LastApplication: Dial
        LastData: SIP/spbx0101lx/91569329545
        StartTime: 2020-08-07 08:47:56
        AnswerTime: 2020-08-07 08:48:10
        EndTime: 2020-08-07 08:48:12
        Duration: 16
        BillableSeconds: 2
        Disposition: ANSWERED
        AMAFlags: DOCUMENTATION
        UniqueID: 1594669308.400
        UserField: 
        */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getUniqueId() +" | CdrEvent | Finaliza una llamada.");
        if (grupoLlamadaSaliente.chequea_StrLinkedid_ObjLlamada(evento.getUniqueId())) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | CdrEvent | Consulto si la llamada estaba en Hold.");
            if (grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getLlamadaHold().isIsOpen()) {
                tratamientoUnHold(evento.getUniqueId(), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getIdAgente(), evento.getChannel(), "saliente", grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getAgenteExtension(), evento.getDestination(), System.currentTimeMillis());
            }
            libdb.IngresaDatosDB.agenteSalientes(grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getIdAgente(), evento.getStartTime(), evento.getEndTime(), evento.getDuration(), evento.getBillableSeconds(), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getLlamadaHold().getAcumuladoLlamada(), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getNumeroDestino(), evento.getDisposition(), evento.getUniqueId());
            // El ingreso del log de audios a la DB se realiza por afuera, recorriendo el listado completo.
            //IngresaDatosDB.metodosAudioLog(grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getIdAgente(), Main.agenteObtieneDeptoPorExtension(grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getAgenteExtension()), Main.agenteObtieneSectorPorExtension(grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getAgenteExtension()), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getUniqueId()).getFechaConexion(), evento.getDestination(), "sal", evento.getUniqueId());
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | CdrEvent | Elimino el objeto LlamadaSaliente.");
            grupoLlamadaSaliente.elimina_StrLinkedid_ObjLlamada(evento.getUniqueId());
        }
    }

    /**
     * Manejo de eventos de pausa de cola.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(QueueMemberPauseEvent evento) {
        /*
        Event: QueueMemberPause
        Privilege: agent,all
        Queue: 201
        MemberName: mgiannini
        Interface: Local/206040@from-internal
        StateInterface: SIP/206040
        Membership: dynamic
        Penalty: 0
        CallsTaken: 0
        LastCall: 0
        LastPause: 0
        InCall: 0
        Status: 5
        Paused: 0
        PausedReason: 
        Ringinuse: 0
        Wrapuptime: 0
        */
        if (evento.getPaused()) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Operador: "+evento.getMemberName()+" | QueueMemberPauseEvent.");
            Main.accionesPausaAgente(System.currentTimeMillis(), evento.getMemberName(), evento.getReason());
        } else {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Operador: "+evento.getMemberName()+" | QueueMemberUnPauseEvent.");
            Main.accionesDespausaAgente(System.currentTimeMillis(), evento.getMemberName(), eventoPausa);
        }
    }
    
    /**
     * Manejo del evento NewChannelEvent. Tomo en cuenta solo lo que interpreto
     * como llamadas salientes.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(NewChannelEvent evento) {
        /*
        Event: Newchannel
        Privilege: call,all
        Channel: SIP/206399-000000c4
        ChannelState: 0
        ChannelStateDesc: Down
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: <unknown>
        ConnectedLineName: <unknown>
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 114500
        Priority: 1
        Uniqueid: 1594669308.400
        Linkedid: 1594669308.400
         */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | NewChannelEvent | Consulto si el linkedid existe ya como objetoLlamadaCola.");
        if (!grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedid())) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | NewChannelEvent | Consulto si el linkedid existe ya como objetoLlamadaSaliente.");
            if (!grupoLlamadaSaliente.chequea_StrLinkedid_ObjLlamada(evento.getLinkedid())) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | NewChannelEvent | Consulto si el callerIdNum es diferente a null.");
                if (evento.getCallerIdNum() != null) {
                    Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | NewChannelEvent | Consulto si la extension pertenece a un operador.");
                    if (Main.agenteChequeaExistenciaExtension(evento.getCallerIdNum())) {
                        
                        // La llamada NO EXISTE y la hace una extension de OPERADOR
                        LlamadaSaliente nuevaLlamada = new LlamadaSaliente();
                        String tipo = "sal";
                        nuevaLlamada.setCanal(evento.getChannel());
                        nuevaLlamada.setFechaDiscado(System.currentTimeMillis());
                        nuevaLlamada.setAgenteExtension(evento.getCallerIdNum());
                        nuevaLlamada.setAgenteUsuario(Main.agenteObtieneUsernamePorExtension(evento.getCallerIdNum()));
                        nuevaLlamada.setNumeroDestino(evento.getExten());
                        nuevaLlamada.setIdAgente(Main.agenteObtieneIdAgente(nuevaLlamada.getAgenteUsuario()));
                        
                        if (evento.getExten().equals("s")) {
                            tipo = "ent";
                        }
                        
                        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | NewChannelEvent | Agrego un nuevo Objeto LlamadaSaliente.");
                        grupoLlamadaSaliente.agrega_StrLinkedid_ObjLlamada(evento.getLinkedid(), nuevaLlamada);
                        
                        if (Main.agenteObtieneGrabacionSaliente(evento.getCallerIdNum())) {
                            agenteGrabaAudio(tipo, nuevaLlamada.getIdAgente(), evento.getExten(), nuevaLlamada.getFechaDiscado(), evento.getUniqueId(), evento.getChannel(), Main.agenteObtieneSectorPorExtension(evento.getCallerIdNum()), Main.agenteObtieneDeptoPorExtension(evento.getCallerIdNum()));
                        }
                        
                        Main.agenteSeteaColaLlamada(nuevaLlamada.getAgenteUsuario(), "Saliente");
                        Main.agenteSeteaNumeroHablando(nuevaLlamada.getAgenteUsuario(), evento.getExten());
                        Main.agenteInfoAgentes(nuevaLlamada.getAgenteUsuario());
                    }
                }
            }   
        }
    }
    
    /**
     * Manejo del evento QueueSummary.
     * Muestra el estado de las colas.
     * 
     * @param evento 
     */
    @Override
    public void handleEvent(QueueSummaryEvent evento) {
        int agenteCarga = 0;
        if (evento.getLoggedIn() != 0) {
            agenteCarga = (evento.getLoggedIn() - evento.getAvailable()) * 100 / evento.getLoggedIn();
        }
        Main.colaAgentesIngresaCargaEspera(evento.getQueue(), evento.getCallers(), agenteCarga);
        Main.colaSupervisoresIngresaCargaEspera(evento.getQueue(), evento.getCallers(), agenteCarga);
        Main.colaSupervisorIngresaSLA(evento.getQueue());
    }
    
    /**
     * Manejo del evento BlindTransfer.
     * 
     * @param evento 
     */
    @Override
    public void handleEvent(BlindTransferEvent evento) {
        /*
        Event: BlindTransfer
        Privilege: call,all
        Result: Success
        TransfererChannel: SIP/206040-00000225
        TransfererChannelState: 6
        TransfererChannelStateDesc: Up
        TransfererCallerIDNum: 206040
        TransfererCallerIDName: 206040
        TransfererConnectedLineNum: 206399
        TransfererConnectedLineName: 206399
        TransfererLanguage: en
        TransfererAccountCode: 
        TransfererContext: macro-dial-one
        TransfererExten: s
        TransfererPriority: 1
        TransfererUniqueid: 1595334064.1147
        TransfererLinkedid: 1595334063.1144
        TransfereeChannel: SIP/206399-00000224
        TransfereeChannelState: 6
        TransfereeChannelStateDesc: Up
        TransfereeCallerIDNum: 206399
        TransfereeCallerIDName: 206399
        TransfereeConnectedLineNum: 206040
        TransfereeConnectedLineName: 206040
        TransfereeLanguage: en
        TransfereeAccountCode: 
        TransfereeContext: from-internal
        TransfereeExten: 201
        TransfereePriority: 51
        TransfereeUniqueid: 1595334063.1144
        TransfereeLinkedid: 1595334063.1144
        BridgeUniqueid: 1df17cba-b06f-4748-8a56-191190142517
        BridgeType: basic
        BridgeTechnology: simple_bridge
        BridgeCreator: <unknown>
        BridgeName: <unknown>
        BridgeNumChannels: 2
        BridgeVideoSourceMode: none
        IsExternal: Yes
        Context: check-xfer
        Extension: 206041   // Extension destino de la transferencia.
        */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getTransfererLinkedId() +" | BlindTransferEvent | Consulto si callerIdNum y Extension son diferentes a null.");
        if (evento.getTransfererCallerIdNum() != null && evento.getExtension() != null) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getTransfererLinkedId() +" | BlindTransferEvent | Consulto si existe el objeto LlamadaCola.");
            if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId())) {
                
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getTransfererLinkedId() +" | BlindTransferEvent | Consulto si la llamada esta en Hold.");
                if (grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).getLlamadaHold().isIsOpen()) {
                    tratamientoUnHold(evento.getTransfererLinkedId(), Main.agenteObtieneIdAgente(Main.agenteObtieneUsernamePorExtension(evento.getTransfererCallerIdNum())), evento.getTransfererChannel(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).getCola(), evento.getTransfererCallerIdNum(), evento.getExtension(), System.currentTimeMillis());
                }
                
                cargaTrazabilidad(evento.getTransfererLinkedId(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).getIdAgente(), evento.getTransfererChannel(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).getCola(), evento.getTransfererCallerIdNum(), evento.getExtension(), blindtransfer, System.currentTimeMillis(), "");
                
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getTransfererLinkedId() +" | BlindTransferEvent | Consulto si el destino es operador para ejecutar el popup.");
                if (Main.agenteChequeaExistenciaExtension(evento.getExtension())) {
                    Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getTransfererLinkedId() +" | BlindTransferEvent | Consulto si existe la URL para el popup.");
                    if (!grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).getCtiurl().isEmpty()) {
                        Main.agentePopUp(Main.agenteObtieneUsernamePorExtension(evento.getExtension()), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).getCtiurl());
                    }
                    
                    grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).setIdAgente(Main.agenteObtieneIdAgente(Main.agenteObtieneUsernamePorExtension(evento.getExtension())));
                    grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getTransfererLinkedId()).setAgenteUsuario(Main.agenteObtieneUsernamePorExtension(evento.getExtension()));
                }
            }
        }
    }
    
    /**
     * Manejo de las transferencias.
     * 
     * @param evento 
     */
    @Override
    public void handleEvent(AttendedTransferEvent evento) {
        /*
        Event: AttendedTransfer
        Privilege: call,all
        Result: Success
        OrigTransfererChannel: SIP/206040-00000034
        OrigTransfererChannelState: 6
        OrigTransfererChannelStateDesc: Up
        OrigTransfererCallerIDNum: 206040
        OrigTransfererCallerIDName: 206040
        OrigTransfererConnectedLineNum: 206041
        OrigTransfererConnectedLineName: 206041
        OrigTransfererLanguage: en
        OrigTransfererAccountCode: 
        OrigTransfererContext: macro-dial-one
        OrigTransfererExten: s
        OrigTransfererPriority: 1
        OrigTransfererUniqueid: 1594054062.126
        OrigTransfererLinkedid: 1594054062.123
        OrigBridgeUniqueid: 4d3897f6-34a9-458c-968d-1442b381c015
        OrigBridgeType: basic
        OrigBridgeTechnology: simple_bridge
        OrigBridgeCreator: <unknown>
        OrigBridgeName: <unknown>
        OrigBridgeNumChannels: 2
        OrigBridgeVideoSourceMode: none
        SecondTransfererChannel: SIP/206040-00000034
        SecondTransfererChannelState: 6
        SecondTransfererChannelStateDesc: Up
        SecondTransfererCallerIDNum: 206040
        SecondTransfererCallerIDName: 206040
        SecondTransfererConnectedLineNum: 206041
        SecondTransfererConnectedLineName: 206041
        SecondTransfererLanguage: en
        SecondTransfererAccountCode: 
        SecondTransfererContext: macro-dial-one
        SecondTransfererExten: s
        SecondTransfererPriority: 1
        SecondTransfererUniqueid: 1594054062.126
        SecondTransfererLinkedid: 1594054062.123
        SecondBridgeUniqueid: d45aeced-099b-49b7-b6a2-f7c17b26ab74
        SecondBridgeType: basic
        SecondBridgeTechnology: simple_bridge
        SecondBridgeCreator: <unknown>
        SecondBridgeName: <unknown>
        SecondBridgeNumChannels: 0
        SecondBridgeVideoSourceMode: none
        TransfereeChannel: SIP/206399-00000033
        TransfereeChannelState: 6
        TransfereeChannelStateDesc: Up
        TransfereeCallerIDNum: 206399
        TransfereeCallerIDName: 206399
        TransfereeConnectedLineNum: 206040
        TransfereeConnectedLineName: 206040
        TransfereeLanguage: en
        TransfereeAccountCode: 
        TransfereeContext: from-internal
        TransfereeExten: 201
        TransfereePriority: 51
        TransfereeUniqueid: 1594054062.123
        TransfereeLinkedid: 1594054062.123
        TransferTargetChannel: Local/206041@check-xfer-00000025;1
        TransferTargetChannelState: 6
        TransferTargetChannelStateDesc: Up
        TransferTargetCallerIDNum: 206041
        TransferTargetCallerIDName: 206041
        TransferTargetConnectedLineNum: 206040
        TransferTargetConnectedLineName: 206040
        TransferTargetLanguage: en
        TransferTargetAccountCode: 
        TransferTargetContext: check-xfer
        TransferTargetExten: 206041
        TransferTargetPriority: 1
        TransferTargetUniqueid: 1594054076.127
        TransferTargetLinkedid: 1594054062.123
        IsExternal: No
        DestType: Bridge
        DestBridgeUniqueid: 4d3897f6-34a9-458c-968d-1442b381c015
        */
        if (evento.getOrigTransfererCallerIDNum() != null && evento.getOrigTransfererConnectedLineNum() != null) {
            if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId())) {
                
                /* Al realizar la traza de la llamada, tengo que chequear si el HOLD se cerrò o cortaron.
                Esto se puede hacer consultando si el evento siguiente al Hold es Unhold.
                */
                if (Main.agenteChequeaExistenciaExtension(evento.getOrigTransfererCallerIDNum()) && grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).getLlamadaHold().isIsOpen()) {
                    tratamientoUnHold(evento.getOrigTransfererLinkedId(), Main.agenteObtieneIdAgente(Main.agenteObtieneUsernamePorExtension(evento.getOrigTransfererCallerIDNum())), evento.getOrigTransfererChannel(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).getCola(), evento.getOrigTransfererCallerIDNum(), evento.getOrigTransfererConnectedLineNum(), System.currentTimeMillis());
                    cargaTrazabilidad(evento.getOrigTransfererLinkedId(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).getIdAgente(), evento.getOrigTransfererChannel(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).getCola(), evento.getOrigTransfererCallerIDNum(), evento.getOrigTransfererConnectedLineNum(), attendedtransfer, System.currentTimeMillis(), "");
                }
                
                // Consulto si la extension destino pertenece a un agente.
                if (Main.agenteChequeaExistenciaExtension(evento.getOrigTransfererConnectedLineNum())) {
                    
                    if (!grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).getCtiurl().isEmpty()) {
                        Main.agentePopUp(Main.agenteObtieneUsernamePorExtension(evento.getOrigTransfererConnectedLineNum()), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).getCtiurl());
                    }
                    
                    grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).setIdAgente(Main.agenteObtieneIdAgente(Main.agenteObtieneUsernamePorExtension(evento.getOrigTransfererConnectedLineNum())));
                    grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getOrigTransfererLinkedId()).setAgenteUsuario(Main.agenteObtieneUsernamePorExtension(evento.getOrigTransfererConnectedLineNum()));
                }
            }
        }
    }

    /**
     * Evento para establecer el estado de las extensiones.
     * 
     * @param evento 
     */
    @Override
    public void handleEvent(ExtensionStatusEvent evento) {
        /*
        Exten - Name of the extension.
        Context - Context that owns the extension.
        Hint - Hint set for the extension
        Status - Numerical value of the extension status. Extension status is 
        determined by the combined device state of all items contained in the 
        hint.
            -2 - The extension was removed from the dialplan.
            -1 - The extension's hint was removed from the dialplan.
            0 - Idle - Related device(s) are in an idle state.
            1 - InUse - Related device(s) are in active calls but may take more calls.
            2 - Busy - Related device(s) are in active calls and may not take any more calls.
            4 - Unavailable - Related device(s) are not reachable.
            8 - Ringing - Related device(s) are currently ringing.
            9 - InUse&Ringing - Related device(s) are currently ringing and in active calls.
            16 - Hold - Related device(s) are currently on hold.
            17 - InUse&Hold - Related device(s) are currently on hold and in active calls.
        StatusText - Text representation of Status.
            Idle
            InUse
            Busy
            Unavailable
            Ringing
            InUse&Ringing
            Hold
            InUse&Hold
            Unknown - Status does not match any of the above values.
        */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "ExtensionStatusEvent.");
        if (Main.agenteChequeaExistenciaExtension(evento.getExten())) {
            Main.accionesActividadAgente(Main.agenteObtieneUsernamePorExtension(evento.getExten()), evento.getStatus(), evento.getExten());
        }
    }
    
    /**
     * Procesos a ejecutar cuando una llamada ingresa en una cola de atencion.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(QueueCallerJoinEvent evento) {
        /*
        Event: QueueCallerJoin
        Privilege: agent,all
        Channel: SIP/206399-00000033
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: <unknown>
        ConnectedLineName: <unknown>
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 201
        Priority: 51
        Uniqueid: 1594054062.123
        Linkedid: 1594054062.123
        Queue: 201
        Position: 1
        Count: 1
        */
        if (Main.colaChequeaExistencia(evento.getQueue())) {
            if (!grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
                LlamadaCola nuevaLlamada = new LlamadaCola();
                nuevaLlamada.setCanal(evento.getChannel());
                nuevaLlamada.setCola(evento.getQueue());
                nuevaLlamada.setOrigen(evento.getCallerIdNum());
                nuevaLlamada.setFechaIngreso(System.currentTimeMillis());
                nuevaLlamada.setColaPausa(Main.colaObtieneObjetoCola(evento.getQueue()).getColaPausa()); // WrapUp Time
                nuevaLlamada.setColaSector(Main.colaObtieneColaSector(evento.getQueue()));
                nuevaLlamada.setColaDepto(Main.colaObtieneColaDepto(evento.getQueue()));
                
                // Consulto si la llamada posee popup
                if (Main.colaObtieneObjetoCola(evento.getQueue()).isColaSeteoPOPUP()) {
                    try {
                        nuevaLlamada.setCtiurl(libdb.IntegracionSugar.obtengoUrlSugar(evento.getLinkedId()));
                    } catch (IOException | SQLException | ClassNotFoundException e) {
                        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.ERROR, "Metodo: colaObtieneObjetoCola | ", e);
                    }
                }
                
                grupoLlamadaCola.agrega_StrLinkedid_ObjLlamada(evento.getLinkedId(), nuevaLlamada);
                
                // Encolo el evento para la trazabilidad de la llamada.
                cargaTrazabilidad(evento.getLinkedId(), "0", evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), evento.getQueue(), join, System.currentTimeMillis(), "");
            }
        }
    }

    /**
     * Procesos a ejecutar cuando un agente recibe una llamada.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(AgentCalledEvent evento) {
        /*
        Event: AgentCalled
        Privilege: agent,all
        Channel: SIP/206399-00000033
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: <unknown>
        ConnectedLineName: <unknown>
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 201
        Priority: 51
        Uniqueid: 1594054062.123
        Linkedid: 1594054062.123
        DestChannel: Local/206040@from-internal-00000024;1
        DestChannelState: 0
        DestChannelStateDesc: Down
        DestCallerIDNum: 201
        DestCallerIDName: <unknown>
        DestConnectedLineNum: 206399
        DestConnectedLineName: 206399
        DestLanguage: en
        DestAccountCode: 
        DestContext: from-internal
        DestExten: 201
        DestPriority: 1
        DestUniqueid: 1594054062.124
        DestLinkedid: 1594054062.123
        Queue: 201
        Interface: Local/206040@from-internal
        MemberName: mgiannini
        */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | AgentCalledEvent | Consulto el objeto LlamadaCola existe.");
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).setIdAgente(Main.agenteObtieneIdAgente(evento.getMemberName()));
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).setAgenteUsuario(evento.getMemberName());
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).setFechaRingueo(System.currentTimeMillis());
            Main.agenteSeteaColaLlamada(evento.getMemberName(), evento.getQueue());
            Main.agenteSeteaNumeroHablando(evento.getMemberName(), evento.getCallerIdNum());
            
            // Encolo el evento para la trazabilidad de la llamada.
            cargaTrazabilidad(evento.getLinkedId(), Main.agenteObtieneIdAgente(evento.getMemberName()), evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), evento.getDestExten(), called, System.currentTimeMillis(), "");
        }
    }

    /**
     * Procesos a ejecutar cuando un agente NO atiende una llamada.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(AgentRingNoAnswerEvent evento) {
        /*
        Event: AgentRingNoAnswer
        Privilege: agent,all
        Channel: SIP/206399-0000003f
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: <unknown>
        ConnectedLineName: <unknown>
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 201
        Priority: 51
        Uniqueid: 1594068281.147
        Linkedid: 1594068281.147
        DestChannel: Local/206040@from-internal-0000002a;1
        DestChannelState: 5
        DestChannelStateDesc: Ringing
        DestCallerIDNum: 206040
        DestCallerIDName: 206040
        DestConnectedLineNum: 206399
        DestConnectedLineName: 206399
        DestLanguage: en
        DestAccountCode: 
        DestContext: from-internal
        DestExten: 201
        DestPriority: 1
        DestUniqueid: 1594068281.148
        DestLinkedid: 1594068281.147
        Queue: 201
        Interface: Local/206040@from-internal
        MemberName: mgiannini
        RingTime: 15000
         */
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
            // Ingreso datos en las tablas agenteslog y reporteagente.
            libdb.IngresaDatosDB.agenteNoAtiende(grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getIdAgente(), eventoNoAtiende, grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getFechaRingueo(), System.currentTimeMillis(), evento.getRingtime() / 1000, evento.getQueue(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getOrigen(), evento.getLinkedId());
            // Encolo el evento para la trazabilidad de la llamada.
            cargaTrazabilidad(evento.getLinkedId(), Main.agenteObtieneIdAgente(evento.getMemberName()), evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), evento.getDestCallerIdNum(), ringnoanswer, System.currentTimeMillis(), "");
        }
    }
    
    /**
     * Procesos al recibir un evento AgentConnectEvent. El evento dispara cuando
     * un agente conecta con una llamada.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(AgentConnectEvent evento) {
        /*
        Event: AgentConnect
        Privilege: agent,all
        Channel: SIP/206399-00000033
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: 206040
        ConnectedLineName: 206040
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 201
        Priority: 51
        Uniqueid: 1594054062.123
        Linkedid: 1594054062.123
        DestChannel: Local/206040@from-internal-00000024;1
        DestChannelState: 6
        DestChannelStateDesc: Up
        DestCallerIDNum: 206040
        DestCallerIDName: 206040
        DestConnectedLineNum: 206399
        DestConnectedLineName: 206399
        DestLanguage: en
        DestAccountCode: 
        DestContext: from-internal
        DestExten: 201
        DestPriority: 1
        DestUniqueid: 1594054062.124
        DestLinkedid: 1594054062.123
        Queue: 201
        Interface: Local/206040@from-internal
        MemberName: mgiannini
        HoldTime: 6 // Tiempo de Ringueo Total en la cola de atencion.
        RingTime: 5 // Tiempo de ringueo en el agente.
        */
        
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).setTiempoRingCola(evento.getHoldTime());
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).setTiempoRingAgente(evento.getRingtime());
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).setFechaAtencion(System.currentTimeMillis());

            if (Main.colaObtieneObjetoCola(evento.getQueue()).isColaSeteoGRB()) {
                agenteGrabaAudio(evento.getQueue(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getIdAgente(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getOrigen(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getDestLinkedId()).getFechaAtencion(), evento.getLinkedId(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCanal(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getColaSector(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getColaDepto());
            }

            if (!grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCtiurl().isEmpty()) {
                Main.agentePopUp(evento.getMemberName(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCtiurl());
            }

            libdb.IngresaDatosDB.agenteConnect(evento.getQueue(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getFechaAtencion(), Funciones.obtieneCampoImpacto(evento.getHoldTime()));
            
            // Encolo el evento para la trazabilidad de la llamada.
            cargaTrazabilidad(evento.getLinkedId(), Main.agenteObtieneIdAgente(evento.getMemberName()), evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), evento.getDestCallerIdNum(), connected, System.currentTimeMillis(), "");
        }
    }
    
    /**
     * Manejo del Hold de los agentes.
     * 1) Chequeo si la llamada pertenece a un agente.
     * 2) Acciones del Hold.
     * 3) Setea el inicio del Hold. (En la instancia COLA)
     * 4) Setea el innicio del Hold. (En la instancia COLA pero dedicado al Agente).
     * 
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(HoldEvent evento) {
        /*
        Event: Hold
        Privilege: call,all
        Channel: SIP/206399-00000033
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206040
        CallerIDName: 206040
        ConnectedLineNum: 206399
        ConnectedLineName: 206399
        Language: en
        AccountCode: 
        Context: macro-dial-one
        Exten: s
        Priority: 1
        Uniqueid: 1594054062.129
        Linkedid: 1594054062.123
        */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | HoldEvent | Consulto si existe el objeto LlamadaCola y si la extension existe como operador.");
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId()) && Main.agenteChequeaExistenciaExtension(evento.getCallerIdNum())) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | HoldEvent | Seteo Fecha de inicio del Hold en LlamadaCola.");
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaHold().setFechaInicio(System.currentTimeMillis());
            
            // Encolo el evento para la trazabilidad de la llamada.
            cargaTrazabilidad(evento.getLinkedId(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getIdAgente(), evento.getChannel(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCola(), evento.getCallerIdNum(), evento.getConnectedLineNum(), hold, System.currentTimeMillis(), "");
        }
        
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | HoldEvent | Consulto si existe el objeto LlamadaSaliente.");
        if (grupoLlamadaSaliente.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | HoldEvent | Seteo Fecha de inicio del Hold en LlamadaSaliente.");
            grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaHold().setFechaInicio(System.currentTimeMillis());
        }
    }
    
    /**
     * Procesos a ejecutar cuando un operador quita el Hold en una conversacion.
     * 1) Chequeo si la llamada pertenece a un agente.
     * 2) Acciones deel UnHold.
     * 3) Setea el tiempo del Hold restando el horario actual al iniciado por
     * el de inicio.
     * 
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(UnholdEvent evento) {
        /*
        Event: Unhold
        Privilege: call,all
        Channel: SIP/206399-00000033
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206040
        CallerIDName: 206040
        ConnectedLineNum: 206399
        ConnectedLineName: 206399
        Language: en
        AccountCode: 
        Context: macro-dial-one
        Exten: s
        Priority: 1
        Uniqueid: 1594054062.129
        Linkedid: 1594054062.123
        */
        // Consulto si la llamada existe y si hay un hold abierto.
        // La consulta del hold es debido a que en las transferencias ciegas se ejecuta un unhold.
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | UnHoldEvent | Consulto si existe el objeto LlamadaCola y si esta abierto el Hold.");
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId()) && grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaHold().isIsOpen()) {
            tratamientoUnHold(evento.getLinkedId(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getIdAgente(), evento.getChannel(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCola(), evento.getCallerIdNum(), evento.getConnectedLineNum(), System.currentTimeMillis());
        }
        
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ evento.getLinkedId() +" | UnHoldEvent | Consulto si existe el objeto LlamadaSaliente y si esta abierto el Hold.");
        if (grupoLlamadaSaliente.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId()) && grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaHold().isIsOpen()) {
            tratamientoUnHold(evento.getLinkedId(), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getIdAgente(), evento.getChannel(), "", evento.getCallerIdNum(), evento.getConnectedLineNum(), System.currentTimeMillis());
        }
    }
    
    /**
     * Utilizo un metodo para el UnHold ya que puedo llamarlo desde el HangUp.
     * Si lo dejara desde el Override no podría llamarlo.
     * 
     * @param linkedid ID unico de llamada.
     * @param idagente ID unico de agente.
     * @param canal Canal de la llamada.
     * @param cola Cola de la llamada.
     * @param origen Numero de origen de la llamada.
     * @param destino Numero de destino de la llamada.
     * @param fecha Fecha del evento.
     */
    synchronized public void tratamientoUnHold(String linkedid, String idagente, String canal, String cola, String origen, String destino, long fecha) {
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ linkedid +" | tratamientoUnHold | Consulto si existe el objeto LlamadaCola.");
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(linkedid)) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ linkedid +" | tratamientoUnHold | Seteo el acumulado de Hold actual de la llamada por Cola.");
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(linkedid).getLlamadaHold().setAcumuloHold(fecha);
            Main.accionesUnHoldAgente(grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(linkedid).getIdAgente(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(linkedid).getAgenteUsuario(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(linkedid).getLlamadaHold().getFechaInicio(), fecha, eventoHold, linkedid);
                
            // Encolo el evento para la trazabilidad de la llamada.
            cargaTrazabilidad(linkedid, idagente, canal, cola, origen, destino, unhold, fecha, "");
        }   
        
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ linkedid +" | tratamientoUnHold | Consulto si existe el objeto LlamadaSaliente.");
        if (grupoLlamadaSaliente.chequea_StrLinkedid_ObjLlamada(linkedid)) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: "+ linkedid +" | tratamientoUnHold | Seteo el acumulado de Hold actual de la llamada saliente.");
            grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(linkedid).getLlamadaHold().setAcumuloHold(fecha);
            Main.accionesUnHoldAgente(grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(linkedid).getIdAgente(), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(linkedid).getAgenteUsuario(), grupoLlamadaSaliente.obtiene_StrLinkedid_ObjLlamada(linkedid).getLlamadaHold().getFechaInicio(), fecha, eventoHold, linkedid);
        }
    }
    
    /**
     * Manejo de finalizacion de llamada de agente.
     *
     * @param evento Evento del Manager.
     */
    @Override
    public void handleEvent(AgentCompleteEvent evento) {
        /*
        Event: AgentComplete
        Privilege: agent,all
        Channel: SIP/206399-00000033
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: 206040
        ConnectedLineName: 206040
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 201
        Priority: 51
        Uniqueid: 1594054062.123
        Linkedid: 1594054062.123
        DestChannel: SIP/206040-00000034
        DestChannelState: 6
        DestChannelStateDesc: Up
        DestCallerIDNum: 206040
        DestCallerIDName: 206040
        DestConnectedLineNum: 206041
        DestConnectedLineName: 206041
        DestLanguage: en
        DestAccountCode: 
        DestContext: macro-dial-one
        DestExten: s
        DestPriority: 1
        DestUniqueid: 1594054062.126
        DestLinkedid: 1594054062.123
        Queue: 201
        Interface: Local/206040@from-internal
        MemberName: mgiannini
        HoldTime: 6 // Es el tiempo total que el caller espero hasta ser atendido.
        TalkTime: 15
        Reason: transfer / agent / caller
        */
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
            
            /* Al realizar la traza de la llamada, tengo que chequear si el HOLD se cerrò o cortaron.
            Esto se puede hacer consultando si el evento siguiente al Hold es Unhold.
            */       
            if (grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaHold().isIsOpen()) {
                tratamientoUnHold(evento.getLinkedId(), Main.agenteObtieneIdAgente(evento.getMemberName()), evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), evento.getConnectedLineNum(), System.currentTimeMillis());
            }
            
            libdb.IngresaDatosDB.agentCompleteEvent(Main.agenteObtieneIdAgente(evento.getMemberName()), eventoHabla, grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getFechaIngreso(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getTiempoRingCola(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getFechaAtencion(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getTiempoRingAgente(), System.currentTimeMillis(), evento.getTalkTime(), evento.getQueue(), evento.getCallerIdNum(), evento.getReason(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaHold().getAcumuladoAgente(), evento.getLinkedId());
            
            cargaTrazabilidad(evento.getLinkedId(), Main.agenteObtieneIdAgente(evento.getMemberName()), evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), evento.getDestConnectedLineNum(), completed, System.currentTimeMillis(), evento.getReason());
            
            if (grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getColaPausa() != 0) {
                Main.colaPausaFinLlamado(evento.getMemberName(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getColaPausa(), "99");
            }
        }
    }

    /**
     * Manejo del evento al abandonar la llamada. Local SQLite.
     *
     * @param evento Evento QueueCallerAbandonEvent.
     */
    @Override
    public void handleEvent(QueueCallerAbandonEvent evento) {
        /*
        Event: QueueCallerAbandon
        Privilege: agent,all
        Channel: SIP/206399-00000036
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 206399
        CallerIDName: 206399
        ConnectedLineNum: <unknown>
        ConnectedLineName: <unknown>
        Language: en
        AccountCode: 
        Context: from-internal
        Exten: 201
        Priority: 51
        Uniqueid: 1594058334.130
        Linkedid: 1594058334.130
        Queue: 201
        Position: 1                                             // Posicion cuando abandono la cola de atencion.
        OriginalPosition: 1                                     // Posicion cuando ingreso a la cola de atencion.
        HoldTime: 6                                             // Tiempo de espera en Cola.
        */
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId())) {
            libdb.IngresaDatosDB.QueueCallerAbandonEvent(evento.getQueue(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getFechaIngreso(), evento.getHoldTime(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getOrigen(), Funciones.obtieneCampoImpacto(evento.getHoldTime()), evento.getOriginalPosition(), evento.getPosition(), evento.getLinkedId());
            
            cargaTrazabilidad(evento.getLinkedId(), "0", evento.getChannel(), evento.getQueue(), evento.getCallerIdNum(), "", abandon, System.currentTimeMillis(), "");
        }
    }

    /**
     * Manejo del Evento HangUp. Trata solo las llamadas salientes invalidas.
     *
     * @param evento Evento HangupEvent.
     */
    @Override
    synchronized public void handleEvent(HangupEvent evento) {
        /*
        Event: Hangup
        Privilege: call,all
        Channel: SIP/1784-000002cc
        ChannelState: 6
        ChannelStateDesc: Up
                Down
                Rsrvd
                OffHook
                Dialing
                Ring
                Ringing
                Up
                Busy
                Dialing Offhook
                Pre-ring
                Unknown
        CallerIDNum: 1784
        CallerIDName: 1784
        ConnectedLineNum: <unknown>
        ConnectedLineName: <unknown>
        Language: en
        AccountCode: 
        Context: from-operadores-atusu
        Exten: 1569329545
        Priority: 1
        Uniqueid: 1606739153.1610
        Linkedid: 1606739153.1610
        Cause: 16
        Cause-txt: Normal Clearing
        */
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " +evento.getLinkedId()+ " | HangupEvent | Consulto si el linkedid existe como objeto LlamadaCola -- Y -- si el channel pertenece al objeto LlamadaCola.");
        if (grupoLlamadaCola.chequea_StrLinkedid_ObjLlamada(evento.getLinkedId()) && grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCanal().equals(evento.getChannel())) {
            try {
                Funciones.procesoTrazabilidad(grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getLlamadaQueue(), evento.getLinkedId());
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + evento.getUniqueId() + " | HangupEvent | Elimino el objeto LlamadaCola.");
                // El ingreso del log de audios a la DB se realiza por afuera, recorriendo el listado completo.
                //IngresaDatosDB.metodosAudioLog(grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getIdAgente(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getColaDepto(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getColaSector(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getFechaAtencion(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getOrigen(), grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(evento.getLinkedId()).getCola(), evento.getLinkedId());
                grupoLlamadaCola.elimina_StrLinkedid_ObjLlamada(evento.getLinkedId());
            } catch (IOException e) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "HangupEvent", e);
            }
        }
    }
    
    /**
     * Metodo para almacenar los eventos que transcurren bajo un mismo ID de llamada.
     * De esta forma podemos obtener la trazabilidad completa.
     * 
     * @param linkedid ID unico de la llamada.
     * @param idAgente ID de agente que interactua con el evento.
     * @param canal Canal de la llamada.
     * @param cola ID unico de cola de la llamada.
     * @param origen Numero del evento.
     * @param destino Numero destino del evento.
     * @param evento ID unico que identifica el evento.
     * @param fechaEvento Fecha de inicio de evento.
     * @param detalle Cualquier dato adicional del evento.
     */
    public synchronized void cargaTrazabilidad(String linkedid, String idAgente, String canal, String cola, String origen, String destino, int evento, long fechaEvento, String detalle) {
        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + linkedid + " | cargaTrazabilidad | Se encola un nuevo evento en la llamada.");
        try {
            LlamadaColaQueue nuevoIngreso = new LlamadaColaQueue();
            nuevoIngreso.setIdAgente(idAgente);
            nuevoIngreso.setCanal(canal);
            nuevoIngreso.setCola(cola);
            nuevoIngreso.setOrigen(origen);
            nuevoIngreso.setDestino(destino);
            nuevoIngreso.setEvento(evento);
            nuevoIngreso.setFechaInicio(fechaEvento);
            
            grupoLlamadaCola.obtiene_StrLinkedid_ObjLlamada(linkedid).getLlamadaQueue().add(nuevoIngreso);
        } catch (Exception e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "cargaTrazabilidad", e);
        }
    }
    
    /**
     * Carga de forma inicial los datos de las colas. colaCreaNuevaCola: Genera
     * un nuevo Objeto Cola en grupoColasAgentes y grupoColasSupervisores.
     * colaCargaDatosColaAgentes: Introduce los valores de popup y grabacion en
     * el objeto cola del mapeo Agentes. colaCargaDatosColaSupervisores:
     * Introduce los valores SLA, SCO y RangoSLA en el objeto cola del mapeo
     * Supervisores.
     *
     * @param asteriskServer Objeto de la clase Asteriskserver
     */
    public static void colasCargaDesdeDBInicio(AsteriskServer asteriskServer) {
        try {
            asteriskServer.getQueues().forEach((asteriskQueue) -> {
                try {
                    String[] colaDatos = libdb.Colas.colaObtieneDatos(asteriskQueue.getName());
                    if (colaDatos.length > 2) {
                        Main.colaCreaNuevaCola(asteriskQueue.getName(), colaDatos[2], colaDatos[7], colaDatos[8]);
                        Main.colaCargaDatosColaAgentes(asteriskQueue.getName(), colaDatos[0], colaDatos[1], colaDatos[6]);
                        Main.colaCargaDatosColasSupervisores(asteriskQueue.getName(), colaDatos[3], colaDatos[4], colaDatos[5]);
                    }
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "asteriskServer.getQueues. Error: ", e);
                }
            });
        } catch (ManagerCommunicationException e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "cargaQueue | Probable error en consulta al manager.", e);
        }
    }

    /**
     * Ejecucion ciclica para la obtencion de los datos de las Colas.
     *
     * @param asteriskServer Objeto de la clase Asteriskserver
     */
    public void colasCargaDesdeDB(AsteriskServer asteriskServer) {
        try {
            asteriskServer.getQueues().forEach((asteriskQueue) -> {
                
                if (Main.colaChequeaExistencia(asteriskQueue.getName())) {
                    colasPedidoInformacion(asteriskQueue.getName());
                    // Si la cola ya existe, se piden todo los datos
                    try {
                        String[] colaDatos = libdb.Colas.colaObtieneDatos(asteriskQueue.getName());
                    
                        Main.colaCargaDatosColaAgentes(asteriskQueue.getName(), colaDatos[0], colaDatos[1], colaDatos[6]);
                        Main.colaCargaDatosColasSupervisores(asteriskQueue.getName(), colaDatos[3], colaDatos[4], colaDatos[5]);
                        
                    } catch (IOException | ClassNotFoundException | SQLException e) {
                        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "colasCargaDatosDB | Catch para consulta de DB y asignacion. ", e);
                    }
                } else {
                    try {
                        String[] colaDatos = libdb.Colas.colaObtieneDatos(asteriskQueue.getName());
                        if (colaDatos.length > 2) {
                            Main.colaChequeaCreaNuevaCola(asteriskQueue.getName(), colaDatos[2], colaDatos[7], colaDatos[8]);
                        }
                    } catch (SQLException | IOException | ClassNotFoundException e) {
                        Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "colasCargaDatosDB | Catch para consulta de DB y asignacion. ", e);
                    }
                }
            });
        } catch (ManagerCommunicationException e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "colasCargaDatosDB | Probable error en consulta al Manager.", e);
        }
    }
    
    /**
     * Conecta al agente mediante el Manager.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param agenteExtension Numero de extension del Agente.
     * @param agenteColas Conjunto de las Colas del Agente.
     * @param pausado True o False de acuerdo a si el agente debe loguearse
     * pausado o no.
     */
    public void agenteLoguea(String agenteUsuario, String agenteExtension, ConcurrentHashMap<String, List<String>> agenteColas, Boolean pausado) {
        agenteColas.forEach((k, v) -> {
            QueueAddAction agenteNuevo = new QueueAddAction();
            agenteNuevo.setQueue(k);
            agenteNuevo.setInterface("Local/" + agenteExtension + "@" + v.get(0));
            agenteNuevo.setPenalty(Integer.parseInt(v.get(1)));
            agenteNuevo.setMemberName(agenteUsuario);
            agenteNuevo.setStateInterface("SIP/" + agenteExtension);
            agenteNuevo.setPaused(pausado);
            try {
                this.managerConnection.sendAction(agenteNuevo, 30000);
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Operador: "+agenteUsuario+" | Envio de logueo a Asterisk.");
            } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException e) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "agenteLoguea", e);
            }
        });
    }
        
    /**
     * Se pide un resumen del estado de una cola de atencion.
     * 
     * @param idCola ID unico de la cola de atencion.
     */
    public void colasPedidoInformacion(String idCola) {
        QueueSummaryAction infoColas = new QueueSummaryAction();
        infoColas.setQueue(idCola);
        try {
            this.managerConnection.sendAction(infoColas);
        } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "colasPedidoInformacion", e);
        }
        
    }

    /**
     * Desonecta al agente mediante el Manager.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param agenteExtension Numero de extension del Agente.
     * @param agenteColas Conjunto de las Colas del Agente.
     */
    public void agenteDesloguea(String agenteUsuario, String agenteExtension, ConcurrentHashMap<String, List<String>> agenteColas) {
        agenteColas.forEach((k, v) -> {
            QueueRemoveAction agente = new QueueRemoveAction();
            agente.setQueue(k);
            agente.setInterface("Local/" + agenteExtension + "@" + v.get(0));
            try {
                this.managerConnection.sendAction(agente, 30000);
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Operador: "+ agenteUsuario +" | Envio de deslogueo a Asterisk.");
            } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException e) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "agenteDesloguea", e);
            }
        });
    }

    /**
     * Pausa al agente mediante el Manager.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param agenteExtension Numero de extension del Agente.
     * @param agenteColas Conjunto de las Colas del Agente.
     * @param tipoPausa Tipo de pausa del Agente.
     */
    public void agentePausa(String agenteUsuario, String agenteExtension, ConcurrentHashMap<String, List<String>> agenteColas, String tipoPausa) {
        agenteColas.forEach((k, v) -> {
            QueuePauseAction agente = new QueuePauseAction();
            agente.setQueue(k);
            agente.setInterface("Local/" + agenteExtension + "@" + v.get(0));
            agente.setPaused(Boolean.TRUE);
            agente.setReason(tipoPausa);
            try {
                this.managerConnection.sendAction(agente, 30000);
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Operador: "+ agenteUsuario +" | Envio de pausa a Asterisk.");
            } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException e) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "agentePausa", e);
            }
        });
    }

    /**
     * Despausa al agente mediante el Manager.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param agenteExtension Numero de extension del Agente.
     * @param agenteColas Conjunto de las Colas del Agente.
     */
    public void agenteDespausa(String agenteUsuario, String agenteExtension, ConcurrentHashMap<String, List<String>> agenteColas) {
        agenteColas.forEach((k, v) -> {
            QueuePauseAction agente = new QueuePauseAction();
            agente.setQueue(k);
            agente.setInterface("Local/" + agenteExtension + "@" + v.get(0));
            agente.setPaused(Boolean.FALSE);
            try {
                this.managerConnection.sendAction(agente, 30000);
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Operador: "+ agenteUsuario +" | Envio de despausa a Asterisk.");
            } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException e) {
                Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "agenteDespausa", e);
            }
        });
    }

    /**
     * Graba la llamada mediante el Manager.
     *
     * @param idCola Numero de Cola.
     * @param idAgente Numero de ID del Agente.
     * @param numero Origen / Destino de la llamada.
     * @param fecha Fecha de la llamada.
     * @param linkedid ID unico de la llamada.
     * @param canal Canal de la llamada.
     * @param idSector ID del sector asociado a la cola.
     * @param idDepto ID del depto asociado a la cola.
     */
    public void agenteGrabaAudio(String idCola, String idAgente, String numero, long fecha, String linkedid, String canal, String idSector, String idDepto) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        SimpleDateFormat path = new SimpleDateFormat("yyyy/MM/dd/");
        Date date = new Date();
        MixMonitorAction agente = new MixMonitorAction();
        agente.setChannel(canal);
        String nombreAudio = "mate-" + idDepto + "-" + idSector + "-" + idAgente + "-" + idCola + "-" + numero + "-" + sdf.format(fecha) + "-" + linkedid;
        agente.setFile(path.format(date) + nombreAudio + ".wav");
        try {
            this.managerConnection.sendAction(agente, 30000);
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Linkedid: " + linkedid + " | Operador: "+ idAgente +" | agenteGrabaAudio | Envio de grabacion del audio.");
            
            libdb.IngresaDatosDB.metodosAudioLog(idAgente, idDepto, idSector, nombreAudio, fecha, numero, idCola, linkedid);
        } catch (IOException | IllegalArgumentException | IllegalStateException | TimeoutException e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "agenteGrabaAudio", e);
        }
    }

    /**
     * Modifica la penalidad de los agentes en la cola seleccionada.
     * 
     * @param agenteExtension Numero de extension del Agente.
     * @param idCola Numero de Cola.
     * @param contexto Contexto de la extension del Agente.
     * @param penalty Numero que indica la penalidad del agente.
     */
    public void agenteCambiaPenalty(String agenteExtension, String idCola, String contexto, String penalty) {
        QueuePenaltyAction agente = new QueuePenaltyAction();
        agente.setPenalty(Integer.parseInt(penalty));
        agente.setQueue(idCola);
        agente.setInterface("Local/" + agenteExtension + "@" + contexto);
        try {
            this.managerConnection.sendAction(agente, 30000);
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.DEBUG, "Extension: "+ agenteExtension +" | agenteambiaPenalty | Envio de cambio de penalty.");
        } catch (IOException | IllegalArgumentException | IllegalStateException | TimeoutException e) {
            Logger.getLogger(LibManagerEvents.class.getName()).log(Level.FATAL, "agenteCambiaPenalty", e);
        }
    }
}
