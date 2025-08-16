// org/example/model/MessengerPacket.java
package org.example.model.Packet;

public abstract class MessengerPacket extends Packet {
    private String protocolKey;
    protected MessengerPacket(String protocolKey, double x, double y) {
        super( x, y);
        this.protocolKey = protocolKey;

    }
}
