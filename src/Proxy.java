import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Proxy {

	static int i = 0;
	static Socket connectionSocket;

	public static void main(String[] args) throws Exception {
		ServerSocket theSocket = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter port Number" + "\n");
		String portNumber = br.readLine();
		int portNum = 2113;
		try {
			portNum = Integer.parseInt(portNumber);
		} catch (NumberFormatException e) {
			System.err.println(e);
		}
		System.out.println("Starting proxy on port " + portNum);
		try {
			theSocket = new ServerSocket(portNum);
			System.out.println("Proxy Started");
			while (true) {
				connectionSocket = theSocket.accept();
				Thread socketThread = new Thread() {
					public void run() {
						try {
							Socket connectionSocket2 = connectionSocket;
							i++;
							System.out.println("Starting Proxy " + i);
							BufferedReader receiveFromClient = new BufferedReader(
									new InputStreamReader(connectionSocket2.getInputStream()));
							DataOutputStream streamToClient = new DataOutputStream(connectionSocket2.getOutputStream());
							List<String> clientRequest = new ArrayList<String>();

							String input = receiveFromClient.readLine();
							while (input.length() > 0) {

								clientRequest.add(input);
								input = receiveFromClient.readLine();
								System.out.println(input);
							}
							String host = "";
							String path = "";
							String method = "";
							String[] request;
							String version = "";
							input = clientRequest.get(0);
							System.out.println(input);
							request = input.split(" ");
							if (request.length == 3) {
								try {
									URL theURL = new URL(request[1]);
									host = theURL.getHost();
									path = theURL.getPath();
									method = request[0];
									version = request[2];

								} catch (Exception e) {

								}

							}

							if (method.equals("GET") && host.length() != 0) {
								try {
									Socket clientSocket = new Socket(host, 80);
									DataOutputStream toServer = new DataOutputStream(clientSocket.getOutputStream());
									InputStream inputMAl = clientSocket.getInputStream();

									PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
									pw.println("GET " + path + " " + "HTTP/1.0");
									pw.println("Host: " + host);
									pw.println("Connection: close");
									pw.println("");
									pw.flush();

									Socket malwareCheckSocket = new Socket("hash.cymru.com", 43);
									PrintWriter pwMalCheck = new PrintWriter(malwareCheckSocket.getOutputStream());
									DataOutputStream toMalCheck = new DataOutputStream(clientSocket.getOutputStream());

									BufferedReader fromMalCheck = new BufferedReader(
											new InputStreamReader(malwareCheckSocket.getInputStream()));

									String hexHash = "";
									byte[] output = null;
									ByteArrayOutputStream getInput = new ByteArrayOutputStream();
									try {
										// get the MD5 of the inputstream
										MessageDigest md = MessageDigest.getInstance("MD5");

										byte[] buffer1 = new byte[8192];
										int numOfBytesRead;
										while ((numOfBytesRead = inputMAl.read(buffer1)) > 0) {
											md.update(buffer1, 0, numOfBytesRead);
											getInput.write(buffer1, 0, numOfBytesRead);

										}
										byte[] hash = md.digest();
										StringBuilder sb = new StringBuilder();
										for (byte b : hash) {
											sb.append(String.format("%02x", b & 0xFF));
										}
										hexHash = sb.toString();

									} catch (Exception ex) {
									}
									byte[] toTheClient = getInput.toByteArray();

									pwMalCheck.println(hexHash);
									pwMalCheck.flush();

									String malCheck = fromMalCheck.readLine();
									String malCheckPrsd[] = malCheck.split(" ");
									if (malCheckPrsd[malCheckPrsd.length - 1].equals("NO_DATA")) {

										streamToClient.write(toTheClient);

									} else {
										// send block website page
										File file = new File("malwarepage.html");
										byte[] bytesArray = new byte[(int) file.length()];

										FileInputStream fis = new FileInputStream(file);
										fis.read(bytesArray);
										fis.close();
										streamToClient.writeBytes("200 OK\n");
										streamToClient.write(bytesArray);
									}

								} catch (Exception e) {
									System.err.println(e);
								}

							} else if (host.length() == 0) {
								System.out.println("400 BAD REQUEST");
								streamToClient.writeBytes("400 BAD REQUEST\n");
							} else if (path.length() == 0) {
								System.out.println("400 BAD REQUEST");
								streamToClient.writeBytes("400 BAD REQUEST\n");
							} else if (method.equals("POST")) {
								System.out.println("501 NOT IMPLEMENTED");
								streamToClient.writeBytes("501 NOT IMPLEMENTED\n");
							} else if (method.equals("DELETE")) {
								System.out.println("501 NOT IMPLEMENTED");
								streamToClient.writeBytes("501 NOT IMPLEMENTED\n");
							} else if (method.equals("PUT")) {
								System.out.println("501 NOT IMPLEMENTED");
								streamToClient.writeBytes("501 NOT IMPLEMENTED\n");
							} else if (method.equals("HEAD")) {
								System.out.println("501 NOT IMPLEMENTED");
								streamToClient.writeBytes("501 NOT IMPLEMENTED\n");
							} else if (method.equals("OPTIONS")) {
								System.out.println("501 NOT IMPLEMENTED");
								streamToClient.writeBytes("501 NOT IMPLEMENTED\n");
							} else if (method.equals("CONNECT")) {
								System.out.println("501 NOT IMPLEMENTED");
								streamToClient.writeBytes("501 NOT IMPLEMENTED\n");
							} else {
								System.out.println("400 BAD REQUEST");
								streamToClient.writeBytes("400 BAD REQUEST\n");
							}
							
							connectionSocket2.close();
							
						}

						catch (Exception e) {

						}

					}

				};
				socketThread.start();

			}

		} catch (Exception e) {
			System.err.println(e);
		}
		

	}

}
