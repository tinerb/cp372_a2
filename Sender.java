package cp372_a2;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Sender {

	private JFrame frame;
	private JTextField ipAddressText;
	private JTextField portNumTextReceiver;
	private JTextField filenameText;
	private JTextField datagramSizeText;
	private JTextField timeoutText;
	private JTextField transmissionTime;
	private JTextField portNumTextSender;

	public String ipAddress;
	public int receiverPortNum;
	public int senderPortNum;
	public String filename;
	public int maxDatagramSize;
	public int timeout; // in microseconds
	public DatagramSocket ds, ackSocket;
	public InetAddress ip;
	public byte ack[] = new byte[1];
	public DatagramPacket dp, ackPacket = new DatagramPacket(ack, ack.length);
	public byte arrayToSend[];
	public byte data[];
	public byte count = 0;
	private JTextField outputText;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Sender window = new Sender();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Sender() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("Sender");
		frame.setBounds(100, 100, 295, 348);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("Reciever IP Address:");
		lblNewLabel.setBounds(10, 11, 144, 14);
		frame.getContentPane().add(lblNewLabel);

		// Reciever IP Address text field
		ipAddressText = new JTextField();
		ipAddressText.setBounds(164, 8, 86, 20);
		frame.getContentPane().add(ipAddressText);
		ipAddressText.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Port# for Receiver:");
		lblNewLabel_1.setBounds(10, 36, 144, 14);
		frame.getContentPane().add(lblNewLabel_1);

		// Port# for Reciever text field
		portNumTextReceiver = new JTextField();
		portNumTextReceiver.setBounds(164, 33, 86, 20);
		frame.getContentPane().add(portNumTextReceiver);
		portNumTextReceiver.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Name of File:");
		lblNewLabel_2.setBounds(10, 86, 144, 14);
		frame.getContentPane().add(lblNewLabel_2);

		// Name of File text field
		filenameText = new JTextField();
		filenameText.setBounds(164, 83, 86, 20);
		frame.getContentPane().add(filenameText);
		filenameText.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("Max Size of Datagram:");
		lblNewLabel_3.setBounds(10, 111, 144, 14);
		frame.getContentPane().add(lblNewLabel_3);

		// Max Size of Datagram text field
		datagramSizeText = new JTextField();
		datagramSizeText.setBounds(164, 108, 86, 20);
		frame.getContentPane().add(datagramSizeText);
		datagramSizeText.setColumns(10);

		JLabel lblTimeout = new JLabel("Timeout(ms):  ");
		lblTimeout.setBounds(10, 139, 157, 14);
		frame.getContentPane().add(lblTimeout);

		// Timeout(microseconds) **DISPLAY ONLY** text field
		timeoutText = new JTextField();
		timeoutText.setBounds(164, 136, 86, 20);
		frame.getContentPane().add(timeoutText);
		timeoutText.setColumns(10);

		JButton sendButton = new JButton("Transfer");
		sendButton.setBounds(85, 175, 89, 23);
		frame.getContentPane().add(sendButton);

		JLabel lblNewLabel_4 = new JLabel("Transmission Time(ms): ");
		lblNewLabel_4.setBounds(10, 214, 144, 14);
		frame.getContentPane().add(lblNewLabel_4);

		// Transmission Time **DISPLAY ONLY** text field
		transmissionTime = new JTextField();
		transmissionTime.setEditable(false);
		transmissionTime.setBounds(164, 211, 86, 20);
		frame.getContentPane().add(transmissionTime);
		transmissionTime.setColumns(10);

		JLabel lblNewLabel_5 = new JLabel("Port# for Sender:");
		lblNewLabel_5.setBounds(10, 61, 97, 14);
		frame.getContentPane().add(lblNewLabel_5);

		portNumTextSender = new JTextField();
		portNumTextSender.setBounds(164, 58, 86, 20);
		frame.getContentPane().add(portNumTextSender);
		portNumTextSender.setColumns(10);

		JLabel lblOutput = new JLabel("Output:");
		lblOutput.setBounds(10, 239, 46, 14);
		frame.getContentPane().add(lblOutput);

		outputText = new JTextField();
		outputText.setEditable(false);
		outputText.setBounds(10, 257, 240, 41);
		frame.getContentPane().add(outputText);
		outputText.setColumns(10);

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long startTime = System.currentTimeMillis();
				if (ipAddressText.getText().equals("") || portNumTextReceiver.getText().equals("")
						|| filenameText.getText().equals("") || datagramSizeText.getText().equals("")
						|| timeoutText.getText().equals("") || Integer.parseInt(datagramSizeText.getText()) < 1
						|| Integer.parseInt(datagramSizeText.getText()) > 127) {
					outputText.setText("Please enter valid values");
				} else {
					outputText.setText("File transmission started");
					// store user input
					receiverPortNum = Integer.parseInt(portNumTextReceiver.getText());
					senderPortNum = Integer.parseInt(portNumTextSender.getText());
					ipAddress = ipAddressText.getText();
					filename = filenameText.getText();
					maxDatagramSize = Integer.parseInt(datagramSizeText.getText());
					timeout = Integer.parseInt(timeoutText.getText());

					try {
						ip = InetAddress.getByName(ipAddress);
						ds = new DatagramSocket();
						ackSocket = new DatagramSocket(senderPortNum);
						ackSocket.setSoTimeout(timeout);
						ds.connect(ip, receiverPortNum);
						SendFile outFile = new SendFile(filename);
						arrayToSend = new byte[2 + maxDatagramSize];
						// setting buf array to first chunk
						data = outFile.getByteChunk();
						while (true) {
							if (outFile.offset >= outFile.send_data.length) {
								arrayToSend[0] = -1;
							} else {
								arrayToSend[0] = count;
							}
							arrayToSend[1] = (byte) data.length;
							System.arraycopy(data, 0, arrayToSend, 2, data.length);
							dp = new DatagramPacket(arrayToSend, arrayToSend.length, ip, receiverPortNum);
							ds.send(dp);
							try {
								ackSocket.receive(ackPacket);
								// setting count back proper send number
								if (count == 0 && ackPacket.getData()[0] == 0) {
									count = 1;
									data = outFile.getByteChunk();
								} else if (count == 1 && ackPacket.getData()[0] == 1) {
									count = 0;
									data = outFile.getByteChunk();
								} else if (ackPacket.getData()[0] == -1) {
									break;
								}
							} catch (SocketTimeoutException e1) {

							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					outputText.setText("File transmission ended");
					long endTime = System.currentTimeMillis();
					ds.close();
					transmissionTime.setText(Long.toString(endTime - startTime));
				}
			}
		});
	}

	public class SendFile {

		private int CHUNK_SIZE = maxDatagramSize;

		// Initialize values used in chunked file
		private int offset = 0;
		private byte[] send_data;
		private FileInputStream m_internalStream;

		public SendFile(String fileName) throws FileNotFoundException {
			m_internalStream = new FileInputStream(fileName);
			this.send_data = getBytesFromInputStream(this.m_internalStream);
		}

		private byte[] getBytesFromInputStream(FileInputStream is) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
				byte[] buffer = new byte[CHUNK_SIZE];
				int len;
				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
				os.flush();

				return os.toByteArray();
			} catch (IOException e) {
				// something went wrong return null
				return null;
			}
		}

		// C:\Users\Adam Gumieniak\eclipse-workspace\CP372_a2\src\cp372_a2\README.md

		public byte[] getByteChunk() throws IOException {
			byte[] b = new byte[CHUNK_SIZE];
			// when the end of the file has been found
			if (this.offset >= this.send_data.length) {
				return new byte[0];
			}
			// if the rest of the array is smaller than the max datagram size
			if (this.offset + b.length > this.send_data.length) {

				// find the rest of the to send
				int length = this.send_data.length - this.offset;
				b = new byte[length];
				System.arraycopy(this.send_data, this.offset, b, 0, b.length);
				this.offset += b.length;
				return b;
			}

			// Copy our data into where need it to be
			System.arraycopy(this.send_data, this.offset, b, 0, b.length);
			// adding to offset
			this.offset += b.length;

			return b;
		}

	}
}
