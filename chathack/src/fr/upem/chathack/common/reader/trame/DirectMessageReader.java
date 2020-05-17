package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;

import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;
import fr.upem.chathack.common.reader.MessageReader;
import fr.upem.chathack.frame.DirectMessage;

public class DirectMessageReader implements IReader<DirectMessage> {
	private enum State {
		WAITING_TARGET, WAITING_MESSAGE, DONE, ERROR
	}

	private State state;
	private final LongSizedStringReader targetReader;
	private final MessageReader messageReader;
	private LongSizedString target;
	private DirectMessage message;

	public DirectMessageReader() {
		this.targetReader = new LongSizedStringReader();
		this.messageReader = new MessageReader();
		this.state = State.WAITING_TARGET;
	}

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		switch (state) {
		case WAITING_TARGET: {
			var status = targetReader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			target = targetReader.get();
			targetReader.reset();
			state = State.WAITING_MESSAGE;
		}
		case WAITING_MESSAGE: {
			var status = messageReader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			var content = messageReader.get();
			message = new DirectMessage(target, content);
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		default:
			System.out.println(state);
			throw new IllegalStateException();
		}
	}

	@Override
	public DirectMessage get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return message;

	}

	@Override
	public void reset() {
		state = State.WAITING_TARGET;
		targetReader.reset();
		messageReader.reset();
		message = null;

	}
}
