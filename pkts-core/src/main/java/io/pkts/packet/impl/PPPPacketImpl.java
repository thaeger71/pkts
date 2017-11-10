package io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.framer.IPv4Framer;
import io.pkts.packet.PPPPacket;
import io.pkts.packet.PPPoEPacket;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;

public class PPPPacketImpl extends AbstractPacket implements PPPPacket {
	
	private final PPPoEPacket parent;
	private final Buffer header;
	private static final IPv4Framer framer = new IPv4Framer();
	
	public PPPPacketImpl(final Protocol p, final PPPoEPacket parent, final Buffer header , final Buffer payload) {
		super(p, parent, payload);
		this.parent=parent;
		this.header=header;
	}

	@Override
	public int getVersion() throws IndexOutOfBoundsException, IOException {
		return this.parent.getVersion();
	}

	@Override
	public int getType() throws IndexOutOfBoundsException, IOException {
		return this.parent.getType();
	}

	@Override
	public int getCode() throws IndexOutOfBoundsException, IOException {
		return this.parent.getCode();
	}

	@Override
	public int sessionId() {
		return this.parent.sessionId();
	}

	@Override
	public int getPayloadLength() {
		return this.parent.getPayloadLength();
	}

	@Override
	public PPPPacket clone() {
		final PPPoEPacket pkt = this.parent.clone();
        return new PPPPacketImpl(getProtocol(), pkt, this.header.clone(), getPayload().clone());
	}

	@Override
	public String getSourceMacAddress() {
		return this.parent.getSourceMacAddress();
	}

	@Override
	public void setSourceMacAddress(String macAddress) throws IllegalArgumentException {
		this.parent.setSourceMacAddress(macAddress);	
	}

	@Override
	public String getDestinationMacAddress() {
		return this.parent.getDestinationMacAddress();
	}

	@Override
	public void setDestinationMacAddress(String macAddress) throws IllegalArgumentException {
		this.parent.setDestinationMacAddress(macAddress);
		
	}

	@Override
	public long getTotalLength() {
		return this.parent.getTotalLength();
	}

	@Override
	public long getCapturedLength() {
		return this.parent.getCapturedLength();
	}

	@Override
	public long getArrivalTime() {
		return this.parent.getArrivalTime();
	}

	@Override
	public void write(OutputStream out, Buffer payload) throws IOException {
		this.parent.write(out, Buffers.wrap(this.header, payload));
		
	}

	@Override
	public Packet getNextPacket() throws IOException {
		final Buffer payload = getPayload();
        if (payload == null) {
            return null;
        }
        if (getPPPProtocol()==33)//IP
        	return framer.frame(this, payload);
        return null;
	}

	@Override
	public int getPPPProtocol() {
		
		return this.header.getShort(0);
	}

	@Override
	public EtherType getEtherType() {
		return this.parent.getEtherType();
	}

}
