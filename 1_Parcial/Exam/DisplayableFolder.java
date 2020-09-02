import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DisplayableFolder
{
	public SimpleStringProperty folder;
	public SimpleStringProperty creator;

	public DisplayableFolder()
	{

	}

	public DisplayableFolder(String folder, String creator)
	{
		this.folder = new SimpleStringProperty(folder);
		this.creator = new SimpleStringProperty(creator);
	}

	public String getFolder()
	{
		return folder.get();
	}

	public void setFolder(String folder1)
	{
		folder.set(folder1);
	}

	public String getCreator()
	{
		return creator.get();
	}

	public void setCreator(String creator1)
	{
		creator.set(creator1);
	}
}