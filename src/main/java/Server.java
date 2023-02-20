import org.apache.log4j.Logger;
import utilities.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable{
    Socket client;
    Map<String, Integer> clientPortMap = new HashMap();
    public static final Logger logger = Log.getLogger("Server");
    public static String requestType;
    public static List<Buffer> clinetBuffer = new ArrayList<>();
    public static int clientNo;
    public static String localDir;
    public static String backupDir;
    private int receiverUdpPort;
    private int senderUdpPort;




    public Server(Socket client){
        this.client = client;
    }

    public void setClientProperties(String line){
        clientNo = Integer.parseInt(line.trim());
        clientPortMap.put(line.trim(), this.client.getPort());
        if(clientNo==1) {
            this.receiverUdpPort = Constants.CLIENT_ONE_UDP_PORT;
            this.senderUdpPort = Constants.SERVER_UDP_PORT_CLIENT_ONE;
        }else {
            this.receiverUdpPort = Constants.CLIENT_TWO_UDP_PORT;
            this.senderUdpPort = Constants.SERVER_UDP_PORT_CLIENT_TWO;
        }
    }
    public int updateBufferForOthers(int clientNo){
        if(Server.clientNo ==1)return 2;
        return 1;
    }

    @Override
    public void run(){
        logger.info("new request from "+ this.client.getInetAddress() + "/" + this.client.getPort() + " has been accepted");
        // initiate the Scanner and PrintWriter objects
        Scanner receiver = null;
        PrintWriter sender = null;
        try {
            receiver = new Scanner(this.client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sender = new PrintWriter(this.client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // First thing to do is get the client Number
        String line = receiver.nextLine();
        this.setClientProperties(line);
        logger.info("Welcome Client"+line);
        line = receiver.nextLine();
        requestType = line;
//
//        line = receiver.nextLine();
//        StringBuilder data = new StringBuilder();
//        while(!line.equals("STOP")){
//            data.append(line);
//            line = receiver.nextLine();
//        }
        String fileName = null;

        switch (requestType) {
            case "POLLING":
                // Look into client(id) buffer
                assert sender != null;
                sender.println(clinetBuffer.get(clientNo).checkUpdate());
                break;
            case "STARTING":
                clinetBuffer.set(clientNo, new Buffer(clientNo));
                break;
            case "GET NEW FILE":
                logger.info("FILE SEND REQUEST FROM CLIENT" + clientNo);
//                fileName = data.toString();
//                logger.info(fileName+data);
                FileTransferUtility transfer = new FileTransferUtility(0, this.receiverUdpPort, this.senderUdpPort, this.client, localDir, null, logger, 0, sender, receiver);
                fileName = transfer.receiveHandle();
                clinetBuffer.get(this.updateBufferForOthers(clientNo)).setNewFileNames(fileName);
                logger.info("Client Buffer for "+this.updateBufferForOthers(clientNo)+ " has been updated. " + clinetBuffer.get(this.updateBufferForOthers(clientNo)).newFileNames.get(0));
                break;
            case "SEND NEW FILE":
                logger.info("FILE GET REQUEST FROM CLIENT" + clientNo);
                fileName = clinetBuffer.get(clientNo).checkUpdate().split("#")[1];
                transfer = new FileTransferUtility(0, this.receiverUdpPort, this.senderUdpPort, this.client, localDir, fileName, logger, 0, sender, receiver);
                transfer.sendFileServer();
                clinetBuffer.get(clientNo).deleteNewFileNames();
                break;
        }
        try {
            this.client.close();
        } catch (IOException e) {
            logger.info("Client closing was already done!!!");
        }
        logger.info("All requests from "+ this.client.getInetAddress() + "/" + this.client.getPort()+" have been completed");
    }

    public static void main(String[] args){
        ServerSocket socket=null;
        Socket client=null;
        clinetBuffer.add(new Buffer(0));
        clinetBuffer.add(new Buffer(1));
        clinetBuffer.add(new Buffer(2));
        clinetBuffer.add(new Buffer(3));
        // localDir
        localDir = Constants.LOCAL_DIRS[0][0];
        backupDir = Constants.LOCAL_DIRS[1][0];
        // First cleanup the clients' local and backup dirs.
        Helper.deleteAllFiles(logger, localDir, backupDir);

        try{
            socket = new ServerSocket(Constants.SERVER_TCP_PORT);
        }catch(IOException exc){
            logger.error("Unable to setup port and start the server..."+exc.getMessage());
            System.exit(1);
        }
        logger.info("Server is listening to port " + Constants.SERVER_TCP_PORT);
        // Create thread pool
        Thread[] clientThreads = new Thread [10];
        int idx, i;
        while (true){
            idx = -1;
            // Check if any thread slot is empty
            for(i=0; i<10; i++){
                if (clientThreads[i]==null || !clientThreads[i].isAlive()) {
                    idx = i;
                    break;
                }
            }
            // If no thread slot is empty then wait for 10 seconds and continue
            if(idx==-1){
                try{
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            // When there is at least one empty thread slot.
            // Go ahead and Accept client request
            try {
                client = socket.accept();
            } catch (IOException exc){
                logger.error("Failed to listen to requests!");
                System.exit(1);
            }
            clientThreads[idx] = new Thread(new Server(client));
            clientThreads[idx].start();
        }
    }
}

