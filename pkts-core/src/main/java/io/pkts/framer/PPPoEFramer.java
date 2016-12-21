package io.pkts.framer;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PPPoEPacket;
import io.pkts.packet.impl.PPPoEPacketImpl;
import io.pkts.protocol.Protocol;

public class PPPoEFramer  implements Framer<MACPacket>{

	@Override
	public Protocol getProtocol() {
		return Protocol.PPPoE;
	}

	@Override
	public PPPoEPacket frame(MACPacket parent, Buffer buffer) throws IOException {
		if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }
		
		final Buffer header = buffer.readBytes(6);
		final Buffer payload = buffer.slice(buffer.capacity());
		return new PPPoEPacketImpl(Protocol.PPPoE, parent, header, payload);
	}

	@Override
	public boolean accept(Buffer data) throws IOException {
		return false;
	}



}
