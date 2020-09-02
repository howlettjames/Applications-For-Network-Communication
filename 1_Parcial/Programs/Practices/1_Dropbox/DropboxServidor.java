import java.net.*;
import java.io.*;

public class DropboxServidor
{
	public static void main(String[] args) 
	{
		long enviados = 0, tam, leidos = 0;
		int porciento = 0, n = 0, option = 0;
		byte[] b = new byte[2000];
		String carpetaServidorString = "C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Programs/Practices/1_Dropbox/CarpetaServidor/";
		String name;
		Socket cl, cl1;

		try
		{
			//Port 1234 is used for meta information
			ServerSocket s = new ServerSocket(1234);
			//Port 1235 is used to send and receive files
			ServerSocket s1 = new ServerSocket(1235);
			System.out.println("Servicio iniciado...esperando cliente...");
			s.setReuseAddress(true);
			s1.setReuseAddress(true);

			for(;;)
			{
				//Client clicks on bottom "Connect"
				cl = s.accept();
				cl.setSoLinger(true, 5000);
				System.out.println("Cliente conectado desde: " + cl.getInetAddress() + ":" + cl.getPort());
				File carpetaServidor = new File(carpetaServidorString);
				File[] files = carpetaServidor.listFiles();
				DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
				DataInputStream dis = new DataInputStream(cl.getInputStream());

				System.out.println("\nEnviando numero de archivos del directorio");
				dos.writeInt(files.length);
				dos.flush();
				System.out.println("Enviando lista de archivos del directorio");
				for(File f: files)
				{
					if(f.isDirectory())
						dos.writeUTF("../" + f.getName());	
					else
						dos.writeUTF(f.getName());
					dos.flush();
				}
				System.out.println("Lista de archivos del directorio enviada");
				//Waiting to receive an order of sending or receiving a file
				for(;;)
				{
					System.out.println("\nEsperando indicacion de archivo a enviar o recibir...");
					option = dis.readInt();
					//If option is equal to 1 then we will send a file
					if(option == 1)
					{
						name = dis.readUTF();
						System.out.println("Enviando: " + name);
						//Sending an entire directory
						if(name.charAt(0) == '.')
						{
							name = name.substring(3);
							System.out.println("Enviando: " + name);
							for(File f: files)
							{
								if(f.getName().equals(name))
								{
									//Directory found
									File[] filesDirectory = f.listFiles();
									dos.writeInt(filesDirectory.length);	
									dos.flush();
									System.out.println("Numero de archivos de la carpeta a enviar: " + filesDirectory.length);
									for(File fi: filesDirectory)
									{
										cl1 = s1.accept();
										cl1.setSoLinger(true, 5000);
										System.out.println("Cliente conectado desde: " + cl1.getInetAddress() + ":" + cl1.getPort());
										DataOutputStream dos1 = new DataOutputStream(cl1.getOutputStream());
										dos1.writeUTF(fi.getName());
										dos1.flush();
										System.out.println("Nombre del archivo a enviar: " + fi.getName());
										DataInputStream dis1 = new DataInputStream(new FileInputStream(carpetaServidorString + "/" + f.getName() + "/" + fi.getName()));

										enviados = 0;
										porciento = 0;
										n = 0;
										tam = fi.length();
										dos1.writeLong(tam);
										dos1.flush();
										System.out.println("Tamano del archivo a enviar: " + tam);
										while(enviados < tam)
										{
											n = dis1.read(b);
											dos1.write(b, 0, n);
											dos1.flush();
											enviados += n;
											porciento = (int) ((enviados * 100) / tam);
											System.out.print("\rEnviado el: " + porciento + "%");
										}
										System.out.println();
										dos1.close();
										dis1.close();
										cl1.close();		
									}
									break;	
								}
							}							
						}
						//Sending one file
						else
						{
							cl1 = s1.accept();
							cl1.setSoLinger(true, 5000);
							System.out.println("Cliente conectado desde: " + cl1.getInetAddress() + ":" + cl1.getPort());
							DataInputStream dis1 = new DataInputStream(new FileInputStream(carpetaServidorString + "/" + name));
							DataOutputStream dos1 = new DataOutputStream(cl1.getOutputStream());

							for(File f: files)
							{
								if(f.getName().equals(name))
								{
									tam = f.length();
									dos1.writeLong(tam);
									dos1.flush();
									System.out.println("Tamano del archivo a enviar enviado");
									enviados = 0;
									porciento = 0;
									n = 0;
									while(enviados < tam)
									{
										n = dis1.read(b);
										dos1.write(b, 0, n);
										dos1.flush();
										enviados += n;
										porciento = (int) ((enviados * 100) / tam);
										System.out.print("\rEnviado el: " + porciento + "%");
									}
									System.out.println();
									break;
								}
							}
							dos1.close();
							dis1.close();
							cl1.close();
						}
					}
					//Else, if option is equal to 2, then we will receive a file
					else if(option == 2)
					{	
						String fileName = dis.readUTF();
						System.out.println("File/Directory to be received: " + fileName);
						//Receiving an entire directory
						if(fileName.charAt(0) == '.')
						{
							fileName = fileName.substring(3);
							File directoryFile = new File(carpetaServidorString + fileName);
							directoryFile.mkdir();
							int filesToBeReceived = dis.readInt();
							System.out.println("Number of files to be received: " + filesToBeReceived);
							for(int i = 0; i < filesToBeReceived; i++)
							{
								cl1 = s1.accept();
								cl1.setSoLinger(true, 5000);
								System.out.println("\nCliente conectado desde: " + cl1.getInetAddress() + ":" + cl1.getPort());
								DataInputStream dis1 = new DataInputStream(cl1.getInputStream());
								String newFileName = dis1.readUTF();
								System.out.println("Receiving file: " + newFileName);
								DataOutputStream dos1 = new DataOutputStream(new FileOutputStream(carpetaServidorString + fileName + "/" + newFileName));

								leidos = 0;
								n = 0;
								porciento = 0;
								tam = dis1.readLong();
								System.out.println("Receiving file of size: " + tam + " bytes");
								while(leidos < tam)
								{
									n = dis1.read(b);
									dos1.write(b, 0, n);
									dos1.flush();
									leidos += n;
									porciento = (int) ((leidos * 100) / tam);
									System.out.print("\rRecibido el: " + porciento + "%");
								}
								System.out.println();
								dos1.close();
								dis1.close();
								cl1.close();	
							}
						}
						//Receiving one file
						else
						{
							cl1 = s1.accept();
							cl1.setSoLinger(true, 5000);
							System.out.println("Cliente conectado desde: " + cl1.getInetAddress() + ":" + cl1.getPort());
							DataInputStream dis1 = new DataInputStream(cl1.getInputStream());
							DataOutputStream dos1 = new DataOutputStream(new FileOutputStream(carpetaServidorString + "/" + fileName));

							leidos = 0;
							n = 0;
							porciento = 0;
							tam = dis1.readLong();
							System.out.println("Receiving file of size: " + tam + " bytes");
							while(leidos < tam)
							{
								n = dis1.read(b);
								dos1.write(b, 0, n);
								dos1.flush();
								leidos += n;
								porciento = (int) ((leidos * 100) / tam);
								System.out.print("\rRecibido el: " + porciento + "%");
							}
							System.out.println();
							dos1.close();
							dis1.close();
							cl1.close();
						}	
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Cliente salio.");
		}	
	}
}