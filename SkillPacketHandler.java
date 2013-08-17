package assets.levelup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class SkillPacketHandler implements IPacketHandler{

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		handlePacket(packet,(EntityPlayer) player);
	}

	private static void handlePacket(Packet250CustomPayload packet, EntityPlayer player) 
	{
		DataInputStream inStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		String name;
		byte button;
		int[] data = null;
		try {
			name = inStream.readUTF();
			button = inStream.readByte();
			if(button<0)
			{
				data = new int[ClassBonus.skillNames.length];
				for(int i=0; i<data.length; i++)
				{
					data[i] = inStream.readInt();
				}
			}
		} catch (IOException e) {
            e.printStackTrace();
            return;
		}
		if(player.username.equals(name))
		{
			if(packet.channel.equals("LEVELUPCLASSES"))
			{
				PlayerExtendedProperties.setPlayerClass(player, button);
			}
			else if(packet.channel.equals("LEVELUPSKILLS"))
			{
				if(data!=null)
				{
					Map<String,Integer> skillMap = PlayerExtendedProperties.getSkillMap(player);
		        	for(int index=0;index<data.length;index++)
		        	{
		        		skillMap.put(ClassBonus.skillNames[index], data[index]);
		        	}
				}
				else
				{
					ClassBonus.addBonusToSkill(player, ClassBonus.skillNames[button<21?button-1:button-21], 1, button<21);
					ClassBonus.addBonusToSkill(player, "XP", 1, !(button<21));
				}
			}
			if(player instanceof EntityPlayerMP)
			{
				((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
				System.out.println("server packet sent to player client");
			}
			System.out.println("packet received by player");
		}
	}

	public static Packet getPacket(String channel, String username, byte id, int...dat) 
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1+4*dat.length+2*username.length());
        DataOutputStream dos = new DataOutputStream(bos);     
        try
        {
            dos.writeUTF(username);
            dos.write(id);
            if(id<0)
            {
	            for(int da:dat)
	            	dos.writeInt(da);
            }
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }
        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = channel;
        pkt.data = bos.toByteArray();
        pkt.length = bos.size();
        return pkt;
	}

}