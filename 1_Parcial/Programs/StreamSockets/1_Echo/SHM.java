import java.net.*;
import java.io.*;

public class SHM
{
	public static void main(String[] args)
	{
		try
		{
			int pto = 9000;
			ServerSocket s = new ServerSocket(pto);
			System.out.println("Servicio iniciado...esperando cliente...");
			s.setReuseAddress(true);

			for(;;)
			{
				Socket cl = s.accept();
				cl.setSoLinger(true, 5000);
				System.out.println("Cliente conectado desde: " + cl.getInetAddress() + ":" + cl.getPort() + "\nPreparado para enviar mensaje");
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
				BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
				for(;;)
				{
					String msj = br.readLine();
					if(msj.compareToIgnoreCase("salir") == 0)
					{
						pw.close();
						br.close();
						cl.close();
						System.out.println("Cliente cerro la conexion\n");
						break;		
					}	
					else
					{
						pw.println(msj + " ECO");	
						pw.flush();	
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}	
} 