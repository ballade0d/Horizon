package xyz.hstudio.horizon.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Logger {

    private static final ConsoleCommandSender SENDER = Bukkit.getConsoleSender();

    public static void info(final String prefix, final Object object) {
        Logger.SENDER.sendMessage("(H|" + prefix + ") " + object.toString());
    }
}