import java.net.*;
import java.nio.*;

public class ServerUDP {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: java ServerUDP <portnumber>");
			return;
		}
		int portNumber = Integer.parseInt(args[0]);
		DatagramSocket serverSocket = new DatagramSocket(portNumber);
		byte[] receiveData = new byte[1024];
		byte[] sendData;

		while(true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			ByteBuffer wrapped = ByteBuffer.wrap(receiveData);
			byte tml = wrapped.get();
			byte opCode = wrapped.get();
			int operand1 = wrapped.getInt();
			int operand2 = wrapped.getInt();
			short requestId = wrapped.getShort();
			byte opNameLength = wrapped.get();
			byte[] opNameBytes = new byte[opNameLength];
			wrapped.get(opNameBytes);

			// Display the request one byte at a time in hexadecimal
			System.out.println("Request in Hex: " + bytesToHex(receiveData, tml));

			// Display the request in a manner convenient for a typical Internet user
			String opName = new String(opNameBytes, "UTF-16BE");
			System.out.println("Request ID: " + requestId);
			System.out.println("Operand1: " + operand1);
			System.out.println("Operand2: " + operand2);
			System.out.println("Operation: " + opName);

			int result = 0;
			byte errorCode = 0; // assume no error
			switch(opCode) {
				case 0: result = operand1 * operand2; break;
				case 1: result = operand1 / operand2; break;
				case 2: result = operand1 | operand2; break;
				case 3: result = operand1 & operand2; break;
				case 4: result = operand1 - operand2; break;
				case 5: result = operand1 + operand2; break;
				default: errorCode = 127; // invalid request
			}

			ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.put((byte)8); // TML
			buffer.putInt(result);
			buffer.put(errorCode);
			buffer.putShort(requestId);
			sendData = buffer.array();

			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		}
	}

	public static String bytesToHex(byte[] bytes, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(String.format("%02X ", bytes[i]));
		}
		return sb.toString();
	}
}