package chathack.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;


public class Client {
	private static int BUFFER_SIZE = 4096;
	private static Logger logger = Logger.getLogger(Client.class.getName());
	
	private final SocketChannel sc;
	private final Selector selector;
	private final InetSocketAddress serverAddress;
	private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
	private final String login;
	
	static private class Context {

        final private SelectionKey key;
        final private SocketChannel sc;
        final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
        final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
        final private Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode
        //missing the Reader class
        private boolean closed = false;
        

        private Context(SelectionKey key){
            this.key = key;
            this.sc = (SocketChannel) key.channel();
        
        }
	}//fin class Context
	
	public Client(String login, InetSocketAddress serverAddress) throws IOException {
		 	this.serverAddress = serverAddress;
	        this.login = login;
	        this.sc = SocketChannel.open();
	        this.selector = Selector.open();
	}
	
	
}
