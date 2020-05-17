package fr.upem.chathack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.context.ClientContext;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.RequestPrivateConnection;

public class ClientChatHack {

	private enum ConnectionStatus {
		PENDING, ALLOWED, NOT_ALLOWED
	}

	private static class PrivateConnectionInfo {

	}

	private static final Logger logger = Logger.getLogger(ClientChatHack.class.getName());
	private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
	// map use to store private connection between a client and other clients
	private final HashMap<String, PrivateConnectionInfo> map = new HashMap<>();

	private final SocketChannel sc;
	private final Selector selector;
	private final InetSocketAddress serverAddress;
	private ClientContext uniqueContext;
	private final String login;
	private String password;
	private final Thread console;
	private final Queue<RequestPrivateConnection> privateConnectionRequest;

	public ClientChatHack(InetSocketAddress serverAddress, String path, String login) throws IOException {
		this.serverAddress = serverAddress;
		this.login = login;
		this.sc = SocketChannel.open();
		this.selector = Selector.open();
		this.console = new Thread(this::consoleRun);
		this.privateConnectionRequest = new LinkedList<>();
	}

	public ClientChatHack(InetSocketAddress serverAddress, String path, String login, String password)
			throws IOException {
		this(serverAddress, path, login); // call the other constructeur
		this.password = password;
	}

	private void consoleRun() {
		try (Scanner scan = new Scanner(System.in)) {
			while (scan.hasNextLine()) {
				var msg = scan.nextLine();
				sendCommand(msg);
			}
		} catch (InterruptedException e) {
			logger.info("Console thread has been interrupted");
		} finally {
			logger.info("Console thread stopping");
		}
	}

	private void sendCommand(String msg) throws InterruptedException {
		synchronized (commandQueue) {
			this.commandQueue.put(msg);
			selector.wakeup();
		}
	}

	public boolean havePrivateConnection(String destinator) {
		return map.containsKey(destinator);
	}

	private void processCommands() {
		for (;;) {
			synchronized (commandQueue) {
				var line = this.commandQueue.poll();
				if (line == null) {
					return;
				}
				if (line.isBlank())
					return;

				switch (line.charAt(0)) {
				case '#': {

				}
				case '/': {
					if (line.startsWith("/file")) {
						System.out.println("send files");
					}
					if (line.startsWith("/accept")) {
						System.out.println("private connection accepted");
					}
					if (line.startsWith("/reject")) {
						System.out.println(" private connection rejected");
					}
					if (line.startsWith("/requests")) {
						System.out.println("list private connection");
					}
					break;
				}
				case '@': {
					// @login msg -> login msg
					var splited = line.substring(1).split(" ");

					if (splited.length < 2) {
						System.err.println("usage: @targetLogin msg");
						return;
					}

					var targetLogin = splited[0];
					var message = splited[1];
					if (havePrivateConnection(targetLogin)) {

					} else {
						var dmRequest = new RequestPrivateConnection(login, targetLogin);
						this.uniqueContext.queueMessage(dmRequest.toBuffer());
					}
					break;
				}
				default: {
					Message msg = new Message(this.login, line);
					this.uniqueContext.queueMessage(new BroadcastMessage(msg).toBuffer());
					break;
				}
				}
			}
		}
	}

	public void addPrivateConnectionRequest(RequestPrivateConnection requestMessage) {
		privateConnectionRequest.add(requestMessage);
	}

	public void launch() throws IOException {
		sc.configureBlocking(false);
		var key = sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueContext = new ClientContext(key, this);
		key.attach(uniqueContext);
		sc.connect(serverAddress);

		/**
		 * When, client connected, send anonymous or authenticated request to connect
		 * client with server
		 */

		var request = this.password == null //
				? new AnonymousConnection(login)
				: new AuthentificatedConnection(login, password);

		this.uniqueContext.putInQueue(request.toBuffer());
		console.start();// run stdin thread

		while (!Thread.interrupted()) {
			try {
				selector.select(this::treatKey);
				processCommands();
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}

	private void treatKey(SelectionKey key) {
		try {
			if (key.isValid() && key.isConnectable()) {
				uniqueContext.doConnect();
			}
			if (key.isValid() && key.isWritable()) {
				uniqueContext.doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				uniqueContext.doRead();
			}
		} catch (IOException ioe) {
			// lambda call in select requires to tunnel IOException
			throw new UncheckedIOException(ioe);
		}
	}

	public void interruptConsole() {
		this.console.interrupt();
	}

	private static void usage() {
		System.out.println("usage : Client hostname port pathToDirectory login password (optional)");
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length < 4 || args.length > 5) {
			usage();
			return;
		}

		var srvAddr = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
		ClientChatHack client = null;

		// IP address server ,nb port, path, login
		if (args.length == 4) {
			client = new ClientChatHack(srvAddr, args[2], args[3]);
		} else {
			// IP address server ,nb port, path, login, password
			client = new ClientChatHack(srvAddr, args[2], args[3], args[4]);
		}

		System.out.println("connection to server");
		client.launch();
	}
}
