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

public class EnviaFX extends Application
{
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
						int pto = 1234;
						String host = "127.0.0.1";
						File f = e.getDragboard().getFiles().get(0);
						if(f != null)
						{
							String nombre = f.getName();
							long tam = f.length();
							String path = f.getAbsolutePath();
							Socket cl = new Socket(host, pto);
							System.out.println("Se enviara el archivo: " + path + " que mide " + tam + " bytes.");
							DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
							DataInputStream dis = new DataInputStream(new FileInputStream(path));

							dos.writeUTF(nombre);
							dos.flush();
							dos.writeLong(tam);
							dos.flush();
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
								System.out.print("\rTransmitido el: " + porciento + "%");
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
					int pto = 1234;
					String host = "127.0.0.1";
					File f = fileChooser.showOpenDialog(primaryStage);
					if(f != null)
					{
						String nombre = f.getName();
						long tam = f.length();
						String path = f.getAbsolutePath();
						Socket cl = new Socket(host, pto);
						System.out.println("Se enviara el archivo: " + path + " que mide " + tam + " bytes.");
						DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
						DataInputStream dis = new DataInputStream(new FileInputStream(path));

						dos.writeUTF(nombre);
						dos.flush();
						dos.writeLong(tam);
						dos.flush();
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
							System.out.print("\rTransmitido el: " + porciento + "%");
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
		);

		Scene scene = new Scene(hbox, 200, 200);
		primaryStage.setTitle("EnviaFX");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}