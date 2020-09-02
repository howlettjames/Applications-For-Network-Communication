import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.util.Iterator;

public class HangedServer
{
	public static void main(String[] args) 
	{
		int port = 9999;
		
		try
		{
			ServerSocketChannel server = ServerSocketChannel.open(); 
			server.configureBlocking(false);
			server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			server.socket().bind(new InetSocketAddress(port));
			Selector selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server running...");

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
						System.out.println("Cliente conectado desde: " + client.socket().getInetAddress().getHostAddress() + " : " + client.socket().getPort());
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
						continue;
					}
					if(selectionkey.isReadable())
					{
						SocketChannel socketchannel = (SocketChannel) selectionkey.channel();
						ByteBuffer bytebuffer = ByteBuffer.allocate(2000);

						bytebuffer.clear();
						int n = socketchannel.read(bytebuffer);
						bytebuffer.flip();
						String msg = new String(bytebuffer.array(), 0, n);

						if(msg.equalsIgnoreCase("Salir"))
						{
							System.out.println("Message received: " + msg + "\nClient closes connection...");
							socketchannel.close();
							continue;
						}
						else
						{
							System.out.println("Message received: " + msg + "\nSending back message...");
							String echo = "ECO_" + msg;
							ByteBuffer bytebuffer1 = ByteBuffer.wrap(echo.getBytes());
							socketchannel.write(bytebuffer1);
							continue;
						}
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