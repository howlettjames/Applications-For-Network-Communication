import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class ChatServer 
{
    public static void main(String[] args )
    {
        InetAddress mcGroup = null;
        int serverPort = 9876, clientPort = 9999, noClients = 0;
        int i = 0;
        MulticastSocket mcServerSocket;
        String msg;
        String[] clientLogString;
        byte[] byteArray;
        DatagramPacket dtgPacket;
        User user;
        List<User> usersList = new ArrayList<User>();
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        try
        {
            mcServerSocket = new MulticastSocket(serverPort);
            mcServerSocket.setReuseAddress(true);
            mcServerSocket.setTimeToLive(128);

            try
            {
                mcGroup = InetAddress.getByName("228.1.1.1");
            }
            catch(UnknownHostException u)
            {
                u.printStackTrace();
            }
            mcServerSocket.joinGroup(mcGroup);
            System.out.println("Server running...");
            for(;;)
            {
            	dtgPacket = new DatagramPacket(new byte[5000], 5000);
	            mcServerSocket.receive(dtgPacket);
	            System.out.println("Datagram received...");
	            msg = new String(dtgPacket.getData());
	            // -------------------------------- NEW CLIENT --------------------------------------
	            //If 1 a new client tries to connect
	            if(msg.charAt(0) == '1')
	            {
	            	//1 <opCode> <Port> <Client Name>
	            	clientLogString = msg.split("\\s+");
	               	user = new User(Integer.parseInt(clientLogString[1]), clientLogString[2].trim());
	            	usersList.add(user);
	            	System.out.println("Client discovered: " + dtgPacket.getAddress() + "\nClient's name: " + usersList.get(noClients).getUserName() + "\nClient's port: " + usersList.get(noClients).getUserPort());
	            	noClients++;
	            	//Sending Client the server info plus the number of other clients connected
	            	//2 <opCode> <Server Name> <Port> <Number of Clients connected>
	            	msg = "2 Server 9876 " + noClients;
		            byteArray = msg.getBytes();            
		            dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
		            mcServerSocket.send(dtgPacket);
		            System.out.println("\nSending msg: " + msg + " with TTL = "+ mcServerSocket.getTimeToLive());
		            //Sending each of the clients names and ports
		            for(User u: usersList)
		            {
		            	baos = new ByteArrayOutputStream();
		            	oos = new ObjectOutputStream(baos);

		            	System.out.println("Sending -> " + u.getUserName() + " : " + u.getUserPort());
		            	oos.writeObject(u);
		            	oos.flush();

		            	byteArray = baos.toByteArray();
						dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, user.getUserPort());
						mcServerSocket.send(dtgPacket);
						oos.close();
						baos.close();	
		            }
		            System.out.println("Clients sent");	
		            System.out.println();
	            }
	            // -------------------------------- CLIENT DISCONNECT --------------------------------------
	            //If 3 a client tries to disconnect
	            else if(msg.charAt(0) == '3')
	            {
	            	//3 <opCode> <Port> <Client Name>
	            	clientLogString = msg.split("\\s+");
	               	for(i = 0; i < usersList.size(); i++)
	               	{
	               		if(usersList.get(i).getUserName().compareTo(clientLogString[2].trim()) == 0)	
	               		{
	               			System.out.println("Client removed: " + dtgPacket.getAddress() + "\nClient's name: " + usersList.get(i).getUserName() + "\nClient's port: " + usersList.get(i).getUserPort());
	               			usersList.remove(i);
	               			break;
	               		}
	               	}
	               		
	            	noClients--;
	            	//Sending rest of clients: the server info plus the number of other clients still connected
	            	//2 <opCode> <Server Name> <Port> <Number of Clients connected>
	            	msg = "2 Server 9876 " + noClients;
		            byteArray = msg.getBytes();            
		            dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
		            mcServerSocket.send(dtgPacket);
		            System.out.println("\nSending msg: " + msg + " with TTL = "+ mcServerSocket.getTimeToLive());
		            //Sending each of the clients names and ports
		            for(User u: usersList)
		            {
		            	baos = new ByteArrayOutputStream();
		            	oos = new ObjectOutputStream(baos);

		            	System.out.println("Sending -> " + u.getUserName() + " : " + u.getUserPort());
		            	oos.writeObject(u);
		            	oos.flush();

		            	byteArray = baos.toByteArray();
						dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, Integer.parseInt(clientLogString[1]));
						mcServerSocket.send(dtgPacket);
						oos.close();
						baos.close();	
		            }
		            System.out.println("Clients sent");	
		            System.out.println();
	            }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();       
        }
    }
}