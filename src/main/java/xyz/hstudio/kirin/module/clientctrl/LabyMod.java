package xyz.hstudio.kirin.module.clientctrl;

import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.kirin.module.ClientCtrl;

import java.util.List;

@LoadFrom("kirin/clientctrl/labymod.yml")
public class LabyMod extends ClientCtrl {

    @LoadPath("improved_lava")
    private static boolean IMPROVED_LAVA;
    @LoadPath("crosshair_sync")
    private static boolean CROSSHAIR_SYNC;
    @LoadPath("refill_fix")
    private static boolean REFILL_FIX;

    @LoadPath("gui_all")
    private static boolean GUI_ALL;
    @LoadPath("gui_potion_effects")
    private static boolean GUI_POTION_EFFECTS;
    @LoadPath("gui_armor_hud")
    private static boolean GUI_ARMOR_HUD;
    @LoadPath("gui_item_hud")
    private static boolean GUI_ITEM_HUD;

    @LoadPath("blockbuild")
    private static boolean BLOCKBUILD;
    @LoadPath("tags")
    private static boolean TAGS;
    @LoadPath("chat")
    private static boolean CHAT;
    @LoadPath("animations")
    private static boolean ANIMATIONS;
    @LoadPath("saturation_bar")
    private static boolean SATURATION_BAR;

    @LoadPath("execution")
    private static List<String> EXECUTION;

    private final PacketDataSerializer dataToSend = new PacketDataSerializer(Unpooled.buffer());

    public LabyMod(HPlayer p) {
        super(p, "LMC", EXECUTION);

        // Creating a json object we will put the permissions in
        JsonObject object = new JsonObject();

        // Adding the permissions to the json object
        object.addProperty("IMPROVED_LAVA", IMPROVED_LAVA);
        object.addProperty("CROSSHAIR_SYNC", CROSSHAIR_SYNC);
        object.addProperty("REFILL_FIX", REFILL_FIX);

        object.addProperty("GUI_ALL", GUI_ALL);
        object.addProperty("GUI_POTION_EFFECTS", GUI_POTION_EFFECTS);
        object.addProperty("GUI_ARMOR_HUD", GUI_ARMOR_HUD);
        object.addProperty("GUI_ITEM_HUD", GUI_ITEM_HUD);

        object.addProperty("BLOCKBUILD", BLOCKBUILD);
        object.addProperty("TAGS", TAGS);
        object.addProperty("CHAT", CHAT);
        object.addProperty("ANIMATIONS", ANIMATIONS);
        object.addProperty("SATURATION_BAR", SATURATION_BAR);

        dataToSend.a("PERMISSIONS");
        dataToSend.a(object.toString());
    }

    @Override
    public void send() {
        p.pipeline.writeAndFlush(new PacketPlayOutCustomPayload(channel, dataToSend));
    }
}