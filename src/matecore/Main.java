/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matecore;

/**
 *
 * @author mgiannini
 */
import java.io.File;
import libdb.LibSQLite;
import java.io.FileInputStream;
import manager.LibManagerEvents;

import jakarta.xml.bind.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import libgeneral.AgenteMapeo;
import libgeneral.Cola;
import libgeneral.ColaMapeo;
import libgeneral.Funciones;
import libgeneral.SupervisorMapeo;
import manager.AccionesMG;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import libdb.IngresaDatosDB;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

/**
 * Clase principal.
 *
 * @author mgiannini
 */
public class Main extends WebSocketServer {

    private static AgenteMapeo grupoAgentes = new AgenteMapeo();
    private static ColaMapeo grupoColasAgentes = new ColaMapeo();
    private static ColaMapeo grupoColasSupervisores = new ColaMapeo();
    private static SupervisorMapeo grupoSupervisores = new SupervisorMapeo();
    private static LibManagerEvents eventos;
    private static Main socket;
    private static ControlTemporalPeriodico controlPeriodico;
    private static ControlTemporalUnico controlUnico;

    private static String mngServidor;
    private static String mngUsuario;
    private static String mngPassword;
    private static String mngPuerto;
    private static String dbConexionCentral;
    private static String dbUsuarioCentral;
    private static String dbPasswordCentral;
    private static String dbConexionLocal;
    private static String dbUsuarioLocal;
    private static String dbPasswordLocal;
    private static String dbSQLiteName;
    private static String portWebSocket;

    private static Properties prop = new Properties();
    private static InputStream input = null;

    /**
     * @param portToolBar Puerto para la escucha del WebSocket.
     * @throws java.net.UnknownHostException Tipo de Exception.
     */
    public Main(int portToolBar) throws UnknownHostException {
        super(new InetSocketAddress(portToolBar));
    }
    
    /**
     * @param address SocketAddress
     */
    public Main(InetSocketAddress address) {
        super(address);
    }
    
