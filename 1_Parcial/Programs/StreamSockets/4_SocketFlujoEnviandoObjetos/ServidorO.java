import java.net.*;
import java.io.*;

public class ServidorO
{
	public static void main(String[] args) 
	{
		try
		{
			int pto = 1234;
			ServerSocket s = new ServerSocket(pto);
			System.out.println("Servidor Iniciado. Esperando cliente...");
			s.setReuseAddress(true);

			for(;;)
			{
				Socket cl = s.accept();
				cl.setSoLinger(true, 5000);
				System.out.println("Cliente conectado desde: " + cl.getInetAddress() + ":" + cl.getPort());
				System.out.println("Enviando objeto");
				ObjectOutputStream oos = new ObjectOutputStream(cl.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(cl.getInputStream());
				Dato d1 = new Dato("uno", 2, 3.0f, 4);
				oos.writeObject(d1);
				oos.flush();
				System.out.println("Objecto enviado con la informacion: \nV1: " + d1.getV1() + " V2: " + d1.getV2() + " V3: " + d1.getV3() + " V4: " + d1.getV4());
				System.out.println("Preparado para recibir objeto");
				Dato d2 = (Dato) ois.readObject();
				System.out.println("Objeto recibido con la informacion: \nV1: " + d2.getV1() + " V2: " + d2.getV2() + " V3: " + d2.getV3() + " V4: " + d2.getV4());
				ois.close();
				oos.close();
				cl.close();
			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
