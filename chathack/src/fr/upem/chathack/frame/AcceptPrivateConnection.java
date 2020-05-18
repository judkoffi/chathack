package fr.upem.chathack.frame;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class AcceptPrivateConnection implements IFrame {
	private final LongSizedString fromLogin;
	private final LongSizedString targetLogin;
	private final InetSocketAddress targetAddress;

	public AcceptPrivateConnection(LongSizedString fromLogin, LongSizedString targetLogin,
			InetSocketAddress targetAddress) {
		this.fromLogin = fromLogin;
		this.targetLogin = targetLogin;
		this.targetAddress = targetAddress;
	}

	public AcceptPrivateConnection(String fromLogin2, String login, InetSocketAddress addr) {
		 this.fromLogin = new LongSizedString(fromLogin2);
		 this.targetLogin = new LongSizedString(login);
		 this.targetAddress = addr;
	}

	@Override
	public ByteBuffer toBuffer() {
		var s = Byte.BYTES + fromLogin.getTrameSize() + targetLogin.getTrameSize() + (2 * Long.BYTES);
		var bb = ByteBuffer.allocate((int) s);
		bb.put(OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION);
		bb.put(targetLogin.toBuffer());
		bb.put(targetAddress.getAddress().getAddress());
		bb.put(fromLogin.toBuffer());

		return bb.flip();
	}

	@Override
	public void accept(IFrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
}
