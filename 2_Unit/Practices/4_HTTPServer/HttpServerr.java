import java.net.*;
import java.io.*;
import java.util.*;

public class HttpServerr
{
	public static final int PORT = 8000;
	ServerSocket serverSocker;

	public HttpServer() throws Exception
	{
		this.serverSocker = new ServerSocket(PORT);
		System.out.println("Server running...");
		for(;;)
		{
			Socket accept = serverSocker.accept();
			new Manager(accept).start();
		}
	}

	public static void main(String[] args) throws Exception
	{
		HttpServer httpServer = new HttpServer();			
	}
	
	class Manager extends Thread
	{
		protected Socket socket;
		protected PrintWriter printWriter;
		protected BufferedOutputStream bos;
		protected DataInputStream dis;
		BufferedReader br;

		public Manager(Socket _socket) throws Exception
		{
			this.socket = _socket;
		}

		public void run()
		{
			try
			{
				dis = new DataInputStream(socket.getInputStream());
				bos = new BufferedOutputStream(socket.getOutputStream());
				printWriter = new PrintWriter(new OutputStreamWriter(bos));
				
				byte[] byteArray = new byte[2000];
				int n = 0;
				int bytesRead = 0;
				while((n = dis.read(byteArray)) > 0)
					bytesRead += n;


				/*
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bos = new BufferedOutputStream(socket.getOutputStream());
				pw = new PrintWriter(new OutputStreamWriter(bos));
				String line=br.readLine();
				*/

				//Detected nothing
				if(bytesRead == 0)
				{
					printWriter.print("<html><head><title>Servidor WEB</title>");
					printWriter.print("<body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>");
					printWriter.print("</body></html>");
					printWriter.close();
					bos.close();
					dis.close();
					socket.close();
					return;
				}
				String request = new String(byteArray, 0, bytesRead);
				System.out.println("\nClient conected from: " + socket.getInetAddress() + ":" + socket.getPort());
				System.out.println("Request received: " + request);
				/*
				String[] lines = request.split("\n");
				request = lines[0];
				System.out.println("Request received: " + request);
				*/
				//Client didn't send parameters
                //Remember: GET /cosa.php?p1=v1&p2=v2... HTTP/1.0
				if(request.indexOf("?") == -1)
				{
					String fileName = getFile(request);
					if(fileName.compareTo("") == 0)
						sendFile("index.htm");
					else
						sendFile(fileName);

					System.out.println("File sent: " + fileName);
				}
				//Case there are parameters
				else if(request.toUpperCase().startsWith("GET"))
				{
					StringTokenizer tokens = new StringTokenizer(request, "?");
					String req_a = tokens.nextToken();
					String req = tokens.nextToken();
					System.out.println("Token 1: " + req_a + "\r\n\r\n");
					System.out.println("Token 2: " + req + "\r\n\r\n");
					printWriter.println("HTTP/1.0 200 Okay");
					printWriter.flush();
					printWriter.println();
					printWriter.flush();
					printWriter.print("<html><head><title>SERVIDOR WEB</title></head>");
					printWriter.flush();
					printWriter.print("<body bgcolor=\"#AACCFF\"><center><h1><br>Parameters...</br></h1>");
					printWriter.flush();
					printWriter.print("<h3><b>" + req + "</b></h3>");
					printWriter.flush();
					printWriter.print("</center></body></html>");
					printWriter.flush();
				}
				else
				{
					printWriter.println("HTTP/1.0 501 Not Implemented");
					printWriter.println();
					printWriter.flush();
				}
				printWriter.flush();
				bos.flush();
				//printWriter.close();
				//bos.close();
				//dis.close();
				//socket.close();
 			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public String getFile(String request)
		{
			int beginIndex, endIndex;
			String fileName = null;

			if(request.toUpperCase().startsWith("GET"))
			{
				beginIndex = request.indexOf("/");
				endIndex = request.indexOf(" ", beginIndex);
				fileName = request.substring(beginIndex + 1, endIndex);
			}

			return fileName;
		}

		public void sendFile(String arg)
		{
			try{
					 int b_leidos=0;
					 BufferedInputStream bis2=new BufferedInputStream(new FileInputStream(arg));
                     byte[] buf=new byte[1024];
                     int tam_bloque=0;
                     if(bis2.available()>=1024)
                     {
                        tam_bloque=1024;
                     }
                     else
                     {
                        bis2.available();
                     }
			
                     int tam_archivo=bis2.available();
		     /***********************************************/
				String sb = "";
				sb = sb+"HTTP/1.0 200 ok\n";
			        sb = sb +"Server: Axel Server/1.0 \n";
				sb = sb +"Date: " + new Date()+" \n";
				sb = sb +"Content-Type: text/html \n";
                                //sb = sb +"Content-Type: application/pdf \n"; Para PDF
                                //sb = sb +"Content-Type: image/jpeg \n"; Para Imagen
				sb = sb +"Content-Length: "+tam_archivo+" \n";
				sb = sb +"\n";

				System.out.println("Response: " + sb);
				bos.write(sb.getBytes());
				bos.flush();

				//out.println("HTTP/1.0 200 ok");
				//out.println("Server: Axel Server/1.0");
				//out.println("Date: " + new Date());
				//out.println("Content-Type: text/html");
				//out.println("Content-Length: " + mifichero.length());
				//out.println("\n");

		     /***********************************************/
			
                     while((b_leidos=bis2.read(buf,0,buf.length))!=-1)
                     {
                        bos.write(buf,0,b_leidos);
                        
                        
                     }
                     bos.flush();
                     bis2.close();
                     
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			/*
			try
			{	
				File file = new File(fileName);
				if(file.exists())
				{
					int bytesRead = 0;
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file.getName()));
					byte[] buffer = new byte[1024];	
					long fileSize = file.length();

					String stringResponse = "";
					stringResponse = stringResponse.concat("HTTP/1.0 200 OK\n");
					stringResponse = stringResponse.concat("Server: James Server/1.0 \n");
					stringResponse = stringResponse.concat("Date: " + new Date() + " \n");
					stringResponse = stringResponse.concat("Content-Type: text/html \n");
					stringResponse = stringResponse.concat("Content-Length: " + fileSize + " \n");
					stringResponse = stringResponse.concat("\n");

					System.out.println("Response: " + stringResponse);
					bos.write(stringResponse.getBytes());
					bos.flush();

					while((bytesRead = dis1.read(buffer)) != -1)
						bos.write(buffer, 0, bytesRead);

					bos.flush();
					bis.close();
				}
			}	
			catch(Exception e)
			{
				e.printStackTrace();
			}
			*/
		}
	}
}