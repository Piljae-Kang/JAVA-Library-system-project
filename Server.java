package cse3040fp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
	public static String serverIp;
	public static int serverPorts;
	public String mode = "";
	Map<String, String> map = new HashMap<>();
	Map<String, String> borrowedMap = new HashMap<>();
	List<String> bookAndAuthor = new ArrayList<>();
	List<String> lowerBookTitle = new ArrayList<>();
	
	class Ascending implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			
			//o1.toLowerCase();
			//o2.toLowerCase();
			int num =0;
			if(o1.length() >= o2.length())
				num = o2.length();
			if(o1.length() < o2.length())
				num = o1.length();
			
			for(int k = 0; k<num; k++) {
			
			char c1 = Character.toLowerCase(o1.charAt(k));
			char c2 = Character.toLowerCase(o2.charAt(k));
			
			
			if(c1 > c2) 
				return 1;
			
			if(c1 < c2) 
				return -1;
			
			if(c1 == c2) {
				
					if (o1.charAt(k) < o2.charAt(k))
						return 1;
					if (o1.charAt(k) > o2.charAt(k))
						return -1;
				
			}
				
			}			
			return 0;
		}
		
	}
	public void textReadReset() {
		
		try {
		BufferedReader br = new BufferedReader(new FileReader("books.txt"));
		
		while(true) {
		String line = br.readLine();
		if(line == null) break;
		
		String [] strArr = line.split("\t");
		 
		map.put(strArr[0], strArr[1] + "/" + strArr[2]);
		bookAndAuthor.add(strArr[0] + ", " + strArr[1]);
		lowerBookTitle.add(strArr[0].toLowerCase());
		
		if(!strArr[2].equals("-")) {
			borrowedMap.put(strArr[0], strArr[2]);
		}
		
		}
		
		br.close();
	}catch(IOException e) {}
		
	}
	
	public void textWriteReset() {
		//bookAndAuthor.sort(null); //list sort
		Collections.sort(bookAndAuthor, new Ascending());
		try {
			FileWriter fw = new FileWriter("books.txt", false);
			for(String str : bookAndAuthor) {
				//System.out.println(str);
				String strArray[] = str.split(", ");
				String booktitle = strArray[0];
				String bookAuthor = strArray[1];
				
				if(borrowedMap.containsKey(booktitle)) {
					String value = borrowedMap.get(booktitle);
					//String [] valueArr = value.split("/");
					//String user = valueArr[1];
					fw.write(booktitle +"\t" + bookAuthor + "\t" + value + "\r\n");
				}
				else
					fw.write(booktitle +"\t" + bookAuthor + "\t" + "-" + "\r\n");
			}
			
			
			fw.close();
		}catch(IOException e) {}
	}
	
	class Library extends Thread {
		Socket socket;
		String userName = "";
		int userInputStep = 0;
		//Map<String, String> map = new HashMap<>();
		//Map<String, String> borrowedMap = new HashMap<>();
		//List<String> bookAndAuthor = new ArrayList<>();
		DataInputStream dis;
		DataOutputStream dos;
		
		public void sendMessage(Socket socket, String str) {
			try {
				dos.writeUTF(str);
			}catch (IOException e) { e.printStackTrace(); }
		}
		
		public String getMessage(Socket socket) {
			String str = "";
			try {
				str = dis.readUTF();
			
			}catch (IOException e) { e.printStackTrace(); }
			
			return str;
		}
		
		
		
		Library(Socket socket){
			this.socket = socket;
			
		}
		
		public void run() {
			try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			while(true) {
				
				if(userInputStep == 0) {           
					userName = getMessage(socket);
					//System.out.println(userName);
						if (userName.matches("^[a-z0-9]*$") && userName.length()>0) {
							//System.out.println(1);
							sendMessage(socket, "permit");
							sendMessage(socket, userName);
							userInputStep = 1;
							//textReadReset();
						}
						else
							sendMessage(socket, "error");
							continue;
				}
				
				if(userInputStep == 1) {           // 이름 승인 났음.
					mode = getMessage(socket);    // 여기서 mode 입력이 들어오기까지 계속 기다림.
					
					if(mode.equals("add")) {
						if(getMessage(socket).equals("again"))
							continue;
						
						if(getMessage(socket).equals("again"))
							continue;
						
						String bookTitle = getMessage(socket);
						//String srr = bookTitle;
						String bookAuthor = getMessage(socket);
						String lowerVersion = bookTitle.toLowerCase();
						if(lowerBookTitle.contains(lowerVersion)) {
							sendMessage(socket, "already");
							continue;
						}
						else {
							sendMessage(socket, "empty");
						
						String bookString = bookAuthor + "/" + userName;
						map.put(bookTitle, bookString); 
						String libraryList = bookTitle + ", " + bookAuthor;
						bookAndAuthor.add(libraryList);   /// 리스트에 넣었엉
						lowerBookTitle.add(lowerVersion);
						//textWriteReset();
						}
					}
					
					if(mode.equals("borrow")) {
						if(getMessage(socket).equals("again"))
							continue;
						
						String borrowBookTitle = getMessage(socket);

						int borrowedFlag = 0;
						for(String str : borrowedMap.keySet()) {
							String a = borrowBookTitle.toLowerCase();
							String b = str.toLowerCase();
							if(a.equals(b)) {
								sendMessage(socket, "already");
								borrowedFlag = 1;
								break;
							}							
						}
						if(borrowedFlag == 1)
							continue;
						
						int num = 0;
						for(String str : map.keySet()) {
							String a = borrowBookTitle.toLowerCase();
							String b = str.toLowerCase();
							if(a.equals(b)) {
								sendMessage(socket, "okay");
								String user = getMessage(socket);
								borrowedMap.put(str, user);
								sendMessage(socket, str);
								num = 1;
								break;
							}							
						}
						
						if(num == 0) {
							sendMessage(socket, "already");
							continue;
						}
						
					}
					
					if(mode.equals("return")) {
						if(getMessage(socket).equals("again"))
							continue;
						
						String returnBookTitle = getMessage(socket);
						String user = getMessage(socket);
						int num = 0;
						for(String str : borrowedMap.keySet()) {
							String a = str.toLowerCase();
							String b = returnBookTitle.toLowerCase();
							
							if(a.equals(b)) {
								if(!user.equals(borrowedMap.get(str)))
										break;
								
								num = 1;
								sendMessage(socket, "already");
								borrowedMap.remove(str);
								sendMessage(socket, str);
								break;
							}
						}
						
						if(num == 0) {
							sendMessage(socket, "empty");
							continue;
						}
					}
					
					if(mode.equals("info")) {
						String book = "";
						int num = 0;
						for(String key : borrowedMap.keySet()) {
							String value = borrowedMap.get(key);
							//String[] str = value.split("/");
							
							
							if(value.equals(userName)) {
								num++;
								book += num + ". " + key +", " + value +"\n";
							}
						}
						
						String info = "You are currently borrowing " + num + " books:\n" + book;
						
						sendMessage(socket, info);
				
					}
					
					if(mode.equals("search")) {
						
						String word = getMessage(socket);
						
						if(word.equals("quit")) {
							continue;
						}
						int num = 0;
						String sr = "";
						
						for (String str : bookAndAuthor) {
							String a = str.toLowerCase();
							String b = word.toLowerCase();
							
							if(a.contains(b)) {
								num++;
								sr += num + ". " + str + "\n";
							}
							
						}
						
						sendMessage(socket, "Your search matched " + num + " results.\n" + sr);
						
						
				}
					
				}
				textWriteReset();
				//System.out.println("Length of book list : " + bookAndAuthor.size());
			}
			}catch (IOException e) { e.printStackTrace(); } catch(Exception ee) {ee.printStackTrace();}
				}
				
			}
		

	public void start() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		textReadReset();
		try {
			serverSocket = new ServerSocket(serverPorts);   // server socket 생성
			while(true) {
			
			socket = serverSocket.accept();
			
			Library library = new Library(socket);
			library.start();
			}
		

		} catch(IOException e) { e.printStackTrace(); }catch(Exception ee) {ee.printStackTrace();}
	}

	
	public static void main(String[] args) {
		try {
		if(args.length != 1) {
			System.out.print("Please give the port number as an argument.");
			System.exit(0);
			}
		serverPorts = Integer.parseInt(args[0]);
		serverIp = "127.0.0.1";
		new Server().start();
		}catch(Exception ee) {ee.printStackTrace();}
	}

}
