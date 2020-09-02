import java.net.*;
import java.io.*;

public class Server
{
	public static void main(String[] args) 
	{
		try
		{
			DatagramSocket s = new DatagramSocket(1234);
			DataOutputStream dos = null;
			DataInputStream dis = null;
			ByteArrayInputStream bais1 = null;
			Object o = null;	
			Data data = null;
			System.out.println("Server waiting for datagram(s)...");
			for(;;)
			{
				DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
				s.receive(p);
				System.out.println("Datagram received from: " + p.getAddress() + ":" + p.getPort());
				ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
				ObjectInputStream ois = new ObjectInputStream(bais);

				o = ois.readObject();
				if(o instanceof Data)
				{
					data = (Data) o;
					System.out.println("Data received ->" + "\nNumber of sequence: " + data.noSequence + "\nTotal of packets: " + data.total + "\nFile name: " + data.fName);	
					bais1 = new ByteArrayInputStream(data.bytes);
					dis = new DataInputStream(bais1);
					if(data.noSequence == 1)
						dos = new DataOutputStream(new FileOutputStream(data.fName));

					long tam = data.bytes.length;
					System.out.println("Size of file/chunk: " + tam + " bytes");
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
						System.out.print("\rWritten: " + porciento + "%");
					}
					System.out.println("\n");
					if(data.noSequence == data.total)
						dos.close();
				}
				else
				{
					System.out.println("Object received is not instace of class Data");
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
}