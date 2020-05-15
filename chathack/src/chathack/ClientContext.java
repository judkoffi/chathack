package chathack;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import chathack.common.model.Message;
import chathack.common.reader.IReader;
import chathack.common.reader.MessageReader;
import chathack.context.BaseContext;
import chathack.frame.ClientFrameVisitor;

public class ClientContext extends BaseContext{
	
	private final MessageReader messageReader = new MessageReader();
	private final ClientFrameVisitor clientFrameVisitor;
	
	public ClientContext(SelectionKey key) {
		super(key);
		this.clientFrameVisitor = new ClientFrameVisitor();
			
	}

	@Override
	public void processIn() {
		for (;;) {
	        IReader.ProcessStatus status = messageReader.process(bbin);
	        switch (status) {
	          case DONE:
	            Message msg = messageReader.get();
	            messageReader.reset();
	            System.out.println("message read : " + msg);
	            break;
	          case REFILL:
	            return;
	          case ERROR:
	            silentlyClose();
	            return;
	        }
	      }
		
	}

	@Override
	public void processOut() {
		while (!queue.isEmpty()) {
	        var bb = queue.peek();
	        if (bb.remaining() <= bbout.remaining()) {
	          queue.remove();
	          bbout.put(bb);
	        } else {
	          return;
	        }
	      }
		
	}
	
	public void doConnect() throws IOException {
      if (!sc.finishConnect()) {
        return;
      }
      
      updateInterestOps();
	}
}
