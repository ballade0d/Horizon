package xyz.hstudio.horizon;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class HorizonBungee extends Plugin implements Listener {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel("horizon:data_transporter");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!e.getTag().equals("horizon:data_transporter")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());

        short len = in.readShort();
        byte[] msgBytes = new byte[len];
        in.readFully(msgBytes);

        DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));

        try {
            String command = msgIn.readUTF();
            getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        e.setCancelled(true);
    }
}