// ------- JAVAFX LIBRARIES
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.stage.FileChooser;
import javafx.scene.web.*;
import javafx.application.Platform;
// -------- UTIL LIBRARIES
import java.util.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
// ------- MULTICAST AND NET LIBRARIES
import java.net.*;
import java.net.MulticastSocket;
// ------- TABLEVIEW LIBRARIES
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
// ------- JAVASCRIPT LIBRARIES
import netscape.javascript.JSObject;

public class HangedClient extends Application
{
	int i;
	int noAttempts = 6;
	int match;
	int noCharsToMatch;
	String word;
	String imagesPath = "HangedManImages/";
	boolean ok;

	@Override
	public void start(Stage primaryStage)
	{
		VBox hangedPane = new VBox(10);
		HBox hbSelectDifficulty = new HBox(10);
		HBox hbLabelsWord = new HBox(5);
		HBox hbWriteChar = new HBox(30);
		Label lbSelectDifficulty = new Label("Select Difficulty -> 1:Easy 2:Medium 3:Hard ");
		Label lbWriteAChar = new Label("Write a char: ");
		Label lbNoAttempts = new Label("Number of attempts: 6");
		TextField tfSelectDifficulty = new TextField("");
		TextField tfChar = new TextField("");
		Button btSelectDifficulty = new Button("Send");
		Button btSelectChar = new Button("Try");
		Label[] lbsString = new Label[20];
		Image[] hangedManImages = new Image[7];
		ImageView hangedManImageView = new ImageView();

		//Setting view preferences
		hangedPane.setPadding(new Insets(10));
		hbSelectDifficulty.setPadding(new Insets(10));
		hbLabelsWord.setPadding(new Insets(10));
		hbWriteChar.setPadding(new Insets(10));
		tfChar.setPrefColumnCount(3);
		tfSelectDifficulty.setPrefColumnCount(3);
		hangedManImageView.setFitHeight(200);
		hangedManImageView.setFitWidth(200);

		for(i = 0; i < 7; i++)
			hangedManImages[i] = new Image(imagesPath + i + ".png");

		for(i = 0; i < 20; i++)
		{
			lbsString[i] = new Label("");
			hbLabelsWord.getChildren().add(lbsString[i]);
		}

		//Putting nodes into nodes
		hbSelectDifficulty.getChildren().addAll(lbSelectDifficulty, tfSelectDifficulty, btSelectDifficulty);
		hbWriteChar.getChildren().addAll(lbWriteAChar, tfChar, btSelectChar, lbNoAttempts);
		hangedPane.getChildren().addAll(hbSelectDifficulty, hbLabelsWord, hbWriteChar, hangedManImageView);

		Scene scene = new Scene(hangedPane, 800, 500);
		primaryStage.setScene(scene);
		primaryStage.setTitle("HANGED MAN");
		primaryStage.show();

		btSelectChar.setOnAction
		(
			e ->
			{
				char ch = tfChar.getText().charAt(0);
				ok = false;
			
				for(i = 0; i < word.length(); i++)
				{
					System.out.println("CCC");
					if(word.charAt(i) == ch)
					{
						lbsString[i].setText("" + ch);
						match++;
						ok = true;
					}
				}
				if(!ok)
				{
					lbNoAttempts.setText("Number of attemps: " + (--noAttempts));
					if(noAttempts == 0)
						lbNoAttempts.setText("Game Over!");
					hangedManImageView.setImage(hangedManImages[6 - noAttempts]);
				}				
				if(match == noCharsToMatch)
					lbNoAttempts.setText("You Won!");	
			}
		);

		btSelectDifficulty.setOnAction
		(
			e ->
			{
				try
				{
					int port = 9999;

					noAttempts = 6;
					match = 0;
					lbNoAttempts.setText("Number of attemps: 6");
					hangedManImageView.setImage(hangedManImages[0]);

					for(i = 0; i < 20; i++)
						lbsString[i].setText("");

					Socket client = new Socket("localhost", port);
					System.out.println("\nConnection established...");
					PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
					BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

					pw.println(tfSelectDifficulty.getText());
					pw.flush();

					word = br.readLine();
					word = word.trim();
					System.out.println("Word recceived: " + word + " Word length: " + word.length());

					br.close();
					pw.close();
					client.close();

					noCharsToMatch = word.length();
					for(i = 0; i < word.length(); i++)
					{
						if(word.charAt(i) == ' ')
						{
							lbsString[i].setText(" ");
							noCharsToMatch--;
						}	
						else
							lbsString[i].setText("-");
					}						

					// DATAGRAM VERSION
					// DatagramSocket clientSocket = new DatagramSocket();
					// InetAddress ip = null;
					// int port = 9999;
					// noAttempts = 6;
					// match = 0;
					// lbNoAttempts.setText("Number of attemps: 6");
					// hangedManImageView.setImage(hangedManImages[0]);

					// for(i = 0; i < 20; i++)
					// 	lbsString[i].setText("");

					// try
					// {
					// 	ip = InetAddress.getByName("127.0.0.1");
					// }
					// catch(UnknownHostException ex)	
					// {
					// 	System.out.println("IP not valid");
					// 	System.exit(1);
					// }
					// ByteArrayOutputStream baos = new ByteArrayOutputStream();
					// DataOutputStream dos = new DataOutputStream(baos);
					// int difficulty = Integer.parseInt(tfSelectDifficulty.getText());

					// dos.writeShort(difficulty);
					// dos.flush();

					// byte[] byteArray = baos.toByteArray();
					// dos.close();
					// baos.close();

					// //String difficulty = new String(tfSelectDifficulty.getText());
					// //byte[] byteArray = difficulty.getBytes();

					// DatagramPacket dtgPacket = new DatagramPacket(byteArray, byteArray.length, ip, port);
					// clientSocket.send(dtgPacket);

					// //Words are no longer than 100 bytes
					// dtgPacket = new DatagramPacket(new byte[100], 100);
					// clientSocket.receive(dtgPacket);
					// System.out.println("Datagram received from: " + dtgPacket.getAddress() + ":" + dtgPacket.getPort());
					// word = new String(dtgPacket.getData());
					// word = word.trim();
					// System.out.println("Word received: " + word + " Word length: " + word.length());
					// clientSocket.close();

					// noCharsToMatch = word.length();
					// for(i = 0; i < word.length(); i++)
					// {
					// 	if(word.charAt(i) == ' ')
					// 	{
					// 		lbsString[i].setText(" ");
					// 		noCharsToMatch--;
					// 	}	
					// 	else
					// 		lbsString[i].setText("-");
					// }
					// END DATAGRAM VERSION
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);
	}
}