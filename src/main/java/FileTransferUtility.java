

import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FileTransferUtility {
    public int startBlock;
    public int sourceUDPPOrt;
    public int receiverUdpPort;
    public Socket tcpSocket;
    public PrintWriter sender;
    public Scanner receiver;
    public String dir;
    public String fileName;
    public static InetAddress serverIp = null;
    public static Logger logger;
    public static int clientNum;


    static {
        try {
            serverIp = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public FileTransferUtility(int startBlock, int sourceUDPPOrt, int destinationPort, Socket tcpSocket, String dir, String fileName,
                               Logger logger, int clientNum, PrintWriter sender, Scanner receiver) {
        this.startBlock = startBlock;
        this.sourceUDPPOrt = sourceUDPPOrt;
        this.receiverUdpPort = destinationPort;
        this.tcpSocket = tcpSocket;
        this.sender = sender;
        this.receiver = receiver;
        this.dir = dir;
        this.fileName = fileName;
        FileTransferUtility.logger = logger;
        FileTransferUtility.clientNum = clientNum;
    }

    public void createTcpConn(){
        try {
            InetAddress serverIp = InetAddress.getByName("localhost");
            this.tcpSocket = new Socket(serverIp, Constants.SERVER_TCP_PORT);
        } catch (Exception e) {
            logger.info("Server isn't available");
            System.exit(1);
        }
    }


    public void sendFileServer() {
        try {
            PacketBoundedBufferMonitor bufferMonitor=new PacketBoundedBufferMonitor(Constants.MONITOR_BUFFER_SIZE);
            InetAddress senderIp=InetAddress.getByName("localhost");

            PacketSender packetSender=new PacketSender(bufferMonitor,senderIp,sourceUDPPOrt,serverIp,receiverUdpPort);
            packetSender.start();

            FileReader fileReader=new FileReader(bufferMonitor, this.fileName, dir, this.startBlock);
            fileReader.start();

            try {
                packetSender.join();
                fileReader.join();
            }
            catch (InterruptedException e) {}

        }catch(Exception e) {e.printStackTrace();}
    }

    public void  receiveHandleClient() {
        try {
            PacketBoundedBufferMonitor bm=new PacketBoundedBufferMonitor(Constants.MONITOR_BUFFER_SIZE);

            this.receiveFile(bm);// receive the file

        }catch(Exception e) {e.printStackTrace();}
    }


    public void sendfile() {
        try {
            this.sender.println(this.fileName);

            PacketBoundedBufferMonitor bufferMonitor=new PacketBoundedBufferMonitor(Constants.MONITOR_BUFFER_SIZE);
            InetAddress senderIp=InetAddress.getByName("localhost");

            PacketSender packetSender=new PacketSender(bufferMonitor,senderIp,sourceUDPPOrt,serverIp,receiverUdpPort);
            packetSender.start();

            FileReader fileReader=new FileReader(bufferMonitor, fileName, dir, this.startBlock);
            fileReader.start();

            try {
                packetSender.join();
                fileReader.join();
            }
            catch (InterruptedException e) {}

        }catch(Exception e) {e.printStackTrace();}
    }



    public String  receiveHandle() {
        this.fileName = this.receiver.nextLine();
        try {
            PacketBoundedBufferMonitor bm=new PacketBoundedBufferMonitor(Constants.MONITOR_BUFFER_SIZE);

            this.receiveFile(bm);// receive the file

        }catch(Exception e) {e.printStackTrace();}
        return this.fileName;
    }

    public void receiveFile(PacketBoundedBufferMonitor bm) {

        PacketReceiver packetReceiver=new PacketReceiver(bm, serverIp,this.receiverUdpPort, serverIp,this.sourceUDPPOrt);
        packetReceiver.start();

        FileWriter fileWriter=new FileWriter(bm, this.dir, this.fileName);
        fileWriter.start();
        try {
            packetReceiver.join();
            fileWriter.join();
        }
        catch (InterruptedException e) {e.printStackTrace();}

    }
}
