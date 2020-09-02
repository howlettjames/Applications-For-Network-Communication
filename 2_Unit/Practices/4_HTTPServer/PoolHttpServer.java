import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolHttpServer implements Runnable
{
	protected int port = 8000;
    protected ServerSocket serverSocket = null;
    protected boolean stop = false;
	protected ExecutorService pool = Executors.newFixedThreadPool(3);
	
	public PoolHttpServer(int port)
	{
		this.port = port;
	}	

	@Override
	public void run()
	{
		try
		{
			this.serverSocket = new ServerSocket(this.port);
			System.out.println("Pool server running...");
		}	
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Cannot initialize server in port: " + this.serverSocket.getLocalPort());	
		}

		stop = false;
		while(true)
		{
			Socket clientSocket = null;
			try
			{
				clientSocket = this.serverSocket.accept();
				System.out.println("Client connected -> " + clientSocket.getInetAddress() + " : " + clientSocket.getPort());
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
				System.out.println("Error: couldn't accept new connection");
				stop = true;
			}
			if(stop)
			{
				this.pool.shutdown();
				System.out.println("Pool server shutdown");	
			}	
			this.pool.execute(new HttpServer(clientSocket));
		}
	}

	public static void main(String[] args) 
    {
        PoolHttpServer poolHttpServer = new PoolHttpServer(8000);
        new Thread(poolHttpServer).start();    
    }
}

class HttpServer implements Runnable
{
	static final File WEB_ROOT = new File("WebRoot");
	static final File FILES_ROOT = new File("FilesRoot");
	static final File REQUESTS_FILE = new File("Requests.txt");
	static final String DEFAULT_FILE = "index.htm"; 
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	static final int PORT = 8000;
	static final boolean verbose = true;
	protected Socket connect;

	public HttpServer(Socket c)
	{
		connect = c;
	}

	/*
	public static void main(String[] args) 
	{
		try
		{
			ServerSocket serverConnect = new ServerSocket(PORT);	
			System.out.println("Server running...");

			while(true)
			{
				HttpServer myServer = new HttpServer(serverConnect.accept());

				if(verbose)
''					System.out.println("Connection opened.(" + new Date() + ")");

				Thread thread = new Thread(myServer);
				thread.start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	*/

