package io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.framer.PPPFramer;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PPPoEPacket;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;

public class PPPoEPacketImpl extends AbstractPacket implements PPPoEPacket{

	private final MACPacket parent;
	private final Buffer header;
	private static final PPPFramer framer = new PPPFramer();
	
	public PPPoEPacketImpl(final Protocol p, final MACPacket parent,final Buffer header, final Buffer payload) {
		super(p, parent, payload);
		this.parent=parent;
		this.header=header;
		
	}

	@Override
	public String getSourceMacAddress() {
		return this.parent.getDestinationMacAddress();
	}

	@Override
	public void setSourceMacAddress(String macAddress) throws IllegalArgumentException {
		this.parent.setDestinationMacAddress(macAddress);
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
        return framer.frame(this, payload);
	}

	@Override
	public int getVersion() throws IndexOutOfBoundsException, IOException {
		byte b = this.header.getByte(0);
		return b & 0x0F;
	}

	@Override
	public int getType() throws IndexOutOfBoundsException, IOException {
		byte b = this.header.getByte(0);
		return b >> 4;
	}

	@Override
	public int getCode() throws IndexOutOfBoundsException, IOException {
		return this.header.getByte(1);
	}

	@Override
	public int sessionId() {
		return this.header.getShort(2);
	}

	@Override
	public int getPayloadLength() {
		return this.header.getShort(4);
	}

	@Override
	public PPPoEPacket clone() {
		 final MACPacket pkt = this.parent.clone();
	        return new PPPoEPacketImpl(getProtocol(), pkt, this.header.clone(), getPayload().clone());
	}

	@Override
	public EtherType getEtherType() {
		return this.parent.getEtherType();
	}

}
