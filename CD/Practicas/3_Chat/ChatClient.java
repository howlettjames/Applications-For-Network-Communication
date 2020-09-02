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
// ------- MULTICAST AND NET LIBRARIES
import java.net.*;
import java.net.MulticastSocket;
// ------- JAVASCRIPT LIBRARIES
import netscape.javascript.JSObject;

public class ChatClient extends Application
{
    InetAddress mcGroup = null;
    int clientPort = 9999, serverPort = 9876, noClients;
    int i, emojisLength, j;
    MulticastSocket mcClientSocket;
    DatagramPacket dtgPacket;
    String[] userLogString;
    String[] headerMessage;
    String[] header;
    String[] messageEmojis;
    String[] emojis;
    TableView<DisplayableUser> tableLobbyUsers = new TableView<>();
    ObservableList<DisplayableUser> usersObservableList; //is Abstract
    List<DisplayableUser> usersDisplayableList = new ArrayList<DisplayableUser>();
    byte[] byteArray;
    ByteArrayInputStream bais;
    ObjectInputStream ois;
    List<User> usersList = new ArrayList<User>();
    User user, thisUser;
    Object o;
    JSObject jsoInputText, jsoTextArea, jsoEmojisValues; 
    String htmlMessage, htmlTextAreaString, receiver;
    String msg, sender, emojisString;
    //char ch = '\u0905';
    //String[] emojisStrings = {"\u1F603", "\u1F605"};

