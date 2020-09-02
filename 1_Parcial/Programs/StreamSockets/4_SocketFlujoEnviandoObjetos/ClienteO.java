import java.net.*;
import java.io.*;

public class ClienteO
{
	public static void main(String[] args) 
	{
		try
		{
			Dato d1 = null;
			int pto = 1234;
			String host = "127.0.0.1";
			Socket cl = new Socket(host, pto);
			System.out.println("Cliente conectado, recibiendo objeto...");
			ObjectOutputStream oos = new ObjectOutputStream(cl.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(cl.getInputStream());
			Object o = ois.readObject();
			if(o instanceof Dato)
				d1 = (Dato) o;
			//Dato d1 = (Dato) ois.readObject();
			System.out.println("Objeto recibido con la informacion: \nV1: " + d1.getV1() + " V2: " + d1.getV2() + " V3: " + d1.getV3() + " V4: " + d1.getV4());
			Dato d2 = new Dato("cinco", 6, 7.0f, 8);
			oos.writeObject(d2);
			oos.flush();
			System.out.println("Objecto enviado con la informacion: \nV1: " + d2.getV1() + " V2: " + d2.getV2() + " V3: " + d2.getV3() + " V4: " + d2.getV4());
			ois.close();
			oos.close();
			cl.close();
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}