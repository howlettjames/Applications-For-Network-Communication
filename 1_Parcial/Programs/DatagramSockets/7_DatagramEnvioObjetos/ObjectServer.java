import java.net.*;
import java.io.*;

public class ObjectServer
{
	public static void main(String[] args) 
	{
		try
		{
			DatagramSocket s = new DatagramSocket(1234);
			DatagramSocket cl = new DatagramSocket();
			System.out.println("Server waiting for object...");
			for(;;)
			{
				DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
				s.receive(p);
				System.out.println("Datagram received from: " + p.getAddress() + ":" + p.getPort());
				ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
				ObjectInputStream ois = new ObjectInputStream(bais);

				InetAddress dst = null;
				try
				{
					dst = InetAddress.getByName("127.0.0.1");
				}
				catch(UnknownHostException ex)
				{
					System.out.println("IP not valid");
					System.exit(1);
				}
				Object o = null;
				while((o = ois.readObject()) != null)
				{
					if(o instanceof Data)
					{
						Data data = (Data) o;
						String msg = new String(data.bytes, 0, data.bytes.length);
						System.out.println("\nData" + "\nNumber of sequence: " + data.noSequence + "\nMessage: " + msg + "\nTotal of packets: " + data.total);	
					}
					else
					{
						System.out.println("Object received is not instace of class Data");
						break;
					}
				}
				s.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
}