	@Override
	public void run()
	{
		BufferedReader br = null;
		PrintWriter pw = null;
		BufferedOutputStream bos = null;
		String fileRequested = null;
		String parameters = null;
		PrintWriter pwRequests = null;

		try
		{
			br = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			pw = new PrintWriter(connect.getOutputStream());
			bos = new BufferedOutputStream(connect.getOutputStream());
			pwRequests = new PrintWriter(new BufferedWriter(new FileWriter(REQUESTS_FILE.getName(), true)));

			System.out.println("\nClient connected from :" + connect.getInetAddress() + " : " + connect.getPort());
			String input = br.readLine();
			if(input == null)
			{
				pw.print("<html><head><title>Servidor WEB");
				pw.print("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>");
				pw.print("</body></html>");
				return;
			}
			System.out.println("Request: " + input);
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			System.out.println("Method: " + method);
			String fileWithParams = parse.nextToken();
			System.out.println("File With Params: " + fileWithParams);
			StringTokenizer parseFileParams = new StringTokenizer(fileWithParams, "?");
			fileRequested = parseFileParams.nextToken();
			//Could be parameters or not
			if(parseFileParams.hasMoreElements())
			{
				parameters = parseFileParams.nextToken();
				System.out.println("Parameters: " + parameters);
			}

			// METHOD NOT SUPPORTED
			if(!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST") && !method.equals("DELETE"))
			{
				if(verbose)
					System.out.println("501 Not Implemented : " + method + " method.");

				sendResponseFile(METHOD_NOT_SUPPORTED, pw, bos);
			}
			else
			{
				if(method.equals("GET"))
				{
					//if there are no parameters then it is a file request
					if(parameters == null)
					{
						if(fileRequested.endsWith("/"))
							fileRequested += DEFAULT_FILE;

						sendResponseFile(fileRequested, pw, bos);
					}
					//If parameters then it is a data form request
					else
					{
						//WRITTING PARAMETERS TO REQUESTS_FILE
						pwRequests.println("GET\n" + new Date() + "\n" + parameters + "\n");

						//SENDING DEFAULT PAGE
						fileRequested = DEFAULT_FILE;
						sendResponseFile(fileRequested, pw, bos);
					}
				}
				else if(method.equals("POST"))
				{
					// READING HEADER FROM POSTMAN
					/*
					System.out.println(br.readLine());	
					System.out.println(br.readLine());	
					System.out.println(br.readLine());	
					System.out.println(br.readLine());	
					System.out.println(br.readLine());
					System.out.println(br.readLine());
					System.out.println(br.readLine());
					System.out.println(br.readLine());
					System.out.println(br.readLine());
					System.out.println(br.readLine());
					*/
					// READING HEADER FROM POSTMAN
					String[] contentTypeAndBoundary = br.readLine().split(";"); 					// Content-Type
					// We are treating with multiple data to be read i.e. files or parameters
					if(contentTypeAndBoundary[0].contains("multipart/form-data"))
					{
						// GETTING BOUNDARY
						/*
						int i = contentTypeAndBoundary[1].indexOf("-");
						String boundary = contentTypeAndBoundary[1].substring(i);
						System.out.println("Boundary: " + boundary);
						*/
						// READING REMAIN OF THE HEADER
						String connection = br.readLine();

						br.readLine();					// Cache-control
						br.readLine();					// Postman-Token
						br.readLine();					// User-Agent
						br.readLine();					// Accept
						br.readLine(); 					// Host
						br.readLine();					// Accept-encoding
						
						// GETTING CONTENT-LENGTH
						String contentLengthString = br.readLine(); 						// Content-length
						/*
						String[] contentLengthAndLength = contentLengthString.split(" ");
						int	contentLength = Integer.parseInt(contentLengthAndLength[1]);
						*/
						System.out.println("Type of connection: " + connection);					// Connection: keep-alive
						if(connection.contains("keep-alive"))
						{
							Scanner consoleScanner = new Scanner(System.in);
							System.out.println("Type a char: ");
							consoleScanner.nextLine();
							consoleScanner.close();
						}
						br.readLine();					//Blank line before actual content

						/*
						// READING PARAMETERS
						char[] cbuf = new char[contentLength + 10]; 	// 10 is a guard space
						int read = 0;
						read = br.read(cbuf, 0, contentLength);
						parameters = new String(cbuf, 0, read);
						System.out.println("Parameters: \n" + parameters);
						*/

						/*
						System.out.println("BEGIN SCAN");
						Scanner scanner = new Scanner(parameters);
						String line = null;
						while(scanner.hasNext())
						{
							line = scanner.nextLine();
							System.out.println(line);
							// FOUND A POST PARAMETER
							if(line.contains(boundary))
							{
								String[] contentDisposition = scanner.nextLine().split(";");
								// THE POST PARAMETER IS A FILE
								if(contentDisposition.length == 3)
								{
									// OBTAINING FILENAME FROM filename="FileName.txt"
									int j = contentDisposition[2].indexOf("=");
									String fileName = contentDisposition[2].substring(i + 1);
									int k = fileName.length();
									fileName = fileName.substring(0, k - 1);
									System.out.println("FILE NAME: " + fileName);

									// WRITTING PARAMETERS TO REQUESTS_FILE
									pwRequests.println("POST\n" + new Date() + "\n" + fileName + "\n");

									System.out.println(scanner.next());
									System.out.println(scanner.next());

									PrintWriter pwFile = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));					
									while(!scanner.hasNext(boundary))
										pwFile.print(scanner.next());

									pwFile.close();
								}
								else if(contentDisposition.length == 2)
								{

								}
							}

						}
						*/

						/*
						// WRITTING PARAMETERS TO REQUESTS_FILE
						pwRequests.println("POST\n" + new Date() + "\n" + parameters + "\n");
						*/

						// SENDING DEFAULT PAGE
						fileRequested = DEFAULT_FILE;
						sendResponseFile(fileRequested, pw, bos);	
					}
					else if(contentTypeAndBoundary[0].contains("x-www-form-urlencoded"))
					{
						// WRITTING PARAMETERS TO REQUESTS_FILE
						pwRequests.println("POST\n" + new Date() + "\n" + parameters + "\n");

						// SENDING DEFAULT PAGE
						fileRequested = DEFAULT_FILE;
						sendResponseFile(fileRequested, pw, bos);	
					}	
				}
				else if(method.equals("DELETE"))
				{
					File fileToDelete = new File(WEB_ROOT, fileRequested);
					if(fileToDelete.exists())
					{
						fileToDelete.delete();
						// SENDING DEFAULT PAGE
						fileRequested = DEFAULT_FILE;
						sendResponseFile(fileRequested, pw, bos);		
					}
					else
						throw new FileNotFoundException();
				}
				else if(method.equals("HEAD"))
				{					
					if(fileRequested.endsWith("/"))
						fileRequested += DEFAULT_FILE;

					sendResponseFileHEAD(fileRequested, pw);
					// SENDING DEFAULT PAGE
					fileRequested = DEFAULT_FILE;
					sendResponseFile(fileRequested, pw, bos);		
				}
			}
		}
		catch(FileNotFoundException fnfe)
		{
			try
			{
				fileNotFound(pw, bos, fileRequested);	
			}
			catch(IOException ioe)
			{
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bos.close();
				pw.close();
				br.close();
				pwRequests.close();
				connect.close();
			}
			catch(Exception e)
			{
				System.err.println("Error closing stream : " + e.getMessage());
			}

			if(verbose)
				System.out.println("Connection closed.\n");
		}
	}

	private void sendResponseFileHEAD(String fileRequested, PrintWriter pw) throws IOException
	{
		File file = new File(WEB_ROOT, fileRequested);
		int fileLength = (int) file.length();
		String contentMimeType = getContentType(fileRequested);
		byte[] fileData = readFileData(file, fileLength);

		pw.println("HTTP/1.1 200 OK");
		pw.println("Server: Java HTTP Server from James : 1.0");				
		pw.println("Date: " + new Date());
		pw.println("Content-type: " + contentMimeType);
		pw.println("Content-length: " + fileLength);
		pw.println(); 			//IMPORTANT: Blank line between header and content
		pw.flush();	

		if(verbose)
			System.out.println("Header of file: " + fileRequested + " of type " + contentMimeType + " returned");
	}

	private void sendResponseFile(String fileRequested, PrintWriter pw, BufferedOutputStream bos) throws IOException
	{
		File file = new File(WEB_ROOT, fileRequested);
		int fileLength = (int) file.length();
		String contentMimeType = getContentType(fileRequested);
		byte[] fileData = readFileData(file, fileLength);

		if(fileRequested.equals(METHOD_NOT_SUPPORTED))
			pw.println("HTTP/1.1 501 Not Implemented");			
		else if(fileRequested.equals(FILE_NOT_FOUND))
			pw.println("HTTP/1.1 404 File Not Found");
		else
			pw.println("HTTP/1.1 200 OK");

		pw.println("Server: Java HTTP Server from James : 1.0");				
		pw.println("Date: " + new Date());
		pw.println("Content-type: " + contentMimeType);
		pw.println("Content-length: " + fileLength);
		pw.println(); 			//IMPORTANT: Blank line between header and content
		pw.flush();	

		bos.write(fileData, 0, fileLength);
		bos.flush();

		if(verbose)
			System.out.println("File " + fileRequested + " of type " + contentMimeType + " returned");
	}

	private String getContentType(String fileRequested)
	{
		if(fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html"; 
		else if(fileRequested.endsWith(".jpg") || fileRequested.endsWith(".png"))
			return "image/ief";
		else if(fileRequested.endsWith(".pdf"))
			return "application/pdf";
		else
			return "text/plain";
	}

	private byte[] readFileData(File file, int fileLength) throws IOException
	{
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try
		{
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);	
		}
		finally
		{
			if(fileIn != null)
				fileIn.close();
		}

		return fileData; 
	}

	private void fileNotFound(PrintWriter pw, BufferedOutputStream bos, String fileRequested) throws IOException
	{
		sendResponseFile(FILE_NOT_FOUND, pw, bos);

		if(verbose)
			System.out.println("File " + fileRequested + " not found");
 	}
 }