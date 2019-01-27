
// Prakhar Sapre
// 1001514586

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The class implements the Server functionality to send and receive data from
 * client
 * 
 * @author Prakhar
 *
 */
public class Server extends JFrame implements ActionListener {

	// Declare global variables
	static JTextArea txtMessageBox;
	static Socket socket = new Socket();
	JPanel panel;
	JScrollPane scroll;
	JButton btnStopServer;
	JButton btnPollClient;
	static List<ClientHandler> clientList = new ArrayList<ClientHandler>();
	static List<QueuePojo> list = new ArrayList<QueuePojo>();
	static String operations;
	static String initialValue = "1";

	// Server constructor will create the GUI for server to display the messages
	// received from client
	public Server() {
		this.setTitle("Server");
		this.setSize(800, 700);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		// Textarea to display the messages received from the client
		txtMessageBox = new JTextArea();
		txtMessageBox.setBounds(50, 50, 750, 400);
		txtMessageBox.setEditable(false);

		// Button to poll the client
		btnPollClient = new JButton("Poll Client");
		btnPollClient.setBounds(0, 592, 800, 25);
		btnPollClient.addActionListener(this);
		add(btnPollClient);

		// Button to stop the server
		btnStopServer = new JButton("Stop Server");
		btnStopServer.setBounds(100, 600, 80, 50);
		btnStopServer.addActionListener(this);
		add(btnStopServer, BorderLayout.SOUTH);

		// This will give the scroll functionality to the textarea if the messages go
		// beyond the textarea limit.
		scroll = new JScrollPane(txtMessageBox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(scroll, BorderLayout.CENTER);

		this.setVisible(true);
	}

	/**
	 * This action listener method will listen to the button clicks and stop the
	 * server when the stop server button is clicked
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("Poll Client")) {

			DataOutputStream dos = null;
			try {
				dos = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				dos.writeUTF("Poll");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (e.getActionCommand().equals("Stop Server")) {

			try {
				// Close all connections for all clients
				for (int i = 0; i < clientList.size(); i++) {
					socket.close();
					// dis.close();
				}
				System.exit(0);
			} catch (IOException e1) {
			}
		}
	}

	/**
	 * The main method will create a new server object and once the client is
	 * connected to the server it will spawn a new client thread to perform the
	 * transfer of messages.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// create server object
		Server server = new Server();
		txtMessageBox.append("Server started.\n");
		ServerSocket serverSocket = new ServerSocket(5000);
		String clientName;

		// This loop will keep listening for new client connections and create a thread
		// for every client that connects to the server
		while (true) {
			try {
				socket = serverSocket.accept();
				// check if socket is closed or not
				if (!socket.isClosed()) {
					// read the client name received from client and display in the textarea.
					DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					clientName = dis.readUTF();
					txtMessageBox.append("\nClient name " + clientName + " is connected.\n");

					operations = "";
					// create a new thread and initialize it to the clienthandler class
					ClientHandler clientHandler = new ClientHandler(socket, dis, dos, txtMessageBox, clientName, list,
							clientList, operations, initialValue);
					clientList.add(clientHandler);
					Thread t = new Thread(clientHandler);
					t.setName(clientName);
					t.start();
				}

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}

		}

	}

}

/**
 * This class will handle all the client actions
 * 
 * @author Prakhar
 *
 */
class ClientHandler extends Thread implements Runnable {

	// Declare global variables
	final Socket socket;
	final DataInputStream dis;
	final DataOutputStream dos;
	JTextArea txtMessageBox;
	String clientName;
	static String calculateValue;
	List<QueuePojo> list;
	List<ClientHandler> clientList;
	static int count = 0;
	String operations;
	String initialValue;

	// ClientHandler constructor to initialize variables
	public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos, JTextArea txtMessageBox,
			String clientName, List<QueuePojo> list, List<ClientHandler> clientList, String operations,
			String initialValue) {

		this.socket = socket;
		this.dis = dis;
		this.dos = dos;
		this.txtMessageBox = txtMessageBox;
		this.clientName = clientName;
		this.list = list;
		this.clientList = clientList;
		this.operations = operations;
		this.initialValue = initialValue;
	}

	/**
	 * This method is called implicitly to execute the thread and perform any
	 * operations
	 */
	@Override
	public void run() {
		count++;
		String message;
		String finalAnswer;
		List<QueuePojo> toRemove = new ArrayList<QueuePojo>();

		// Keep running the loop to send and recieve messages from client untill the
		// client is killed or server is stopped
		while (true) {
			// check is socket is connected or not
			if (socket.isConnected()) {
				try {
					// read the message received from server or client
					message = dis.readUTF();

					// If the message contains 'Kill', then display which client is killed in server
					// textarea and kill the client
					if (message.contains("Kill")) {
						txtMessageBox.append("\n Client name " + clientName + " has been killed. \n");
						List<QueuePojo> rmv = new ArrayList<QueuePojo>();
						// remove the client from the queue since its killed
						for (QueuePojo obj : list) {
							if (obj.getClientName().equals(clientName)) {
								rmv.add(obj);
							}
						}
						list.removeAll(rmv);
						dos.writeUTF("Killed");
						break;
					}

					// Store client information in entity class
					QueuePojo q = new QueuePojo();
					q.setMessage(message);
					q.setSocket(socket);
					q.setClientName(clientName);
					list.add(q);

					//When server polls the clients for all the operations, it will then compute the result and send it back to all the clients.
					//It will also update its initialValue
					if (message.contains("Poll")) {

						//Remove the client from queue once its operation is fetched by server.
						if (clientList.size() == count) {
							for (QueuePojo obj : list) {
								operations += obj.getMessage();
								toRemove.add(obj);
							}

							operations = operations.replaceAll("[^\\d\\W]", "");
							finalAnswer = calculateAnswer(initialValue + operations);
							txtMessageBox.append("\nOperation:" + (initialValue + operations));
							txtMessageBox.append("\nResult:" + finalAnswer);
							Double val = Double.parseDouble(finalAnswer);
							if (val.isNaN()) {
								initialValue = "1";
							} else {
								initialValue = finalAnswer;
							}
							operations = "";
							for (QueuePojo obj1 : list) {
								DataOutputStream dos = new DataOutputStream(obj1.getSocket().getOutputStream());
								dos.writeUTF(initialValue);
							}
							list.removeAll(toRemove);
						}
					}
				} catch (Exception e) {
					System.out.println("Server/Client stopped. Connection closed.");
					e.printStackTrace();
					break;
				}
			}
		}

	}

	/**
	 * This method will calculate result of all the operations from clients.
	 * 
	 * @param message
	 * @return
	 * @throws ScriptException
	 */
	private String calculateAnswer(String message) throws ScriptException {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		return String.valueOf(engine.eval(message));
	}

}
