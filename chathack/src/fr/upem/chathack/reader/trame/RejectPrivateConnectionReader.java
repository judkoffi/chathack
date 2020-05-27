package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongSizedStringReader;
/**
 * Class use to read RejectPrivateConnection type
 *
 */
public class RejectPrivateConnectionReader implements IReader<RejectPrivateConnection> {
	private enum State {
		WAITING_TARGET_LOGIN, WAITING_FROM_LOGIN, DONE, ERROR
	}

	private State state;
	private RejectPrivateConnection value;
	private LongSizedString targetLogin;
	private LongSizedString fromLogin;
	private final LongSizedStringReader sizedStringReader;

	public RejectPrivateConnectionReader() {
		this.sizedStringReader = new LongSizedStringReader();
		this.state = State.WAITING_TARGET_LOGIN;
	}

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		switch (state) {
		case WAITING_TARGET_LOGIN: {
			var status = sizedStringReader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			targetLogin = sizedStringReader.get();
			sizedStringReader.reset();
			state = State.WAITING_FROM_LOGIN;
		}

		case WAITING_FROM_LOGIN: {
			var status = sizedStringReader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			fromLogin = sizedStringReader.get();
			value = new RejectPrivateConnection(fromLogin, targetLogin);
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public RejectPrivateConnection get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	@Override
	public void reset() {
		state = State.WAITING_TARGET_LOGIN;
		sizedStringReader.reset();
		value = null;
		targetLogin = null;
		fromLogin = null;

	}

}
