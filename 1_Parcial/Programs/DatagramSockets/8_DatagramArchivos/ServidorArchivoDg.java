import java.net.*;
import java.io.*;

public class ServidorArchivoDg
{
	public static void main(String[] args) 
	{
		try
		{
			DatagramSocket s = new DatagramSocket(1234);
			System.out.println("Servicio iniciado...esperando cliente...");

			for(;;)
			{
				DatagramPacket pName = new DatagramPacket(new byte[65535], 65535);
				s.receive(pName);
				System.out.println("Datagrama con el nombre del archivo recibido desde: " + pName.getAddress() + ":" + pName.getPort());
				String nameFile = new String(pName.getData(), 0, pName.getLength());
				System.out.println("Nombre del archivo a recibir: " + nameFile);

				DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
				s.receive(p);
				System.out.println("Datagrama recibido con archivo a recibir desde: " + p.getAddress() + ":" + p.getPort());
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(nameFile));
				ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
				DataInputStream dis = new DataInputStream(bais);

				long tam = p.getLength();
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
				dis.close();
				dos.close();
				System.out.println("\nArchivo recibido.\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
}