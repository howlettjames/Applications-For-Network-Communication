import java.net.*;
import java.io.*;
import java.util.Scanner;	
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class FourChanServer
{
	public static void main(String[] args) 
	{
		InetAddress destinyIP = null;
		DatagramSocket serverSendSocket, serverReceiveSocket;
		int serverPort = 1235;
		int clientPort = 1234;
		String serverDirString = "C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Exam/ServerDirectory";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		File serverDir = new File(serverDirString);

		try
		{
			serverReceiveSocket = new DatagramSocket(serverPort);
			serverSendSocket = new DatagramSocket();
			try
			{
				destinyIP = InetAddress.getByName("127.0.0.1");
			}
			catch(UnknownHostException ex)
			{
				System.out.println("IP not valid");
				System.exit(1);
			}
			System.out.println("Server running...\n");
			for(;;)
			{
				DatagramPacket dtgPacket = new DatagramPacket(new byte[10000], 10000);
				serverReceiveSocket.receive(dtgPacket);
				System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
				ByteArrayInputStream bais = new ByteArrayInputStream(dtgPacket.getData());
				ObjectInputStream ois = new ObjectInputStream(bais);

				Object o = (Object) ois.readObject();
				if(o instanceof Forum)
				{
					Forum forum = (Forum) o;
					//Code = 1, a new forum to be created
					if(forum.code == 1)
					{
						String[] foldersStrings = serverDir.list();
						String currentDate = new String(format.format(new Date()));
						boolean folderAlreadyExists = false;
						File currentFolder = null;
						for(String folder: foldersStrings)
							if(folder.equals(currentDate))
							{
								currentFolder = new File(serverDirString + "/" + currentDate);
								folderAlreadyExists = true;
								break;
							}

						if(!folderAlreadyExists)	
						{
							currentFolder = new File(serverDirString + "/" + currentDate);
							currentFolder.mkdir();
						}

						File newForum = new File(currentFolder.getPath() + "/" + forum.name + "-" + forum.creator);
						newForum.mkdir();
						//CREATING metainfo.txt
						FileWriter fileWriterForumMetaInfo = new FileWriter(newForum.getPath() + "/metainfo.txt");
						PrintWriter printWriterForumMetaInfo = new PrintWriter(fileWriterForumMetaInfo);
						printWriterForumMetaInfo.print(forum.name);
						printWriterForumMetaInfo.println();
						printWriterForumMetaInfo.print(forum.description);
						printWriterForumMetaInfo.println();
						printWriterForumMetaInfo.print(forum.date);
						printWriterForumMetaInfo.println();
						printWriterForumMetaInfo.print(forum.creator);
						printWriterForumMetaInfo.println();
						printWriterForumMetaInfo.close();
						fileWriterForumMetaInfo.close();

						//CREATING Images folder
						File imagesFolder = new File(newForum.getPath() + "/Images");
						imagesFolder.mkdir();

						//CREATING log.txt
						File logFile = new File(newForum.getPath() + "/log.txt");
						FileWriter fileWriter = new FileWriter(logFile, false);
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
						PrintWriter printWriter = new PrintWriter(bufferedWriter);
						printWriter.println("Hello, Welcome to this new Forum!");
						printWriter.close();
						//logFile.createNewFile();
					}
					//Code = 2, Client is searching by DATE
					else if(forum.code == 2)
					{
						String[] foldersStrings = serverDir.list();
						String[] forumsInFolder = null;
						int noFolders = 0;
						File folderSelected = null;  		//To avoid warnings of the compiler
						boolean folderFound = false;
						for(String folder: foldersStrings)
							if(folder.equals(forum.name))
							{
								folderSelected = new File(serverDirString + "/" + forum.name);
								forumsInFolder = folderSelected.list();
								for(String forumString: forumsInFolder)
									noFolders++;
								folderFound = true;
								break;
							}

						if(folderFound)
						{
							//Code = 4, sending number of folders/forums in folder with the DATE specified
							forum = new Forum((short) 4, noFolders + "", null, null, null);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(baos);
							oos.writeObject(forum);
							oos.flush();
							byte[] byteArray = baos.toByteArray();
							dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
							serverSendSocket.send(dtgPacket);
							oos.close();
							baos.close();

							//Sending each forum in the folder with the DATE specified
							for(String forumString: forumsInFolder)
							{	
								String[] nameAndCreator = forumString.split("-");
								//Code doesn't matters below
								forum = new Forum((short) 0, nameAndCreator[0], null, null, nameAndCreator[1]);
								baos = new ByteArrayOutputStream();
								oos = new ObjectOutputStream(baos);
								oos.writeObject(forum);
								oos.flush();
								byteArray = baos.toByteArray();
								dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
								serverSendSocket.send(dtgPacket);
								oos.close();
								baos.close();
							}
						}	
						else
						{
							//Code 5, folder with the DATE specified not found
							forum = new Forum((short) 5, null, null, null, null);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(baos);
							oos.writeObject(forum);
							oos.flush();
							byte[] byteArray = baos.toByteArray();
							dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
							serverSendSocket.send(dtgPacket);
							oos.close();
							baos.close();							
						}
					}
					//Code = 3, Client is searching by NAME
					else if(forum.code == 3)
					{
						String[] forumNameCreator = forum.name.split("-");
						File[] foldersByDate = serverDir.listFiles();
						int noForums = 0;
						List<String> forumsNamesList = new ArrayList<>();
						for(File folder: foldersByDate)
						{
							String[] forumsInFolder = folder.list();
							for(String forumString: forumsInFolder)
							{
								String[] forumStringNameCreator = forumString.split("-");
								if(forumStringNameCreator[0].equals(forumNameCreator[0]))
								{
									noForums++;
									forumsNamesList.add(forumString);
								}
							}
						}

						if(noForums == 0)
						{
							//Code 5, folder with the NAME specified not found
							forum = new Forum((short) 5, null, null, null, null);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(baos);
							oos.writeObject(forum);
							oos.flush();
							byte[] byteArray = baos.toByteArray();
							dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
							serverSendSocket.send(dtgPacket);
							oos.close();
							baos.close();							
						}
						else
						{
							//Code = 4, sending number of folders/forums in folder with the DATE specified
							forum = new Forum((short) 4, noForums + "", null, null, null);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(baos);
							oos.writeObject(forum);
							oos.flush();
							byte[] byteArray = baos.toByteArray();
							dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
							serverSendSocket.send(dtgPacket);
							oos.close();
							baos.close();

							for(String forumString: forumsNamesList)
							{
								String[] nameAndCreator = forumString.split("-");
								//Code doesn't matters below
								forum = new Forum((short) 0, nameAndCreator[0], null, null, nameAndCreator[1]);
								baos = new ByteArrayOutputStream();
								oos = new ObjectOutputStream(baos);
								oos.writeObject(forum);
								oos.flush();
								byteArray = baos.toByteArray();
								dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
								serverSendSocket.send(dtgPacket);
								oos.close();
								baos.close();	
							}
						}
					}
					//Code = 6, Client wants to open a Forum with the NAME and CREATOR specified
					else if(forum.code == 6)
					{
						File[] foldersByDate = serverDir.listFiles();
						boolean folderFound = false;
						for(File folder: foldersByDate)
						{
							File[] forumsInFolder = folder.listFiles();
							for(File forumFolder: forumsInFolder)
							{
								if(forumFolder.getName().equals(forum.name))
								{
									String[] forumNameAndCreator = forum.name.split("-");
									String forumName = forumNameAndCreator[0];
									String forumCreator = forumNameAndCreator[1];
									File metaFile = new File(forumFolder.getPath() + "/" + "metainfo.txt");
									Scanner scanner = new Scanner(metaFile);

									scanner.nextLine();
									String forumDescription = scanner.nextLine();
									scanner.close();
									String forumDate = folder.getName();

									try
									{
										File logFile = new File(forumFolder.getPath() + "/log.txt");
										DataInputStream dis = new DataInputStream(new FileInputStream(logFile.getPath()));
										ByteArrayOutputStream baos = new ByteArrayOutputStream();
										DataOutputStream dos = new DataOutputStream(baos);

										byte[] byteArray = new byte[2000];
										long sent = 0;
										int percentage = 0;
										int n = 0;
										int size = (int) logFile.length();
										int logFileSize = size; 		//Important, this value is used further to create the forum object
										while(sent < size)
										{
											n = dis.read(byteArray);
											dos.write(byteArray, 0, n);
											dos.flush();
											sent += n;
											percentage = (int) ((sent * 100) / size);
											System.out.print("\rWritten into byte array: " + percentage + "%");
										}
										System.out.println();
										byte[] logFileInBytes = baos.toByteArray(); //Important, this value is used further to create the forum object
										dos.close();
										baos.close();
										dis.close();

										//Finding number of images in folder Images of current Forum folder
										File imagesFolder = new File(forumFolder.getPath() + "/Images");
										File[] imagesInFolder = imagesFolder.listFiles();
										short noImagesInFolder = (short) imagesInFolder.length;	  		//Important: This value is used further to create the forum object
										int[] imagesSizes = new int[noImagesInFolder];   		//Important: This value is used further to create the forum object
										baos = new ByteArrayOutputStream();
										short i = 0;
										for(File imageFile: imagesInFolder)
										{
											dis = new DataInputStream(new FileInputStream(imageFile.getPath()));
											dos = new DataOutputStream(baos);

											byteArray = new byte[2000];
											sent = 0;
											percentage = n = 0;
											size = (int) imageFile.length();
											imagesSizes[i++] = size;   			//Important: This value is used further to create the forum object
											while(sent < size)
											{
												n = dis.read(byteArray);
												dos.write(byteArray, 0, n);
												dos.flush();
												sent += n;
												percentage = (int) ((sent * 100) / size);
												System.out.print("\rWritten into byte array: " + percentage + "%");
											}
											System.out.println();
											dos.close();
											dis.close();											
										}
										byte[] imagesInBytes = baos.toByteArray();
										int imagesTotalSize = imagesInBytes.length;      //Important: this value is used further to create the forum object
										baos.close();

										//Code = 7, sending FORUM object along with all its information (metainfo, log file and images)
										forum = new Forum((short) 7, forumName, forumDescription, forumDate, forumCreator, logFileInBytes, logFileSize, imagesInBytes, noImagesInFolder, imagesSizes, imagesTotalSize);
										baos = new ByteArrayOutputStream();
										ObjectOutputStream oos = new ObjectOutputStream(baos);
										oos.writeObject(forum);
										oos.flush();
										byteArray = baos.toByteArray();
										dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
										serverSendSocket.send(dtgPacket);
										oos.close();
										baos.close();
									}
									catch(Exception e)
									{
										e.printStackTrace();
									}
									folderFound = true;
									break;
								}
							}
						}
						if(!folderFound)
						{
							//Code = 5, FORUM specified not found
							forum = new Forum((short) 5, null, null, null, null);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(baos);
							oos.writeObject(forum);
							oos.flush();
							byte[] byteArray = baos.toByteArray();
							dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, clientPort);
							serverSendSocket.send(dtgPacket);
							oos.close();
							baos.close();							
						}
					}
					//Code = 7, Client wants to save a FORUM change
					else if(forum.code == 7)
					{
						//SEARCHING FOR DESIRED FORUM TO BE UPDATED
						String[] foldersStrings = serverDir.list();
						File currentFolder = null;
						for(String folder: foldersStrings)
							if(folder.equals(forum.date))
							{
								currentFolder = new File(serverDirString + "/" + forum.date);
								break;
							}

						File forumDir = new File(currentFolder.getPath() + "/" + forum.name + "-" + forum.creator);
						//UPDATING LOG FILE
						//File logFile = new File(forumDir.getPath() + "/log.txt");
						bais = new ByteArrayInputStream(forum.logFile);
						DataInputStream dis = new DataInputStream(bais);
						DataOutputStream dos = new DataOutputStream(new FileOutputStream(forumDir.getPath() + "/log.txt"));

						int size = forum.logFileSize;
						System.out.println("Log file size -> " + size + " bytes");
						int bytesRead = 0;
						//Max size of a logFile accepted by this program = 50000 bytes
						byte[] byteArray = new byte[50000];
						bytesRead = dis.read(byteArray, 0, size);
						dos.write(byteArray, 0, bytesRead);
						dos.flush();
						dos.close();
						dis.close();
						bais.close();

						//UPDATING IMAGES FOLDER
						File imagesFolder = new File(forumDir.getPath() + "/Images");
						bais = new ByteArrayInputStream(forum.images);
						for(short i = 0; i < forum.noImages; i++)
						{
							dis = new DataInputStream(bais);
							dos = new DataOutputStream(new FileOutputStream(imagesFolder.getPath() + "/" + i + ".jpg"));

							size = forum.imagesSizes[i];
							System.out.println("Image file size -> " + size + " bytes");
							bytesRead = 0;
							//Max size of an image to be accepted by this program = 50000 bytes
							byteArray = new byte[50000];
							bytesRead = dis.read(byteArray, 0, size);
							dos.write(byteArray, 0, bytesRead);
							dos.flush();
							dos.close();
							dis.close();
						}
						bais.close();						
					}
					ois.close();
					bais.close(); 
				}
				else
				{
					System.out.println("Object not instance of class Forum, ejecting...");
					System.exit(1);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}