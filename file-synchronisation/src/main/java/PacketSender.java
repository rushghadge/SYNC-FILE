import java.net.*;

public class PacketSender extends Thread {
	private String threadName="PacketSender";
	private PacketBoundedBufferMonitor bufferMonitor;	
	private DatagramSocket udpSenderSocket;
	private DatagramPacket udpSenderPacket;
	private InetAddress senderIp;//local
	private int senderPort;//local
	private InetAddress receiverIp;//remote
	private int receiverPort;//remote

	
	public PacketSender() throws Exception {
		//udpSenderSocket=new DatagramSocket(senderPort,senderIp);
	}
	public PacketSender(PacketBoundedBufferMonitor bm, InetAddress senderIp, int senderPort, InetAddress receiverIp, int receiverPort) {
		this.bufferMonitor=bm;		
		this.senderIp=senderIp;
		this.senderPort=senderPort;	
		this.receiverIp=receiverIp;
		this.receiverPort=receiverPort;			
	}
	
	public void run() {
		byte[] buf=new byte[Constants.MAX_DATAGRAM_SIZE];
		byte []receiveBuf=new byte[1024];
		try {
			//newly commented
			try {
				if (udpSenderSocket == null || senderIp == null) {
					System.out.println(""+senderIp+senderPort);
					udpSenderSocket = new DatagramSocket(senderPort, senderIp);
				}
			}catch (BindException e){
				e.printStackTrace();
			}
			udpSenderPacket=new DatagramPacket(buf,Constants.MAX_DATAGRAM_SIZE,receiverIp,receiverPort);
			
			System.out.println(">> Begin to send packets"+Constants.CRLF);
			while(true) {
				//withdraw an item
				System.out.println(">> Begin to send packets inside while "+Constants.CRLF);
				Packet pktS=this.bufferMonitor.withdraw();
				buf=new byte[Constants.MAX_DATAGRAM_SIZE];
				buf=pktS.packetToByteArray();
				System.out.println("pktS.getContentSize()"+pktS.getContentSize());
				udpSenderPacket.setData(buf,0,pktS.getContentSize()+4);
				udpSenderSocket.send(udpSenderPacket);
				
				System.out.println(">> Send the packet with index "+pktS.getIndex());
				
				// get an ACK packet				
				while(true){
					try {
						udpSenderSocket.setSoTimeout(100);
						udpSenderPacket.setData(receiveBuf,0,receiveBuf.length);
						udpSenderSocket.receive(udpSenderPacket);
					}catch(SocketTimeoutException e) {
						// resend the packet if the corresponding ACK packet is not received
						System.out.println(">> Resend the packet with index "+pktS.getIndex()+Constants.CRLF);
						udpSenderPacket.setData(buf,0,pktS.getContentSize()+4);
						udpSenderSocket.send(udpSenderPacket);
					}
					int index=Helper.byteArrayToInt(Helper.get4Bytes(udpSenderPacket.getData()));
					if (index==pktS.getIndex()) {
						System.out.println("ACK for the packet with index "+index+Constants.CRLF);			
						break;
					}								
				}
				
				if (pktS.getIndex()==-1) {
					System.out.println(">> Finish sending packets.");
					break;
				}

				//sleep(10);	
				
			}//end of while
			//new added
			udpSenderSocket.close();
		}catch(Exception e) {e.printStackTrace();}
		
	}
	
	private void sleep(int i) {
		try {
			Thread.currentThread().sleep(100);
		}catch(Exception e) {e.printStackTrace();}
		
	}

}
