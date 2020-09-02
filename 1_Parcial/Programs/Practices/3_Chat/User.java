public class User implements java.io.Serializable
{
	private String userName;
	private int userPort;

	public User(int userPort, String userName)
	{
		this.userName = userName;
		this.userPort = userPort;
	}

	public String getUserName()
	{
		return userName;
	}

	public int getUserPort()
	{
		return userPort;
	}

}