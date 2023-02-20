import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PacketReceiver extends Thread {
	private String threadName="PacketReceiver";
	private PacketBoundedBufferMonitor bufferMonitor;	
	private DatagramSocket udpReceiverSocket;
	private DatagramPacket udpReceiverPacket;
	private InetAddress receiverIp;//local
	private int receiverPort;//local
	private InetAddress senderIp;//remote
	private int senderPort;//remote

	
	public PacketReceiver() {}
	public PacketReceiver(PacketBoundedBufferMonitor bm, InetAddress receiverIp, int receiverPort, InetAddress senderIp, int senderPort) {
		this.bufferMonitor=bm;
		this.receiverIp=receiverIp;
		this.receiverPort=receiverPort;	
		this.senderIp=senderIp;
		this.senderPort=senderPort;
	
	}
	
	public void run() {
		try {		
			byte[] buf=new byte[Constants.MAX_DATAGRAM_SIZE];
			System.out.println(""+receiverPort+receiverIp);
			udpReceiverSocket=new DatagramSocket(receiverPort, receiverIp);		
			udpReceiverPacket=new DatagramPacket(buf,Constants.MAX_DATAGRAM_SIZE,senderIp,senderPort);
			int expectPacketIndex=0;
			int currentPacketIndex=0;
			boolean depositFlag=false;
			
			System.out.println(">> Begin to receive packets"+Constants.CRLF);
			while(true) {
				System.out.println(">>  while(true) {   Begin to re ");
				//receive packets
				buf=new byte[Constants.MAX_DATAGRAM_SIZE];
				udpReceiverPacket.setData(buf,0,buf.length);
				udpReceiverSocket.receive(udpReceiverPacket);
								
				Packet pkt=new Packet(udpReceiverPacket.getData(),udpReceiverPacket.getLength());													
				System.out.println(">> Receive the packet with index "+pkt.getIndex());
				
				// send an ACK packet in case that the packet has already received.
				currentPacketIndex=pkt.getIndex();
				if (currentPacketIndex!=-1) {
					while(currentPacketIndex<expectPacketIndex) {
						// send an ACK with the packet index received
						udpReceiverPacket.setData(Helper.intToByteArray(currentPacketIndex),0,4);				
						udpReceiverSocket.send(udpReceiverPacket);
						
						buf=new byte[Constants.MAX_DATAGRAM_SIZE];
						udpReceiverSocket.receive(udpReceiverPacket);									
						pkt=new Packet(udpReceiverPacket.getData(),udpReceiverPacket.getLength());
						currentPacketIndex=pkt.getIndex();
					}
				}
				
				// send an ACK with the packet index received
				udpReceiverPacket.setData(Helper.intToByteArray(currentPacketIndex),0,4);				
				udpReceiverSocket.send(udpReceiverPacket);
				System.out.println("   Send an ACK packet for packet "+currentPacketIndex+Constants.CRLF);
							         
				//deposit
				this.bufferMonitor.deposit(pkt);
				// increase expectPacketIndex
				expectPacketIndex+=1;
				
				if (pkt.getIndex()==-1) {
					System.out.println(">> Finish receiving packets");
					break;
				}				
				
			}//end of receiving a file
			//new added
			this.udpReceiverSocket.close();
		}catch(Exception e) {e.printStackTrace();}finally {
			try {
				if(this.udpReceiverSocket!=null) {
					this.udpReceiverSocket.close();
				}
			}catch(Exception e) {e.printStackTrace();}
		}	
	}
}
