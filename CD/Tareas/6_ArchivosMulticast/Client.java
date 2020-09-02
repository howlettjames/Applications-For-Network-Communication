import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.*;
//-------------------------DRAG AND DROP
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Client extends Application
{
	long enviados;
	int limit = 10000, n, porciento, c, np, chunkSize;
	byte[] b, fileChunk;

	@Override
	public void start(Stage primaryStage)
	{
		HBox hbox = new HBox(15);
		Button button = new Button("Seleccionar Archivo");
		FileChooser fileChooser = new FileChooser();

		hbox.setPadding(new Insets(10));
		button.setStyle("-fx-background-color: gold");

		hbox.getChildren().add(button);

		hbox.setOnDragOver
		(
			e ->
			{
				if(e.getGestureSource() != hbox && e.getDragboard().hasFiles())
				{
					e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
					hbox.setStyle("-fx-background-color: red");
				}
				e.consume();
			}
		);

		hbox.setOnDragDropped
		(
			e ->
			{
				boolean success = false;
				if(e.getDragboard().hasFiles())
				{
					success = true;
					try
					{
						File f = e.getDragboard().getFiles().get(0);
						if(f != null)
						{
							String fName = f.getName();
							long tam = f.length();
							String path = f.getAbsolutePath();
							DatagramSocket cl = new DatagramSocket();
							InetAddress dst = null;

							try
							{
								dst = InetAddress.getByName("127.0.0.1");
							}
							catch(UnknownHostException ex)	
							{
								System.out.println("IP not valid");
								System.exit(1);
							}
								
							DataInputStream dis = new DataInputStream(new FileInputStream(path));
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(baos);

							b = new byte[2000];
							enviados = 0;
							porciento = n = 0;
							while(enviados < tam)
							{
								n = dis.read(b);
								dos.write(b, 0, n);
								dos.flush();
								enviados += n;
								porciento = (int) ((enviados * 100) / tam);
								System.out.print("\rWritten into byte array: " + porciento + "%");
							}
							System.out.println();
							byte[] fileInBytes = baos.toByteArray();
							dos.close();
							baos.close();
							dis.close();

							if(fileInBytes.length > limit)
							{
								fileChunk = new byte[limit];
								ByteArrayInputStream bais = new ByteArrayInputStream(fileInBytes);

								chunkSize = c = 0; 
								np = (int) (fileInBytes.length / fileChunk.length);
								if(fileInBytes.length % fileChunk.length > 0)
									np++;
								while(c < np)
								{
									ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
									ObjectOutputStream oos1 = new ObjectOutputStream(baos1);
									chunkSize = bais.read(fileChunk);
									Data d = new Data(c + 1, fileChunk, chunkSize, np, fName);
									oos1.writeObject(d);
									oos1.flush();
									byte[] tmp = baos1.toByteArray();
									DatagramPacket dp = new DatagramPacket(tmp, tmp.length, dst, 1234);
									cl.send(dp);
									c++;
									System.out.println("Sent part number: " + c + " of " + np);
									oos1.close();
									baos1.close();
								}
								bais.close();
								cl.close();
								System.out.println("File totally sent.");
							}
							else
							{
								ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
								ObjectOutputStream oos1 = new ObjectOutputStream(baos1);

								Data data1 = new Data(1, fileInBytes, fileInBytes.length, 1, fName);
								oos1.writeObject(data1);
								oos1.flush();

								byte[] bytes = baos1.toByteArray();
								DatagramPacket p = new DatagramPacket(bytes, bytes.length, dst, 1234);
								cl.send(p);
								oos1.close();
								baos1.close();
								cl.close();
								System.out.println("\nFile sent.");	
							}
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}		
				}
				e.setDropCompleted(success);
				e.consume();
			}
		);	

		hbox.setOnDragExited
		(
			e ->
			{
				hbox.setStyle("-fx-background-color: white");
				e.consume();
			}
		);

		button.setOnAction
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
						DatagramSocket cl = new DatagramSocket();
						InetAddress dst = null;

						try
						{
							dst = InetAddress.getByName("127.0.0.1");
						}
						catch(UnknownHostException ex)	
						{
							System.out.println("IP not valid");
							System.exit(1);
						}
							
						DataInputStream dis = new DataInputStream(new FileInputStream(path));
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);

						b = new byte[2000];
						enviados = 0;
						porciento = n = 0;
						while(enviados < tam)
						{
							n = dis.read(b);
							dos.write(b, 0, n);
							dos.flush();
							enviados += n;
							porciento = (int) ((enviados * 100) / tam);
							System.out.print("\rWritten into byte array: " + porciento + "%");
						}
						System.out.println();
						byte[] fileInBytes = baos.toByteArray();
						dos.close();
						baos.close();
						dis.close();

						if(fileInBytes.length > limit)
						{
							fileChunk = new byte[limit];
							ByteArrayInputStream bais = new ByteArrayInputStream(fileInBytes);

							chunkSize = c = 0; 
							np = (int) (fileInBytes.length / fileChunk.length);
							if(fileInBytes.length % fileChunk.length > 0)
								np++;
							while(c < np)
							{
								ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
								ObjectOutputStream oos1 = new ObjectOutputStream(baos1);
								chunkSize = bais.read(fileChunk);
								Data d = new Data(c + 1, fileChunk, chunkSize, np, fName);
								oos1.writeObject(d);
								oos1.flush();
								byte[] tmp = baos1.toByteArray();
								DatagramPacket dp = new DatagramPacket(tmp, tmp.length, dst, 1234);
								cl.send(dp);
								c++;
								System.out.println("Sent part number: " + c + " of " + np);
								oos1.close();
								baos1.close();
							}
							bais.close();
							cl.close();
							System.out.println("File totally sent.");
						}
						else
						{
							ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
							ObjectOutputStream oos1 = new ObjectOutputStream(baos1);

							Data data1 = new Data(1, fileInBytes, fileInBytes.length, 1, fName);
							oos1.writeObject(data1);
							oos1.flush();

							byte[] bytes = baos1.toByteArray();
							DatagramPacket p = new DatagramPacket(bytes, bytes.length, dst, 1234);
							cl.send(p);
							oos1.close();
							baos1.close();
							cl.close();
							System.out.println("\nFile sent.");	
						}
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);

		Scene scene = new Scene(hbox, 200, 200);
		primaryStage.setTitle("EnviaFX");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}