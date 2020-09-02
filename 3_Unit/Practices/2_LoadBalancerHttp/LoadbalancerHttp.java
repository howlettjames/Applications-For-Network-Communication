import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.util.Iterator;
import java.util.Date;
import java.util.Random;
import java.io.*;
import java.net.*;

public class LoadBalancerHttp
{
	public static void main(String[] args) 
	{
		int port = 9999;
		int[] serverPorts = {8001, 8002, 8081};
		int roundRobinCounter = 0;
		Random random = new Random();

		try
		{
			ServerSocketChannel server = ServerSocketChannel.open(); 
			server.configureBlocking(false);
			server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			server.socket().bind(new InetSocketAddress(port));
			Selector selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server Balancer running on port " + port + "...");

			while(true)
			{
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

				while(iterator.hasNext())
				{
					SelectionKey selectionkey = (SelectionKey) iterator.next();
					iterator.remove();

					if(selectionkey.isAcceptable())
					{
						SocketChannel client = server.accept();
						System.out.println("Client conected to Balancer from: " + client.socket().getInetAddress().getHostAddress() + " : " + client.socket().getPort());
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);

						roundRobinCounter++;
						System.out.println("Round Robin: " + roundRobinCounter);
						continue;
					}
					if(selectionkey.isReadable())
					{
						SocketChannel socketchannel = (SocketChannel) selectionkey.channel();
						ByteBuffer bytebuffer = ByteBuffer.allocate(2000);

						bytebuffer.clear();
						int n = socketchannel.read(bytebuffer);
						bytebuffer.flip();

						byte[] byteArray = bytebuffer.array();

						// int serverPort = serverPorts[roundRobinCounter % serverPorts.length];
						int serverPort = serverPorts[random.nextInt(3)];
						System.out.println("Server Port: " + serverPort);
						Socket cl = new Socket("localhost", serverPort);
						System.out.println("\nConnection with the server on port " + serverPort + " established...sending data from Google...");

						DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
						//BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));	
						DataInputStream dis = new DataInputStream(cl.getInputStream());

						dos.write(byteArray);
						dos.flush();
						System.out.println("Data sent to server");

						System.out.println("Data retrieved from server and sending to client...");
						byte[] byteArrayResponse = new byte[10000];
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos1 = new DataOutputStream(baos);
						while((n = dis.read(byteArrayResponse)) > 0)
						{
							dos1.write(byteArrayResponse, 0, n);
							dos1.flush();
						}
						System.out.println("Data stored into buffer");
						byte[] responseInBytes = baos.toByteArray();
						dos1.close();
						baos.close();
						dis.close();
						dos.close();
						cl.close();

						System.out.println("Data prepared to be sent to client");
						ByteBuffer bytebuffer1 = ByteBuffer.wrap(responseInBytes);
						socketchannel.write(bytebuffer1);
						System.out.println("Data sent to client");

						socketchannel.close();
						continue;

						// String msg = new String(bytebuffer.array(), 0, n);

						// if(msg.equalsIgnoreCase("Salir"))
						// {
						// 	System.out.println("Message received: " + msg + "\nClient closes connection...");
						// 	socketchannel.close();
						// 	continue;
						// }
						// else
						// {
						// 	System.out.println("Message received: " + msg + "\nSending back message...");
						// 	String echo = "ECO_" + msg;
						// 	ByteBuffer bytebuffer1 = ByteBuffer.wrap(echo.getBytes());
						// 	socketchannel.write(bytebuffer1);
						// 	continue;
						// }
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