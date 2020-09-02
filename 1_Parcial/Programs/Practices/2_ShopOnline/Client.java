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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Application
{
	DatagramSocket s, cl;
	DatagramPacket p;
	InetAddress dst = null;
	byte[] b;
	ByteArrayInputStream bais, bais1;
	ByteArrayOutputStream baos;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	DataInputStream dis;
	DataOutputStream dos;
	Object o;
	Article article;
	String imagesFolderString = "C:/Users/James/Documents/ESCOM_SEMESTRE_7/3CM7_REDESII/1_Parcial/Programs/Practices/2_ShopOnline/ImagesClient/";
	File clientStockFile = new File("ClientStock.txt");
	long size;
	int bytesRead, i, j, imageSelected = 0, noUpdated;
	short articleQuantity, articleStock;
	short[] quantitiesOfArticles = new short[6];
	short[] realQuantitiesOfArticles = new short[6];
	short[] quantitiesStockCart = new short[6];
	double totalPriceCart = 0;
	ArrayList<Article> catalogAL = new ArrayList<>();
	String msg;
	PrintWriter printWriter;
	Scanner scanner;

	public void start(Stage primaryStage)
	{
		//------------------------------------------------------ MAIN STAGE --------------------------------------------------//
		VBox mainPane = new VBox(10);
		HBox buttonsPane = new HBox(200);
		Button btConnect = new Button("Connect");
		Button btCart = new Button("Shopping Cart");
		Button btExit = new Button("Exit");
		FlowPane catalogPane = new FlowPane();
		Image emptyImage = new Image("file:///" + imagesFolderString + "empty.jpg");
		Image[] images = new Image[18];
		ImageView[] imagesViews = new ImageView[6];

		mainPane.setPadding(new Insets(10));
		buttonsPane.setPadding(new Insets(10));
		catalogPane.setPadding(new Insets(10));
		catalogPane.setHgap(10);
		catalogPane.setVgap(10);

		for(i = 0; i < 6; i++)
		{
			imagesViews[i] = new ImageView(emptyImage);
			imagesViews[i].setFitHeight(200);
			imagesViews[i].setFitWidth(200);
			catalogPane.getChildren().add(imagesViews[i]);
		}					

		buttonsPane.getChildren().addAll(btConnect, btCart, btExit);
		mainPane.getChildren().addAll(catalogPane, buttonsPane);

		Scene scene = new Scene(mainPane, 670, 500);
		//scene.getStylesheets().add("/Chart.css");
		primaryStage.setTitle("MINAS MORGUL STOCK");
		primaryStage.setScene(scene);
		primaryStage.show();

		//------------------------------------------------------ ARTICLE STAGE --------------------------------------------------//
		VBox vbox1 = new VBox(20);
		HBox hbox1 = new HBox(50);
		HBox hbox2 = new HBox(45);
		HBox hbox3 = new HBox(40);
		Image[] imagesArticleDemos = new Image[3];
		ImageView[] imagesViewsArticleDemos = new ImageView[3];
		Button btAdd = new Button("+");
		Button btSub = new Button("-");
		Button btSubmitQuantity = new Button("Submit Quantity");
		Label lbQuantity = new Label("Quantity: ");
		Label lbId = new Label("ID: ");
		Label lbName = new Label("Name: ");
		Label lbClass = new Label("Classification: ");
		Label lbDescription = new Label("Description: ");
		Label lbPrice = new Label("Price: $");
		Label lbStock = new Label("Stock: ");
		Label lbArticleTotalPrice = new Label("Article Total Price: $");
		TextField tfArticleTotalPrice = new TextField("0");
		TextField tfStock = new TextField("0");
		TextField tfQuantity = new TextField("0");

		vbox1.setPadding(new Insets(10));
		hbox1.setPadding(new Insets(10));
		hbox2.setPadding(new Insets(10));
		hbox3.setPadding(new Insets(10));

		lbDescription.setWrapText(true);
		tfQuantity.setEditable(false);
		tfStock.setEditable(false);
		tfStock.setPrefColumnCount(5);
		tfQuantity.setPrefColumnCount(5);
		tfArticleTotalPrice.setPrefColumnCount(10);

		for(i = 0; i < 3; i++)
		{
			imagesViewsArticleDemos[i] = new ImageView(emptyImage);
			imagesViewsArticleDemos[i].setFitHeight(220);
			imagesViewsArticleDemos[i].setFitWidth(220);
			hbox1.getChildren().add(imagesViewsArticleDemos[i]);
		}

		//Initializing array for further use
		for(i = 0; i < 6; i++)
			quantitiesOfArticles[i] = 0;

		hbox2.getChildren().addAll(lbId, lbName, lbClass, lbPrice, lbStock, tfStock);
		hbox3.getChildren().addAll(lbQuantity, tfQuantity, btAdd, btSub, btSubmitQuantity, lbArticleTotalPrice, tfArticleTotalPrice);
		vbox1.getChildren().addAll(hbox1, hbox2, lbDescription, hbox3);

		Stage articleStage = new Stage();
		Scene articleScene = new Scene(vbox1, 800, 550);
		articleStage.setScene(articleScene);
		articleStage.setTitle("ARTICLE");	

		//------------------------------------------------------ CART STAGE --------------------------------------------------//
		VBox vbox2 = new VBox(20);
		HBox hbox4 = new HBox(140);
		HBox hbox6 = new HBox(50);
		HBox[] hbArticles = new HBox[6];
		HBox hbox5 = new HBox(150);
		HBox hbox7 = new HBox(150);
		Label lbCartName = new Label("Name");
		Label lbCartQuantity = new Label("Quantity");
		Label lbCartPrice = new Label("Price");
		Label[] lbNames = new Label[6];
		Label[] lbQuantities = new Label[6];
		Label[] lbPrices = new Label[6];
		Label lbTotalPrice = new Label("Total Price: $");
		TextField tfTotalPrice = new TextField("0");
		Button btPurchase = new Button("Purchase");
		Button btSaveCart = new Button("Save Cart");

		vbox2.setPadding(new Insets(10));
		hbox4.setPadding(new Insets(10));
		hbox6.setPadding(new Insets(10));
		for(i = 0; i < 6; i++)
		{
			hbArticles[i] = new HBox(60);
			hbArticles[i].setPadding(new Insets(10));
		}
		hbox5.setPadding(new Insets(10));
		hbox7.setPadding(new Insets(10));

		tfTotalPrice.setEditable(false);	
		tfTotalPrice.setPrefColumnCount(12);

		for(i = 0; i < 6; i++)
		{
			lbNames[i] = new Label("-");
			lbQuantities[i] = new Label("-");
			lbPrices[i] = new Label("-");
		}
		
		hbox6.getChildren().addAll(lbCartQuantity, lbCartPrice);
		hbox4.getChildren().addAll(lbCartName, hbox6);
		for(i = 0; i < 6; i++)
			hbArticles[i].getChildren().addAll(lbNames[i], lbQuantities[i], lbPrices[i]);
		hbox5.getChildren().addAll(lbTotalPrice, tfTotalPrice);
		hbox7.getChildren().addAll(btPurchase, btSaveCart);
		vbox2.getChildren().addAll(hbox4, hbArticles[0], hbArticles[1], hbArticles[2], hbArticles[3], hbArticles[4], hbArticles[5], hbox5, hbox7);

		Stage cartStage = new Stage();
		Scene cartScene = new Scene(vbox2, 450, 600);
		cartStage.setScene(cartScene);
		cartStage.setTitle("SHOPPING CART");	

		//------------------------------------------------------ TICKET STAGE --------------------------------------------------//
		VBox ticketPane = new VBox(10);
		HBox hbTicketInfoStore = new HBox(30);
		HBox[] hbsTicketItems = new HBox[6];
		HBox hbTicketInfoPurchase = new HBox(50);
		Label lbTicketDate = new Label("March 3rd 2019");
		Label lbTicketStoreName = new Label("Minas Morgul Store");
		Label lbTicketHour = new Label("14:31 hrs");
		Label lbTicketItemsPurchased = new Label("Items purchased: ");
		Label[] lbsTicketItemsName = new Label[6];
		Label[] lbsTicketItemsPrice = new Label[6];
		Label[] lbsTicketItemsQuantity = new Label[6];
		Label lbTicketTotalPrice = new Label("Total price: ");
		TextField tfTicketTotalPrice = new TextField("");
		Button btTicketOk = new Button("OK");	

		ticketPane.setPadding(new Insets(10));
		hbTicketInfoStore.setPadding(new Insets(10));
		for(i = 0; i < 6; i++)
		{
			hbsTicketItems[i] = new HBox(20);
			hbsTicketItems[i].setPadding(new Insets(10));
		}
		hbTicketInfoPurchase.setPadding(new Insets(10));
		tfTicketTotalPrice.setEditable(false);

		hbTicketInfoStore.getChildren().addAll(lbTicketDate, lbTicketStoreName, lbTicketHour);
		for(i = 0; i < 6; i++)
		{
			lbsTicketItemsName[i] = new Label("-");
			lbsTicketItemsPrice[i] = new Label("-");
			lbsTicketItemsQuantity[i] = new Label("-");
			hbsTicketItems[i].getChildren().addAll(lbsTicketItemsName[i], lbsTicketItemsQuantity[i], lbsTicketItemsPrice[i]);
		}
		hbTicketInfoPurchase.getChildren().addAll(lbTicketTotalPrice, tfTicketTotalPrice, btTicketOk);
		ticketPane.getChildren().addAll(hbTicketInfoStore, lbTicketItemsPurchased);
		for(i = 0; i < 6; i++)
			ticketPane.getChildren().add(hbsTicketItems[i]);
		ticketPane.getChildren().addAll(hbTicketInfoPurchase);

		Stage ticketStage = new Stage();
		Scene ticketScene = new Scene(ticketPane, 450, 600);
		ticketStage.setScene(ticketScene);
		ticketStage.setTitle("TICKET");
		// ---------------------------------------------------- IMAGE VIEWS -------------------------------------------------- //
		
		imagesViews[0].setOnMouseClicked
		(
			e ->
			{
				imageSelected = 0;
				primaryStage.toBack();
				//Cleaning text
				lbId.setText("");
				lbName.setText("");
				lbClass.setText("");
				lbPrice.setText("");
				tfStock.setText("0");
				lbDescription.setText("");
				tfQuantity.setText("0");
				tfArticleTotalPrice.setText("0");
				//CLeaning quantities of articles (not the real)
				quantitiesOfArticles[imageSelected] = 0;
				//Setting text
				lbId.setText("ID: " + catalogAL.get(imageSelected).id);
				lbName.setText("Name: " + catalogAL.get(imageSelected).name);
				lbClass.setText("Classification: " + catalogAL.get(imageSelected).classification);
				lbPrice.setText("Price: $" + catalogAL.get(imageSelected).price);
				lbDescription.setText("Description: " + catalogAL.get(imageSelected).description);
				tfStock.setText("" + (catalogAL.get(imageSelected).stock - quantitiesStockCart[imageSelected]));
				//Setting images
				for(i = 0; i < 3; i++)
				{
					imagesArticleDemos[i] = new Image("file:///" + imagesFolderString + catalogAL.get(imageSelected).name + (i + 1) + ".jpg");
					imagesViewsArticleDemos[i].setImage(imagesArticleDemos[i]);
				}
				articleStage.show();		
			}
		);
		
		imagesViews[1].setOnMouseClicked
		(
			e ->
			{
				imageSelected = 1;
				primaryStage.toBack();
				//Cleaning text
				lbId.setText("");
				lbName.setText("");
				lbClass.setText("");
				lbPrice.setText("");
				tfStock.setText("0");
				lbDescription.setText("");
				tfQuantity.setText("0");
				tfArticleTotalPrice.setText("0");
				//CLeaning quantities of articles (not the real)
				quantitiesOfArticles[imageSelected] = 0;
				//Setting text
				lbId.setText("ID: " + catalogAL.get(imageSelected).id);
				lbName.setText("Name: " + catalogAL.get(imageSelected).name);
				lbClass.setText("Classification: " + catalogAL.get(imageSelected).classification);
				lbPrice.setText("Price: $" + catalogAL.get(imageSelected).price);
				lbDescription.setText("Description: " + catalogAL.get(imageSelected).description);
				tfStock.setText("" + (catalogAL.get(imageSelected).stock - quantitiesStockCart[imageSelected]));
				//Setting images
				for(i = 0; i < 3; i++)
				{
					imagesArticleDemos[i] = new Image("file:///" + imagesFolderString + catalogAL.get(imageSelected).name + (i + 1) + ".jpg");
					imagesViewsArticleDemos[i].setImage(imagesArticleDemos[i]);
				}
				articleStage.show();		
			}
		);

		imagesViews[2].setOnMouseClicked
		(
			e ->
			{
				imageSelected = 2;
				primaryStage.toBack();
				//Cleaning text
				lbId.setText("");
				lbName.setText("");
				lbClass.setText("");
				lbPrice.setText("");
				tfStock.setText("0");
				lbDescription.setText("");
				tfQuantity.setText("0");
				tfArticleTotalPrice.setText("0");
				//CLeaning quantities of articles (not the real)
				quantitiesOfArticles[imageSelected] = 0;
				//Setting text
				lbId.setText("ID: " + catalogAL.get(imageSelected).id);
				lbName.setText("Name: " + catalogAL.get(imageSelected).name);
				lbClass.setText("Classification: " + catalogAL.get(imageSelected).classification);
				lbPrice.setText("Price: $" + catalogAL.get(imageSelected).price);
				lbDescription.setText("Description: " + catalogAL.get(imageSelected).description);
				tfStock.setText("" + (catalogAL.get(imageSelected).stock - quantitiesStockCart[imageSelected]));
				//Setting images
				for(i = 0; i < 3; i++)
				{
					imagesArticleDemos[i] = new Image("file:///" + imagesFolderString + catalogAL.get(imageSelected).name + (i + 1) + ".jpg");
					imagesViewsArticleDemos[i].setImage(imagesArticleDemos[i]);
				}
				articleStage.show();		
			}
		);

		imagesViews[3].setOnMouseClicked
		(
			e ->
			{
				imageSelected = 3;
				primaryStage.toBack();
				//Cleaning text
				lbId.setText("");
				lbName.setText("");
				lbClass.setText("");
				lbPrice.setText("");
				tfStock.setText("0");
				lbDescription.setText("");
				tfQuantity.setText("0");
				tfArticleTotalPrice.setText("0");
				//CLeaning quantities of articles (not the real)
				quantitiesOfArticles[imageSelected] = 0;
				//Setting text
				lbId.setText("ID: " + catalogAL.get(imageSelected).id);
				lbName.setText("Name: " + catalogAL.get(imageSelected).name);
				lbClass.setText("Classification: " + catalogAL.get(imageSelected).classification);
				lbPrice.setText("Price: $" + catalogAL.get(imageSelected).price);
				lbDescription.setText("Description: " + catalogAL.get(imageSelected).description);
				tfStock.setText("" + (catalogAL.get(imageSelected).stock - quantitiesStockCart[imageSelected]));
				//Setting images
				for(i = 0; i < 3; i++)
				{
					imagesArticleDemos[i] = new Image("file:///" + imagesFolderString + catalogAL.get(imageSelected).name + (i + 1) + ".jpg");
					imagesViewsArticleDemos[i].setImage(imagesArticleDemos[i]);
				}
				articleStage.show();		
			}
		);

		imagesViews[4].setOnMouseClicked
		(
			e ->
			{
				imageSelected = 4;
				primaryStage.toBack();
				//Cleaning text
				lbId.setText("");
				lbName.setText("");
				lbClass.setText("");
				lbPrice.setText("");
				tfStock.setText("0");
				lbDescription.setText("");
				tfQuantity.setText("0");
				tfArticleTotalPrice.setText("0");
				//CLeaning quantities of articles (not the real)
				quantitiesOfArticles[imageSelected] = 0;
				//Setting text
				lbId.setText("ID: " + catalogAL.get(imageSelected).id);
				lbName.setText("Name: " + catalogAL.get(imageSelected).name);
				lbClass.setText("Classification: " + catalogAL.get(imageSelected).classification);
				lbPrice.setText("Price: $" + catalogAL.get(imageSelected).price);
				lbDescription.setText("Description: " + catalogAL.get(imageSelected).description);
				tfStock.setText("" + (catalogAL.get(imageSelected).stock - quantitiesStockCart[imageSelected]));
				//Setting images
				for(i = 0; i < 3; i++)
				{
					imagesArticleDemos[i] = new Image("file:///" + imagesFolderString + catalogAL.get(imageSelected).name + (i + 1) + ".jpg");
					imagesViewsArticleDemos[i].setImage(imagesArticleDemos[i]);
				}
				articleStage.show();		
			}
		);

		imagesViews[5].setOnMouseClicked
		(
			e ->
			{
				imageSelected = 5;
				primaryStage.toBack();
				//Cleaning text
				lbId.setText("");
				lbName.setText("");
				lbClass.setText("");
				lbPrice.setText("");
				tfStock.setText("0");
				lbDescription.setText("");
				tfQuantity.setText("0");
				tfArticleTotalPrice.setText("0");
				//CLeaning quantities of articles (not the real)
				quantitiesOfArticles[imageSelected] = 0;
				//Setting text
				lbId.setText("ID: " + catalogAL.get(imageSelected).id);
				lbName.setText("Name: " + catalogAL.get(imageSelected).name);
				lbClass.setText("Classification: " + catalogAL.get(imageSelected).classification);
				lbPrice.setText("Price: $" + catalogAL.get(imageSelected).price);
				lbDescription.setText("Description: " + catalogAL.get(imageSelected).description);
				tfStock.setText("" + (catalogAL.get(imageSelected).stock - quantitiesStockCart[imageSelected]));
				//Setting images
				for(i = 0; i < 3; i++)
				{
					imagesArticleDemos[i] = new Image("file:///" + imagesFolderString + catalogAL.get(imageSelected).name + (i + 1) + ".jpg");
					imagesViewsArticleDemos[i].setImage(imagesArticleDemos[i]);
				}
				articleStage.show();		
			}
		);
	// ------------------------------------------------------------- TICKET STAGE BUTTONS --------------------------------//
	btTicketOk.setOnAction
	(
		e ->
		{
			ticketStage.toBack();
		}
	);

	// ------------------------------------------------------------- CART STAGE BUTTONS --------------------------------//
		btSaveCart.setOnAction
		(
			e ->
			{
				try
				{
					printWriter = new PrintWriter(clientStockFile);
					for(i = 0; i < 6; i++)
					{
						if(realQuantitiesOfArticles[i] != 0)
						{
							printWriter.print(realQuantitiesOfArticles[i]);
							printWriter.println();
						}
						else
						{
							printWriter.print(0);
							printWriter.println();
						}	
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				
				printWriter.close();			
			}
		);

		btPurchase.setOnAction
		(
			e ->
			{
				try
				{
					//Cleaning labels of Ticket Stage
					for(i = 0; i < 6; i++)
					{
						lbsTicketItemsName[i].setText("-");
						lbsTicketItemsQuantity[i].setText("-");
						lbsTicketItemsPrice[i].setText("-");
					}

					//Sending we want a purchase
					msg = new String("Purchase");
					b = msg.getBytes();
					p = new DatagramPacket(b, b.length, dst, 1234);
					cl.send(p);

					//Sending number of items to be updated
					noUpdated = 0;
					for(i = 0; i < 6; i++)
						if(realQuantitiesOfArticles[i] != 0)
							noUpdated++;

					msg = new String(noUpdated + "");
					b = msg.getBytes();
					p = new DatagramPacket(b, b.length, dst, 1234);
					cl.send(p);

					//Sending wich items will be updated
					for(i = 0; i < 6; i++)
						if(realQuantitiesOfArticles[i] != 0)
						{
							//Sending Items as Objects
							baos = new ByteArrayOutputStream();
							oos = new ObjectOutputStream(baos);
							article = new Article((short) (i + 1), catalogAL.get(i).name, "", "", 0, realQuantitiesOfArticles[i]);
							oos.writeObject(article);
							oos.flush();
							b = baos.toByteArray();						
							p = new DatagramPacket(b, b.length, dst, 1234);
							cl.send(p);
							oos.close();
							baos.close();
							System.out.println("Item to be purchased sent");
							//Copying items to be purchased into ticket labels
							lbsTicketItemsName[i].setText(lbNames[i].getText());
							lbsTicketItemsQuantity[i].setText(lbQuantities[i].getText());
							lbsTicketItemsPrice[i].setText(lbPrices[i].getText());
						}

					//Setting total price for Ticket Stage and showing it
					tfTicketTotalPrice.setText(totalPriceCart + "");	
					ticketStage.show();

					//Cleaning labels of Cart Stage
					for(i = 0; i < 6; i++)
					{
						lbNames[i].setText("-");
						lbQuantities[i].setText("-");
						lbPrices[i].setText("-");
					}

					//Cleaning Cart Save
					printWriter = new PrintWriter(clientStockFile);
					for(i = 0; i < 6; i++)
					{
						printWriter.print(0);
						printWriter.println();
					}	
					printWriter.close();
				}	
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);
		
	// ------------------------------------------------------------- ARTICLE STAGE BUTTONS --------------------------------//	
		btAdd.setOnAction
		(
			e ->
			{
				articleStock = Short.parseShort(tfStock.getText());
				articleQuantity = Short.parseShort(tfQuantity.getText());
				if(articleStock > 0)
				{
					tfStock.setText(--articleStock + "");
					tfQuantity.setText(++articleQuantity + "");
					quantitiesOfArticles[imageSelected] = articleQuantity;
					tfArticleTotalPrice.setText((articleQuantity * catalogAL.get(imageSelected).price) + "");
				}
			}
		);

		btSub.setOnAction
		(
			e ->
			{
				articleStock = Short.parseShort(tfStock.getText());
				articleQuantity = Short.parseShort(tfQuantity.getText());
				if(articleQuantity > 0)
				{
					tfStock.setText(++articleStock + "");
					tfQuantity.setText(--articleQuantity + "");
					quantitiesOfArticles[imageSelected] = articleQuantity;
					tfArticleTotalPrice.setText((articleQuantity * catalogAL.get(imageSelected).price) + "");
				}
			}
		);

		btSubmitQuantity.setOnAction
		(
			e ->
			{
				catalogAL.get(imageSelected).stock = Short.parseShort(tfStock.getText());
				realQuantitiesOfArticles[imageSelected] = quantitiesOfArticles[imageSelected];
			}
		);

	// ------------------------------------------------------------- MAIN STAGE BUTTONS ----------------------------------------//	
		btConnect.setOnAction
		(
			e ->
			{
				try
				{
					//---------------------------------------------- READING CATALOG -------------------------------------------//
					try
					{
						dst = InetAddress.getByName("127.0.0.1");
					}
					catch(UnknownHostException ex)
					{
						System.out.println("La direccion no es valida");
						System.exit(1);
					}
					s = new DatagramSocket(1235);
					cl = new DatagramSocket();

					msg = new String("Hello");
					b = msg.getBytes();
					p = new DatagramPacket(b, b.length, dst, 1234);
					cl.send(p);

					for(i = 0; i < 6; i++)
					{
						p = new DatagramPacket(new byte[65535], 65535, dst, 1235);
						s.receive(p);
						System.out.println("\nDatagram received from: " + p.getAddress() + ":" + p.getPort());
						bais = new ByteArrayInputStream(p.getData());
						ois = new ObjectInputStream(bais);

						o = ois.readObject();
						if(o instanceof Article)
						{
							article = (Article) o;
							catalogAL.add(article);
							System.out.println("Article received ->" + "\nID: " + article.id + "\nName: " + article.name + "\nDescription: " + article.description + "\nClassification: " + article.classification + "\nPrice: " + article.price + "\nStock: " + article.stock);	
							bais1 = new ByteArrayInputStream(article.images);
							for(j = 0; j < 3; j++)
							{
								dis = new DataInputStream(bais1);
								dos = new DataOutputStream(new FileOutputStream(imagesFolderString + article.name + (j + 1) + ".jpg"));

								size = article.imageSizes[j];
								System.out.println("Image: " + article.name + "size -> " + size + " bytes");
								bytesRead = 0;
								//Max size of an image accepted by this program = 50000 bytes
								b = new byte[50000];
								bytesRead = dis.read(b, 0, (int) size);
								dos.write(b, 0, bytesRead);
								dos.flush();
								
								dos.close();
								dis.close();
							}
							bais1.close();
						}
						else
						{
							System.out.println("Object received is not instace of class Article");
							System.exit(1);
						}
						bais.close();
						ois.close();
					}
					//---------------------------------------------- SETTING IMAGES --------------------------------------------//
					for(i = 0; i < 6; i++)
					{
						images[i] = new Image("file:///" + imagesFolderString + catalogAL.get(i).name + "1.jpg");
						imagesViews[i].setImage(images[i]);
					}

					//---------------------------------------------- SETTING PREVIOUS CART SAVE ---------------------------------//
					//Reading Cart Stock saved
					scanner =  new Scanner(clientStockFile);
					for(i = 0; i < 6; i++)
					{
						quantitiesStockCart[i] = scanner.nextShort();
						if(quantitiesStockCart[i] != 0)
						{
							lbNames[i].setText(catalogAL.get(i).name);
							lbQuantities[i].setText(quantitiesStockCart[i] + "");
							lbPrices[i].setText((catalogAL.get(i).price * quantitiesStockCart[i]) + "");			
						}
						else
						{
							lbNames[i].setText("-");
							lbQuantities[i].setText("-");
							lbPrices[i].setText("-");				
						}
						realQuantitiesOfArticles[i] = quantitiesStockCart[i];
					}
					scanner.close();					
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		);

		btCart.setOnAction
		(
			e ->
			{
				primaryStage.toBack();
				totalPriceCart = 0;
				for(i = 0; i < 6; i++)
					if(realQuantitiesOfArticles[i] != 0)
					{
						lbNames[i].setText(catalogAL.get(i).name);
						lbQuantities[i].setText(realQuantitiesOfArticles[i] + "");
						lbPrices[i].setText("$" + (realQuantitiesOfArticles[i] * catalogAL.get(i).price));
						totalPriceCart += realQuantitiesOfArticles[i] * catalogAL.get(i).price;
					}
				tfTotalPrice.setText(totalPriceCart + "");	
				cartStage.show();
			}	
		);

		btExit.setOnAction
		(
			e ->
			{
				s.close();
				cl.close();
				articleStage.close();
				cartStage.close();
				primaryStage.close();
			}
		);
	}
}