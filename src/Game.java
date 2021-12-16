
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Dimension;




public class Game {
	Scanner scan = new Scanner(System.in);
	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	
	public JFrame frame;
	
	public String ip="localhost";
	public int port = 30000;
	public byte[] hitLoc;
	public int health;
	public int enemyHealth;
	
    
	public Socket socket;
	public DataInputStream input;
	public DataOutputStream output;
	public ServerSocket serverSocket;

	public Paint painter;
	public BufferedImage board;
	public BufferedImage water;
	public BufferedImage missGreen;
	public BufferedImage hitRed;
	public BufferedImage hitGreen;
	public BufferedImage missRed;
	public BufferedImage waterC;
	public BufferedImage waterB;
	public BufferedImage waterD;
	public BufferedImage waterS;
	public BufferedImage hitC;
	public BufferedImage hitB;
	public BufferedImage hitD;
	public BufferedImage hitS;
	
	public boolean yTurn=false;
	public boolean controller = false;
	public boolean player1=false;
	
	 
	
	public static int Gmap[][];
	public static int enemyMap[][];
	
	
	
	
	public Game() {
		health=14;
		enemyHealth=14;
		Gmap = new int [10][10];
		enemyMap = new int[10][10]; 
		
		for(int i=0;i<10;i++) 
			for(int j=0;j<10;j++) {
				Gmap[i][j]=0;
				enemyMap[i][j]=0;
			}
		
		
		hitLoc=new byte[2];
		
		Boats c = new Boats(5);
		Boats b = new Boats(4);
		Boats s = new Boats(3);
		Boats d = new Boats(2);
				
		//System.out.println("IP:");
		//ip=scan.nextLine();
		
		System.out.println("IP automatically chosed: "+ip);
		System.out.println("Port: ");
		port = scan.nextInt();
		
			while(port <1 || port >65535) {
				
				System.out.println("Wrong port try again:");
				port=scan.nextInt();
			}
			if(!connection()) 
				serverInit();
		
			loadAssets();
			
			painter = new Paint();
			painter.setPreferredSize(new Dimension(1496, 780));
			
			frame = new JFrame();
			frame.setTitle("Battleship");
			
			frame.setSize(1496, 780);
			frame.setContentPane(painter);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(true); // it should be false but gameboard is too big
			frame.setVisible(true);
			
			
			
			startGame(c,b,s,d);
			
		
		
		}
		
		
	
	
	public void startGame(Boats c,Boats b, Boats s, Boats d) {
		boolean loc1;
		
		if(player1 && !controller)
		loc1=true;
		
		else
			loc1=false;
		
		
		while(loc1) {
					
			if(player1 && !controller) {
				loc1=waitForPlayer(loc1);
				
			}	
		}
	
		painter.repaint();
		
		askLoc(c,b,s,d);
		
		
		
		
		//renderBoard();
		
		/* while a sok
		 * yturn ise hit loc iste 
		 * hitlocu servera yolla
		 * yturn degil ise serverdan hitlocu al
		 * gmapte kontrol et ve gmapi renderla
		 * sonucu geri yolla
		 * enemymapte yerine koy
		 * yTurnleri degistir
		 * */
		boolean game=true;
		while(game) {
			painter.repaint();
				giveMeLoc();
				game=checkForLose();
				game=checkForWin();
				
			
		}
	}
	
	public boolean checkForWin() {
		if(enemyHealth==0) {
			System.out.println("You Win!");
			
			if(!player1) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				
					}
				}
			
