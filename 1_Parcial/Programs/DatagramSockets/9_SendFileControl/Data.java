public class Data implements java.io.Serializable
{
	int noSequence;
	byte[] bytes;
	int chunkSize;
	int total;
	String fName;

	public Data(int noSequence, byte[] bytes, int chunkSize, int total, String fName)
	{
		this.noSequence = noSequence;
		this.bytes = new byte[chunkSize];
		System.arraycopy(bytes, 0, this.bytes, 0, chunkSize);
		this.chunkSize = chunkSize;
		this.total = total;
		this.fName = fName;
	}
}