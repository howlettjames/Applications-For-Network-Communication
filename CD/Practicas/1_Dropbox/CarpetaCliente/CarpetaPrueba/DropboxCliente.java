import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.*;
//-------------------------DRAG AND DROP
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ClipboardContent;

public class DropboxCliente extends Application
{
	long tam = 0, leidos = 0, enviados = 0;
	int i = 0, nofilesCliente = 0, porciento = 0, n = 0, nofilesServidor = 0;
	byte[] b = new byte[2000];
	Socket cl, cl1;
	String carpetaClienteString = "C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Programs/Practices/1_Dropbox/CarpetaCliente/";
	DataOutputStream dos, dos1;
	DataInputStream dis, dis1;
	File[] archivosCliente;
	boolean doNothing;

	@Override
	public void start(Stage primaryStage)
	{
		VBox mainPane = new VBox(5);
		HBox hbox1 = new HBox(100);
		HBox hbox2 = new HBox(170);
		HBox hbox3 = new HBox(330);
		VBox vbox1 = new VBox(10);
		VBox vbox2 = new VBox(10);
		Button btConectar = new Button("Conectar al Servidor");
		Button btSalir = new Button("Salir");
		Label[] lbsC = new Label[50];
		Label[] lbsS = new Label[50];
		Label lbServidor = new Label("Servidor");
		Label lbCliente = new Label("Cliente");
		
		mainPane.setPadding(new Insets(10));
		hbox1.setPadding(new Insets(10));
		vbox1.setPadding(new Insets(10));
		vbox2.setPadding(new Insets(10));

		VBox.setVgrow(mainPane, Priority.ALWAYS);
		hbox2.setAlignment(Pos.BOTTOM_CENTER);
		vbox1.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.50));
		vbox2.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.50));
		vbox1.setStyle("-fx-background-color: white");
		vbox2.setStyle("-fx-background-color: white");

		for(i = 0; i < 12; i++)
		{
			lbsC[i] = new Label("");	
			vbox1.getChildren().add(lbsC[i]);
		}
		for(i = 0; i < 12; i++)
		{
			lbsS[i] = new Label("");	
			vbox2.getChildren().add(lbsS[i]);
		}
		hbox1.getChildren().addAll(vbox1, vbox2);
		hbox3.getChildren().addAll(lbCliente, lbServidor);
		hbox2.getChildren().addAll(btConectar, btSalir);
		mainPane.getChildren().addAll(hbox3, hbox1, hbox2);

		//Showing all files on client's side
		try
		{
			System.out.println("Cargando archivos de la carpeta cliente");
			File carpetaCliente = new File(carpetaClienteString);
			archivosCliente = carpetaCliente.listFiles();
			nofilesCliente = 0;
			for(File f: archivosCliente)
			{
				if(f.isDirectory())
				{
					lbsC[nofilesCliente].setText("../" + f.getName());
					lbsC[nofilesCliente++].setStyle("-fx-background-color: yellow");
				}
				else
					lbsC[nofilesCliente++].setText(f.getName());
			}
			System.out.println("Archivos cargados");
		}
		catch(Exception exx)
		{
			System.out.println("AAA");
			exx.printStackTrace();
		}

		//Setting functionality DnD from Server to Client
		vbox1.setOnDragDropped
		(
			e ->
			{
				boolean success = false;
				if(e.getDragboard().hasString())
				{
					success = true;
					doNothing = false;
					//Verifying file is not already in Client's directory
					for(int i = 0; i < nofilesCliente; i++)
						if(e.getDragboard().getString().equals(lbsC[i].getText()))
							doNothing = true;
					if(!doNothing)
					{
						lbsC[nofilesCliente++].setText(e.getDragboard().getString());		
						try
						{
							//Write 1, to indicate we want to receive a file
							dos.writeInt(1);
							dos.flush();
							dos.writeUTF(e.getDragboard().getString());
							dos.flush();
							System.out.println("\nNombre de archivo a recibir: " + e.getDragboard().getString());
							//If file is a directory then 
							if(e.getDragboard().getString().charAt(0) == '.')
							{
								String newDirectoryString = e.getDragboard().getString();
								newDirectoryString = newDirectoryString.substring(3);
								File newDirectory = new File(carpetaClienteString + newDirectoryString);
								newDirectory.mkdir();
								int noFilesReceive = dis.readInt();
								System.out.println("Number of files to receive: " + noFilesReceive);
								for(int i = 0; i < noFilesReceive; i++)
								{
									cl1 = new Socket("127.0.0.1", 1235);
									System.out.println("Conectado socket 2");
									dis1 = new DataInputStream(cl1.getInputStream());
									String nameNewFile = dis1.readUTF();
									System.out.println("Name of new file: " + nameNewFile);
									dos1 = new DataOutputStream(new FileOutputStream(carpetaClienteString + newDirectory.getName() + "/" + nameNewFile));

									leidos = 0;
									n = 0;
									porciento = 0;
									tam = dis1.readLong();
									System.out.println("Recibiendo el archivo de tamano: " + tam + " bytes");
									while(leidos < tam)
									{
										n = dis1.read(b);
										dos1.write(b, 0, n);
										dos1.flush();
										leidos += n;
										porciento = (int) ((leidos * 100) / tam);
										System.out.print("\rRecibido el: " + porciento + "%");
									}
									System.out.println();
									dos1.close();
									dis1.close();
									cl1.close();		
								}
							}
							else
							{
								cl1 = new Socket("127.0.0.1", 1235);
								System.out.println("Conectado socket 2");
								dis1 = new DataInputStream(cl1.getInputStream());
								dos1 = new DataOutputStream(new FileOutputStream(carpetaClienteString + e.getDragboard().getString()));
								
								leidos = 0;
								n = 0;
								porciento = 0;
								tam = dis1.readLong();
								System.out.println("Recibiendo el archivo de tamano: " + tam + " bytes");
								while(leidos < tam)
								{
									n = dis1.read(b);
									dos1.write(b, 0, n);
									dos1.flush();
									leidos += n;
									porciento = (int) ((leidos * 100) / tam);
									System.out.print("\rRecibido el: " + porciento + "%");
								}
								System.out.println();
								dos1.close();
								dis1.close();
								cl1.close();
							}	
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}	
				}
				e.setDropCompleted(success);
				e.consume();
			}
		);

		//Setting functionality DnD from Client to Server
		vbox2.setOnDragDropped
		(
			e ->
			{
				boolean success = false;
				if(e.getDragboard().hasString())
				{
					success = true;
					doNothing = false;
					//Verifying file is not already in Server's directory
					for(int i = 0; i < nofilesServidor; i++)
						if(e.getDragboard().getString().equals(lbsS[i].getText()))
							doNothing = true;
					if(!doNothing)
					{
						lbsS[nofilesServidor++].setText(e.getDragboard().getString());		
						try
						{
							//Write 2, to indicate we want to send a file
							dos.writeInt(2);
							dos.flush();
							dos.writeUTF(e.getDragboard().getString());
							dos.flush();
							System.out.println("Directory/File to be send: " + e.getDragboard().getString());
							//Sending an entire directory
							if(e.getDragboard().getString().charAt(0) == '.')
							{
								String directoryName = e.getDragboard().getString();
								directoryName = directoryName.substring(3);
								//Finding file in client's folder
								for(File f: archivosCliente)
									if(f.getName().equals(directoryName))
									{
										File[] filesToBeSend = f.listFiles();
										dos.writeInt(filesToBeSend.length);
										dos.flush();
										System.out.println("File to be send: " + filesToBeSend.length);
										for(File fi: filesToBeSend)
										{
											cl1 = new Socket("127.0.0.1", 1235);
											System.out.println("Conectado socket 2");
											dis1 = new DataInputStream(new FileInputStream(carpetaClienteString + f.getName() + "/" + fi.getName()));
											dos1 = new DataOutputStream(cl1.getOutputStream());

											dos1.writeUTF(fi.getName());
											dos1.flush();
											System.out.println("\nName of file to send: " + fi.getName());
											dos1.writeLong(fi.length());
											dos1.flush();
											System.out.println("Size of file to send: " + fi.length());

											tam = fi.length();
											enviados = 0;
											porciento = 0;
											n = 0;
											while(enviados < tam)
											{
												n = dis1.read(b);
												dos1.write(b, 0, n);
												dos1.flush();
												enviados += n;
												porciento = (int) ((enviados * 100) / tam);
												System.out.print("\rEnviado el: " + porciento + "%");
											}
											System.out.println();
											dos1.close();
											dis1.close();
											cl1.close();
										}
										break;
									}
							}
							else
							{
								//Finding file in client's folder and sending
								for(File f: archivosCliente)
									if(f.getName().equals(e.getDragboard().getString()))
									{
										cl1 = new Socket("127.0.0.1", 1235);
										System.out.println("Conectado socket 2");
										dis1 = new DataInputStream(new FileInputStream(carpetaClienteString + f.getName()));
										dos1 = new DataOutputStream(cl1.getOutputStream());

										tam = f.length();
										dos1.writeLong(tam);
										dos1.flush();
										System.out.println("Sending file of size: " + tam);

										enviados = 0;
										porciento = 0;
										n = 0;
										while(enviados < tam)
										{
											n = dis1.read(b);
											dos1.write(b, 0, n);
											dos1.flush();
											enviados += n;
											porciento = (int) ((enviados * 100) / tam);
											System.out.print("\rEnviado el: " + porciento + "%");
										}
										System.out.println();
										dos1.close();
										dis1.close();
										cl1.close();
										break;
									}
							}
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
				e.setDropCompleted(success);
				e.consume();
			}
		);

		//Connecting to server and showing all files on server's side
		btConectar.setOnAction
		(
			e ->
			{
				try
				{
					cl = new Socket("127.0.0.1", 1234);
					System.out.println("\nConectado socket 1");
					dis = new DataInputStream(cl.getInputStream());
					dos = new DataOutputStream(cl.getOutputStream());

					System.out.println("Leyendo el numero de archivos");
					nofilesServidor = dis.readInt();
					for(i = 0; i < nofilesServidor; i++)
					{
						lbsS[i].setText(dis.readUTF());
						if(lbsS[i].getText().charAt(0) == '.')
							lbsS[i].setStyle("-fx-background-color: yellow");
					}	
					System.out.println("Archivos en el servidor leidos.");
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);

// --------------------------------------------------------- DnD CONFIGURATION --------------------------------------		
		//Setting functionality to DnD
		vbox1.setOnDragOver
		(
			e -> 
			{
				if(e.getGestureSource() != vbox1 && e.getDragboard().hasString())
				{
					e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
					vbox1.setStyle("-fx-background-color: blue");
				}
				e.consume();
			}
		);

		//Setting functionality to DnD
		vbox1.setOnDragExited
		(
			e ->
			{
				vbox1.setStyle("-fx-background-color: white");
				e.consume();
			}
		);

		//Setting functionality to DnD
		vbox2.setOnDragOver
		(
			e -> 
			{
				if(e.getGestureSource() != vbox2 && e.getDragboard().hasString())
				{
					e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
					vbox2.setStyle("-fx-background-color: blue");
				}
				e.consume();
			}
		);
		
		//Setting functionality to DnD
		vbox2.setOnDragExited
		(
			e ->
			{
				vbox2.setStyle("-fx-background-color: white");
				e.consume();
			}
		);
// --------------------------------------------------------- LABELS DnD CONFIGURATION --------------------------------------		
		lbsS[0].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsS[0].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsS[0].getText());
				db.setContent(content);
				e.consume();
			}
		);
		
		lbsS[1].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsS[1].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsS[1].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsS[2].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsS[2].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsS[2].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsS[3].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsS[3].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsS[3].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsS[4].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsS[4].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsS[4].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsC[0].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsC[0].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsC[0].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsC[1].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsC[1].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsC[1].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsC[2].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsC[2].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsC[2].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsC[3].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsC[3].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsC[3].getText());
				db.setContent(content);
				e.consume();
			}
		);

		lbsC[4].setOnDragDetected
		(
			e -> 
			{
				Dragboard db = lbsC[4].startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString(lbsC[4].getText());
				db.setContent(content);
				e.consume();
			}
		);
// --------------------------------------------------------- FINAL CONFIGURATION --------------------------------------		
		btSalir.setOnAction
		(
			e ->
			{
				try
				{
					dis.close();
					dos.close();
					cl.close();	
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				primaryStage.close();
			}
		);
		Scene scene = new Scene(mainPane, 650, 500);
		primaryStage.setTitle("Dropbox Cliente");
		primaryStage.setScene(scene);
		primaryStage.show();
	}	
}