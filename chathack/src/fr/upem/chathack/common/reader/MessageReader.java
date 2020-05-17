package fr.upem.chathack.common.reader;

import java.nio.ByteBuffer;

import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.Message;

public class MessageReader implements IReader<Message> {
	private enum State {
		WAITING_FROM, WAITING_MESSAGE, DONE, ERROR
	}

	private final LongSizedStringReader reader;
	private State state;
	private Message message;
	private LongSizedString login;

	public MessageReader() {
		this.reader = new LongSizedStringReader();
		this.state = State.WAITING_FROM;
	}

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		switch (state) {
		case WAITING_FROM: {
			var status = reader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			login = reader.get();
			reader.reset();
			state = State.WAITING_MESSAGE;
		}
		case WAITING_MESSAGE: {
			var status = reader.process(bb);
			if (status != ProcessStatus.DONE) {
				return status;
			}
			var content = reader.get();
			message = new Message(login, content);
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		default:
			System.out.println(state);
			throw new IllegalStateException();
		}
	}
	
	@Override
	public Message get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return message;
	}

	@Override
	public void reset() {
		state = State.WAITING_FROM;
		reader.reset();
		message = null;

	}

}
