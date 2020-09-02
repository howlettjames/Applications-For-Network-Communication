import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DisplayableUser
{
	public SimpleStringProperty user;
	public SimpleIntegerProperty port;

	public DisplayableUser()
	{

	}

	public DisplayableUser(String user, int port)
	{
		this.user = new SimpleStringProperty(user);
		this.port = new SimpleIntegerProperty(port);
	}

	public String getUser()
	{
		return user.get();
	}

	public void setUser(String user1)
	{
		user.set(user1);
	}

	public int getPort()
	{
		return port.get();
	}

	public void setPort(int port1)
	{
		port.set(port1);
	}
}