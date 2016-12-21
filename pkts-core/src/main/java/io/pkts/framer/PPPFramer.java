package io.pkts.framer;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.packet.PPPPacket;
import io.pkts.packet.PPPoEPacket;
import io.pkts.packet.impl.PPPPacketImpl;
import io.pkts.protocol.Protocol;

public class PPPFramer  implements Framer<PPPoEPacket>{

	@Override
	public Protocol getProtocol() {
		return Protocol.PPP;
	}

	@Override
	public PPPPacket frame(PPPoEPacket parent, Buffer buffer) throws IOException {
		if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }
		
		final Buffer header = buffer.readBytes(2);
		final Buffer payload = buffer.slice(buffer.capacity());
		return new PPPPacketImpl(Protocol.PPP, parent, header, payload);
	}

	@Override
	public boolean accept(Buffer data) throws IOException {
		return false;
	}

}
