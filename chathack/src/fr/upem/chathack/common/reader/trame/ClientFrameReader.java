package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.frame.IFrame;
import fr.upem.chathack.frame.ServerMessageReader;

public class ClientFrameReader implements IReader<IFrame> {
	private enum State {
		WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
	}

	/**
	 * Readers
	 */
	private final ServerMessageReader serverMessageReader;
	private final BroadcastMessageReader broadcastMessageReader;
	private final DirectMessageReader directMessageReader;
	private final RequestPrivateConnectionReader requestConnectionReader;
	private final AcceptPrivateConnectionReader acceptPrivateConnectionReader;
	private final RejectPrivateConnectionReader rejectPrivateConnectionReader;

	public ClientFrameReader() {
		this.serverMessageReader = new ServerMessageReader();
		this.broadcastMessageReader = new BroadcastMessageReader();
		this.directMessageReader = new DirectMessageReader();
		this.requestConnectionReader = new RequestPrivateConnectionReader();
		this.acceptPrivateConnectionReader = new AcceptPrivateConnectionReader();
		this.rejectPrivateConnectionReader = new RejectPrivateConnectionReader();
		this.state = State.WAITING_OPCODE;
	}

	private State state;
	private IReader<? extends IFrame> currentFrameReader;
	private IFrame value;

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		switch (state) {
		case WAITING_OPCODE: {
			bb.flip();
			if (!bb.hasRemaining()) {
				bb.compact();
				return ProcessStatus.REFILL;
			}
			var opcode = bb.get();
			bb.compact();
			switch (opcode) {
			case OpCode.SERVER_RESPONSE_MESSAGE:
				currentFrameReader = serverMessageReader;
				break;
			case OpCode.BROADCAST_MESSAGE:
				currentFrameReader = broadcastMessageReader;
				break;
			case OpCode.PRIVATE_MESSAGE:
				currentFrameReader = directMessageReader;
				break;
			case OpCode.REQUEST_PRIVATE_CLIENT_CONNECTION:
				currentFrameReader = requestConnectionReader;
				break;
			case OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION:
				currentFrameReader = acceptPrivateConnectionReader;
				break;
			case OpCode.REJECTED_PRIVATE_CLIENT_CONNECTION:
				currentFrameReader = rejectPrivateConnectionReader;
				break;
			default:
				throw new IllegalArgumentException("unknown opcode " + opcode);
			}
			state = State.WAITING_CONTENT;
		}
		case WAITING_CONTENT: {
			var status = currentFrameReader.process(bb);
			if (status != ProcessStatus.DONE)
				return status;
			value = currentFrameReader.get();
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public IFrame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	@Override
	public void reset() {
		state = State.WAITING_OPCODE;
		value = null;
		serverMessageReader.reset();
		broadcastMessageReader.reset();
		directMessageReader.reset();
		requestConnectionReader.reset();
		acceptPrivateConnectionReader.reset();
		rejectPrivateConnectionReader.reset();
	}
}
