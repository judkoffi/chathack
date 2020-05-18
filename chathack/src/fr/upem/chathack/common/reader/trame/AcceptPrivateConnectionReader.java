package fr.upem.chathack.common.reader.trame;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.InetSocketAddressReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;
import fr.upem.chathack.frame.AcceptPrivateConnection;

public class AcceptPrivateConnectionReader implements IReader<AcceptPrivateConnection> {
	private enum State {
		WAITING_TARGET_LOGIN, WAITING_TARGET_ADDR, WAITING_FROM_LOGIN, DONE, ERROR
	}

	private State state;
	private AcceptPrivateConnection value;
	private LongSizedString targetLogin;
	private LongSizedString fromLogin;
	private final LongSizedStringReader reader;
	private final InetSocketAddressReader socketAddressReader;
	private InetSocketAddress socketAddress;

	public AcceptPrivateConnectionReader() {
		this.socketAddressReader = new InetSocketAddressReader();
		this.reader = new LongSizedStringReader();
		this.state = State.WAITING_TARGET_LOGIN;

	}

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		switch (state) {
		case WAITING_TARGET_LOGIN: {
			var status = reader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			targetLogin = reader.get();
			reader.reset();
			state = State.WAITING_TARGET_ADDR;
		}
		case WAITING_TARGET_ADDR: {
			var status = socketAddressReader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			socketAddress = socketAddressReader.get();
			state = State.WAITING_FROM_LOGIN;
		}
		case WAITING_FROM_LOGIN: {
			var status = reader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			fromLogin = reader.get();
			value = new AcceptPrivateConnection(fromLogin, targetLogin, socketAddress);
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public AcceptPrivateConnection get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	@Override
	public void reset() {
		state = State.WAITING_TARGET_LOGIN;
		reader.reset();
		socketAddressReader.reset();
		value = null;
		targetLogin = null;
		fromLogin = null;
		socketAddress = null;

	}

}
