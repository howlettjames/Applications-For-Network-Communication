import java.net.*;
import java.io.*;

public class EchoCliente
{
	public static void main(String[] args) 
	{		
		int limite = 10;
		try
		{
			DatagramSocket cl = new DatagramSocket();
			DatagramSocket s = new DatagramSocket(1235);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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

			System.out.println("Escribe una cadena de texto <Enter> para enviar.Escribe \"salir\" para finalizar");
			for(;;)
			{
				String texto = br.readLine();
				if(texto.compareToIgnoreCase("salir") == 0)
				{
					cl.close();
					System.exit(1);
				}
				
				byte[] buf = texto.getBytes();
				if(buf.length > limite)
				{
					ByteArrayInputStream bais = new ByteArrayInputStream(buf);
					int n = 0;
					byte[] buf2 = new byte[100];
					while((n = bais.read(buf2)) != -1)
					{
						DatagramPacket p = new DatagramPacket(buf2, buf2.length, dst, 1234);
						cl.send(p);
					}
				}
				else
				{	
					DatagramPacket p = new DatagramPacket(buf, buf.length, dst, 1234);
					cl.send(p);
				}

				DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
				s.receive(p);
				String msj = new String(p.getData(), 0, p.getLength());
				System.out.println(msj + "-echo");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
