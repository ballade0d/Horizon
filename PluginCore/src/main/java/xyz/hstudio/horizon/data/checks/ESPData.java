package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ESPData extends Data {

    public final Set<UUID> hiddenPlayers = new HashSet<>();
}