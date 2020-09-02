public class Article implements java.io.Serializable
{
	short id;
	String name;
	String description;
	String classification;
	double price;
	short stock;
	byte[] images;
	int imagesSize;
	long[] imageSizes = new long[3];

	public Article (short id, String name, String description, String classification, double price, short stock, byte[] images, int imagesSize, long[] imageSizes)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.classification = classification;
		this.price = price;
		this.stock = stock;
		this.images = new byte[imagesSize + 5000];
		System.arraycopy(images, 0, this.images, 0, imagesSize);
		System.arraycopy(imageSizes, 0, this.imageSizes, 0, 3);
	}

	public Article (short id, String name, String description, String classification, double price, short stock)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.classification = classification;
		this.price = price;
		this.stock = stock;
	}
}