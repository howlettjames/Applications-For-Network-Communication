import java.net.*;
import java.io.*;

public class CHM
{
	public static void main(String[] args) 
	{
		try
		{
			int pto = 9000;
			BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("\nEscribe la dir IP del servidor: ");
			String host = br1.readLine();
			Socket cl = new Socket(host, pto);
			System.out.println("\nConexion con el servidor establecida...escribe una cadena de texto <Enter> para enviar, \"salir\" para terminar");
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
			BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
			for(;;)
			{
				String msj = br1.readLine();
				pw.println(msj);
				pw.flush();
				if(msj.compareToIgnoreCase("salir") == 0)
				{
					System.out.println("Termina aplicacion");
					br1.close();
					br2.close();
					pw.close();
					cl.close();
					System.exit(0);
				}
				else
				{
					String eco = br2.readLine();
					System.out.println("Eco recibido: " + eco);
				}
			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}