public class Example
{
	public static void main(String[] args) 
	{
		byte[] b1 = cadena.getBytes();
		if(b1.length > 1200)
		{
			//Implementar con archivos y con echo
			//Parte de la lectura de la consola
			byte[] b2 = new byte[1200];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(b1);

			int n = 0, c = 0; //C es el número de parte
			int np = (int) (b1.length / b2.length);
			if(b1.length % b2.length > 0)
				np++;
			while(c < np)
			{
				n = bais.read(b2);
				Datos d = new Datos(c + 1, b2, np);
				oos.writeObject(d);
				oos.flush();
				byte[] tmp = baos.toByteArray();
				DatagramPacket dp = new DatagramPacket(tmp., tmp.length, dst, pto);
				cl.send();
				c++;
			}
		}	
	}
}