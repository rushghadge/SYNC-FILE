import org.apache.log4j.Logger;
import utilities.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;


public class Client2 implements Runnable{
    private static boolean reader = false;
    private static boolean writer = false;
    public boolean sender = false;
    public static String localDir;
    public static String backupDir;
    private static final Logger logger = Log.getLogger("Client");//    private static final Logger logger = LogManager.getLogger(Client.class);
    public static Socket tcpSocket;
    public static int receiverUDPPort;
    public static int senderUDPPOrt;
    private static final int clientNum = 2;
    private static WatchFolder watcher;

    public void getNewWatcher() {
        try {
            watcher = new WatchFolder(localDir, logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client2(){
    }

    @Override
    public void run(){
        try {
            switch (Thread.currentThread().getName()) {
                case "pollingAgent":
                    this.polling();
                    break;
                case "watcher":
                    this.watchOverFiles();
                    break;
            }
        } catch (IOException e) {
            logger.error("Exception", e);
        } catch (InterruptedException e) {
            logger.error("Exception", e);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }
    public static Socket createTcpConn(){
        try {
            InetAddress serverIp = InetAddress.getByName("localhost");
            tcpSocket = new Socket(serverIp, Constants.SERVER_TCP_PORT);
        } catch (Exception e) {
            logger.info("Server isn't available");
            System.exit(1);
        }
        return tcpSocket;
    }

    public boolean canRead(){
        if(writer) return false;
        return true;
    }
    public boolean canWrite(){
        if(reader) return false;
        return true;
    }

    public void watchOverFiles() throws Exception {
        logger.info("Watching directory for changes"+localDir);
        this.getNewWatcher();
        // STEP4: Poll for events
        while (true) {
            if(this.canRead()) {
                reader = true;
                this.watcher.watchFolder();
                if (watcher.newFile != null) {
                    logger.info("A new file is created : " + watcher.newFile);
                    // Send the file over UDP
                    Socket tcpSocket = createTcpConn();
                    PrintWriter sender = new PrintWriter(tcpSocket.getOutputStream(), true);
                    Scanner receiver = new Scanner(tcpSocket.getInputStream());
                    String action = clientNum + Constants.CRLF + "GET NEW FILE";
                    sender.println(action);
//                sender.close();
                    FileTransferUtility transfer = new FileTransferUtility(0, senderUDPPOrt, receiverUDPPort, tcpSocket, localDir, watcher.newFile.toString(), logger, clientNum, sender, receiver);
                    transfer.sendfile();
                    watcher.newFile = null;
                    tcpSocket.close();
                    sender.close();
                    receiver.close();
                } else if (watcher.modifiedFile != null) {
                    logger.info("A file is modified : " + watcher.modifiedFile);
                    watcher.modifiedFile = null;
                } else if (watcher.deletedFile != null) {
                    logger.info("A new file is deleted : " + watcher.deletedFile);
                    // Send the modified blocks over UDP
                 //   Helper.DeleteSingleFile(localDir, watcher.deletedFile.toString());
                    Helper.DeleteSingleFile(Constants.LOCAL_DIRS[0][0], watcher.deletedFile.toString());
                    watcher.deletedFile = null;
                }
                Thread.sleep(100);
                boolean valid = watcher.watchKey.reset();
                if (!valid) {
                    break;
                }
            }else {
                reader = false;
                Thread.sleep(20);
            }
        }
    }
    public void polling() throws IOException, InterruptedException {
        logger.info("Preparing to poll the server");
        while (true) {
            tcpSocket = createTcpConn();
            logger.info("Client Socket created");
            PrintWriter sender = new PrintWriter(tcpSocket.getOutputStream(), true);
            Scanner receiver = new Scanner(tcpSocket.getInputStream());
            logger.info("Polling the server started");
            String line = clientNum + "\nPOLLING";
            // send the message
            sender.println(line);
            line = receiver.nextLine();
            logger.info(line);
            if (!line.equals("NONE")) {
                // The horror
                logger.info("Server has some update " + line);
                tcpSocket.close();
                sender.close();
                receiver.close();
                writer = true;
                while(!this.canWrite()){Thread.sleep(1);}

                if (line.startsWith("ADD")) {
//                    FileTransferUtility transfer = new FileTransferUtility(0, senderUDPPOrt, receiverUDPPort, tcpSocket, localDir, line.split("#")[1], logger, clientNum, sender, receiver);
//                    transfer.receiveHandle();
                    this.getFile(line.split("#")[1]);

                }

                this.getNewWatcher();
                writer = false;
            }else {
                tcpSocket.close();
                sender.close();
                receiver.close();
            }
            logger.info("Polling the server Finished");
            Thread.sleep(20000);
        }
    }

    public void getFile(String fileName) throws IOException {
        String command = clientNum+Constants.CRLF+"SEND NEW FILE";
        tcpSocket = createTcpConn();
        logger.info("Client Socket created");
        PrintWriter sender = new PrintWriter(tcpSocket.getOutputStream(), true);
        Scanner receiver = new Scanner(tcpSocket.getInputStream());

        try {
            // send the file name
            sender.println(command);
            FileTransferUtility transfer = new FileTransferUtility(0, senderUDPPOrt, receiverUDPPort, tcpSocket, localDir, fileName, logger, clientNum, sender, receiver);
            transfer.receiveHandleClient();

            tcpSocket.close();
            sender.close();
            receiver.close();

        }catch (Exception e){
            logger.error("Failed to chat", e);
        }
    }



    private static void setProperties(){
        if(clientNum==1){
            receiverUDPPort = Constants.SERVER_UDP_PORT_CLIENT_ONE;
            senderUDPPOrt = Constants.CLIENT_ONE_UDP_PORT;
        }else{
            receiverUDPPort = Constants.SERVER_UDP_PORT_CLIENT_TWO;
            senderUDPPOrt = Constants.CLIENT_TWO_UDP_PORT;
        }
        localDir = Constants.LOCAL_DIRS[0][clientNum];
        backupDir = Constants.LOCAL_DIRS[1][clientNum];
    }

    public static void main(String[] args) throws IOException {
        // Expectation is Client.class file will be executed with an identifier like 0, 1, 2 etc
        // And localDir will be decided using that client_no
        setProperties();
        // First cleanup the clients' local and backup dirs.
        Helper.deleteAllFiles(logger, localDir, backupDir);
        tcpSocket = createTcpConn();
        PrintWriter sender = new PrintWriter(tcpSocket.getOutputStream(), true);
        sender.println(clientNum+"\nSTARTING");
        tcpSocket.close();
        // Create threads
        Thread watcher = new Thread(new Client2());
        Thread pollingAgent = new Thread(new Client2());
        watcher.setName("watcher");
        pollingAgent.setName("pollingAgent");
        // Start them
        watcher.start();
        pollingAgent.start();

    }

}
