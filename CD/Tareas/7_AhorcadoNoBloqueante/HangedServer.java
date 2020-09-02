import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.util.Iterator;
import java.util.Random;

public class HangedServer
{
	public static void main(String[] args) 
	{
		int port = 9999;
		String[] easy_strings = {"home", "run", "stuff"};
		String[] medium_strings = {"running", "datagram", "stratocaster"};
		String[] hard_strings = {"flying away from you", "making up some noise", "rest in peace"};

		try
		{
			ServerSocketChannel server = ServerSocketChannel.open(); 
			server.configureBlocking(false);
			server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			server.socket().bind(new InetSocketAddress(port));
			Selector selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server running...");

			boolean firstTime = true;
			Random random = new Random();

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
						System.out.println();
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

						msg = msg.trim();
						int difficulty = Integer.parseInt(msg);
						System.out.println("Client choosed difficulty: " + difficulty);

						String word = null;
						if(difficulty == 1)
							word = easy_strings[random.nextInt(3)];
						else if(difficulty == 2)
							word = medium_strings[random.nextInt(3)];
						else if(difficulty == 3)
							word = hard_strings[random.nextInt(3)];
						
						System.out.println("Word: " + word);
						ByteBuffer bytebuffer1 = ByteBuffer.wrap(word.getBytes());
						socketchannel.write(bytebuffer1);
						socketchannel.close();
						continue;
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