public class ExampleServer
{
	public static void main(String[] args) 
	{
		while(true)
		{
			DatagramPacket p = new DatagramPacket(new byte[1500], 1500);
			s.receive(p);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrrayInputStream(p.getBytes()));
			Datos d1 = (Datos) ois.readObject();
		}	
	}
}