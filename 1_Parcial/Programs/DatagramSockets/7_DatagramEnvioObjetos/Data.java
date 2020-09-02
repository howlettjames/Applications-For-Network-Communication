public class Data implements java.io.Serializable
{
	int noSequence;
	byte[] bytes;
	int total;

	public Data(int noSequence, byte[] bytes, int total)
	{
		this.noSequence = noSequence;
		this.bytes = bytes.clone();
		this.total = total;
	}
}