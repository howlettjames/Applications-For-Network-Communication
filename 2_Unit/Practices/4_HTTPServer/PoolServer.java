import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolServer implements Runnable
{
    protected int port = 9000;
    protected ServerSocket serverSocket = null;
    protected boolean stopped = false;
    // protected Thread runningThread = null;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);

    public PoolServer(int port)
    {
        this.port = port;
    }

    @Override
    public void run()
    {
        /*
        synchronized(this)
        {
            this.runningThread = Thread.currentThread();
        }
        */
        initializeServer();
        while(!isStopped())
        {
            Socket clientSocket = null;
            try
            {
                clientSocket = this.serverSocket.accept();
                System.out.println("Connection accepted...");
            }
            catch(IOException e)
            {
                if(isStopped())
                {
                    System.out.println("Server stopped");
                    break;
                }
                throw new RuntimeException("Error: Couldn't accept new connection", e);
            }
            this.pool.execute(new Manager(clientSocket));
        }
        this.pool.shutdown();
        System.out.println("Server shutdown");
    }

    private synchronized boolean isStopped()
    {
        return this.stopped;
    }    

    public synchronized void stop()
    {
        this.stopped = true;
        try
        {
            this.serverSocket.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException("Error: Cannot close server socket", e);
        }
    }
    private void initializeServer()
    {
        try
        {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("Server running...");
        }
        catch(Exception e)
        {
            throw new RuntimeException("Cannot initialize server in port: " + serverSocket.getLocalPort(), e);
        }
    }

    public static void main(String[] args) 
    {
        PoolServer poolServer = new PoolServer(9000);
        new Thread(poolServer).start();    
    }
}

class Manager implements Runnable
{
    protected Socket clientSocket = null;

    public Manager(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        try
        {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(true)
            {
                String line = br.readLine();
                if(line.compareToIgnoreCase("exit") == 0)
                {
                    br.close();
                    pw.close();
                    clientSocket.close();
                    break;
                }
                else
                {
                    System.out.println("Client connected from: " + clientSocket.getInetAddress() + " : " + clientSocket.getPort());
                    System.out.println("Message: " + line + "\n");
                    pw.println(line);
                    pw.flush();
                }
            }
            System.out.println("Client dispatched");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }        
    }
}