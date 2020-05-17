package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;

import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;
import fr.upem.chathack.frame.RequestPrivateConnection;

public class RequestPrivateConnectionReader implements IReader<RequestPrivateConnection> {
	private enum State {
		WAITING_FROM_LOGIN, WAITING_TARGET_LOGIN, DONE, ERROR
	}

	private final LongSizedStringReader reader;
	private State state;
	private RequestPrivateConnection value;
	private LongSizedString targetLogin;

	public RequestPrivateConnectionReader() {
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
		}
			targetLogin = reader.get();
			reader.reset();
			state = State.WAITING_FROM_LOGIN;

		case WAITING_FROM_LOGIN: {
			var status = reader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			var fromLogin = reader.get();
			value = new RequestPrivateConnection(fromLogin, targetLogin);
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		default:
			throw new IllegalStateException();
		}

	}

	@Override
	public RequestPrivateConnection get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	@Override
	public void reset() {
		state = State.WAITING_TARGET_LOGIN;
		reader.reset();
		value = null;

	}

}
