
// Prakhar Sapre
// 1001514586

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * This class implements the client functionality to send and receive data from
 * server
 * 
 * @author Prakhar
 *
 */
public class Client extends JFrame implements ActionListener, WindowListener {

	// Declare global variables
	static JTextArea txtClientMessages;
	static JTextArea txtMessage;
	static JButton btnSendMessage;
	static JButton btnExecute;
	static JButton btnKillClient;
	static Socket clientSocket;
	static DataInputStream dis;
	static DataOutputStream dos;
	static String clientName;
	int min = 3, max = 10;
	static String initialValue = "1";
	static String operationSeq;
	static File file;
	static FileWriter writer;
	ScriptEngineManager mgr = new ScriptEngineManager();
	ScriptEngine engine = mgr.getEngineByName("JavaScript");

	// Constructor will create the GUI for the client to send name, upload integer
	// and kill client
	public Client() {
		// TODO Auto-generated constructor stub
		this.setTitle("Client");
		this.setSize(800, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);

		// Textarea for displaying the name of the client and other messages received
		// from server
		txtClientMessages = new JTextArea();
		txtClientMessages.setBounds(50, 50, 650, 300);
		txtClientMessages.setEditable(false);
		add(txtClientMessages);

		// Textarea for taking client name as input from user
		txtMessage = new JTextArea();
		txtMessage.setBounds(50, 380, 530, 120);
		txtMessage.setEditable(true);
		add(txtMessage);

		// Send message button to send the client name to server
		btnSendMessage = new JButton("Send");
		btnSendMessage.setBounds(600, 380, 100, 30);
		btnSendMessage.addActionListener(this);
		add(btnSendMessage);

		// Upload number button to send the post http message along with the integer to
		// the server
		btnExecute = new JButton("Execute");
		btnExecute.setBounds(600, 420, 100, 30);
		btnExecute.addActionListener(this);
		add(btnExecute);

		// Kill client button to kill the client and notify the server which client is
		// killed
		btnKillClient = new JButton("Kill Client");
		btnKillClient.setBounds(600, 460, 100, 30);
		btnKillClient.addActionListener(this);
		add(btnKillClient);

		// this will display the gui
		this.setVisible(true);
	}

	/**
	 * This is the action listener method which will listen to button click events
	 * and according to the action command name will send name, upload number or
	 * kill the client
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		try {

			if (arg0.getActionCommand().equals("Send")) {
				sendMessage();
			} else if (arg0.getActionCommand().equals("Execute")) {
				executeSequence();
			} else {
				killClient();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * The killClient method will send a message to the server with the client name
	 * that is killed and shutdown the client thread accordingly
	 */
	public void killClient() {

		try {
			dos.writeUTF("Kill " + clientName);
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * The sendMessage method will fetch the input given by the user from the
	 * textbox and store it as the client name. It will then send the name to server
	 * and will also display in the clients textarea.
	 */
	public void sendMessage() {
		clientName = txtMessage.getText().trim();
		txtClientMessages.append("Client name is " + clientName + "\n");
		File folder = new File("C:\\Users\\Prakhar\\Desktop\\Distributed Systems\\Labs\\Lab3\\ReplicaMgtLab3\\Files\\");
		File[] fileList = folder.listFiles();
		boolean flag = false;
		try {
			dos.writeUTF(clientName);
			
			//For all the files in folder find the clientname file and upload operations from it.
			for (File f : fileList) {
				if (f.isFile() && f.getName().equals(clientName + ".txt")) {
					file = f;
					BufferedReader br = new BufferedReader(new FileReader(f));
					String s;
					while ((s = br.readLine()) != null) {
						operationSeq = s;
						txtClientMessages.append("Operation Sequence uploaded from file after client killed.\n");
						dos.writeUTF(operationSeq);
					}
					flag = true;
					break;
				}
			}

			//If flag is false it means file was not found so we create a new file for persistent storage
			if (flag == false) {
				file = new File("C:\\Users\\Prakhar\\Desktop\\Distributed Systems\\Labs\\Lab3\\ReplicaMgtLab3\\Files\\"
						+ clientName + ".txt");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		btnSendMessage.setEnabled(false);
		txtMessage.setText("");

	}

	/**
	 * Execute the sequence of operations entered by the user.
	 * 
	 * @throws ScriptException
	 * @throws IOException
	 */
	public void executeSequence() throws ScriptException, IOException {
		writer = new FileWriter(file);
		operationSeq = txtMessage.getText().trim();
		txtClientMessages.append("\n Initial value: " + initialValue);
		txtClientMessages.append("\n Operations: " + operationSeq);
		String calculatedAnswer = String.valueOf(engine.eval(initialValue + operationSeq));
		txtClientMessages.append("\n = " + calculatedAnswer);
		Double val = Double.parseDouble(calculatedAnswer);
		if (val.isNaN() || val.isInfinite()) {
			txtClientMessages.append("\n The operation results in NaN or Infinite.");
			operationSeq = "";
		}
		writer.write(operationSeq);
		txtMessage.setText("");
		dos.writeUTF(operationSeq);
		writer.close();
	}

	/**
	 * The main method will create an object of the client so that the gui can be
	 * created and then create a new socket and connect to port 5000.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Create a new Client object
		Client client = new Client();
		int port = 5000;
		clientName = "";
		try {
			clientSocket = new Socket("127.0.0.1", port);
			dos = new DataOutputStream(clientSocket.getOutputStream());

		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());

		}

		// The client will keep on running in this loop to fetch any incoming messages
		// from the server.
		// if the client is killed or the server is stopped then it will break from the
		// loop and kill the
		// connections
		while (true) {

			try {

				dis = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				String message = dis.readUTF();

				// if this message is received from the server then it will break from the loop
				if (message.contains("Killed")) {
					break;
				}

					
				if (message.contains("Poll")) {
					dos.writeUTF("Poll");
					operationSeq = "";
					deleteFileContents();
				}

				initialValue = message;

			} catch (IOException e) {
				txtClientMessages.append("\n Server has stopped. Connection closed.");
				break;
			}
		}
	}
	
	public static void deleteFileContents() throws IOException {
		writer = new FileWriter(file);
		writer.write("");
		writer.close();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Client has been killed.");
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}
