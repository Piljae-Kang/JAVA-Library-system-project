package cse3040fp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
	public static String clientIp;
	public static int clientPorts;
	public static int userPermission = 0;
	public static String userName = "";
	public static String mode = "";
	public static String input = "";
	static DataInputStream dis;
	static DataOutputStream dos;
	static Scanner scanner;
	public static void sendMessage(Socket socket) {
		try {
			String str = scanner.nextLine();
			dos.writeUTF(str);
		}catch (IOException e) { e.printStackTrace(); }
		
	}
	
	public static String getMessage(Socket socket) {
		String str = "";
		try {
			str = dis.readUTF();
			
		}catch (IOException e) { e.printStackTrace(); }
		
		return str;
	}
	
	public static void sendString (Socket socket, String str) {
		try {
			dos.writeUTF(str);
		}catch (IOException e) { e.printStackTrace(); }
	}
	

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.print("Please give the IP address and port number as arguments.");
			System.exit(0);
			}
		
		clientIp = args[0];
		clientPorts = Integer.parseInt(args[1]);
		try {
		Socket socket = new Socket(clientIp, clientPorts);
		
		if(!socket.isConnected()) {  /////////////////////////// socket connection check
			System.out.println("Connection establishment failed.");
			System.exit(0);
		}
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		scanner = new Scanner(System.in);
		
		while(true) { 
			
			if(userPermission ==0 ) {
				System.out.print("Enter userID>> ");
				sendMessage(socket);
				if(getMessage(socket).equals("permit")) {
					userName = getMessage(socket);
					System.out.println("Hello " + userName + "!");   //// Hello "userName!'
					userPermission = 1;
				}
				else {
					System.out.println("UserID must be a single word with lowercase alphabets and numbers.");
					continue;
				}
			}
			if(userPermission == 1) {
				System.out.print(userName + ">> ");
				mode = scanner.nextLine();
				
				/////////////////////////////////////// input Mode ////////////////////////////////////////////
				
				if(mode.equals("add")) {         
					dos.writeUTF(mode);
					
					System.out.print("add-title> ");
					String bookTitle = scanner.nextLine();
					
					if(bookTitle.length() == 0) {
						sendString(socket, "again");
						
						continue;
					}
					else {
						int space = 0;
						for(int i = 0; i<bookTitle.length(); i++) {
							if(bookTitle.charAt(i) == ' ')
								space++;
						}
						if(space == bookTitle.length()) {
							sendString(socket,"again");
							continue;
						}
						
						sendString(socket, "accept");   
						
						System.out.print("add-author> ");
						String bookAuthor = scanner.nextLine();
						
						if(bookAuthor.length() == 0) {
							sendString(socket, "again");
							continue;
						}
						int spac = 0;
						for(int i = 0; i<bookAuthor.length(); i++) {
							if(bookAuthor.charAt(i) == ' ')
								spac++;
						}
						if(spac == bookAuthor.length()) {
							sendString(socket,"again");
							continue;
						}
						
						sendString(socket, "accept"); 
						
						sendString(socket, bookTitle); // send book title
						sendString(socket, bookAuthor); // send book author
						
						if(getMessage(socket).equals("already")) {
							System.out.println("The book already exists in the list.");
							continue;
						}
						
						else {
						sendString(socket, bookAuthor);
						System.out.println("A new book added to the list.");
						}	
					}
					
					continue;
				}
		/////////////////////////////  add  /////////////////////////////////////////
				
				if(mode.equals("borrow")) {
					dos.writeUTF(mode);
					
					System.out.print("borrow-title> ");
					String borrowBookTitle = scanner.nextLine();
					
					if(borrowBookTitle.length() == 0) {
						sendString(socket, "again");
						continue;
					}
					
					int space = 0;
					for(int i = 0; i<borrowBookTitle.length(); i++) {
						if(borrowBookTitle.charAt(i) == ' ')
							space++;
					}
					if(space == borrowBookTitle.length()) {
						sendString(socket,"again");
						continue;
					}
					
					
					sendString(socket, "accept");
					
					sendString(socket, borrowBookTitle);
					
					if(getMessage(socket).equals("already")) {
						System.out.println("The book is not available.");
						continue;
					}
					
					sendString(socket, userName);
					System.out.println("You borrowed a book." + " - " + getMessage(socket));
					
					
					continue;
				}
		///////////////////////////// borrow ///////////////////////////////////////
				
				if(mode.equals("return")) {
					dos.writeUTF(mode);
					
					System.out.print("return-title> ");
					String returnBookTitle = scanner.nextLine();
					
					if(returnBookTitle.length() == 0) {
						sendString(socket, "again");
						continue;
					}
					int space = 0;
					for(int i = 0; i<returnBookTitle.length(); i++) {
						if(returnBookTitle.charAt(i) == ' ')
							space++;
					}
					if(space == returnBookTitle.length()) {
						sendString(socket,"again");
						continue;
					}
					
					sendString(socket, "accept");
					
					sendString(socket, returnBookTitle);
					sendString(socket, userName);
					
					if(getMessage(socket).equals("empty")) {
						System.out.println("You did not borrow the book.");
						continue;
					}
					
					System.out.println("You returned a book." + " - " + getMessage(socket));
					
					continue;
				}
				
				///////////////////////////// return ///////////////////////////////////////
				
				if(mode.equals("info")) {
					dos.writeUTF(mode);
					
					System.out.print(getMessage(socket));
					continue;
				}
				///////////////////////////// info ////////////////////////////////////////
				
				if(mode.equals("search")) {
					dos.writeUTF(mode);
					String word = "";
					int num;
					while(true) {
						System.out.print("search-string> ");
						
						String searchWords = scanner.nextLine();
						num = searchWords.length();
					
						if(num == 0)
							break;
					
						else if(num < 3) {
							System.out.println("Search string must be longer than 2 characters.");
							continue;
						}
						else {
							word = searchWords;
							break;
						}
						
					}
					
					if(num == 0) {
						sendString(socket, "quit");
						continue;
					}
					
					else {
						sendString(socket, word);
						
						System.out.print(getMessage(socket));
					}
					continue;
				}
				//////////////////////////// search ///////////////////////////////////////
				
				else {
					System.out.println("[available commands]\n" +
							"add: add a new book to the list of books.\n" +
							"borrow: borrow a book from the library.\n" +
							"return: return a book to the library.\n" +
							"info: show list of books I am currently borrowing.\n" +
							"search: search for books.");
					
					
					continue;
				}
				
				
			}
			
			
			
		}
		}
		catch(ConnectException e) {System.out.print("Connection establishment failed.");}
		catch(IOException ce) {
			ce.printStackTrace();
		} catch(Exception ee) {ee.printStackTrace();}
	}

}

