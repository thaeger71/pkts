package io.pkts.packet;

import java.io.IOException;

public interface PPPoEPacket extends MACPacket, Cloneable{
	
	int getVersion() throws IndexOutOfBoundsException, IOException;
	
	int getType() throws IndexOutOfBoundsException, IOException;
	
	int getCode() throws IndexOutOfBoundsException, IOException;
	
	int sessionId();
	
	int getPayloadLength();
	
	@Override 
	PPPoEPacket clone();
	
}
