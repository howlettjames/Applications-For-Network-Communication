public class Forum implements java.io.Serializable
{
	short code;
	String name;
	String description;
	String date;
	String creator;
	int logFileSize;
	byte[] logFile;
	byte[] images;
	int[] imagesSizes;
	short noImages;
	int imagesTotalSize;

	public Forum(short code, String name, String description, String date, String creator)
	{
		this.code = code;
		this.name = name;
		this.description = description;
		this.date = date;
		this.creator = creator;	
	}

	public Forum(short code, String name, String description, String date, String creator, byte[] logFile, int logFileSize, byte[] images, short noImages, int[] imagesSizes, int imagesTotalSize)
	{
		this.code = code;
		this.name = name;
		this.description = description;
		this.date = date;
		this.creator = creator;
		this.logFileSize = logFileSize;
		this.logFile = new byte[logFileSize]; 						//Admit up to 10000 bytes of size for the log file
		System.arraycopy(logFile, 0, this.logFile, 0, logFileSize);
		this.imagesSizes = new int[noImages]; 				     		//Admit up to 100 images in a forum
		System.arraycopy(imagesSizes, 0, this.imagesSizes, 0, noImages);
		this.imagesTotalSize = imagesTotalSize;
		this.images = new byte[imagesTotalSize + 2000]; 				//+2000?
		System.arraycopy(images, 0, this.images, 0, imagesTotalSize);
	}
}