package thread;

import Classes.Message;
import Classes.MessageType;
import Classes.SigninSignup;
import Classes.User;
import Exceptions.EmailAlreadyExistException;
import Exceptions.IncorrectLoginException;
import Exceptions.MaxUserException;
import Exceptions.ServerErrorException;
import Exceptions.UnknownTypeException;
import application.App;
import factory.ServerFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a worker thread responsible for handling client
 * requests in a multi-threaded server( App ) application. Each instance of this
 * class is designed to serve a single client connection by processing
 * authentication and sign-up requests operations using a provided data access
 * object (dao).
 *
 * The main responsibilities of this class include:
 *
 * - Receiving and processing messages from clients.
 *
 * - Performing user authentication and signUp operation.
 *
 * - Sending response messages to clients based on the outcome of the operation.
 *
 * @author Janam
 */
public class WorkerThread extends Thread {

    // Stores information about the user associated with this worker thread.
    private User user;

    // Stores the message received from the client.
    private static Message msg;

    // The data access object used for authentication and sign-up.
    private SigninSignup dao;

    // The client socket used for communication with the client.
    private Socket skCliente;

    // Input stream for reading objects from the client.
    private ObjectInputStream inputStream;

    // Output stream for sending objects to the client.
    private ObjectOutputStream outputStream;

    // logger
    private static final Logger logger = Logger.getLogger(WorkerThread.class.getName());

    /**
     * Default constructor with no parameters.
     */
    public WorkerThread() {

    }

    /**
     * Constructor to create a WorkerThread instance with a client socket and a
     * data access object.
     *
     * @param skCliente The socket representing the client connection.
     * @param dao The data access object used for authentication and sign-up.
     */
    public WorkerThread(Socket skCliente) {

        // Assign the provided client socket to the instance variable.
        this.skCliente = skCliente;

        // Assign the data access object we get from the factory to the instance variable.
        this.dao = ServerFactory.getServer();
    }

    /**
     * It will get the object from the client socket and interpretate the
     * recieved message type in order to make a SignIn or a SignUp.
     */
    @Override
    public void run() {

        try {

            logger.info("Initializing workerThread.");

            /**
             * Create an output stream to send objects to the client through its
             * socket.
             */
            outputStream = new ObjectOutputStream(skCliente.getOutputStream());

            /**
             * Create an input stream to read objects from the client's socket.
             */
            inputStream = new ObjectInputStream(skCliente.getInputStream());

            /**
             * Read a Message object from the client.
             */
            msg = (Message) inputStream.readObject();

            /**
             * Extract the User object from the received message.
             */
            user = msg.getUser();

            /**
             * Determine the type of request (LOGIN_REQUEST or SIGNUP_REQUEST)
             * and process it accordingly.
             */
            switch (msg.getType()) {

                case LOGIN_REQUEST:

                    /**
                     * Attempt to signIn the user using the provided data access
                     * object.
                     */
                    user = dao.SignIn(user);

                    break;

                case SIGNUP_REQUEST:

                    /**
                     * Attempt to signUp a new user using the provided data
                     * access object.
                     */
                    user = dao.signUp(user);

                    break;
            }

            /**
             * Prepare a response message indicating success (OKAY_RESPONSE) and
             * include the updated User object.
             */
            msg.setType(MessageType.OKAY_RESPONSE);

            msg.setUser(user);

        } catch (IncorrectLoginException e) {

            /**
             * Handle the exception for incorrect login.
             */
            msg.setType(MessageType.INCORRECT_LOGIN_RESPONSE);

            logger.severe("The email or password are incorrect: " + e.getMessage());

        } catch (EmailAlreadyExistException e) {

            /**
             * Handle the exception for email already existing during signup.
             */
            msg.setType(MessageType.EMAIL_ALREADY_EXIST_RESPONSE);

            logger.severe("The email already exists: " + e.getMessage());

        } catch (MaxUserException | ServerErrorException | UnknownTypeException
                | IOException | ClassNotFoundException e) {

            /**
             * Handle other exceptions by sending a server error response.
             */
            msg.setType(MessageType.SERVER_ERROR_RESPONSE);

            logger.severe("Error: " + e.getMessage());

        } finally {

            try {

                /**
                 * Send the response message to the client through the output
                 * stream.
                 */
                outputStream.writeObject(msg);

                outputStream.close();

                inputStream.close();

                /**
                 * It is called when a client disconnects from the server, and
                 * its primary purpose is to remove that client from the list of
                 * active clients.
                 */
                App.countThreads(-1, null);

                // Close the client socket
                skCliente.close();

            } catch (IOException ex) {

                /**
                 * Handle any exceptions that may occur during the closing of
                 * streams or the socket.
                 */
                logger.severe("Error: " + ex.getMessage());
            }
        }
    }
}
