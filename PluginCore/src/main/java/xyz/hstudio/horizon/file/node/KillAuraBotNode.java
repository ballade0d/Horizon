package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;

public class KillAuraBotNode extends CheckFile {

    @Load(path = "command_only")
    public boolean command_only = true;
    @Load(path = "update_interval")
    public int update_interval = 2;
    @Load(path = "xz_distance")
    public double xz_distance = 3.3;
    @Load(path = "y_distance")
    public double y_distance = 2.0;
    @Load(path = "offset.x")
    public double offset_x = 0.5;
    @Load(path = "offset.y")
    public double offset_y = 0.8;
    @Load(path = "offset.z")
    public double offset_z = 0.5;

    @Load(path = "show_damage")
    public boolean show_damage = true;
    @Load(path = "show_swing")
    public boolean show_swing = true;
    @Load(path = "show_armor")
    public boolean show_armor = true;
    @Load(path = "show_on_tab")
    public boolean show_on_tab = true;
    @Load(path = "realistic_ping")
    public boolean realistic_ping = true;
    @Load(path = "realistic_name")
    public boolean realistic_name = true;
    @Load(path = "respawn_interval")
    public int respawn_interval = 180;
}