    /**
     * Funcion principal.
     *
     * @param args Array de argumentos.
     * @throws IOException Tipo de Exception -
     * @throws AuthenticationFailedException Tipo de Exception -
     * @throws TimeoutException Tipo de Exception -
     * @throws InterruptedException Tipo de Exception -
     * @throws SQLException Tipo de Exception -
     * @throws java.lang.ClassNotFoundException -
     */
    public static void main(String[] args) throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException, SQLException, ClassNotFoundException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, ".:MATECORE:. .:Inicia Servicio:.");
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Log de Logger Log4J");
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Formato de Logueo: | TIPO LOG | Nombre del Metodo | Detalle Interno | Detalle Exception");
                        
        try {
            input = new FileInputStream("/opt/mate/mate.config.properties");
            prop.load(input);

            mngServidor = prop.getProperty("mngServidor");
            mngUsuario = prop.getProperty("mngUsuario");
            mngPassword = prop.getProperty("mngPassword");
            mngPuerto = prop.getProperty("mngPuerto");
            dbConexionCentral = prop.getProperty("dbConexionCentral");
            dbUsuarioCentral = prop.getProperty("dbUsuarioCentral");
            dbPasswordCentral = prop.getProperty("dbPasswordCentral");
            dbConexionLocal = prop.getProperty("dbConexionLocal");
            dbUsuarioLocal = prop.getProperty("dbUsuarioLocal");
            dbPasswordLocal = prop.getProperty("dbPasswordLocal");
            dbSQLiteName = prop.getProperty("dbSQLiteName");
            portWebSocket = prop.getProperty("portWebSocket");

        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, "Main | No se puede iniciar la aplicacion. No se encuentra o no se puede leer correctamente el archivo de configuracion. Recuerde que el archivo debe ser: /opt/mate/mate.config.properties. |" + e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Logger.getLogger(Main.class.getName()).log(Level.FATAL, "Main | No se puede cerrar FileInputStream. |" + e);
                }
            }
        }
        
        LibSQLite conexion = new LibSQLite();
        conexion.crearDB();
        
        controlPeriodico = new ControlTemporalPeriodico();
        controlUnico = new ControlTemporalUnico();
        controlPeriodico.colaMonitoreo();
        controlPeriodico.agenteChequeaActividad();
        
        int port = Integer.parseInt(portWebSocket);
        socket = new Main(port);
        
        // Las lineas siguiente son para testeo de Let's Encrypt
        try {
            SSLContext sslContext = null;
            String password = "Cr3d1c00p";
            String pathname = "/opt/mate/";

            // Para la conversion de los certificados de Sectigo:
            // Uno el cert con el bundle: #cat 434184146.crt 434184146.ca-bundle > cert.crt
            // Convierto a PEM el cert: #openssl x509 -outform PEM -in cert.crt -out cert.pem
            
            // Convierto la key a PEM: #openssl rsa -outform PEM -in myprivatekey.key  -out privkey_tmp.pem
            // Convierto el formato a PKCS8: #openssl pkcs8 -topk8 -nocrypt -in privkey_tmp.pem -out privkey.pem
            
            sslContext = SSLContext.getInstance("TLS");

            byte[] certBytes = parseDERFromPEM(getBytes(new File(pathname + File.separator + "cert.pem")),
                    "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
            byte[] keyBytes = parseDERFromPEM(getBytes(new File(pathname + File.separator + "privkey.pem")),
                    "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

            X509Certificate cert = generateCertificateFromDER(certBytes);
            RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, password.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();

            sslContext.init(km, null, null);
            socket.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));

            socket.setConnectionLostTimeout(30);
            socket.setReuseAddr(true);
            socket.start();

        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, "Certificados | Sucedio algun problema con los certificados. Chequear si se vencieron.", e);
        }
                
        eventos = new LibManagerEvents();
        eventos.run();

        // Limpio la tabla Agentes de la DB antes de iniciar Mate
        libdb.Agentes.agenteLimpiaDB();
    }
    
    /**
     * Metodo para el parseo del certificado.
     * 
     * @param pem Certificado.
     * @param beginDelimiter Delimitador en TEXTO
     * @param endDelimiter Delimitador en TEXTO
     * @return Retorna el certificado parseado.
     */
    private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);
        String[] tokens = data.split(beginDelimiter);
        tokens = tokens[1].split(endDelimiter);
        return DatatypeConverter.parseBase64Binary(tokens[0]);
    }

    /**
     * Metodo para generar la Key privada de los certificados.
     * 
     * @param keyBytes
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException 
     */
    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    /**
     * Metodo para obtener el certificado.
     * 
     * @param certBytes
     * @return
     * @throws CertificateException 
     */
    private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    /**
     * Metodo para obtener el archivo de certificado.
     * 
     * @param file
     * @return 
     */
    private static byte[] getBytes(File file) {
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray); //read file into bytes[]
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesArray;
    }

    /**
     * Se ejecuta al iniciar la aplicacion. Si el LOG muestra la linea de INFO,
     * significa que la aplicacion inicio correctamente la libreria de
     * WebSocket.
     */
    @Override
    public void onStart() {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, ".:WEBSOCKET:. .:Inicia Servicio:.");
    }

    /**
     * Se ejecuta cuando un cliente abre un WebSocket. Funcion vacia, no se
     * utiliza por el momento.
     *
     * @param conn Objeto WebSocket que identifica la conexion que se esta
     * @param handshake Objeto ClientHandshake.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
    }

    /**
     * Se ejecuta al cerrarse un WebSocket. Al manejar los cierres de WebSocket
     * mediante mensajes, el evento onClose se utiliza para cierre por razones
     * inesperadas.
     *
     * @param conn Objeto WebSocket
     * @param i Codigo de cierre de la conexion.
     * @param mensaje No se y el RFC no ayuda.
     * @param bln No se y el RFC no ayuda.
     */
    @Override
    public void onClose(WebSocket conn, int i, String mensaje, boolean bln) {
        try {
            // Consulto si es Agente o Supervisor
            if (grupoAgentes.obtiene_ListWebSockets().contains(conn)) {
                // Es agente
                String username = grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(conn.toString())).getAgenteUsuario();
                if (!grupoAgentes.obtiene_StrUsername_ObjAgente(username).getAgenteEstado().equals("Deslogueado")) {
                    AccionesMG.cierreInesperadoAgente(username, grupoAgentes, eventos);
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: " + username + " | Cierre de sesion inesperado. Codigo: " + i);
                }
            } else if (grupoSupervisores.obtiene_ListWebSockets().contains(conn)) {
                // Es Supervisor
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Supervisor: " + grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).getSupervisorUsuario() + " | Cierre de sesion.");
                Funciones.supervisorDesloguea(conn, grupoSupervisores, grupoColasSupervisores);
            }
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "onClose. Error: ", e);
        }
    }

    /**
     * Se ejecuta al recibir mensajes desde los clientes de WebSockets. Se
     * utiliza tanto para agentes como supervisores. Maneja los eventos de
     * ambos, actuando en consecuencia de los datos recibidos.
     *
     * @param conn Objeto WebSocket
     * @param mensaje String que contiene el mensaje cifrado de los agentes /
     * supervisores.
     */
    @Override
    public void onMessage(WebSocket conn, String mensaje) {
        try {
            // Divido el mensaje para obtener el codigo
            String datos[] = mensaje.split(";");
            // => Agentes 1XX
            // datos[0] => codigo;      datos[1] => extension;
            // datos[2] => username     datos[3] => aleatorio (de acuerdo al codigo);
            // => Supervisores 2XX
            // datos[0] => codigo;      datos[1] => username;
            // datos[2] => aleatorio    
            switch (datos[0]) {
                case "101":
                    // Retorno de PING del Cliente
                    // Nothing TODO
                    break;
                case "201":
                    Funciones.supervisorConsultaOperacion(datos[1], datos[2], datos[3], datos[4], grupoAgentes, grupoColasAgentes);
                    break;
                case "111":
                    // Nuevo logueo de Agente
                    Funciones.agenteLoguea(conn, datos[1], datos[2], datos[3], grupoAgentes, grupoColasAgentes); // datos[3] es el idagente
                    AccionesMG.conectaAgente(datos[1], datos[2], grupoAgentes, eventos);
                    //agenteInfoNuevoAgente();
                    break;
                case "211":
                    // Nuevo logueo de Supervisor
                    Funciones.supervisorLoguea(conn, datos[1], datos[2], grupoSupervisores, grupoColasSupervisores);
                    supervisorInfoSupervisor(conn);
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Supervisor: " + datos[1] + " | Inicio de sesion.");
                    break;
                case "221":
                    AccionesMG.supervisorLogueaAgente(datos[1], datos[2], datos[3], datos[4], grupoAgentes, eventos);
                    break;
                case "113":
                    // Deslogueo de Agente
                    AccionesMG.desconectaAgente(datos[1], datos[2], grupoAgentes, eventos);
                    break;
                case "213":
                    AccionesMG.supervisorDeslogueaAgente(datos[1], datos[2], grupoAgentes, eventos);
                    break;
                case "115":
                    // Activa Agente
                    AccionesMG.despausaAgente(datos[1], datos[2], grupoAgentes, eventos);
                    break;
                case "215":
                    AccionesMG.supervisorPenaltyAgente(datos[1], datos[2], datos[3], datos[4], grupoAgentes, eventos);
                    break;
                case "117":
                    // Pauso Agentes
                    AccionesMG.pausaAgente(datos[3], datos[1], datos[2], grupoAgentes, eventos); // datos[3] es el tipo de pausa
                    break;
                case "119":
                    // Seteo alarma de agente.
                    agenteSeteaAlarma(datos[2], datos[3]);
                    break;
                case "217":
                    AccionesMG.supervisorPausaAgente(datos[1], datos[2], datos[3], grupoAgentes, eventos);
                    break;
                case "131":
                    // Deslogueo del Agente por TimeOut
                    AccionesMG.cierreInesperadoAgente(datos[2], grupoAgentes, eventos);
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: " + datos[2] + " | Cierre de sesion por TimeOut.");
                    break;
                case "219":
                    // Nueva Seleccion de Colas de Supervisor
                    Funciones.supervisorModifica(conn, datos[1], datos[2], grupoSupervisores, grupoColasSupervisores);
                    supervisorInfoSupervisor(conn);
                    break;
                default:
                    Logger.getLogger(Main.class.getName()).log(Level.WARN, "Llegó un valor no esperado. Valor: " + datos[0] + " ; " + datos[1] + " ; " + datos[2]);
                    break;
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, "onMessage | Metodo de la libreria WebSocket que analiza los mensajes recibidos.", e);
        }
    }

    /**
     * Maneja los errores de la conexion WebSocket. Se ejecuta cuando el
     * WebSocket se cierra por algun error.
     *
     * @param conn Objeto WebSocket
     * @param mensaje Mensaje del error.
     */
    @Override
    public void onError(WebSocket conn, Exception mensaje) {
        Logger.getLogger(Main.class.getName()).log(Level.FATAL, "onError | Metodo de la libreria WebSocket que maneja el error en la conexión.", mensaje);
        // Do Nothing
        // Por ahora no tiene sentido catchear on Error. Tal vez se pueda utilizar para la reconexion.
    }
    
    /**
     * Envia una URL al agente para la ejecucion del PopUp. Se ejecuta siempre y
     * cuando la cola por la que ingreso la llamada posea integracion.
     *
     * @param agenteUsuario Nombre de usuario del Agente
     * @param ctiUrl URL que se enviara al agente.
     */
    public static void agentePopUp(String agenteUsuario, String ctiUrl) {
        try {
            if (agenteChequeaExistenciaUsername(agenteUsuario)) {
                WebSocket conn = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjWS();
                if (conn.isOpen()) {
                    conn.send("140;" + ctiUrl);
                    Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "Operador: "+ agenteUsuario +" | agentePopUp | Envio de la URL al operador.");
                }
            }
        } catch (NumberFormatException e) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, "Operador: "+ agenteUsuario +" | agentePopUp", e);
        }
    }

    /**
     * Envia informacion a los agentes de las colas a las cuales pertenece. Se
     * ejecuta cada 15 segundos (tiempo determinado en la clase ControlTemporal
     * del paquete matecore).
     */
    public static void agenteInfoColas() {
        try {
            grupoColasAgentes.obtiene_NumerosCola().forEach((colaNumero) -> {
                grupoColasAgentes.obtiene_StrColaNum_ArrayWebSocket(colaNumero).forEach((conn) -> {
                    if (conn.isOpen()) {
                        conn.send("170;" + colaNumero + ";" + grupoColasAgentes.obtiene_StrColaNum_StrColaNom(colaNumero) + ";" + grupoColasAgentes.obtiene_ColaNum_ObjCola(colaNumero).getColaValorCA() + ";" + grupoColasAgentes.obtiene_ColaNum_ObjCola(colaNumero).getColaValorLLE());
                    }
                });
            });
        } catch (NotYetConnectedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteInfoColas. Error: ", ex);
        }
    }

    /**
     * Envia informacion a los agentes de los otros agentes que pertenecen a las
     * mismas colas.. Por cada cola, consulto los WebSockets de agentes
     * asociados a la misma. Envio a cada uno de los WebSockets la actualizacion
     * de estado/actividad del agente que genero un cambio.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     */
    public static void agenteInfoAgentes(String agenteUsuario) {
        // Para enviar el nuevo estado a todos los agentes que comparten la cola del username
        // Tengo que saber a que colas esta asociado este agente
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteInfoAgentes | Agente: "+agenteUsuario+", Se envia la info del nuevo agente a todos los demas agentes que comparten cola de atencion.");
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasNumero().forEach((cola) -> {
                grupoColasAgentes.obtiene_StrColaNum_ArrayWebSocket(cola).forEach((conn) -> {
                    if (conn.isOpen()) {
                        conn.send("160;" + agenteUsuario + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteActividad() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColaLlamada() + " : " + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteNumeroHablando());
                    }
                });
            });
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteInfoAgentes | Agente: "+agenteUsuario+", Consulta las colas del agente. Por cada cola se consulta el WS de cada agente asociado y se envia la infomacion a cada uno.", e);
        }
    }

    /**
     * Envia los supervisores la informacion del agente. La funcion se trae
     * todas las colas del agente y envia la invormacion del agente a todos los
     * supervisores que monitorean esas colas.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     */
    public static void supervisorInfoAgente(String agenteUsuario) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasNumero().forEach((agenteCola) -> {
                grupoColasSupervisores.obtiene_StrColaNum_ArrayWebSocket(agenteCola).forEach((conn) -> {
                    if (conn.isOpen()) {
                        conn.send("230;" + agenteUsuario + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasNumero() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteFechaUltEst() / 1000 + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteActividad() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteFechaUltAct() / 1000 + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColaLlamada() + " : " + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteNumeroHablando());
                    }
                });
            });
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorInfoAgentes. Error: ", e);
        }
    }

    /**
     * Envia a los supervisores la informacion de los agentes. Se obtiene la
     * totalidad de los usuarios de agentes. Se obtienen las colas de cada
     * agente y se le envia la informacion a los supervisores que comparten las
     * colas de atencion.
     *
     */
    public static void supervisorInfoAgentes() {
        try {
            grupoAgentes.obtiene_ListUsernames().forEach((agenteUsuario) -> {
                grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasNumero().forEach((agenteCola) -> {
                    grupoColasSupervisores.obtiene_StrColaNum_ArrayWebSocket(agenteCola).forEach((conn) -> {
                        if (conn.isOpen()) {
                            conn.send("230;" + agenteUsuario + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasNumero() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteFechaUltEst() / 1000 + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteActividad() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteFechaUltAct() / 1000 + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColaLlamada() + " : " + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteNumeroHablando());
                        }
                    });
                });
            });
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorInfoAgentes. Error: ", e);
        }
    }

    /**
     * Envia la informacion de las nuevas colas seleccionadas por el supervisor.
     * Se obtienen las colas del supervisor (Colas enviadas a traves de la
     * monitoria). Por cada cola obtengo los WebSockets de los agentes asociados
     * a las mismas. Le envio al Supervisor los datos de cada agente.
     *
     * @param conn Objeto WebSocket
     */
    public static void supervisorInfoSupervisor(WebSocket conn) {
        try {
            grupoSupervisores.obtiene_StrWebSocket_ObjSupervisor(conn.toString()).getSupervisorColas().forEach((idCola) -> {
                grupoColasAgentes.obtiene_StrColaNum_ArrayWebSocket(idCola).forEach((agenteConn) -> {
                    if (conn.isOpen()) {
                        conn.send("240;" + grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString()) + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteExtension() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteColasNumero() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteEstado() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteFechaUltEst() / 1000 + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteActividad() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteFechaUltAct() / 1000 + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(agenteConn.toString())).getAgenteColaLlamada());
                    }
                });
            });
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorInfoSupervisor. Error: ", e);
        }
    }

    /**
     * Envio de mensaje simple al supervisor. Se puede utilizar para multiples
     * usos. El codigo 290 le indica al portal del supervisor que debe mostrar
     * un alerta.
     *
     * @param supervisor Nombre de usuario del Supervisor.
     * @param mensaje Mensaje de texto.
     */
    public static void supervisorEnviaMensajeSimple(String supervisor, String mensaje) {
        try {
            WebSocket conn = grupoSupervisores.obtiene_StrWebSocket_ObjWebSocket(grupoSupervisores.obtiene_StrUsername_StrWebSocket(supervisor));
            if (conn.isOpen()) {
                conn.send("290;" + mensaje);
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorEnviaMensajeSimple. Error: ", e);
        }
    }

    /**
     * Envio de mensaje complejo al supervisor. Se puede utilizar para multiples
     * usos. El codigo 290 le indica al portal del supervisor que debe mostrar
     * un alerta.
     *
     * @param supervisor Nombre de usuario del Supervisor.
     * @param colaNum Numero de la cola.
     * @param colaNom Nombre de la cola.
     * @param agente Nombre del agente.
     */
    public static void supervisorEnviaMensajeComplejo(String supervisor, String colaNum, String colaNom, String agente) {
        try {
            WebSocket conn = grupoSupervisores.obtiene_StrWebSocket_ObjWebSocket(grupoSupervisores.obtiene_StrUsername_StrWebSocket(supervisor));
            if (conn.isOpen()) {
                conn.send("292;" + colaNum + ";" + colaNom + ";" + agente);
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorEnviaMensajeComplejo. Error: ", e);
        }
    }

    /**
     * Envia los datos para los graficos del agente.
     *
     * @param supervisor Nombre de usuario del Supervisor.
     * @param extension Numero de extension del agente.
     * @param datosAgente datosAgente[0] es HABLA, datosAgente[1] es PAUSA,
     * datos[2] es HOLD
     * @param llamadasAtendidas Cantidad de llamadas atendidas del agente.
     */
    public static void supervisorEnviaStatsAgente(String supervisor, String extension, List<String> datosAgente, String llamadasAtendidas) {
        try {
            WebSocket conn = grupoSupervisores.obtiene_StrWebSocket_ObjWebSocket(grupoSupervisores.obtiene_StrUsername_StrWebSocket(supervisor));
            if (conn.isOpen()) {
                conn.send("294;" + extension + ";" + datosAgente.get(0) + ";" + datosAgente.get(1) + ";" + datosAgente.get(2) + ";" + llamadasAtendidas);
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorEnviaStatsAgente. Error: ", e);
        }
    }

    /**
     * Envia a los supervisores los penalties de los agentes. Esta funcion solo
     * se ejecuta en caso de que haya habiado algun cambio en los penalties.
     *
     * @param supervisorUsuario Nombre de Usuario del Supervisor.
     * @param agenteUsuario Nombre de Usuario del Agente.
     */
    public static void supervisorEnviaPenalty(String supervisorUsuario, String agenteUsuario) {
        try {
            grupoColasSupervisores.obtiene_NumerosCola().forEach((idCola) -> {
                grupoColasSupervisores.obtiene_StrColaNum_ArrayWebSocket(idCola).forEach((conn) -> {
                    if (conn.isOpen()) {
                        StringBuilder colaPenalty = new StringBuilder();
                        grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa().forEach((k, v) -> {
                            colaPenalty.append(k).append(",").append(v.get(1)).append(",");

                        });
                        conn.send("298;" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension() + ";" + colaPenalty);
                    }
                });
            });
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorEnviaPenalty. Error: ", e);
        }

    }

    /**
     * Envia los datos para la operacion del Supervisor sobre un agente.
     *
     * @param supervisorUsuario Nombre de Usuario del Supervisor.
     * @param agenteUsuario Nombre de Usuario del Agente.
     */
    public static void supervisorEnviaInfoAgente(String supervisorUsuario, String agenteUsuario) {
        try {
            WebSocket conn = grupoSupervisores.obtiene_StrWebSocket_ObjWebSocket(grupoSupervisores.obtiene_StrUsername_StrWebSocket(supervisorUsuario));
            if (conn.isOpen()) {
                StringBuilder colaPenalty = new StringBuilder();
                grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColasMapa().forEach((k, v) -> {
                    colaPenalty.append(k).append(",").append(v.get(1)).append(",");

                });
                conn.send("296;" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension() + ";" + agenteUsuario + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado() + ";" + colaPenalty);
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorEnviaStatsAgente. Error: ", e);
        }
    }

    /**
     * Envia informacion a los agentes del nuevo agente logueado. Se ejecuta al
     * loguearse un nuevo agente. Se le envia toda la informacion de los agentes
     * a TODOS los agentes.
     */
    public static void agenteInfoNuevoAgente() {
        Collection<String> usernames = grupoAgentes.obtiene_ListUsernames();
        usernames.forEach((username) -> {
            agenteInfoAgentes(username);
        });
    }

    /**
     * Envia informacion a los supervisores de las colas a las cuales pertenece.
     * Se ejecuta cada 15 segundos (tiempo determinado en la clase
     * ControlTemporal del paquete matecore).
     */
    public static void supervisorInfoColas() {
        try {
            grupoColasSupervisores.obtiene_NumerosCola().forEach((idCola) -> {
                grupoColasSupervisores.obtiene_StrColaNum_ArrayWebSocket(idCola).forEach((conn) -> {
                    if (conn.isOpen()) {
                        conn.send("270;" + idCola + ";" + grupoColasSupervisores.obtiene_StrColaNum_StrColaNom(idCola) + ";" + grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).getColaValorCA() + ";" + grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).getColaValorLLE() + ";" + grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).getColaValorSLA());
                    }
                });
            });
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorInfoColas. Error: ", e);
        }
    }

    /**
     * Contiene todas las acciones que se deben ejecutar al loguear un agente.
     * Funcion que viene de QueueMemberAddedEvent de la clase LibManagerEvents.
     *
     * @param fechaInicio Fecha del logueo del Agente.
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param extensionEstado Estado de la extension.
     */
    public static void accionesLogueaAgente(long fechaInicio, String agenteUsuario, int extensionEstado) {
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstadoFlag() != 1 && !grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).isAgenteAccionSupervisor()) {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteEstadoFlag(1);
            /*
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
            */
            // El Estado debe ser 1,2 o 3.
            // La actividad depende directo del estado.
            // Si el estado es: 0, 4 o 5 se debe indicar ERROR.
            
            Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "Agente: "+agenteUsuario+" | Seteo estado: "+Funciones.obtieneDescripcionEstado(extensionEstado));
            agenteSeteoFechaUltimoEstado(agenteUsuario, fechaInicio);
            agenteSeteaEstado(agenteUsuario, Funciones.obtieneDescripcionEstado(extensionEstado));
            
            agenteSeteoFechaUltimaActividad(agenteUsuario, fechaInicio);
            agenteEnviaEstadoActividad(agenteUsuario);
            
            libdb.IngresaDatosDB.agenteLogueo(agenteObtieneIdAgente(agenteUsuario), grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension());
            
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: " + agenteUsuario + " | Accion: LOGUEA");
            Main.agenteInfoNuevoAgente();
        } else if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).isAgenteAccionSupervisor()) {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteAccionSupervisor(false);
            supervisorInfoAgente(agenteUsuario);
        }
    }

    /**
     * Contiene todas las acciones que se deben ejecutar al desloguear un
     * agente. Funcion que viene de QueueMemberRemovedEvent de la clase
     * LibManagerEvents.
     *
     * @param fechaDeslogueo Fecha del deslogueo del agente.
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param eventoLogueo ID unico del evento de logueo.
     */
    public static synchronized void accionesDeslogueaAgente(long fechaDeslogueo, String agenteUsuario, int eventoLogueo) {
        if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).isAgenteAccionSupervisor()) {
            agenteSeteaEstado(agenteUsuario, "Deslogueado");
            agenteEnviaEstadoActividad(agenteUsuario);
            
            libdb.IngresaDatosDB.agenteDeslogueo(agenteObtieneIdAgente(agenteUsuario), eventoLogueo, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteFechaLogueo(), fechaDeslogueo, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension());
            Funciones.agenteDesloguea(agenteUsuario, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension(), grupoAgentes, grupoColasAgentes);
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: " + agenteUsuario + " | Accion: DESLOGUEA");
        }
    }

    /**
     * Contiene todas las acciones que se deben ejecutar al pausar un agente.
     * Funcion que viene de QueueMemberPausedEvent de la clase LibManagerEvents.
     *
     * @param fechaInicio Fecha del inicio de la pausa.
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param pausaCodigo Tipo de pausa seleccionada.
     */
    public static void accionesPausaAgente(long fechaInicio, String agenteUsuario, String pausaCodigo) {
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstadoFlag() != 2) {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteEstadoFlag(2);
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().setFechaInicio(fechaInicio);
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().setPausaNombre(grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getListadoPausas(pausaCodigo));
            agenteSeteoFechaUltimoEstado(agenteUsuario, fechaInicio);
            agenteSeteaEstado(agenteUsuario, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getListadoPausas(pausaCodigo));
            agenteEnviaEstadoActividad(agenteUsuario);
        }
    }

    /**
     * Contiene todas las acciones que se deben ejecutar al despausar un agente.
     * Funcion que viene de QueueMemberPausedEvent de la clase LibManagerEvents.
     *
     * @param fechaFin Fecha del fin de la pausa.
     * @param agenteUsuario Nombre de usuario del Agente.
     */
    public static void accionesDespausaAgente(long fechaFin, String agenteUsuario, int eventoPausa) {
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstadoFlag() != 1) {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteEstadoFlag(1);
            
            agenteSeteaEstado(agenteUsuario, "Activo");
            agenteSeteoFechaUltimoEstado(agenteUsuario, fechaFin);
            agenteEnviaEstadoActividad(agenteUsuario);
            
            libdb.IngresaDatosDB.agentePausa(agenteObtieneIdAgente(agenteUsuario), eventoPausa, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().getFechaInicio(), fechaFin, (fechaFin - grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().getFechaInicio()) / 1000, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().getPausaNombre());
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().setFechaInicio(0);
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().setPausaNombre("");
        }
    }

    /**
     * Chequea si la extension acaba de salir del estado HOLD. En caso de salir
     * de ese estado, debo setear 2 valor: 1) UnHold agente. 2) El vaor de
     * fechaUltimoEstado debe retornar al valor temporal. Puesto en el Hold.
     *
     * @param idAgente ID unico del agente.
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param fechaInicio Fecha del inicio del hold.
     * @param fechaFinaliza Fecha de finalizacion del hold.
     * @param eventoHold ID unico del evento Hold.
     * @param linkedid Identificacion unica de la llamada.
     */
    public static void accionesUnHoldAgente(String idAgente, String agenteUsuario, long fechaInicio, long fechaFinaliza, int eventoHold, String linkedid) {
        agenteSeteoFechaUltimaActividad(agenteUsuario, fechaFinaliza);
        libdb.IngresaDatosDB.agenteHold(idAgente, fechaInicio, fechaFinaliza, eventoHold, linkedid);
    }

    /**
     * Funcion que setea el Estado de las Extensiones, es decir, LA ACTIVIDAD.
     * ExtensionStatusEvent. Este evento se ejecuta en cada cambio de estado
     * de las extensiones.
     * 
     * @param agenteUsuario Usuario del Agente.
     * @param estado Valor numerico del Estado del Agente.
     */
    public static void accionesActividadAgente(String agenteUsuario, int estado, String extension) {
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
        switch (estado) {
            case 0:
                // Consulto si esta volviendo de una desconexion. Si es asi, debo impactar en el lgo que se volviò a conectar.
                extensionChequeoDesconexion(agenteUsuario, extension);
                agenteSeteaActividad(agenteUsuario, "Libre");
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                agenteSeteaColaLlamada(agenteUsuario, "-");
                agenteSeteaNumeroHablando(agenteUsuario, "-");
                break;
            case 1:
                agenteSeteaActividad(agenteUsuario, "Hablando");
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                break;
            case 2:
                agenteSeteaActividad(agenteUsuario, "Hablando");
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                break;
            case 4:
                agenteSeteaActividad(agenteUsuario, "Reconectando...");
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                break;
            case 8:
                agenteSeteaActividad(agenteUsuario, "Ringueando");
                break;
            case 9:
                agenteSeteaActividad(agenteUsuario, "Ringueando");
                break;
            case 16:
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                agenteSeteaActividad(agenteUsuario, "Espera");
                break;
            case 17:
                agenteSeteaActividad(agenteUsuario, "Espera");
                break;
            default:
                agenteSeteaActividad(agenteUsuario, "Reconectando...");
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                break;
        }
        agenteEnviaEstadoActividad(agenteUsuario);
    }
    
    /**
     * Metodo que permite cerrar el evento de desconexion de la extension en caso de que asi fuere.
     * 
     * @param operadorUsuario Nombre de usuario del Operador.
     * @param extension Extension del operador.
     */
    public static void extensionChequeoDesconexion(String operadorUsuario, String extension) {
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(operadorUsuario).getAgenteActividad().equals("Reconectando...")) {
            IngresaDatosDB.agenteMarcaExtensionDesconectada(agenteObtieneIdAgente(operadorUsuario), grupoAgentes.obtiene_StrUsername_ObjAgente(operadorUsuario).getAgenteFechaUltAct(), System.currentTimeMillis(), (System.currentTimeMillis() - grupoAgentes.obtiene_StrUsername_ObjAgente(operadorUsuario).getAgenteFechaUltAct()) / 1000, extension);
        }
    }

    /**
     * Funcion que se ejecuta cuando un agente se encuentra libre. El mensaje
     * es: 1 - AST_DEVICE_NOT_INUSE. Este mensaje indica que la extension NO SE
     * ENCUENTRA EN USO. Hace comprobaciones para el cambio de ESTADO y
     * ACTIVIDAD.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param agentePausa Indica si el agente se encuentra pausado o no.
     */
    public static void accionesActivaAgente(String agenteUsuario, boolean agentePausa) {
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstadoFlag() != 7) {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteEstadoFlag(7);
            if (!agentePausa) {
                agenteSeteaEstado(agenteUsuario, "Activo");
                agenteSeteoFechaUltimoEstado(agenteUsuario, System.currentTimeMillis());
                agenteSeteaColaLlamada(agenteUsuario, "-");
            }
            if (!grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteActividad().equals("Libre")) {
                agenteSeteaActividad(agenteUsuario, "Libre");
                agenteSeteoFechaUltimaActividad(agenteUsuario, System.currentTimeMillis());
                agenteSeteaColaLlamada(agenteUsuario, "-");
                agenteSeteaNumeroHablando(agenteUsuario, "-");
            }
            agenteEnviaEstadoActividad(agenteUsuario);
        }
    }

    /**
     * Contiene todas las acciones que se deben ejecutar al detectar un error en
     * la extension. Funcion que viene de QueueMemberEvent de la clase
     * LibManagerEvents, luego de llamadar a accionesAnalizaEvento de la clase
     * Main.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param extensionError Tipo de Error de la extension.
     */
    public static void accionesErrorAgente(String agenteUsuario, String extensionError) {
        agenteSeteaEstado(agenteUsuario, "Error");
        agenteSeteaActividad(agenteUsuario, extensionError);
        agenteEnviaEstadoActividad(agenteUsuario);
    }

    /**
     * Funcion simple para el chequeo de agente. Devuelve un boolean por TRUE o
     * FALSE en caso de que exista o no el agente.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @return Retorna TRUE en caso de que el Agente exista.
     */
    public static boolean agenteChequeaExistenciaUsername(String agenteUsuario) {
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteChequeaExistenciaUsername | Operador: "+agenteUsuario+" | Chequea que el agente exista (Usuario => ObjetoAgente) en la cache.");
        try {
            return grupoAgentes.chequea_StrUsername_ObjAgente(agenteUsuario);
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteChequeaExistenciaUsername | Operador: "+agenteUsuario+" | No pudo acceder al objeto Agente en la cache.", e);
            /**
             * TODO
             * ¿Se debe desloguear el agente para evitar errores posteriores?
             */
        }
        return false;
    }

    /**
     * Funcion simple para el chequeo de agente. Devuelve un boolean por TRUE o
     * FALSE en caso de que exista o no el agente.
     *
     * @param agenteExtension Numero de extension del agente
     * @return Retorna TRUE en caso de que el Agente exista.
     */
    public static boolean agenteChequeaExistenciaExtension(String agenteExtension) {
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteChequeaExistenciaExtension | Extension: "+agenteExtension+", Chequea que el agente exista (Extension => ObjetoAgente) en la cache.");
        try {
            return grupoAgentes.chequea_StrExtension_StrWebSocket(agenteExtension);
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteChequeaExistenciaExtension. | Agente: "+agenteExtension+", No pudo acceder al objeto Agente en la cache.", e);
            /**
             * TODO
             * ¿Se debe desloguear el agente para evitar errores posteriores?
             */
        }
        return false;
    }

    /**
     * Funcion simple para la obtencion del username del agente. La obtencion se
     * realiza mediante el mapeo de Username - WebSocket.
     *
     * @param agenteExtension Extension del Agente.
     * @return Retorna el nombre de usuario del Agente.
     */
    public static String agenteObtieneUsernamePorExtension(String agenteExtension) {
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteObtieneUsernamePorExtension | Extension: "+agenteExtension+", Obtiene el nombre de usuario (WebSocket => Username) del agente en la cache.");
        try {
            return grupoAgentes.obtiene_StrWebSocket_StrUsername(grupoAgentes.obtiene_StrExtension_StrWebSocket(agenteExtension));
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteObtieneUsernamePorExtension | Extension: "+agenteExtension+", No pudo acceder al Username en la cache.", e);
        }
        return null;
    }
    
    /**
     * Metodo simple para la obtencion username mediante el ID.
     * 
     * @param idagente ID unico del agente.
     * @return 
     */
    public static String agenteObtieneUsernamePorId(String idagente) {
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteObtieneUsernamePorId | idAgente: "+idagente+", Obtiene el nombre de usuario (idAgente => Username) del agente en la cache.");
        try {
            return grupoAgentes.getMapa_StrIdAgente_ObjAgente(idagente).getAgenteUsuario();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteObtieneUsernamePorId. | idAgente: "+idagente+", No pudo acceder al Objeto agente en la cache.", e);
        }
        return null;
    }

    /**
     * Funcion que retorna TRUE si el agente graba llamadas salientes.
     *
     * @param agenteExtension Extension del Agente.
     * @return Retorna TRUE en caso de que el agente grabe llamadas salientes.
     */
    public static boolean agenteObtieneGrabacionSaliente(String agenteExtension) {
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteObtieneGrabacionSaliente | Extension: "+agenteExtension+", Obtiene el flag grabacion (Username => ObjetoAgente) del agente en la cache.");
        try {
            return grupoAgentes.obtiene_StrUsername_ObjAgente(Main.agenteObtieneUsernamePorExtension(agenteExtension)).isAgenteGrabaSaliente();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteObtieneGrabacionSaliente | Extension: "+agenteExtension+", No pudo acceder al Objeto agente en la cache.", e);
        }
        return false;
    }
    
    /**
     * Metodo para obtener el sector asociado a las llamadas salientes del oeprador.
     * 
     * @param agenteExtension Extension del operador.
     * @return Retorna el ID sector.
     */
    public static String agenteObtieneSectorPorExtension(String agenteExtension) {
        try {
            return grupoAgentes.obtiene_StrUsername_ObjAgente(Main.agenteObtieneUsernamePorExtension(agenteExtension)).getAgenteSector();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteObtieneSectorPorExtension | Extension: "+agenteExtension+", No pudo acceder al Objeto agente en la cache.", e);
        }
        return null;
    }
    
    /**
     * Metodo para obtener el depto asociado a las llamadas salientes del oeprador.
     * 
     * @param agenteExtension Extension del operador.
     * @return Retorna el ID Depto.
     */
    public static String agenteObtieneDeptoPorExtension(String agenteExtension) {
        try {
            return grupoAgentes.obtiene_StrUsername_ObjAgente(Main.agenteObtieneUsernamePorExtension(agenteExtension)).getAgenteDepto();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteObtieneDeptoPorExtension | Extension: "+agenteExtension+", No pudo acceder al Objeto agente en la cache.", e);
        }
        return null;
    }

    /**
     * Retorna el ID de Agente.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @return Retorna el ID del agente.
     */
    public static String agenteObtieneIdAgente(String agenteUsuario) {
        Logger.getLogger(Main.class.getName()).log(Level.DEBUG, "agenteObtieneIdAgente | Username: "+agenteUsuario+", Obtiene el idAgente (Username => ObjetoAgente) del agente en la cache.");
        try {
            return grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getIdAgente();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteObtieneIdAgente | Username: "+agenteUsuario+", No pudo acceder al objeto agente en la cache", e);
            /**
             * TODO
             * ¿Se debe desloguear el agente para evitar errores posteriores?
             */
        }
        return null;
    }

    /**
     * Seteo de alarma de Agente.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param minutos Numero de minutos que setea el agente para la alarma.
     */
    public static void agenteSeteaAlarma(String agenteUsuario, String minutos) {
        try {
            int milisegundos = (Integer.parseInt(minutos) * 60) * 1000;
            if (milisegundos != 0) {
                grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteFechaAlarma(System.currentTimeMillis() + milisegundos);
            } else {
                grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteFechaAlarma(milisegundos);
            }
        } catch (NumberFormatException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteaAlarma. Error: ", e);
        }
    }

    /**
     * Crea una nueva cola en la cache del sistema. Crea la nueva cola tanto
     * para agentes como supervisores.
     *
     * @param idCola Numero de Cola.
     * @param colaNombre Nombre de la Cola.
     * @param colaSector ID del Sector de la cola.
     * @param colaDepto ID del Depto de la cola.
     */
    public static void colaCreaNuevaCola(String idCola, String colaNombre, String colaSector, String colaDepto) {
        grupoColasAgentes.agrega_StrColaNum_ObjCola(idCola);
        grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaNombre(colaNombre);
        grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaSector(colaSector);
        grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaDepto(colaDepto);
        grupoColasSupervisores.agrega_StrColaNum_ObjCola(idCola);
        grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaNombre(colaNombre);
    }
    
    /**
     * Crea -Si no existe- una nueva cola en la cache del sistema. Crea la nueva cola tanto
     * para agentes como supervisores.
     *
     * @param idCola Numero de Cola.
     * @param colaNombre Nombre de la Cola.
     * @param colaSector ID del Sector de la cola.
     * @param colaDepto ID del Depto de la cola.
     */
    public static void colaChequeaCreaNuevaCola(String idCola, String colaNombre, String colaSector, String colaDepto) {
        if (!grupoColasAgentes.obtiene_ExistenciaCola(idCola)) {
            // Si la cola no existe en cache, se la genera.
            grupoColasAgentes.agrega_StrColaNum_ObjCola(idCola);
            grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaNombre(colaNombre);
            grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaSector(colaSector);
            grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaDepto(colaDepto);
            grupoColasSupervisores.agrega_StrColaNum_ObjCola(idCola);
            grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaNombre(colaNombre);
        }
    }

    /**
     * Carga los datos relevantes de cada cola de atencion. Estos datos se
     * actualizaran cada X segundos. Los X segundos se establecen en
     * LibManagerEvents, escucharQueues.
     *
     * @param idCola Numero de Cola
     * @param integracion Indica si la Cola posee integracion o no.
     * @param grabacion Indica si la Cola se debe grabar o no.
     * @param colaPausa ID pausa.
     */
    public static void colaCargaDatosColaAgentes(String idCola, String integracion, String grabacion, String colaPausa) {
        if (!grabacion.equals("0")) {
            grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaSeteoGRB(true);
        }
        if (!integracion.equals("sinintegracion")) {
            grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaSeteoPOPUP(true);
        }
        grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaPausa(Integer.parseInt(colaPausa));
    }

    /**
     * Inicia la pausa luego de finalizar cada llamado por cola de atencion.
     * Se le envia el codigo 99 (AutoPausa).
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     * @param segundos Numero de segundos que debe estar pausado el Agente.
     * @param codigo Codigo de pausa.
     */
    public static void colaPausaFinLlamado(String agenteUsuario, int segundos, String codigo) {
        try {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "El agente: "+agenteUsuario+" finalizó una llamada. Se pausa AUTOPAUSA, codigo: "+codigo);
            AccionesMG.pausaAgente(codigo, grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension(), agenteUsuario, grupoAgentes, eventos);
            controlUnico.agenteAgendoFinPausa(agenteUsuario, segundos);
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "colaPausaFinLlamado. Error: ", e);
        }
    }

    /**
     * Despausa la pausa iniciada luego de cada llamada por cola de atencion.
     *
     * @param agenteUsuario Nombre de usuario del Agente.
     */
    public static void colaDespausaFinLlamado(String agenteUsuario) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: "+agenteUsuario+". Consulto si la pausa es igual a: AutoPausa. Es un proceso agendado.");
        if (grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjetoPausa().getPausaNombre().equals("AutoPausa")) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: "+agenteUsuario+". Se despausa la AutoPausa.");
            AccionesMG.despausaAgente(grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteExtension(), agenteUsuario, grupoAgentes, eventos);
        }
    }
    
    /**
     * Carga los valores de SLA, SCO y rango SLA en los perfiles de los
     * supervisores.
     *
     * @param idCola Numero de Cola.
     * @param colasla Valor SLA de la Cola.
     * @param colasco Valor SCO de la Cola.
     * @param rangosla Rango de tiempo para la obtencion del SLA.
     */
    public static void colaCargaDatosColasSupervisores(String idCola, String colasla, String colasco, String rangosla) {
        grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaSeteoSLA(Integer.parseInt(colasla));
        grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaSeteoSCO(Integer.parseInt(colasco));
        grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaSeteoRSLA(Integer.parseInt(rangosla));
    }

    /**
     * Carga los valores de Lista de Espera y Carga de Agentes en los perfiles
     * de los agentes.
     *
     * @param idCola Numero de Cola.
     * @param listaEspera Cantidad de llamadas en espera de la Cola.
     * @param cargaAgentes Cantidad de agentes disponibles para la atencion.
     */
    public static void colaAgentesIngresaCargaEspera(String idCola, int listaEspera, int cargaAgentes) {
        grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaValorCA(cargaAgentes);
        grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).setColaValorLLE(listaEspera);
    }

    /**
     * Carga los valores de Lista de Espera y Carga de Agentes en los perfiles
     * de los supervisores.
     *
     * @param idCola Numero de Cola.
     * @param listaEspera Cantidad de llamadas en espera de la Cola.
     * @param cargaAgentes Cantidad de agentes disponibles para la atencion.
     */
    public static void colaSupervisoresIngresaCargaEspera(String idCola, int listaEspera, int cargaAgentes) {
        grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaValorCA(cargaAgentes);
        grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaValorLLE(listaEspera);
    }

    /**
     * Carga los valores SLA para cada cola en los perfiles de los supervisores.
     *
     * @param idCola Numero de COla.
     */
    public static void colaSupervisorIngresaSLA(String idCola) {
        String camposSLA = Funciones.obtieneSumatoriaDeCamposParaSLA(grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).getColaSeteoSLA());
        String camposSCO = Funciones.obtieneSumatoriaDeCamposParaSLA(grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).getColaSeteoSCO());
        int rangoSLA = grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).getColaSeteoRSLA();
        int[] atendidasSLA = libdb.ReporteSla.obtieneAtendidas(idCola, camposSLA, rangoSLA);
        int abandonadasSLA = libdb.ReporteSco.obtienePerdidas(idCola, camposSCO, rangoSLA);
        if ((atendidasSLA[1] + abandonadasSLA) != 0) {
            grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaValorSLA(atendidasSLA[0] * 100 / (atendidasSLA[1] + abandonadasSLA));
        } else {
            grupoColasSupervisores.obtiene_ColaNum_ObjCola(idCola).setColaValorSLA(100);
        }
    }

    /**
     * Funcion simple para la obtencion del nombre de la cola a traves de su
     * numero. La obtencion se realiza mediante el mapeo de ColaNumero -
     * ColaNombre.
     *
     * @param idCola Numero de Cola.
     * @return Retorna el nombre de la Cola.
     */
    public static String colaObtieneColaNombre(String idCola) {
        return grupoColasAgentes.obtiene_StrColaNum_StrColaNom(idCola);
    }

    /**
     * Chequea la existencia de la cola a traves de su ID (Numero de cola).
     *
     * @param idCola Numero de la cola.
     * @return Retorna TRUE en caso de que la cola exista.
     */
    public static boolean colaChequeaExistencia(String idCola) {
        return grupoColasAgentes.obtiene_ExistenciaCola(idCola);
    }

    /**
     * Obtiene el objeto Cola a traves de su ID (Numero de cola).
     *
     * @param idCola Numero de la cola.
     * @return Retorna el objeto Cola.
     */
    public static Cola colaObtieneObjetoCola(String idCola) {
        return grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola);
    }
    
    /**
     * Obtiene el sector asociado al ID de la cola.
     * 
     * @param idCola ID Unico de la cola.
     * @return  Retorna el ID de Sector asociado a la cola.
     */
    public static String colaObtieneColaSector(String idCola) {
        return grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).getColaSector();
    }
    
    /**
     * Obtiene el Depto asociado al ID de la cola.
     * 
     * @param idCola ID unico de la cola.
     * @return  Retorna el ID de Depto asociado a la cola.
     */
    public static String colaObtieneColaDepto(String idCola) {
        return grupoColasAgentes.obtiene_ColaNum_ObjCola(idCola).getColaDepto();
    }

    /**
     * Envia el estado/actividad al agente.
     *
     * Si la conexion con el agente sigue abierta, se envia la actualizacion del
     * estado. Si la conexion con el agente no se encuentra abierta significa
     * que algo sucedio, por ejemplo un cierre inesperado. Ademas se envia esta
     * actualizacion a los demas agentes y supervisores.
     *
     * @param agenteUsuario Nombre de Usuario del agente.
     */
    public static void agenteEnviaEstadoActividad(String agenteUsuario) {
        try {
            WebSocket conn = grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteObjWS();
            if (conn.isOpen()) {
                conn.send("130;" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteEstado() + ";" + grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteActividad());
            }
            agenteInfoAgentes(agenteUsuario);
            supervisorInfoAgente(agenteUsuario);
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteEnviaEstadoActividad. ", e);
        }
    }

    /**
     * Setea el estado del Agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param agenteEstado Estado del Agente.
     */
    public static void agenteSeteaEstado(String agenteUsuario, String agenteEstado) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteEstado(agenteEstado);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteaEstado. ", ex);
        }
    }

    /**
     * Setea la actividad del Agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param agenteActividad Actividad del Agente.
     */
    public static void agenteSeteaActividad(String agenteUsuario, String agenteActividad) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteActividad(agenteActividad);
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteaActividad. ", e);
        }
    }

    /**
     * Setea el numero con el cual esta hablando el agente
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param numeroHablando Numero con el cual esta hablando.
     */
    public static void agenteSeteaNumeroHablando(String agenteUsuario, String numeroHablando) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteNumeroHablando(numeroHablando);
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteaNumeroHablando.", e);
        }
    }

    /**
     * Setea la Llamada en curso del agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param idCola Si es por cola, almacena el numero de la misma. Si es
     * saliente guarda: Saliente.
     */
    public static void agenteSeteaColaLlamada(String agenteUsuario, String idCola) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteColaLlamada(idCola);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteaColaLlamada. ", ex);
        }
    }

    /**
     * Obtiene el dato de la llamada que esta teniendo el agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @return Numero de la Cola de llamada.
     */
    public static String agenteObtieneColaLlamada(String agenteUsuario) {
        return grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).getAgenteColaLlamada();
    }

    /**
     * Setea la fecha del ultimo cambio de estado del Agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param fecha Fecha del cambio.
     */
    public static void agenteSeteoFechaUltimoEstado(String agenteUsuario, long fecha) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteFechaUltEst(fecha);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteoFechaUltimoEstado. ", ex);
        }
    }

    /**
     * Setea la fecha del ultimo cambio de actividad del Agente.
     *
     * @param agenteUsuario Nombre de Usuario del Agente.
     * @param fecha Fecha del cambio.
     */
    public static void agenteSeteoFechaUltimaActividad(String agenteUsuario, long fecha) {
        try {
            grupoAgentes.obtiene_StrUsername_ObjAgente(agenteUsuario).setAgenteFechaUltAct(fecha);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteSeteoFechaUltimaActividad. ", ex);
        }
    }

    /**
     * Funcion que mantiene vivo el WebSocket mediante el envio de mensajes cada
     * 60 segundos. ver: ControlTemporal.pingPong
     */
    public static void agenteKeepAlive() {
        try {
            Collection<WebSocket> conexiones = grupoAgentes.obtiene_ListWebSockets();
            if (conexiones != null) {
                conexiones.forEach((conn) -> {
                    if (conn.isOpen()) {
                        conn.send("100");
                    }
                });
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteKeepAlive", e);
        }
    }

    /**
     * Funcion que mantiene vivo el websocket de los supervisores.
     *
     */
    public static void supervisorKeepAlive() {
        try {
            Collection<WebSocket> conexiones = grupoSupervisores.obtiene_ListWebSockets();
            if (conexiones != null) {
                conexiones.forEach((conn) -> {
                    if (conn.isOpen()) {
                        conn.send("100");
                    }
                });
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "supervisorKeepAlive", e);
        }
    }

    /**
     * Funcion que expulsa a un agente si transcurren mas de 5 horas sin actividad.
     * ver: ControlTemporal.chequeaAgente.
     */
    public static void agenteExpulsaPorInactivo() {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, " | agenteExpulsaPorInactivo | Se chequea la Actividad de los agentes y se expulsa quien haya tenido mas de 5 hs sin actividad.");
        try {
            Collection<WebSocket> conexiones = grupoAgentes.obtiene_ListWebSockets();
            if (conexiones != null) {
                conexiones.forEach((conn) -> {
                    long fechaAgenteUltimoEstado = grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(conn.toString())).getAgenteFechaUltEst();
                    if ((System.currentTimeMillis() - fechaAgenteUltimoEstado) > 18000000) {
                        Logger.getLogger(Main.class.getName()).log(Level.INFO, " | agenteExpulsaPorInactivo | Operador: " + grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(conn.toString())).getAgenteUsuario() + " | Se envia codigo de deslogueo por transcurrir mas de 5 horas sin actividad.");
                        if (conn.isOpen()) {
                            conn.send("150");
                        }
                    }
                });
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "agenteExpulsaPorInactivo", e);
        }
    }

    /**
     * Metodo para la alarma del agente. Esta deshabilitado.
     * 
     */
    public static void agenteChequeaAlarma() {
        try {
            Collection<WebSocket> conexiones = grupoAgentes.obtiene_ListWebSockets();
            if (conexiones != null) {
                for (WebSocket conn : conexiones) {
                    long agenteAlarma = grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(conn.toString())).getAgenteFechaAlarma();
                    if (agenteAlarma != 0 && (System.currentTimeMillis() - agenteAlarma) >= 0) {
                        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Agente: " + grupoAgentes.obtiene_StrWebSocket_StrUsername(conn.toString()) + " | Se envia la alarma por pausa");
                        if (conn.isOpen()) {
                            conn.send("180");
                            grupoAgentes.obtiene_StrUsername_ObjAgente(grupoAgentes.obtiene_StrWebSocket_StrUsername(conn.toString())).setAgenteFechaAlarma(0);
                        }
                    }
                }
            }
        } catch (NotYetConnectedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.ERROR, "AgenteKick. Error: ", e);
        }
    }

    /**
     * @return Retorna el numero de IP del servidor.
     */
    public static String getMNGServidor() {
        return mngServidor;
    }

    /**
     * @return Retorna el nombre de usuario para la conexion con el Manager.
     */
    public static String getMNGUsuario() {
        return mngUsuario;
    }

    /**
     * @return Retorna el password para la conexion con el Manager.
     */
    public static String getMNGPassword() {
        return mngPassword;
    }

    /**
     * @return Retorna el puerto para la conexion con el Manager.
     */
    public static String getMNGPuerto() {
        return mngPuerto;
    }

    /**
     * @return Retorna el path para la conexion con la DB central.
     */
    public static String getDBConexionCentral() {
        return dbConexionCentral;
    }

    /**
     * @return Retorna el usuario para la conexion con la DB central.
     */
    public static String getDBUsuarioCentral() {
        return dbUsuarioCentral;
    }

    /**
     * @return Retorna el password para la conexion con la DB central.
     */
    public static String getDBPasswordCentral() {
        return dbPasswordCentral;
    }

    /**
     * @return Retorna el path para la conexion con la DB Local.
     */
    public static String getDBConexionLocal() {
        return dbConexionLocal;
    }

    /**
     * @return Retorna el usuario para la conexion con la DB local.
     */
    public static String getDBUsuarioLocal() {
        return dbUsuarioLocal;
    }

    /**
     * @return Retorna el password para la conexion con la DB local.
     */
    public static String getDBPasswordLocal() {
        return dbPasswordLocal;
    }

    /**
     * @return Retorna el nombre de la DB SQLite.
     */
    public static String getDBSQLiteName() {
        return dbSQLiteName;
    }
}
