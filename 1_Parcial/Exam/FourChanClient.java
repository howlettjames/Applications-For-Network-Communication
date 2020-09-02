// ------- JAVAFX LIBRARIES
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
// ------- TableView
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
//----- Scroll Pane and Bar
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
//----- File Chooser
import javafx.stage.FileChooser;
//----- Web View and Engine
import javafx.scene.web.*;
//----- JavaFX Threads
import javafx.application.Platform;
//----- Radio Buttons
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
// -------- UTIL LIBRARIES
import java.util.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
// ------- MULTICAST AND NET LIBRARIES
import java.net.*;
import java.net.MulticastSocket;
// ------- JAVASCRIPT LIBRARIES
import netscape.javascript.JSObject;

public class FourChanClient extends Application 
{
	String thisUserName = null;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	Forum forum;
	DatagramSocket clientReceiveSocket, clientSendSocket;
	InetAddress destinyIP = null;
	int serverPort = 1235;
	int clientPort = 1234;
    ObservableList<DisplayableFolder> foldersObservableList; //is Abstract
    List<DisplayableFolder> foldersDisplayableList = new ArrayList<DisplayableFolder>();
    PrintWriter printWriter;
    short noImagesCurrentForum = 0;
    File imagesClientFolder = null;
    File fileHtml = new File("C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Exam/chat.html");

