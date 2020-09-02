import java.net.*;
import java.io.*;

public class Datagrama2
{
	public static void main(String[] args) 
	{
		try
		{
			DatagramSocket cl = new DatagramSocket();
			//Si quisieramos escribir datos primitivos
			/*
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(5);
			dos.writeFloat(6.0f);
			dos.writeLong(7);
			dos.flush();
			byte[] b = baos.toByteArray();
			*/
			String msj = "Un mensaje por datagrama";
			byte[] b = msj.getBytes();
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
			DatagramPacket p = new DatagramPacket(b, b.length, dst, 1234);
			cl.send(p);
			cl.close();
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}