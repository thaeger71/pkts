package io.pkts.packet;

public interface PPPPacket extends PPPoEPacket, Cloneable{
	int getPPPProtocol();
}
