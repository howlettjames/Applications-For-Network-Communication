import javax.swing.JFileChooser;
import java.io.*;
import java.net.*;

public class Envia
{
	public static void main(String[] args) 
	{
		try
		{
			int pto = 1234;
			String host = "127.0.0.1";
			JFileChooser jf = new JFileChooser();
			//Para seleccionar varios archivos:
			//jf.setMultiSelectionEnable(true);
			jf.requestFocus();
			int r = jf.showOpenDialog(null);

			if(r == JFileChooser.APPROVE_OPTION)
			{
				File f = jf.getSelectedFile();
				//File[] f = jf.getSelectedFiles();
				String nombre = f.getName();
				long tam = f.length();
				String path = f.getAbsolutePath();
				Socket cl = new Socket(host, pto);
				System.out.println("Se enviara el archivo: " + path + " que mide " + tam + " bytes.");
				DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
				DataInputStream dis = new DataInputStream(new FileInputStream(path));

				dos.writeUTF(nombre);
				dos.flush();
				dos.writeLong(tam);
				dos.flush();
				byte[] b = new byte[2000];
				long enviados = 0;
				int porciento = 0, n = 0;

				while(enviados < tam)
				{
					n = dis.read(b);
					dos.write(b, 0, n);
					dos.flush();
					enviados += n;
					porciento = (int) ((enviados * 100) / tam);
					System.out.print("\rTransmitido el: " + porciento + "%");
				}
				dis.close();
				dos.close();
				cl.close();
				System.out.println("\nArchivo enviado.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}