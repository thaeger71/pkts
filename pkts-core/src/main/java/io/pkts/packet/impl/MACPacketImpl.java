/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.framer.IPv4Framer;
import io.pkts.framer.PPPoEFramer;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public final class MACPacketImpl extends AbstractPacket implements MACPacket {

	private static final PPPoEFramer pppoe_framer = new PPPoEFramer();
    private static final IPv4Framer framer = new IPv4Framer();

    private final PCapPacket parent;
    private final String sourceMacAddress;
    private final String destinationMacAddress;
    private final EtherType etherType;

    /**
     * If the headers are set then this overrides any of the source stuff set
     * above.
     */
    private final Buffer headers;

    /**
     * Creates a new {@link MACPacketImpl} and it assumes ethernet II and it
     * does not check whether or not the ethertype is a known type. This method
     * should only be used by the internal packet creating functions such as the
     * {@link TransportPacketFactoryImpl} or the framers.
     * 
     * @param parent
     * @param headers
     * @return
     */
    public static MACPacketImpl create(final PCapPacket parent, final Buffer headers) {
        if (headers.capacity() != 14) {
            throw new IllegalArgumentException("Not enough bytes to create this header");
        }

        if (parent == null) {
            throw new IllegalArgumentException("The parent packet cannot be null");
        }

        return new MACPacketImpl(Protocol.ETHERNET_II, EtherType.IPv4, parent, headers, null);
    }

    /**
     * Construct a new {@link MACPacket} based on the supplied headers.
     * 
     * @param packet
     * @param headers
     */
    public MACPacketImpl(final Protocol protocol,final EtherType etherType, final PCapPacket parent, final Buffer headers, final Buffer payload) {
        super(protocol, parent, payload);
        this.parent = parent;
        this.headers = headers;
        this.etherType=etherType;
        this.sourceMacAddress = null;
        this.destinationMacAddress = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getSourceMacAddress() {
        if (this.sourceMacAddress != null) {
            return this.sourceMacAddress;
        }

        try {
            return toHexString(this.headers, 6, 6);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
        }
    }

    public static String toHexString(final Buffer buffer, final int start, final int length) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; ++i) {
            final byte b = buffer.getByte(i);
            sb.append(String.format("%02X", b));
            if (i < start + length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getDestinationMacAddress() {
        if (this.destinationMacAddress != null) {
            return this.destinationMacAddress;
        }

        try {
            return toHexString(this.headers, 0, 6);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to verify
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Destination Mac Address: ").append(this.destinationMacAddress)
          .append(" Source Mac Address: ").append(this.sourceMacAddress);
        return sb.toString();
    }

    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        this.parent.write(out, Buffers.wrap(this.headers, payload));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceMacAddress(final String macAddress) {
        setMacAddress(macAddress, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationMacAddress(final String macAddress) {
        setMacAddress(macAddress, false);
    }

    /**
     * Helper method for setting the mac address in the header buffer.
     * 
     * @param macAddress
     *            the mac address to parse
     * @param setSourceMacAddress
     *            whether this is to bet set as the source mac address or not.
     *            False implies destination mac address of course.
     * @throws IllegalArgumentException
     */
    private void setMacAddress(final String macAddress, final boolean setSourceMacAddress)
            throws IllegalArgumentException {
        if (macAddress == null || macAddress.isEmpty()) {
            throw new IllegalArgumentException("Null or empty string cannot be a valid MAC Address.");
        }
        // very naive implementation first.
        final String[] segments = macAddress.split(":");
        if (segments.length != 6) {
            throw new IllegalArgumentException("Invalid MAC Address. Not enough segments");
        }

        final int offset = setSourceMacAddress ? 6 : 0;
        for (int i = 0; i < 6; ++i) {
            final byte b = (byte) ((Character.digit(segments[i].charAt(0), 16) << 4) + Character.digit(segments[i]
                    .charAt(1), 16));
            this.headers.setByte(i + offset, b);
        }
    }

    @Override
    public MACPacket clone() {
        final PCapPacket pkt = this.parent.clone();
        return new MACPacketImpl(getProtocol(),this.etherType, pkt, this.headers.clone(), getPayload().clone());
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
    public Packet getNextPacket() throws IOException {
        final Buffer payload = getPayload();
        if (payload == null) {
            return null;
        }
        if (this.etherType==EtherType.PPPoE)
        	return pppoe_framer.frame(this, payload);
        
        return framer.frame(this, payload);
    }

	@Override
	public EtherType getEtherType() {
		return this.etherType;
	}

}
