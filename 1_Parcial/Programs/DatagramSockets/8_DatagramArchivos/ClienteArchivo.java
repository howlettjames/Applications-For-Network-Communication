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

public class ClienteArchivo extends Application
{
	long limite = 20000;

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
							String nombre = f.getName();
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
								System.out.println("La direccion no es valida");
								System.exit(1);
							}

							System.out.println("Se enviara el archivo: " + path + " que mide " + tam + " bytes.");
							byte[] nombreEnBytes = f.getName().getBytes();
							DatagramPacket pName = new DatagramPacket(nombreEnBytes, nombreEnBytes.length, dst, 1234);
							cl.send(pName);

							DataInputStream dis = new DataInputStream(new FileInputStream(path));
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(baos);

							byte[] b = new byte[1000];
							long enviados = 0;
							int porciento = 0, n = 0;
							while(enviados < tam)
							{
								n = dis.read(b);
								dos.write(b, 0, n);
								dos.flush();
								enviados += n;
								porciento = (int) ((enviados * 100) / tam);
								System.out.print("\rTransmitido el: " + porciento + "%");
							}
							byte[] archivoEnBytes = baos.toByteArray();

							if(archivoEnBytes.length > limite)
							{
								ByteArrayInputStream bais = new ByteArrayInputStream(archivoEnBytes);
								int n1 = 0;
								byte[] pedazoArchivo = new byte[100];
								while((n1 = bais.read(pedazoArchivo)) != -1)
								{
									DatagramPacket p = new DatagramPacket(pedazoArchivo, pedazoArchivo.length, dst, 1234);
									cl.send(p);
								}
							}
							else
							{	
								DatagramPacket p = new DatagramPacket(archivoEnBytes, archivoEnBytes.length, dst, 1234);
								cl.send(p);
							}
							dis.close();
							dos.close();
							cl.close();
							System.out.println("\nArchivo enviado.");
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
					System.out.println("Nada");
					
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