import java.net.*;
import java.io.*;

public class ServidorEnvia
{
	public static void main(String[] args) 
	{
		try
		{
			int pto = 1234;
			ServerSocket s = new ServerSocket(pto);
			System.out.println("Servicio iniciado...esperando cliente...");
			s.setReuseAddress(true);

			for(;;)
			{
				Socket cl = s.accept();
				cl.setSoLinger(true, 5000);
				System.out.println("Cliente conectado desde: " + cl.getInetAddress() + ":" + cl.getPort() + "\nPreparado para recibir archivo.");
				DataInputStream dis = new DataInputStream(cl.getInputStream());
				String name = dis.readUTF();
				System.out.println("Nombre del archivo a recibir: " + name);
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(name));
				long tam = dis.readLong();
				System.out.println("Tamano del archivo a recibir: " + tam + " bytes");
				long leidos = 0;
				int porciento = 0, n = 0;
				byte[] b = new byte[2000];

				while(leidos < tam)
				{
					n = dis.read(b);
					dos.write(b, 0, n);
					dos.flush();
					leidos += n;
					porciento = (int) ((leidos * 100) / tam);
					System.out.print("\rRecibido el: " + porciento + "%");
				}
				dos.close();
				dis.close();
				cl.close();
				System.out.println("\nArchivo recibido.\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
}