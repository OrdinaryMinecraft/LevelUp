package ru.flametaichou.levelup.Model;

public class ExtPropPacket {

    public int airData;
    public boolean effectData;
    public long skillCooldownData;
    public int doubleShotData;

    public ExtPropPacket () {
        this.airData = 0;
        this.effectData = false;
        this.skillCooldownData = 0;
        this.doubleShotData = 0;
    }

    public ExtPropPacket (int ad, boolean ed, long scd, int dsd) {
        this.airData = ad;
        this.effectData = ed;
        this.skillCooldownData = scd;
        this.doubleShotData = dsd;
    }

    public static ExtPropPacket fromString(String packetString) {
        ExtPropPacket packet = new ExtPropPacket();
        String[] parts = packetString.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && parts[i] != null) packet.airData = Integer.parseInt(parts[i]);
            if (i == 1 && parts[i] != null) packet.effectData = Boolean.parseBoolean(parts[i]);
            if (i == 2 && parts[i] != null) packet.skillCooldownData = Long.parseLong(parts[i]);
            if (i == 3 && parts[i] != null) packet.doubleShotData = Integer.parseInt(parts[i]);
        }
        return packet;
    }

    public String toString() {
        String string = airData + "/" +
                effectData + "/" +
                skillCooldownData + "/" +
                doubleShotData;
        return string;
    }
}