    public void start(Stage primaryStage)
    {
        // ---------------------------------------------------- LOBBY PANE -------------------------------------------
        HBox lobbyPane = new HBox(10);
        VBox chatPane = new VBox(10);
        HBox msgPane = new HBox(10);
        HBox logPane = new HBox(10);
        HBox connectExitPane = new HBox(10);
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        File fileHtml = new File("C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Programs/Practices/3_Chat/chat.html");
        webEngine.load(fileHtml.toURI().toString());
        webEngine.setJavaScriptEnabled(true); //IMPORTANT
        TextField tfLobbyWriteUser = new TextField("");
        TextField tfLobbyWriteUserPrivateMsg = new TextField("");
        Button btLobbySendMsg = new Button("Send Message");
        Button btLobbySendFile = new Button("Send File");
        Button btLobbySendGif = new Button("Send Gif");
        Button btLobbyConnect = new Button("Connect");
        Button btLobbyExit = new Button("Exit");
        Label lbLobbyWriteUser = new Label("Write your user: ");
        Label lbLobbyWriteReceiverUser = new Label("Write receiver user: ");
        TableColumn tbcLobbyUser = new TableColumn("Users");
        TableColumn tbcLobbyPort = new TableColumn("Port");
        FileChooser fileChooser = new FileChooser();
        ToggleGroup tgLobbyChoosePrivate = new ToggleGroup();
        RadioButton rbLobbyPublic = new RadioButton("Public");
        RadioButton rbLobbyPrivate = new RadioButton("Private");

        //Setting properties for view
        lobbyPane.setPadding(new Insets(10));
        chatPane.setPadding(new Insets(10));
        msgPane.setPadding(new Insets(10));
        logPane.setPadding(new Insets(10));
        connectExitPane.setPadding(new Insets(10));
        webView.setPrefSize(500, 700); //width and height

        //Properties for table view
        tbcLobbyUser.setCellValueFactory(new PropertyValueFactory("user"));
        tbcLobbyPort.setCellValueFactory(new PropertyValueFactory("port"));
        tableLobbyUsers.getColumns().setAll(tbcLobbyUser, tbcLobbyPort);
        tableLobbyUsers.setPrefWidth(200);
        tableLobbyUsers.setPrefHeight(500);
        tableLobbyUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //Properties for toggle group
        rbLobbyPublic.setToggleGroup(tgLobbyChoosePrivate);
        rbLobbyPrivate.setToggleGroup(tgLobbyChoosePrivate);

        //Putting nodes into nodes
        msgPane.getChildren().addAll(btLobbySendMsg, btLobbySendFile, btLobbySendGif);
        logPane.getChildren().addAll(lbLobbyWriteUser, tfLobbyWriteUser, lbLobbyWriteReceiverUser, tfLobbyWriteUserPrivateMsg, rbLobbyPublic, rbLobbyPrivate);
        connectExitPane.getChildren().addAll(btLobbyConnect, btLobbyExit);
        chatPane.getChildren().addAll(webView, msgPane, logPane, connectExitPane);
        lobbyPane.getChildren().addAll(chatPane, tableLobbyUsers);

        Scene lobbyScene = new Scene(lobbyPane, 950, 700);
        primaryStage.setScene(lobbyScene);
        primaryStage.setTitle("LOBBY");
        primaryStage.show();

        // ---------------------------------------------------- GIFS PANE -------------------------------------------
        VBox gifsPane = new VBox(10);
        ScrollPane spGifs = new ScrollPane();
        FlowPane fpGifs = new FlowPane(10, 10);
        Image[] gifsImages = new Image[5];
        File[] gifsFiles = new File[5];
        ImageView[] gifsImagesViews = new ImageView[5];

        for(i = 0; i < 5; i++)
        {
            gifsImages[i] = new Image("file:///C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Programs/Practices/3_Chat/gifs/gif" + (i + 1) + ".gif");
            gifsFiles[i] = new File("C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Programs/Practices/3_Chat/gifs/gif" + (i + 1) + ".gif");
            gifsImagesViews[i] = new ImageView(gifsImages[i]);
            gifsImagesViews[i].setFitHeight(200);
            gifsImagesViews[i].setFitWidth(200);
            fpGifs.getChildren().add(gifsImagesViews[i]);
        }

        fpGifs.setOrientation(Orientation.HORIZONTAL);
        fpGifs.setPadding(new Insets(10));
        spGifs.setHbarPolicy(ScrollBarPolicy.NEVER);
        spGifs.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        spGifs.setPrefSize(500, 500);
        spGifs.setContent(fpGifs);
        spGifs.setPannable(true);
        gifsPane.setPadding(new Insets(10));
        gifsPane.getChildren().add(spGifs);

        Scene gifsScene = new Scene(gifsPane, 250, 500);    
        Stage gifsStage = new Stage();
        gifsStage.setScene(gifsScene);
        gifsStage.setTitle("GIFS");

        // ---------------------------------------------------- PROGRAMMING GIFS ON CLICK ------------------------------------
        gifsImagesViews[0].setOnMouseClicked
        (
            e ->
            {
                try
                {
                    if(rbLobbyPublic.isSelected())
                    {
                        //8 <opCode> <Port> <Sender Name> <Gif Name>
                        msg = "8 " + thisUser.getUserPort() + " " + thisUser.getUserName() + " " + gifsFiles[0].getName();
                        byteArray = msg.getBytes();            
                        dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
                        mcClientSocket.send(dtgPacket);
                        System.out.println("\nSending msg to group -> " + msg + " with TTL = "+ mcClientSocket.getTimeToLive());
                        gifsStage.close();    
                    }
                    else if(rbLobbyPrivate.isSelected())
                    {
                        //9 <opCode> <Port> <Sender Name> <Receiver Name> <Gif Name>
                        msg = "9 " + thisUser.getUserPort() + " " + thisUser.getUserName() + " " + tfLobbyWriteUserPrivateMsg.getText() + " " + gifsFiles[0].getName();
                        byteArray = msg.getBytes();            
                        dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
                        mcClientSocket.send(dtgPacket);
                        System.out.println("\nSending msg to group -> " + msg + " with TTL = "+ mcClientSocket.getTimeToLive());
                        gifsStage.close();       
                    }
                    webEngine.executeScript("putGif('" + gifsFiles[0].getName() + "');");                
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        );

        // ---------------------------------------------------- SHOWING GIFS STAGE -------------------------------------------
        btLobbySendGif.setOnAction
        (
            e ->
            {
                gifsStage.show();
            }
        );

        // ------------------------------------------------ SENDING A MULTICAST/PRIVATE FILE ------------------------------
        btLobbySendFile.setOnAction
        (
            e ->
            {
                try
                {
                    File f = fileChooser.showOpenDialog(primaryStage);
                    if(f != null)
                    {
                        String fName = f.getName();
                        long tam = f.length();
                        String path = f.getAbsolutePath();
                                                    
                        DataInputStream dis = new DataInputStream(new FileInputStream(path));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);

                        byte[] b = new byte[2000];
                        long enviados = 0;
                        int porciento = 0, n = 0;
                        while(enviados < tam)
                        {
                            n = dis.read(b);
                            dos.write(b, 0, n);
                            dos.flush();
                            enviados += n;
                            porciento = (int) ((enviados * 100) / tam);
                            System.out.print("\rFile written into byte array: " + porciento + "%");
                        }
                        System.out.println();
                        byte[] fileInBytes = baos.toByteArray();
                        dos.close();
                        baos.close();
                        dis.close();
                    
                        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                        ObjectOutputStream oos1 = new ObjectOutputStream(baos1);

                        Data data1 = new Data(1, fileInBytes, fileInBytes.length, 1, fName);
                        oos1.writeObject(data1);
                        oos1.flush();

                        //6 <opCode> <Port> <Sender Name>
                        if(rbLobbyPublic.isSelected())
                            msg = "6 " + thisUser.getUserPort() + " " + thisUser.getUserName();
                        //7 <opCode> <Port> <Sender Name> <Receiver Name>
                        else if(rbLobbyPrivate.isSelected())
                            msg = "7 " + thisUser.getUserPort() + " " + thisUser.getUserName() + " " + tfLobbyWriteUserPrivateMsg.getText();
                        
                        byteArray = msg.getBytes();            
                        dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
                        mcClientSocket.send(dtgPacket);
                        System.out.println("\nSending msg to group -> " + msg + " with TTL = "+ mcClientSocket.getTimeToLive());

                        byteArray = baos1.toByteArray();
                        dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
                        mcClientSocket.send(dtgPacket);
                        System.out.println("\nSending file to group -> " + fName + " with TTL = "+ mcClientSocket.getTimeToLive());                        
                        oos1.close();
                        baos1.close();
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        );
    
        // ------------------------------------------------ SENDING A MULTICAST/PRIVATE MESSAGE ------------------------------
        btLobbySendMsg.setOnAction
        (
        	e ->
        	{
        		try
        		{
                    webEngine.executeScript("sendMessage();");
                    jsoInputText = (JSObject) webEngine.executeScript("inputText");
                    jsoEmojisValues = (JSObject) webEngine.executeScript("emojisValues");
                    emojisLength = (int) webEngine.executeScript("emojisLength");    
                    htmlMessage = (String) jsoInputText.getMember("value");
                    htmlMessage = htmlMessage.concat(" XZ" + jsoEmojisValues + " XZ" + emojisLength);
                    if(rbLobbyPublic.isSelected())
                        //4 <opCode> <Port> <Client Name> | <Message>    
                        msg = "4 " + thisUser.getUserPort() + " " + thisUser.getUserName() + "\n" + htmlMessage;
                    else if(rbLobbyPrivate.isSelected())
                        //5 <opCode> <Port> <Sender Name> <Receiver Name> | <Message>    
                        msg = "5 " + thisUser.getUserPort() + " " + thisUser.getUserName() + " " + tfLobbyWriteUserPrivateMsg.getText() + "\n" + htmlMessage;
                    byteArray = msg.getBytes();            
                    dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, clientPort);
                    mcClientSocket.send(dtgPacket);
                    System.out.println("\nSending msg to group -> " + msg + " with TTL = "+ mcClientSocket.getTimeToLive());    
                        
         		}
        		catch(Exception ex)
        		{
        			ex.printStackTrace();
        		}
        	}
        );

        // ------------------------------------------------ LEAVING LOBBY ------------------------------
        btLobbyExit.setOnAction
        (
        	e ->
        	{
	            //Sending server msg to exit lobby and update list
	            try
	            {
                    //3 <opCode> <Port> <Client Name>
	            	msg = "3 " + thisUser.getUserPort() + " " + thisUser.getUserName();
		            byteArray = msg.getBytes();            
		            dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, serverPort);
		            mcClientSocket.send(dtgPacket);
		            System.out.println("\nSending msg to disconnect -> " + msg + " with TTL = "+ mcClientSocket.getTimeToLive());
		            primaryStage.close();
		            System.exit(1);	
	            }
	            catch(Exception ex)
	            {
	            	ex.printStackTrace();
	            }
        	}
        );

        // ------------------------------------------------ JOINING LOBBY -------------------------------------
        btLobbyConnect.setOnAction
        (
        	e ->
        	{
        		try
		        {
		            mcClientSocket = new MulticastSocket(clientPort);
		            System.out.println("Client listening port: "+ mcClientSocket.getLocalPort());
		            mcClientSocket.setReuseAddress(true);
                    mcClientSocket.setTimeToLive(128);

		            try
		            {
		                mcGroup = InetAddress.getByName("228.1.1.1");
		            }
		            catch(UnknownHostException u)
		            {
		                System.err.println("IP not valid");
		            }
		            mcClientSocket.joinGroup(mcGroup);
		            System.out.println("Joined to the group");
		            
		            // ---------------------------- CONNECTING TO SERVER -----------------------------
		            //Connecting to server by sending the user name	and port
		            thisUser = new User(mcClientSocket.getLocalPort(), tfLobbyWriteUser.getText());
                    //1 <opCode> <Port> <Client Name>
		            msg = "1 " + thisUser.getUserPort() + " " + thisUser.getUserName();
		            byteArray = msg.getBytes();            
		            dtgPacket = new DatagramPacket(byteArray, byteArray.length, mcGroup, serverPort);
		            mcClientSocket.send(dtgPacket);
		            System.out.println("Sending msg to connect -> " + msg + " with TTL = "+ mcClientSocket.getTimeToLive());
		            tfLobbyWriteUser.setEditable(false);
		        }
		        catch(Exception ex)
		        {
		            ex.printStackTrace();       
		        }
        	}
        );

        // ------------------------------------------------ RECEIVING ANY TYPE OF MESSAGE --------------------------------
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() 
                {
                    try
                    {
                        //Receiving Server user name, port and list of other clients in the lobby
                        dtgPacket = new DatagramPacket(new byte[500], 500);
                        mcClientSocket.receive(dtgPacket);
                        System.out.println("\nDatagram received...");
                        msg = new String(dtgPacket.getData());
                        //2 Load list
                        if(msg.charAt(0) == '2')
                        {
                            //If 2 a new client has been connected to the lobby so we load the complete list from Server again
                            usersList.clear();
                            usersDisplayableList.clear();
                            //When the client connects the server send a string with his info and at the end the number of other clients connected
                            //2 <opCode> <Server Name> <Port> <Number of Clients connected>
                            userLogString = msg.split("\\s+");
                            noClients = Integer.parseInt(userLogString[3].trim());
                            System.out.println("Server discovered: " + dtgPacket.getAddress() + "\nServer user: " +userLogString[1] + "\nServer port: " + userLogString[2] + "\nNumber of clients: " + noClients);
                            user = new User(Integer.parseInt(userLogString[2].trim()), userLogString[1]);
                            usersList.add(user);
                            usersDisplayableList.add(new DisplayableUser(user.getUserName(), user.getUserPort()));
                            for(i = 0; i < noClients; i++)
                            {
                                dtgPacket = new DatagramPacket(new byte[65535], 65535);
                                mcClientSocket.receive(dtgPacket);
                                bais = new ByteArrayInputStream(dtgPacket.getData());
                                ois = new ObjectInputStream(bais);

                                o = ois.readObject();
                                if(o instanceof User)
                                {
                                    user = (User) o;
                                    System.out.println("User -> " + user.getUserName() + " : " + user.getUserPort());
                                    usersList.add(user);
                                    usersDisplayableList.add(new DisplayableUser(user.getUserName(), user.getUserPort()));
                                }
                                bais.close();
                                ois.close();
                            }
                            usersObservableList = FXCollections.observableList(usersDisplayableList);
                            tableLobbyUsers.setItems(usersObservableList);  
                        }
                        //4 Multicast Message
                        else if(msg.charAt(0) == '4')
                        {
                            //Run a Thread this way when you want to update GUI JavaFX and calling to Javascript
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //4 <opCode> <Port> <Sender Name> | <Message> 
                                    headerMessage = msg.split("\n");
                                    System.out.println("Header: " + headerMessage[0]);
                                    header = headerMessage[0].split("\\s+");
                                    sender = header[2];
                                    System.out.println("Sender: " + sender);
                                    if(!sender.equals(thisUser.getUserName()))
                                    {
                                        messageEmojis = headerMessage[1].trim().split("XZ");
                                        msg = messageEmojis[0];
                                        emojisString = messageEmojis[1];
                                        emojis = emojisString.split("\\,") ;
                                        emojisLength = Integer.parseInt(messageEmojis[2]);
                                        System.out.println("Message: " + msg);
                                        String[] msgs = msg.split(" ");
                                        for(i = 0, j = 0; i < msgs.length && j < emojisLength; i++)
                                        {
                                            if(msgs[i].contains("?") || msgs[i].length() == 1)
                                                msgs[i] = emojis[j++].trim();
                                        }
                                        msg = "";
                                        for(i = 0; i < msgs.length; i++)
                                        {
                                            if(i == 0)
                                                msg = msg.concat(msgs[i]);   
                                            else    
                                                msg = msg.concat(" " + msgs[i]);   
                                        }
                                        System.out.println("Real message: " + msg);
                                        System.out.println("Length of emojis: " + emojisLength);
                                        webEngine.executeScript("putText('" + (msg + " -" + sender) + "');");    
                                    }
                                }
                            });
                        }
                        //5 Private Message
                        else if(msg.charAt(0) == '5')
                        {
                            //Run a Thread this way when you want to update GUI JavaFX and calling to Javascript
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //5 <opCode> <Port> <Sender Name> <Receiver Name> | <Message> 
                                    headerMessage = msg.split("\n");
                                    System.out.println("Header: " + headerMessage[0]);
                                    header = headerMessage[0].split("\\s+");
                                    sender = header[2];
                                    receiver = header[3];
                                    System.out.println("Sender: " + sender);
                                    System.out.println("Receiver: " + receiver); 
                                    if(receiver.equals(thisUser.getUserName()))
                                    {
                                        messageEmojis = headerMessage[1].trim().split("XZ");
                                        msg = messageEmojis[0];
                                        emojisString = messageEmojis[1];
                                        emojis = emojisString.split("\\,") ;
                                        emojisLength = Integer.parseInt(messageEmojis[2]);
                                        System.out.println("Message: " + msg);
                                        String[] msgs = msg.split(" ");
                                        for(i = 0, j = 0; i < msgs.length && j < emojisLength; i++)
                                        {
                                            if(msgs[i].contains("?") || msgs[i].length() == 1)
                                                msgs[i] = emojis[j++].trim();
                                        }
                                        msg = "";
                                        for(i = 0; i < msgs.length; i++)
                                        {
                                            if(i == 0)
                                                msg = msg.concat(msgs[i]);   
                                            else    
                                                msg = msg.concat(" " + msgs[i]);   
                                        }
                                        System.out.println("Real message: " + msg);
                                        System.out.println("Length of emojis: " + emojisLength);
                                        webEngine.executeScript("putText('" + (msg + " -" + sender) + "');");    
                                    }
                                }
                            });
                        }
                        //6 Multicast File
                        else if(msg.charAt(0) == '6')
                        {
                            //6 <opCode> <Port> <Sender Name>
                            headerMessage = msg.split("\n");
                            System.out.println("Header: " + headerMessage[0].trim());
                            header = headerMessage[0].split("\\s+");
                            sender = header[2].trim();
                            System.out.println("Sender: " + sender);
                            if(!sender.equals(thisUser.getUserName()))
                            {
                                try
                                {
                                    dtgPacket = new DatagramPacket(new byte[65535], 65535);
                                    mcClientSocket.receive(dtgPacket);
                                    System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
                                    ByteArrayInputStream bais = new ByteArrayInputStream(dtgPacket.getData());
                                    ObjectInputStream ois = new ObjectInputStream(bais);

                                    o = ois.readObject();
                                    if(o instanceof Data)
                                    {
                                        Data data = (Data) o;
                                        System.out.println("Data received ->" + "\nNumber of sequence: " + data.noSequence + "\nTotal of packets: " + data.total + "\nFile name: " + data.fName);   
                                        ByteArrayInputStream bais1 = new ByteArrayInputStream(data.bytes);
                                        DataInputStream dis = new DataInputStream(bais1);
                                        DataOutputStream dos = new DataOutputStream(new FileOutputStream(data.fName));

                                        long tam = data.bytes.length;
                                        System.out.println("Size of file: " + tam + " bytes");
                                        long leidos = 0;
                                        int porciento = 0, n = 0;
                                        byte[] b = new byte[2000];
                                        while(leidos < tam)
                                        {
                                            n = dis.read(b);
                                            dos.write(b, 0, n);
                                            dos.flush();
                                            leidos += n;
                                            porciento = (int) ((leidos * 100) / tam);
                                            System.out.print("\rWritten: " + porciento + "%");
                                        }
                                        System.out.println("\n");
                                        dos.close();
                                    }
                                    else
                                        System.out.println("Object received is not instace of class Data");
                                }
                                catch(Exception ex)
                                {
                                    ex.printStackTrace();
                                }  
                            }
                        }
                        //7 Private File
                        else if(msg.charAt(0) == '7')
                        {
                            //7 <opCode> <Port> <Sender Name> <Receiver Name>
                            headerMessage = msg.split("\n");
                            System.out.println("Header: " + headerMessage[0].trim());
                            header = headerMessage[0].split("\\s+");
                            sender = header[2].trim();
                            receiver = header[3].trim();
                            System.out.println("Sender: " + sender);
                            System.out.println("Receiver: " + receiver);
                            if(receiver.equals(thisUser.getUserName()))
                            {
                                try
                                {
                                    dtgPacket = new DatagramPacket(new byte[65535], 65535);
                                    mcClientSocket.receive(dtgPacket);
                                    System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
                                    ByteArrayInputStream bais = new ByteArrayInputStream(dtgPacket.getData());
                                    ObjectInputStream ois = new ObjectInputStream(bais);

                                    o = ois.readObject();
                                    if(o instanceof Data)
                                    {
                                        Data data = (Data) o;
                                        System.out.println("Data received ->" + "\nNumber of sequence: " + data.noSequence + "\nTotal of packets: " + data.total + "\nFile name: " + data.fName);   
                                        ByteArrayInputStream bais1 = new ByteArrayInputStream(data.bytes);
                                        DataInputStream dis = new DataInputStream(bais1);
                                        DataOutputStream dos = new DataOutputStream(new FileOutputStream(data.fName));

                                        long tam = data.bytes.length;
                                        System.out.println("Size of file: " + tam + " bytes");
                                        long leidos = 0;
                                        int porciento = 0, n = 0;
                                        byte[] b = new byte[2000];
                                        while(leidos < tam)
                                        {
                                            n = dis.read(b);
                                            dos.write(b, 0, n);
                                            dos.flush();
                                            leidos += n;
                                            porciento = (int) ((leidos * 100) / tam);
                                            System.out.print("\rWritten: " + porciento + "%");
                                        }
                                        System.out.println("\n");
                                        dos.close();
                                    }
                                    else
                                        System.out.println("Object received is not instace of class Data");
                                }
                                catch(Exception ex)
                                {
                                    ex.printStackTrace();
                                }  
                            }
                        }
                        //8 Receiving a multicast GIF
                        else if(msg.charAt(0) == '8')
                        {
                            //Run a Thread this way when you want to update GUI JavaFX and calling to Javascript
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //8 <opCode> <Port> <Sender Name> <GIF Name>
                                    System.out.println("Header: " + msg.trim());
                                    headerMessage = msg.split(" ");
                                    String gifName = headerMessage[3].trim();
                                    for(i = 0; i < gifsImages.length; i++)
                                        if(gifName.equals(gifsFiles[i].getName()))
                                            webEngine.executeScript("putGif('" + gifName + "');");                
                                }
                            });
                        }
                        //9 Receiving a private GIF
                        else if(msg.charAt(0) == '9')
                        {
                            //Run a Thread this way when you want to update GUI JavaFX and calling to Javascript
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //9 <opCode> <Port> <Sender Name> <Receiver Name> <GIF Name>
                                    System.out.println("Header: " + msg.trim());
                                    headerMessage = msg.split(" ");
                                    receiver = headerMessage[3].trim();
                                    if(receiver.equals(thisUser.getUserName()))
                                    {
                                        String gifName = headerMessage[4].trim();
                                        for(i = 0; i < gifsImages.length; i++)
                                            if(gifName.equals(gifsFiles[i].getName()))
                                                webEngine.executeScript("putGif('" + gifName + "');");                    
                                    }
                                }
                            });
                        }
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    System.out.println("ping");
                }
        }, 0, 4000);    
    }
}