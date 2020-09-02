import java.net.*;
import java.io.*;

public class EchoServidor
{
	public static void main(String[] args) 
	{
		try
		{
			DatagramSocket s = new DatagramSocket(1234);
			DatagramSocket cl = new DatagramSocket();
			System.out.println("Servidor esperando cadena...");
			for(;;)
			{
				DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
				s.receive(p);
				System.out.println("Datagrama recibido desde: " + p.getAddress() + ":" + p.getPort());
				String msj = new String(p.getData(), 0, p.getLength());
				System.out.println("Datos: " + msj);
				
				InetAddress dst = null;
				try
				{
					dst = InetAddress.getByName("127.0.0.1");
				}
				catch(UnknownHostException ex)
				{
					System.out.println("La direccion no es valida");
					System.exit(1);
				}
				DatagramPacket p1 = new DatagramPacket(p.getData(), p.getLength(), dst, 1235);
				cl.send(p1);
			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}