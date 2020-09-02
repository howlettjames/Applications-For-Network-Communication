import java.net.*;
import java.io.*;

public class ObjectClient
{
	public static void main(String[] args) 
	{
		int limit = 10;
		try
		{
			DatagramSocket cl = new DatagramSocket();
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

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream  oos = new ObjectOutputStream(baos);

			String msg1 = "Hello";
			Data data1 = new Data(1, msg1.getBytes(), 2);
			String msg2 = "Daniela Cochita";
			Data data2 = new Data(2, msg2.getBytes(), 2);

			oos.writeObject(data1);
			oos.writeObject(data2);
			oos.flush();

			byte[] b = baos.toByteArray();
			DatagramPacket p = new DatagramPacket(b, b.length, dst, 1234);
			cl.send(p);
			oos.close();
			baos.close();
			cl.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}