			return false;
		}
		
		return true;
	}
	public boolean checkForLose() {
		if(health==0) {
			System.out.println("You Lose!");
			
			if(!player1) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			
				}
			}
		return false;
		}
		return true;
		
	}
	
	//takes location
	public void giveMeLoc() {
		boolean loc1=yTurn;
		
		if(!yTurn)
			System.out.println("Waiting for Opponent to Attack");
		
		while(!yTurn &&  health>0 && enemyHealth>0) {
			int rowColumn = 0;
			yTurn=waitForLocation(rowColumn);
			
		}
		
		while(yTurn && loc1 && enemyHealth>0 && health>0) {
			
		String location="";
		int row;
		int column;
		int rowColumn;
		
		System.out.println("Hit Location(a1,b5,j2):");
		
		try {
			location=inFromUser.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		row=makeItInt(location.charAt(0));
		column=Integer.parseInt(location.substring(1,location.length()))-1;
		rowColumn=row*10 + column;
		
		if(row>=0 && row <10 && column<10 && column >=0) {
		if(enemyMap[row][column]==0 ) {
			
			sendLoc(rowColumn);
			while(loc1) {
			/*
				try {
					String a=inFromUser.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				loc1=waitForResponse(row,column);
			}
			
		}
		else {
			System.out.println("You already hit there.");
			}
		}
		else
			System.out.println("invalid location");
		
	}
		
	}
	
	

	//waits for which location that attacker will choose and returns map info
	
	public boolean waitForLocation(	int rowColumn){
		
		
		try {
			
			rowColumn=input.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		int column=rowColumn%10;
		int row=(rowColumn-column)/10;
		
		
		if(Gmap[row][column]==0) {
			try {
				
				output.writeInt(0);
				output.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		else {
			try {
				
				output.writeInt(1);
				output.flush();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Gmap[row][column]=Gmap[row][column]*10;
			health--;
			return false;
		}
		
		
		
	}
	
	
	// attacker waits for enemy map info
	//
	public boolean waitForResponse(int row, int column) {
		int response;
		
		try {
			System.out.println("waiting for response...");
			
			response=input.readInt();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
		if(response==0) {
			System.out.println("Missed");
			enemyMap[row][column]=-1;
			painter.repaint();
			yTurn=!yTurn;
			return false;
		}
		else {
			System.out.println("Hit!\nYou can hit one more time:");
			enemyMap[row][column]=1;
			painter.repaint();
			enemyHealth--;
			return false;
		}
		
		
	}
	
	
	// attacker sends where he will attack
	public void sendLoc(int rowColumn) {
		try {
			
			
			output.writeInt(rowColumn);
			//output.writeInt(row);
			//output.writeInt(column);		
			output.flush();
			
			System.out.println("torpedo launched!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	//checks if server initialized
	public boolean connection() {
		
		try {
			socket = new Socket(ip, port);
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
			controller = true;
			
		} catch (IOException e) {
			
			System.out.println("Player 1 is creating the game at: " + ip + ":" + port + "\nWaiting for Player 2...");
			return false;
		}
		System.out.println("Connected to the server. You are Player 2");
		return true;
	}
	// initializes server
	public void serverInit() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		}catch(Exception e) {	
			e.printStackTrace();
		}
		yTurn = true;
		player1=true;
		
	}

	private void loadAssets() {
		try {
			board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
			water = ImageIO.read(getClass().getResourceAsStream("/water.png"));
			missGreen = ImageIO.read(getClass().getResourceAsStream("/greenMiss.png"));
			hitGreen = ImageIO.read(getClass().getResourceAsStream("/greenHit.png"));
			missRed = ImageIO.read(getClass().getResourceAsStream("/redMiss.png"));
			waterB = ImageIO.read(getClass().getResourceAsStream("/waterB.png"));
			waterC =ImageIO.read(getClass().getResourceAsStream("/waterC.png"));
			waterD = ImageIO.read(getClass().getResourceAsStream("/waterD.png"));
			waterS = ImageIO.read(getClass().getResourceAsStream("/waterS.png"));
			hitB = ImageIO.read(getClass().getResourceAsStream("/HitB.png"));
			hitC = ImageIO.read(getClass().getResourceAsStream("/HitC.png"));
			hitD = ImageIO.read(getClass().getResourceAsStream("/HitD.png"));
			hitS = ImageIO.read(getClass().getResourceAsStream("/HitS.png"));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	
	//player 1 waits for opponent
	//Server waits until one more player enters to game
	
	private boolean waitForPlayer(boolean loc1) {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
			controller = true;
			System.out.println("Player 2 joined.");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	
	//Renders the game board
	public void renderBoard(Graphics g) {
	
		g.drawImage(board, 0,0,null);
		
		for(int i=0; i<10;i++)
			for(int j=0;j<10; j++) {
				
				
					if (Gmap[i][j]==0) {
						
							g.drawImage(water, (j * 65) + 66, (i*65)+130, null);
					} 
					else if(Gmap[i][j]==-1) {
						
							g.drawImage(missGreen, (j * 65) + 66, (i*65)+130, null);
					}
					
					
					else if(Gmap[i][j]==1) {
						
						g.drawImage(waterD, (j * 65) + 66, (i*65)+130, null);
				}

					else if(Gmap[i][j]==2) {
						
						g.drawImage(waterS, (j * 65) + 66, (i*65)+130, null);
				}
					else if(Gmap[i][j]==3) {
						
						g.drawImage(waterB, (j * 65) + 66, (i*65)+130, null);
				}
					else if(Gmap[i][j]==4) {
						
						g.drawImage(waterC, (j * 65) + 66, (i*65)+130, null);
				}
					
					else if(Gmap[i][j]==10) {
						
						g.drawImage(hitD, (j * 65) + 66, (i*65)+130, null);
				}
					else if(Gmap[i][j]==20) {
						
						g.drawImage(hitS, (j * 65) + 66, (i*65)+130, null);
				}
					else if(Gmap[i][j]==30) {
						
						g.drawImage(hitB, (j * 65) + 66, (i*65)+130, null);
				}
					else if(Gmap[i][j]==40) {
					
					g.drawImage(hitC, (j * 65) + 66, (i*65)+130, null);
					}
					
					
					
					
				}
				
				
			
		for(int i=0; i<10;i++)
			for(int j=0;j<10; j++) {
				
				if (enemyMap[i][j]==0) {
					
					g.drawImage(water, (j * 65) + 846, (i*65)+130, null);
				} 
				else if (Gmap[i][j]==1) {
					
					g.drawImage(hitGreen, (j * 65) + 846, (i*65)+130, null);
			} 
				else if (Gmap[i][j]==-1) {
					
					g.drawImage(missRed, (j * 65) + 846, (i*65)+130, null);
			} 
				
				
			}
		
	}
	
	// asks where to deploy ships
	
	//player decides location of ships
	public void  askLoc (Boats c, Boats b, Boats s, Boats d) {
		
		Boats a;
		char row;
		int column;
		boolean loc1=true;
		boolean loc2=true;
		boolean loc3=true;
		
		System.out.println("You Have:\n");		
		
		for(int i=0;i<4;i++) {
			
			painter.repaint();
			
			loc1=true;
			loc2=true;
			loc3=true;
			
			System.out.print(c.count+" Carrier, "+b.count+" Battleship, "+s.count+" Submarine, "+d.count+" Destroyer ");
					
			if(i != 0) {
				
				System.out.print(" remains\n");
				
			}
			
			a=Choose(c,b,s,d);
			
			
			while(loc3){
				
				loc1=true;
				loc2=true;
			
				while(loc1) {
					System.out.println("Enter Row (A-J):");
					row=scan.next().charAt(0);
					//row=inFromUser.readLine();
					row=java.lang.Character.toLowerCase(row);
					if((row=='a' || row=='b' || row=='c' || row=='d' || row=='e' || row=='f' || row=='g' || row=='h' || row=='i'|| row=='j')) {
					a.location[0]=makeItInt(row);
						
						loc1=false;
					}
					else System.out.println("Try again");
			
				}
			
				while(loc2) {
					System.out.println("Enter Column (1-10):");
					column=scan.nextInt();
					if(column<=10 && column>0 && Gmap[a.location[0]][column-1]==0) {
						a.location[1]=column-1;
						loc2=false;
					}
					else System.out.println("Try again");
				}
							
				loc3=ChooseDirection(a);
				
				//renderBoard();
							
			}						
		}		
	}
	
	// player choses which direction he will deploy ships
	
	//player decides direction of ship
	public boolean ChooseDirection(Boats a) {
		int count=0;
		String choice="";
		boolean up=false;
		boolean right=false;
		boolean down=false;
		boolean left=false;
		
		if(a.location[0]-a.length-1>=0) {
			for(int i=0;i<a.length;i++) {
				if(Gmap[a.location[0]-i][a.location[1]]==0)
					count++;
			}
			if(count==a.length)
				up=true;
			
		}
		count=0;
		
		if(a.location[1]-a.length-1>=0) {
			
			for(int i=0;i<a.length;i++) {
				if(Gmap[a.location[0]][a.location[1]-i]==0)
					count++;
			}
			if(count==a.length)
				left=true;
		}
		
		count=0;
		
		if(a.location[0]+a.length-1<10) {
			
			for(int i=0;i<a.length;i++) {
				if(Gmap[a.location[0]+i][a.location[1]]==0)
					count++;
			}
			if(count==a.length)
				down=true;
			
		}
		count=0;
		
		if(a.location[1]+a.length-1<10) {
			for(int i=0;i<a.length;i++) {
				if(Gmap[a.location[0]][a.location[1]+i]==0)
					count++;
			}
			if(count==a.length)
				right=true;
			
		}
		
		System.out.print("You can go ");
		
		if(up)
			System.out.print("up ");
		
		if(left)
			System.out.print("left ");
		if(down)
			System.out.print("down ");
		if(right)
			System.out.print("right ");
		if(up==false && right==false && left==false && down==false) {
			System.out.println("nowhere");
			return true;
			
		}
		System.out.println();
		
		System.out.println("Choose one:");
		
		
		//scan.nextLine();
		try {
			choice=inFromUser.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		choice=choice.toLowerCase();
		placeShip(choice,a);
		
		return false;
		
	}
	
	
	// ships are deployed
	//ships placed according to player's decide
	public void placeShip(String choice, Boats a) {
		
		if(choice.equals("up"))
			for(int i=0;i<a.length;i++)
				Gmap[a.location[0]-i][a.location[1]]=a.notation;
		
		if(choice.equals("down"))
			for(int i=0;i<a.length;i++)
				Gmap[a.location[0]+i][a.location[1]]=a.notation;
		
		if(choice.equals("left"))
			for(int i=0;i<a.length;i++)
				Gmap[a.location[0]][a.location[1]-i]=a.notation;
		
		if(choice.equals("right"))
			for(int i=0;i<a.length;i++)
				Gmap[a.location[0]][a.location[1]+i]=a.notation;
				
	}
	
	
	
	//turns rows to integers
	public int makeItInt(char row) {
		if(row=='a')
			return 0;
		else if(row=='b')
			return 1;
		else if(row=='c')
			return 2;
		else if(row=='d')
			return 3;
		else if(row=='e')
			return 4;
		else if(row=='f')
			return 5;
		else if(row=='g')
			return 6;
		else if(row=='h')
			return 7;
		else if(row=='i')
			return 8;
		else 
			return 9;
		
	}
	
	
	
	
	// player chooses which ship he will place
	// player chooses ship
	public Boats Choose(Boats c,Boats b, Boats s, Boats d) {
		String ship="ship";
		boolean notcorrect=true;
		while(notcorrect) {
		System.out.print("\nChoose Ship:");
		//scan.nextLine();
		try {
			ship=inFromUser.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		ship=ship.toLowerCase();
		
		if((ship.equals("carrier")&& c.count==1) || (ship.equals("destroyer")&& d.count==1) || (ship.equals("submarine") && s.count==1)|| (ship.equals("battleship") && b.count==1))
			notcorrect=false;
			
		else System.out.println("Wrong ship try again...");;	
		
		}
		
		
		if(ship.charAt(0)=='c') {
			c.count--;
			return c;
		}
		else if(ship.charAt(0)=='d') {
			d.count--;
			return d;
			}
		else if(ship.charAt(0)=='s') {
			s.count--;
			return s;
			}
		else {
			b.count--;
			return b;
			}
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Game Battleship = new Game();
		
		
	
	for(int i=0;i<10;i++) {
			
			for(int j=0;j<10;j++) {
				
				System.out.print(Gmap[i][j]+" ");;
				
			}
			System.out.println();
			
		}
	

	for(int i=0;i<10;i++) {
			
			for(int j=0;j<10;j++) {
				
				System.out.print(enemyMap[i][j]+" ");;
				
			}
			System.out.println();
			
		}
		
	}
	
	

	private class Paint extends JPanel {
		private static final long serialVersionUID = 1L;

		public Paint() {
			setFocusable(true);
			requestFocus();
			
			
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			renderBoard(g);
		}
}
}
