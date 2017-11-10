/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.framer.RTPFramer;
import io.pkts.framer.SIPFramer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class TransportPacketImpl extends AbstractPacket implements TransportPacket {

    private static final SIPFramer sipFramer = new SIPFramer();
    private static final RTPFramer rtpFramer = new RTPFramer();

    private final IPPacket parent;
    private final Protocol protocol;
    private final Buffer headers;

    protected TransportPacketImpl(final IPPacket parent, final Protocol protocol, final Buffer headers,
            final Buffer payload) {
        super(protocol, parent, payload);
        assert parent != null;
        this.protocol=protocol;
        this.parent = parent;
        this.headers = headers;
    }

    @Override
    public boolean isUDP() {
        return false;
    }

    @Override
    public boolean isTCP() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getSourcePort() {
        return this.headers.getUnsignedShort(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setSourcePort(final int port) {
        this.headers.setUnsignedShort(0, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getDestinationPort() {
        return this.headers.getUnsignedShort(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationPort(final int port) {
        this.headers.setUnsignedShort(2, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO - verify checksum etc?
    }

    @Override
    public final long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getRawSourceIp() {
        return this.parent.getRawSourceIp();
    }

    @Override
    public final String getSourceIP() {
        return this.parent.getSourceIP();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getRawDestinationIp() {
        return this.parent.getRawDestinationIp();
    }

    @Override
    public final String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    @Override
    public final String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    @Override
    public final String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    @Override
    public final long getTotalLength() {
        return this.parent.getTotalLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setSourceMacAddress(final String macAddress) {
        this.parent.setSourceMacAddress(macAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationMacAddress(final String macAddress) {
        this.parent.setDestinationMacAddress(macAddress);
    }

    @Override
    public final void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    @Override
    public final void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setSourceIP(final String sourceIp) {
        this.parent.setSourceIP(sourceIp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationIP(final String destinationIP) {
        this.parent.setDestinationIP(destinationIP);
    }

    @Override
    public final int getIpChecksum() {
        return this.parent.getIpChecksum();
    }

    @Override
    public final boolean verifyIpChecksum() {
        return this.parent.verifyIpChecksum();
    }

    @Override
    public final void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    @Override
    public final void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    @Override
    public final void reCalculateChecksum() {
        this.parent.reCalculateChecksum();
    }

    @Override
    public abstract TransportPacket clone();

    protected IPPacket getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.PCapPacket#getCapturedLength()
     */
    @Override
    public long getCapturedLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getNextPacket()
     */
    @Override
    public Packet getNextPacket() throws IOException {
        final Buffer payload = getPayload();
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        if (sipFramer.accept(payload)) {
            return sipFramer.frame(this, payload);
        } else if (rtpFramer.accept(payload) && this.protocol==Protocol.UDP) {
            // RTP is tricky to parse so if we return
            // null then it wasn't an RTP packet afterall
            // so fall through...
            final RtpPacket rtp = frameRtp(payload);
            if (rtp != null) {
                return rtp;
            }
        }

        return new UnknownApplicationPacketImpl(this, payload);
    }

    private RtpPacket frameRtp(final Buffer payload) throws IOException {
        try {
            return rtpFramer.frame(this, payload);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getTotalIPLength()
     */
    @Override
    public int getTotalIPLength() {
        return this.parent.getTotalIPLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getVersion()
     */
    @Override
    public int getVersion() {
        return this.parent.getVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getHeaderLength()
     */
    @Override
    public int getHeaderLength() {
        return this.parent.getHeaderLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getIdentification()
     */
    @Override
    public int getIdentification() {
        return this.parent.getIdentification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isFragmented()
     */
    @Override
    public boolean isFragmented() {
        return this.parent.isFragmented();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isReservedFlagSet()
     */
    @Override
    public boolean isReservedFlagSet() {
        return this.parent.isReservedFlagSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isDontFragmentSet()
     */
    @Override
    public boolean isDontFragmentSet() {
        return this.parent.isDontFragmentSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isMoreFragmentsSet()
     */
    @Override
    public boolean isMoreFragmentsSet() {
        return this.parent.isMoreFragmentsSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getFragmentOffset()
     */
    @Override
    public short getFragmentOffset() {
        return this.parent.getFragmentOffset();
    }

}
