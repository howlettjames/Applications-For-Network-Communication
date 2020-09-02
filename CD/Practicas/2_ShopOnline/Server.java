import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server
{
	public static void main(String[] args) 
	{
		DatagramPacket p;
		DatagramSocket s, cl;
		File catalogFile, catalogAuxFile;
		File[] images = new File[3];
		Scanner scanner;
		short id;
		String name;
		String description;
		String classification;
		double price;
		short stock;
		DataOutputStream dos;
		DataInputStream dis;
		ByteArrayOutputStream baos, baos1;
		ByteArrayInputStream bais;
		ObjectOutputStream oos;
		ObjectInputStream ois;
		byte[] b, imagesInBytes;
		long sent, size;
		long[] imageSizes = new long[3];
		int percentage, n, i, j, noUpdated;
		Article article;
		String msg;
		Object o;
		PrintWriter printWriter;
		String[] imagesString = new String[3];

		try
		{
			s = new DatagramSocket(1234);
			cl = new DatagramSocket();
			System.out.println("Server running...");
			for(;;)
			{
				p = new DatagramPacket(new byte[10], 10);
				s.receive(p);
				System.out.println("Datagram received from: " + p.getAddress() + ":" + p.getPort());
				msg = new String(p.getData(), 0, p.getLength());
				System.out.println("Client says: " + msg);
				
				InetAddress dst = null;
				try
				{
					dst = InetAddress.getByName("127.0.0.1");
				}
				catch(UnknownHostException ex)
				{
					System.out.println("IP not valid");
					System.exit(1);
				}
			// -------------------------------------------------------- SENDING CATALOG ---------------------------------------------//
				catalogFile = new File("Catalog.txt");
				scanner = new Scanner(catalogFile);
				for(i = 0; i < 6; i++)
				{
					id = scanner.nextShort();
					System.out.println("ID: " + id);
					//Next line reads '\n' left by previous read
					scanner.nextLine();
					name = scanner.nextLine();
					System.out.println("Name: " + name);
					description = scanner.nextLine();
					System.out.println("Description: " + description);
					classification = scanner.nextLine();
					System.out.println("Classification: " + classification);
					price = scanner.nextDouble();
					System.out.println("Price: " + price);
					stock = scanner.nextShort();
					System.out.println("Stock: " + stock);
					//Next line reads '\n' left by previous read
					scanner.nextLine();
					images[0] = new File(scanner.nextLine());
					images[1] = new File(scanner.nextLine());
					images[2] = new File(scanner.nextLine());
					baos = new ByteArrayOutputStream();
					for(j = 0; j < 3; j++)
					{
						dis = new DataInputStream(new FileInputStream(images[j].getAbsolutePath()));
						dos = new DataOutputStream(baos);

						size = images[j].length();
						imageSizes[j] = size;
						System.out.println("Image: " + images[j].getName() + " size: " + imageSizes[j]);
						b = new byte[2000];
						sent = 0;
						percentage = n = 0;
						while(sent < size)
						{
							n = dis.read(b);
							dos.write(b, 0, n);
							dos.flush();
							sent += n;
							percentage = (int) ((sent * 100) / size);
							System.out.print("\rImage written into byte array: " + percentage + "%");
						}
						System.out.println();
						dos.close();
						dis.close();	
					}
					imagesInBytes = baos.toByteArray();
					baos.close();	

					System.out.println("Total size of images: " + imagesInBytes.length);
					article = new Article(id, name, description, classification, price, stock, imagesInBytes, imagesInBytes.length, imageSizes);
					baos1 = new ByteArrayOutputStream();
					oos = new ObjectOutputStream(baos1);

					oos.writeObject(article);
					oos.flush();

					b = baos1.toByteArray();
					p = new DatagramPacket(b, b.length, dst, 1235);
					cl.send(p);
					oos.close();
					baos1.close();	
					System.out.println();
				}
				scanner.close();
				System.out.println("|+|==> Catalog sent.");	

			// ------------------------------------------------- TREATING WITH PURCHASE AND CART SAVE --------------------------------//	
				//Waiting for order of purchase or cart save
				p = new DatagramPacket(new byte[10], 10);
				s.receive(p);
				System.out.println("Datagram received from: " + p.getAddress() + ":" + p.getPort());
				msg = new String(p.getData(), 0, p.getLength());
				System.out.println("Client says: " + msg);
				if(msg.equals("Purchase"))
				{
					//Waiting for number of items to be updated
					p = new DatagramPacket(new byte[10], 10);
					s.receive(p);
					System.out.println("Datagram received from: " + p.getAddress() + ":" + p.getPort());
					noUpdated = Short.parseShort(new String(p.getData(), 0, p.getLength()));
					System.out.println("Number of Items to be updated: " + noUpdated);
					for(i = 0; i < noUpdated; i++)
					{
						p = new DatagramPacket(new byte[65535], 65535);
						s.receive(p);
						System.out.println("\nDatagram received from: " + p.getAddress() + ":" + p.getPort());
						bais = new ByteArrayInputStream(p.getData());
						ois = new ObjectInputStream(bais);

						o = ois.readObject();
						if(o instanceof Article)
						{
							article = (Article) o;
							catalogFile = new File("Catalog.txt");
							scanner = new Scanner(catalogFile);
							catalogAuxFile = new File("CatalogAux.txt");
							printWriter = new PrintWriter(catalogAuxFile);
							for(j = 0; j < 6; j++)
							{
								//READING FROM CATALOG FILE
								id = scanner.nextShort();
								System.out.println("ID: " + id);
								//Next line reads '\n' left by previous read
								scanner.nextLine();
								name = scanner.nextLine();
								System.out.println("Name: " + name);
								description = scanner.nextLine();
								System.out.println("Description: " + description);
								classification = scanner.nextLine();
								System.out.println("Classification: " + classification);
								price = scanner.nextDouble();
								System.out.println("Price: " + price);
								stock = scanner.nextShort();
								System.out.println("Stock: " + stock);
								//Next line reads '\n' left by previous read
								scanner.nextLine();
								imagesString[0] = scanner.nextLine();
								imagesString[1] = scanner.nextLine();
								imagesString[2] = scanner.nextLine();
								//WRITING TO NEW FILE CATALOG1
								printWriter.print(id);
								printWriter.println();
								printWriter.print(name); 
								printWriter.println();
								printWriter.print(description); 
								printWriter.println();
								printWriter.print(classification);
								printWriter.println();
								printWriter.print(price);
								printWriter.println();
								if(j == (article.id - 1))
									printWriter.print(stock - article.stock);
								else
									printWriter.print(stock);
								printWriter.println();
								printWriter.print(imagesString[0]); 	    
								printWriter.println();
								printWriter.print(imagesString[1]); 	    
								printWriter.println();
								printWriter.print(imagesString[2]); 	    
								printWriter.println();
								printWriter.println();
							}
							printWriter.close();
							scanner.close();
							catalogFile.delete();
							catalogFile = new File("Catalog.txt");
							catalogAuxFile.renameTo(catalogFile);
						}	
						else
						{
							System.out.println("Item received not instance of Item");
							System.exit(1);
						}
						ois.close();
						bais.close();
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