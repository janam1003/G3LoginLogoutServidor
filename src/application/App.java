package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import thread.WorkerThread;
import Classes.SigninSignup;
import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import factory.ServerFactory;
import java.io.ObjectOutputStream;
import Classes.Message;
import Classes.MessageType;

/**
 *
 * @author Dani
 */
public class App {
	private SigninSignup serverImplementation;

	private static int contador = 0;
	private static final int PORT = (Integer.parseInt(ResourceBundle.getBundle("config.config").getString("PORT")));
	private static final int MAXUSERS = (Integer.parseInt(ResourceBundle.getBundle("config.config").getString("MAXUSERS")));

	public void iniciar() {

		try {
			ServerSocket servidor = null;
			servidor = new ServerSocket(PORT);
			while (true) {
				Socket cliente = null;
				System.out.println("Waiting for the client to connect");
				cliente = servidor.accept();
				System.out.println("Client connected successfully");
				if (contador < MAXUSERS) {
					countThreads(1);
					WorkerThread worker = new WorkerThread(cliente);
					worker.run();
				} else {
					LOGGER.warning("Max users reached wait for a bit and try again");
					ObjectOutputStream oos = new ObjectOutputStream(cliente.getOutputStream());
					Message message = new Message();
					message.setType(MessageType.MAX_USER_EXCEPTION);
					oos.writeObject(message);
					oos.close();
					cliente.close();

				}
			}

		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		App s1 = new App();
		s1.iniciar();
	}

	public synchronized static void countThreads(int x) {
		if (x == 1) {
			contador += 1;

		} else {
			contador -= 1;
		}
	}
}
