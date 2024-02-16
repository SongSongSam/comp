import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;

public class ClientUDP {
	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: java ClientUDP servername PortNumber");
			return;
		}

		String serverName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(serverName);
		byte[] sendData;
		byte[] receiveData = new byte[8];

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Enter OpCode, Operand1, Operand2: ");
			int opCode = scanner.nextInt();
			int operand1 = scanner.nextInt();
			int operand2 = scanner.nextInt();

			String opName = "";
			switch(opCode) {
				case 0: opName = "multiplication"; break;
				case 1: opName = "division"; break;
				case 2: opName = "or"; break;
				case 3: opName = "and"; break;
				case 4: opName = "subtraction"; break;
				case 5: opName = "addition"; break;
			}
			byte[] opNameBytes = opName.getBytes("UTF-16BE");
			int opNameLength = opNameBytes.length;
			Random rand = new Random();
			int requestId = rand.nextInt(100); // Randomly generated request ID

			ByteBuffer buffer = ByteBuffer.allocate(13 + opNameLength);
			buffer.put((byte)(13 + opNameLength)); // TML
			buffer.put((byte)opCode);
			buffer.putInt(operand1);
			buffer.putInt(operand2);
			buffer.putShort((short)requestId);
			buffer.put((byte)opNameLength);
			buffer.put(opNameBytes);
			sendData = buffer.array();

			System.out.println("Request in Hex: " + bytesToHex(sendData));
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
			long start = System.currentTimeMillis();
			clientSocket.send(sendPacket);

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			long end = System.currentTimeMillis();

			ByteBuffer wrapped = ByteBuffer.wrap(receiveData);
			byte tml = wrapped.get();
			int result = wrapped.getInt();
			byte errorCode = wrapped.get();
			short responseRequestId = wrapped.getShort();

			System.out.println("Response in Hex: " + bytesToHex(receiveData));
			System.out.println("Request ID: " + responseRequestId);
			System.out.println("Result: " + result);
			System.out.println("Error Code: " + (errorCode == 0 ? "Ok" : errorCode));
			System.out.println("Round trip time: " + (end - start) + "ms");

			System.out.println("Enter q to quit or any other key to continue: ");
			String input = scanner.next();
			if (input.equalsIgnoreCase("q")) {
				break;
			}
		}
		clientSocket.close();
	}
}