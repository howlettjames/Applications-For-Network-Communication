import java.net.*;
import java.io.*;

public class Client
{
    public static void main(String[] args) 
    {
        try
        {
            int port = 9000;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket("127.0.0.1" , port);
            System.out.println("Connection established");
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader br1 = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(true)
            {
                System.out.println("Write a message <Enter> to send \"exit\" to finish");
                String msg = br.readLine();
                pw.println(msg);
                pw.flush();
                if(msg.compareToIgnoreCase("exit") == 0)    
                {
                    System.out.println("Finishing execution...");
                    br1.close();
                    pw.close();
                    clientSocket.close();
                    br.close();
                    break;
                }
                else
                    System.out.println("Echo received: " + br1.readLine());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }    
    }
}