	@Override
	public void start(Stage primaryStage)
	{
		// ------------------------------------------------ LOGIN STAGE -------------------------------------- 
		VBox loginPane = new VBox(10);
		Label lbLoginUser = new Label("Write your user: ");
		TextField tfLoginUser = new TextField("");
		Button btLoginOk = new Button("OK");

		//Panes settings
		loginPane.setPadding(new Insets(10));
		loginPane.setAlignment(Pos.CENTER);
		tfLoginUser.setMaxWidth(200.0);

		//Putting nodes into nodes
		loginPane.getChildren().addAll(lbLoginUser, tfLoginUser, btLoginOk);

		Scene scene = new Scene(loginPane, 200, 200);
		scene.getStylesheets().add("controlStyle.css");
		primaryStage.setScene(scene);
		primaryStage.setTitle("LOGIN");
		primaryStage.show();

		// ------------------------------------------------ LOBBY STAGE -------------------------------------- 
		VBox lobbyPane = new VBox(10);
		HBox hbLobbyEndSession = new HBox();
		Button btLobbyCreateForum = new Button("Create Forum");
		Button btLobbySearchForum = new Button("Search Forum");
		Button btLobbyEndSession = new Button("End Session");

		//Panes settings
		lobbyPane.setPadding(new Insets(10));
		hbLobbyEndSession.setPadding(new Insets(10));
		lobbyPane.setAlignment(Pos.CENTER);
		hbLobbyEndSession.setAlignment(Pos.BOTTOM_RIGHT);

		//Nodes settings
		btLobbySearchForum.setPrefWidth(200.0);
		btLobbySearchForum.setPrefHeight(100.0);
		btLobbyCreateForum.setPrefWidth(200.0);
		btLobbyCreateForum.setPrefHeight(100.0);
		btLobbyEndSession.setId("btLobbyEndSession"); 			//To apply color orange to this button

		//Putting nodes into nodes
		hbLobbyEndSession.getChildren().add(btLobbyEndSession);
		lobbyPane.getChildren().addAll(btLobbyCreateForum, btLobbySearchForum, hbLobbyEndSession);

		Stage lobbyStage = new Stage();
		Scene lobbyScene = new Scene(lobbyPane, 300, 300);
		lobbyScene.getStylesheets().add("controlStyle.css");
		lobbyStage.setScene(lobbyScene);
		lobbyStage.setTitle("LOBBY");

		// ------------------------------------------------ CREATE STAGE -------------------------------------- 
		VBox createPane = new VBox(10);
		Label lbCreateName = new Label("Write the name: ");
		Label lbCreateDescription = new Label("Write the description: ");
		Label lbCreateDate = new Label("Date created: ");
		Label lbCreateCreator = new Label("Creator: ");
		TextField tfCreateName = new TextField("");
		TextField tfCreatDescription = new TextField("");
		String dateCreated = format.format(new Date());
		TextField tfCreateDate = new TextField(dateCreated);
		TextField tfCreateCreator = new TextField("");
		Button btCreateOk = new Button("OK");
		Button btCreateReturn = new Button("Return");

		//Panes settings
		createPane.setPadding(new Insets(10));
		createPane.setAlignment(Pos.CENTER);
		tfCreateDate.setEditable(false);
		tfCreateCreator.setEditable(false);
		btCreateReturn.setId("btLobbyEndSession"); 		//To apply color orange to this button

		//Putting panes into panes
		createPane.getChildren().addAll(lbCreateName, tfCreateName, lbCreateDescription, tfCreatDescription, lbCreateDate, tfCreateDate, lbCreateCreator, tfCreateCreator, btCreateOk, btCreateReturn);

		Stage createStage = new Stage();
		Scene createScene = new Scene(createPane, 400, 500);
		createScene.getStylesheets().add("controlStyle.css");
		createStage.setScene(createScene);
		createStage.setTitle("CREATE FORUM");

		// ------------------------------------------------ SEARCH STAGE -------------------------------------- 
		VBox searchPane = new VBox(10);
		HBox hbSearchInput = new HBox(30);
		HBox hbSearchChoose = new HBox(30);
		HBox hbSearchReturn = new HBox(10);
		TableView<DisplayableFolder> tableSearchFolders = new TableView<>();
		Label lbSearchWrite = new Label("Search the forum or date: ");
		Label lbSearchChoose = new Label("Write the forum <Forum-Creator>: ");
		TextField tfSearchWrite = new TextField();
		TextField tfSearchChoose = new TextField();
		Button btSearchOk = new Button("OK");
		Button btSearchWrite = new Button("Search");
		Button btSearchReturn = new Button("Return");
		TableColumn tbcSearchForumName = new TableColumn("Forum Name");
		TableColumn tbcSearchForumCreator = new TableColumn("Forum Creator");
		ToggleGroup tgSearchDateOrName = new ToggleGroup();
        RadioButton rbSearchDate = new RadioButton("Date");
        RadioButton rbSearchName = new RadioButton("Name");

		//Panes settings
		searchPane.setPadding(new Insets(10));
		hbSearchInput.setPadding(new Insets(10));
		hbSearchChoose.setPadding(new Insets(10));
		hbSearchReturn.setPadding(new Insets(10));
		hbSearchReturn.setAlignment(Pos.BOTTOM_RIGHT);

		//Nodes settings
		btSearchReturn.setId("btLobbyEndSession"); 		//To apply color orange to this button

		//Properties for table view
        tbcSearchForumName.setCellValueFactory(new PropertyValueFactory("folder"));
        tbcSearchForumCreator.setCellValueFactory(new PropertyValueFactory("creator"));
        tableSearchFolders.getColumns().setAll(tbcSearchForumName, tbcSearchForumCreator);
        tableSearchFolders.setPrefWidth(200);
        tableSearchFolders.setPrefHeight(300);
        tableSearchFolders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //Properties for toggle group
        rbSearchName.setToggleGroup(tgSearchDateOrName);
        rbSearchDate.setToggleGroup(tgSearchDateOrName);

        //Putting nodes into nodes
        hbSearchInput.getChildren().addAll(lbSearchWrite, tfSearchWrite, rbSearchDate, rbSearchName, btSearchWrite);
        hbSearchChoose.getChildren().addAll(lbSearchChoose, tfSearchChoose, btSearchOk);
        hbSearchReturn.getChildren().addAll(btSearchReturn);
        searchPane.getChildren().addAll(hbSearchInput, tableSearchFolders, hbSearchChoose, hbSearchReturn);

        Stage searchStage = new Stage();
        Scene searchScene = new Scene(searchPane, 770, 500);
        searchScene.getStylesheets().add("controlStyle.css");
        searchStage.setScene(searchScene);
        searchStage.setTitle("SEARCH FORUM");

        // ------------------------------------------------ FORUM STAGE -------------------------------------- 
        VBox forumPane = new VBox(10);
        HBox hbForumInfo = new HBox(40);
        HBox hbForumDescription = new HBox(10);
        HBox hbForumInputMessage = new HBox(10);
        HBox hbForumInputControls = new HBox(20);
        HBox hbForumExitReturnButtons = new HBox(20);
        WebView webView = new WebView();
    	WebEngine webEngine = webView.getEngine();
        webEngine.load(fileHtml.toURI().toString());
        webEngine.setJavaScriptEnabled(true); //IMPORTANT
        Label lbForumName = new Label("");
        Label lbForumCreator = new Label("");
        Label lbForumDate = new Label("");
        Label lbForumDescription = new Label("");
        Label lbForumMessage = new Label("Write: ");
        TextField tfForumMessage = new TextField();
        Button btForumMessageSend = new Button("Send Message");
        Button btForumImageSend = new Button("Send Image");
        Button btForumExit = new Button("Exit");
        Button btForumReturn = new Button("Return");
        FileChooser fileChooser = new FileChooser();
        
        //Panes settings
        forumPane.setPadding(new Insets(10));
        hbForumInfo.setPadding(new Insets(10));
        hbForumDescription.setPadding(new Insets(10));
        hbForumInputMessage.setPadding(new Insets(10));
        hbForumInputControls.setPadding(new Insets(10));
        hbForumExitReturnButtons.setPadding(new Insets(10));
        hbForumInfo.getStyleClass().add("hbox");
        hbForumDescription.getStyleClass().add("hbox");
        
        //Panes alignment
		hbForumExitReturnButtons.setAlignment(Pos.BOTTOM_RIGHT);

		//Nodes settings
		lbForumDescription.setWrapText(true);
		btForumReturn.setId("btLobbyEndSession"); 		//To set this button color to orange
		btForumExit.setId("btLobbyEndSession"); 		//To set this button color to orange
		lbForumName.setId("custom-label");
		lbForumCreator.setId("custom-label");
		lbForumDate.setId("custom-label");
		lbForumDescription.setId("custom-label");

        //Putting nodes into nodes
        hbForumInfo.getChildren().addAll(lbForumName, lbForumCreator, lbForumDate);
        hbForumDescription.getChildren().addAll(lbForumDescription);
        hbForumInputMessage.getChildren().addAll(lbForumMessage, tfForumMessage);
        hbForumInputControls.getChildren().addAll(btForumMessageSend, btForumImageSend);
        hbForumExitReturnButtons.getChildren().addAll(btForumReturn, btForumExit);
        forumPane.getChildren().addAll(hbForumInfo, hbForumDescription, webView, hbForumInputMessage, hbForumInputControls, hbForumExitReturnButtons);

        Stage forumStage = new Stage();
        Scene forumScene = new Scene(forumPane, 600, 600);
        forumScene.getStylesheets().add("controlStyle.css");
        forumStage.setScene(forumScene);
        forumStage.setTitle("FORUM");

		// ------------------------------------------------ ATTEMPTING TO CONNECT -------------------------------------- 
		// ;
		try
		{
			try
			{
				destinyIP = InetAddress.getByName("127.0.0.1");
			}
			catch(UnknownHostException ex)
			{
				System.out.println("IP not valid");
				System.exit(1);
			}
			clientReceiveSocket = new DatagramSocket(clientPort);
			clientSendSocket = new DatagramSocket();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// ------------------------------------------------ RETURN TO SEARCH AND SAVE FORUM -------------------------------------- 
		//;
		btForumReturn.setOnAction
		(
			e ->
			{
				try
				{
					//Log updater close
					printWriter.close();
					//Erasing table on WEB ENGINE
					webEngine.executeScript("deleteTable();");		
					//SAVING LOG FILE INTO BYTE ARRAY TO BE SEND INTO FORUM OBJECT
					File logFile = new File("ClientDirectory/log.txt");
					DataInputStream dis = new DataInputStream(new FileInputStream(logFile.getPath()));
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);

					byte[] byteArray = new byte[2000];
					long sent = 0;
					int percentage = 0;
					int n = 0;
					int size = (int) logFile.length();
					forum.logFileSize = size; 		//Important, this value is used further to create the forum object
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
					forum.logFile = baos.toByteArray(); //Important, this value is used further to create the forum object
					dos.close();
					baos.close();
					dis.close();

					//FINDING NUMBER OF IMAGES IN FOLDER IMAGES OF CURRENT FORUM FOLDER
					File imagesFolder = new File("ClientDirectory/Images");
					File[] imagesInFolder = imagesFolder.listFiles();
					forum.noImages = (short) imagesInFolder.length;	  		//Important: This value is used further to create the forum object
					forum.imagesSizes = new int[forum.noImages];   		//Important: This value is used further to create the forum object
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
						forum.imagesSizes[i++] = size;   			//Important: This value is used further to create the forum object
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
					forum.images = baos.toByteArray();
					forum.imagesTotalSize = forum.images.length;      //Important: this value is used further to create the forum object
					baos.close();

					//Code = 7, sending FORUM object along with all its information (log file and images)
					//In this particular case we dont create a nes FORUM object since we are using the same FORUM that was send from server
					forum.code = (short) 7;
					baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(forum);
					oos.flush();
					byteArray = baos.toByteArray();
					DatagramPacket dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, serverPort);
					clientSendSocket.send(dtgPacket);
					oos.close();
					baos.close();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				forumStage.close();
				searchStage.show();
			}
		);

		// ------------------------------------------------ SEND A MESSAGE -------------------------------------- 
		//;
		btForumMessageSend.setOnAction
		(
			e ->
			{
				webEngine.executeScript("putText('" + tfForumMessage.getText() + " - " + thisUserName + "');");		
				printWriter.println(tfForumMessage.getText() + " - " + thisUserName);
			}
		);

		// ------------------------------------------------ SEND AN IMAGE -------------------------------------- 
		//;
		btForumImageSend.setOnAction
		(
			e ->
			{
				File imageToSend = fileChooser.showOpenDialog(forumStage);
				if(imageToSend != null)
				{
					webEngine.executeScript("putImage('" + imageToSend.getPath() + "');");
					printWriter.println("FILE:" + (noImagesCurrentForum));
					try
					{
						DataInputStream dis = new DataInputStream(new FileInputStream(imageToSend.getPath()));
						File newImage = new File(imagesClientFolder.getPath() + "/" + (noImagesCurrentForum++) + ".jpg");
						DataOutputStream dos = new DataOutputStream(new FileOutputStream(newImage.getPath()));
						
						int size = (int) imageToSend.length();
						System.out.println("Image copy size -> " + size + " bytes");
						int bytesRead = 0;
						//Max size of an Image accepted by this program = 50000 bytes
						byte[] byteArray = new byte[50000];
						bytesRead = dis.read(byteArray, 0, size);
						dos.write(byteArray, 0, bytesRead);
						dos.flush();
						dos.close();
						dis.close();
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		);

		// ------------------------------------------------ OPEN A FORUM -------------------------------------- 
		//;
		btSearchOk.setOnAction
		(
			e ->
			{
				try
				{
					//Code = 6 i.e. we want the Forum with the NAME and CREATOR
					forum = new Forum((short) 6, tfSearchChoose.getText(), null, null, null);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(forum);
					oos.flush();
					byte[] byteArray = baos.toByteArray();
					DatagramPacket dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, serverPort);
					clientSendSocket.send(dtgPacket);
					oos.close();
					baos.close();

					//Receiving FORUM
					dtgPacket = new DatagramPacket(new byte[50000], 50000);
					clientReceiveSocket.receive(dtgPacket);
					System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
					ByteArrayInputStream bais = new ByteArrayInputStream(dtgPacket.getData());
					ObjectInputStream ois = new ObjectInputStream(bais);

					Object o = (Object) ois.readObject();
					if(o instanceof Forum)
					{
						forum = (Forum) o;
						//Code = 7, FORUM received
						if(forum.code == 7)
						{
							lbForumName.setText("Forum Name: " + forum.name);
							lbForumDate.setText("Date: " + forum.date);
							lbForumCreator.setText("Creator: " + forum.creator);
							lbForumDescription.setText("Description: " + forum.description);

							//READING LOG FILE
							bais = new ByteArrayInputStream(forum.logFile);
							DataInputStream dis = new DataInputStream(bais);
							DataOutputStream dos = new DataOutputStream(new FileOutputStream("ClientDirectory/log.txt"));

							int size = forum.logFileSize;
							System.out.println("Log file size -> " + size + " bytes");
							int bytesRead = 0;
							//Max size of a logFile accepted by this program = 50000 bytes
							byteArray = new byte[50000];
							bytesRead = dis.read(byteArray, 0, size);
							dos.write(byteArray, 0, bytesRead);
							dos.flush();
							dos.close();
							dis.close();
							bais.close();

							//READING IMAGES
							imagesClientFolder = new File("ClientDirectory/Images");
							imagesClientFolder.mkdir();
							bais = new ByteArrayInputStream(forum.images);
							for(short i = 0; i < forum.noImages; i++)
							{
								dis = new DataInputStream(bais);
								dos = new DataOutputStream(new FileOutputStream(imagesClientFolder.getPath() + "/" + i + ".jpg"));

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

							//SENDING LOG FILE TO WEB ENGINE
							File logFile = new File("ClientDirectory/log.txt");
							Scanner scanner = new Scanner(logFile);
							String line;
							noImagesCurrentForum = 0;
							webEngine.executeScript("createTable();");
							while(scanner.hasNext())
							{
								line = scanner.nextLine();
								if(line.charAt(0) == 'F' && line.charAt(1) == 'I')
									webEngine.executeScript("putImage('" + imagesClientFolder.getPath() + "/" + (noImagesCurrentForum++) + ".jpg" + "');");
								else									
									webEngine.executeScript("putText('" + line + "');");
							}
							scanner.close();

							FileWriter fileWriter = new FileWriter(logFile, true);
							BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
							printWriter = new PrintWriter(bufferedWriter);

							searchStage.close();
							forumStage.show();
						}	
						else if(forum.code == 5)
						{
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("ERROR");
							alert.setHeaderText("Sorry, Forum not found :(");
							alert.setContentText(null);
							alert.showAndWait();
						}			
					}
					else
					{
						System.out.println("Object not instance of class Forum, ejecting...");
						System.exit(1);	
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);

		// ----------------------------------------- SEARCH BY NAME OR DATE BUTTON -------------------------------------
		// ;
		btSearchWrite.setOnAction
		(
			e ->
			{
				try
				{
					if(rbSearchDate.isSelected())
					{
						//Code = 2 i.e. we want a list of the forums with the DATE of second argument
						forum = new Forum((short) 2, tfSearchWrite.getText(), null, null, null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(forum);
						oos.flush();
						byte[] byteArray = baos.toByteArray();
						DatagramPacket dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, serverPort);
						clientSendSocket.send(dtgPacket);
						oos.close();
						baos.close();

						//Receiving number of forums in folder of the DATE specified 
						dtgPacket = new DatagramPacket(new byte[1000], 1000);
						clientReceiveSocket.receive(dtgPacket);
						System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
						ByteArrayInputStream bais = new ByteArrayInputStream(dtgPacket.getData());
						ObjectInputStream ois = new ObjectInputStream(bais);

						Object o = (Object) ois.readObject();
						if(o instanceof Forum)
						{
							forum = (Forum) o;
							//Code = 4, receiving number of forums in folder with DATE specified
							if(forum.code == 4)
							{
								int noForums = Integer.parseInt(forum.name);
								foldersDisplayableList.clear();
								//Receiving names of the forums in the folder with the DATE specified 
								for(int i = 0; i < noForums; i++)
								{
									dtgPacket = new DatagramPacket(new byte[1000], 1000);
									clientReceiveSocket.receive(dtgPacket);
									System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
									bais = new ByteArrayInputStream(dtgPacket.getData());
									ois = new ObjectInputStream(bais);

									o = (Object) ois.readObject();
									if(o instanceof Forum)
									{
										forum = (Forum) o;
										foldersDisplayableList.add(new DisplayableFolder(forum.name, forum.creator));
									}
									else
									{
										System.out.println("Object not instance of class Forum, ejecting...");
										System.exit(1);				
									}
									ois.close();
									bais.close();
								}
								foldersObservableList = FXCollections.observableList(foldersDisplayableList);
                            	tableSearchFolders.setItems(foldersObservableList);  
							}
							else if(forum.code == 5)
							{
								Alert alert = new Alert(AlertType.ERROR);
								alert.setTitle("ERROR");
								alert.setHeaderText("Sorry, forums of that date not found :(");
								alert.setContentText(null);
								alert.showAndWait();
							}
						}
						else
						{
							System.out.println("Object not instance of class Forum, ejecting...");
							System.exit(1);
						}
					}
					else if(rbSearchName.isSelected())
					{
						//Code = 3, searching by name
						forum = new Forum((short) 3, tfSearchWrite.getText(), null, null, null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(forum);
						oos.flush();
						byte[] byteArray = baos.toByteArray();
						DatagramPacket dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, serverPort);
						clientSendSocket.send(dtgPacket);
						oos.close();
						baos.close();

						//Waiting for response from the server
						dtgPacket = new DatagramPacket(new byte[1000], 1000);
						clientReceiveSocket.receive(dtgPacket);
						System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
						ByteArrayInputStream bais = new ByteArrayInputStream(dtgPacket.getData());
						ObjectInputStream ois = new ObjectInputStream(bais);

						Object o = (Object) ois.readObject();
						if(o instanceof Forum)
						{
							forum = (Forum) o;
							//Code 4, receiving number of forums with the NAME specified
							if(forum.code == 4)
							{
								//Receiving noForums with the NAME specified
								int noForums = Integer.parseInt(forum.name);
								foldersDisplayableList.clear();
								for(int i = 0; i < noForums; i++)
								{
									dtgPacket = new DatagramPacket(new byte[1000], 1000);
									clientReceiveSocket.receive(dtgPacket);
									System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
									bais = new ByteArrayInputStream(dtgPacket.getData());
									ois = new ObjectInputStream(bais);

									o = (Object) ois.readObject();
									if(o instanceof Forum)
									{
										forum = (Forum) o;
										foldersDisplayableList.add(new DisplayableFolder(forum.name, forum.creator));		
									}
									else
									{	
										System.out.println("Object not instance of class Forum, ejecting...");
										System.exit(1);
									}
									ois.close();
									bais.close();
								}
								foldersObservableList = FXCollections.observableList(foldersDisplayableList);
                            	tableSearchFolders.setItems(foldersObservableList);  
							}	
							else if(forum.code == 5)
							{
								Alert alert = new Alert(AlertType.ERROR);
								alert.setTitle("ERROR");
								alert.setHeaderText("Sorry, forums of that name not found :(");
								alert.setContentText(null);
								alert.showAndWait();	
							}
						}
						else
						{
							System.out.println("Object not instance of class Forum, ejecting...");
							System.exit(1);				
						}
						ois.close();
						bais.close();
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);
	
		// ----------------------------------------- CREATE BUTTON -------------------------------------
		// ;
		btCreateOk.setOnAction
		(
			e ->
			{
				try
				{
					forum = new Forum((short) 1, tfCreateName.getText(), tfCreatDescription.getText(), dateCreated, thisUserName);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(forum);
					oos.flush();
					byte[] byteArray = baos.toByteArray();
					DatagramPacket dtgPacket = new DatagramPacket(byteArray, byteArray.length, destinyIP, serverPort);
					clientSendSocket.send(dtgPacket);
					oos.close();
					baos.close();
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("INFORMATION DIALOG");
					alert.setHeaderText("FORUM CREATED");
					alert.setContentText(null);
					alert.showAndWait();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);

		// ----------------------------------------- RETURN BUTTONS -------------------------------------
		// ;
		btSearchReturn.setOnAction
		(
			e ->
			{
				searchStage.close();
				lobbyStage.show();
			}
		);

		btCreateReturn.setOnAction
		(
			e ->
			{
				createStage.close();
				lobbyStage.show();
			}
		);
		
		// ----------------------------------------- OTHER BUTTONS -------------------------------------
		// ;
		btLobbySearchForum.setOnAction
		(
			e ->
			{
				lobbyStage.close();
				searchStage.show();
			}
		);

		btForumExit.setOnAction
		(
			e ->
			{
				clientReceiveSocket.close();
				clientSendSocket.close();
				forumStage.close();
				searchStage.close();
				createStage.close();
				lobbyStage.close();
				primaryStage.close();	
			}
		);

		btLobbyEndSession.setOnAction
		(
			e ->
			{
				clientReceiveSocket.close();
				clientSendSocket.close();
				forumStage.close();
				searchStage.close();
				createStage.close();
				lobbyStage.close();
				primaryStage.close();
			}
		);

		btLobbyCreateForum.setOnAction
		(
			e ->
			{	
				lobbyStage.close();
				createStage.show();
			}
		);

		btLoginOk.setOnAction
		(
			e ->
			{
				thisUserName = tfLoginUser.getText();
				tfCreateCreator.setText(thisUserName);
				primaryStage.close();
				lobbyStage.show();
			}
		);
	}
}
