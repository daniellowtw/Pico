package sslTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SSLTest {
	private static String HOST = "dlow.me";
	private static int PORT = 8001;

	private static SSLSocketFactory sf;

	/**
	 * Returns a SSL Factory instance that accepts all server certificates.
	 * 
	 * SSLSocket sock = (SSLSocket) getSocketFactory.createSocket(host, 443);
	 * @return An SSL-specific socket factory.
	 **/
	public static final SSLSocketFactory getSocketFactory() {
		if (sf == null) {
			try {
				TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(new KeyManager[0], tm, new SecureRandom());
				sf = (SSLSocketFactory) context
						.getSocketFactory();

			} catch (KeyManagementException e) {
				System.out.println("No SSL algorithm support: " + e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				System.out.println(e.getMessage());
			}
		}
		return sf;
	}

	private SSLSocket sslSocket;

	public static void main(String[] args) throws Exception {
		sf = getSocketFactory();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Socket s = sf.createSocket(HOST, PORT);
		OutputStream out = s.getOutputStream();
		out.write("Client side is ready.\n".getBytes());
		out.flush();
		new Receive(s).start();
		int theCharacter = 0;
		theCharacter = System.in.read();
		while (theCharacter != '~') // The '~' is an escape character to exit
		{
			out.write(theCharacter);
			out.flush();
			theCharacter = System.in.read();
		}
		out.close();
		s.close();
	}
}

class Receive extends Thread {
	Socket s;

	public Receive(Socket s) {
		this.s = s;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			String line = null;
			while (((line = in.readLine()) != null)) {
				System.out.println(s.getRemoteSocketAddress() + ": " + line);
			}
			in.close();
		} catch (IOException e) {
			// This happens when the socket closes
			System.out.println("Exiting");
		}
	}
}
