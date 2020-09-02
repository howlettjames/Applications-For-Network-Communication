import java.net.*;
import java.io.*;

public class Datagrama1
{
	public static void main(String[] args) 
	{
		try
		{
			DatagramSocket s = new DatagramSocket(1234);
			for(;;)
			{
				DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
				s.receive(p);
				System.out.println("Datagrama recibido desde: " + p.getAddress() + ":" + p.getPort());
				String msj = new String(p.getData(), 0, p.getLength());
				//Si quisieramos recibir datos primitivos
				//DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData()));
				//int v1 = dis.readInt();
				System.out.println("Datos: " + msj);
			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}