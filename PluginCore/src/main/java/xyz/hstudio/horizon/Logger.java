package xyz.hstudio.horizon;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public final class Logger {

    private static final ConsoleCommandSender SENDER = Bukkit.getConsoleSender();

    private Logger() {
    }

    public static void msg(final String prefix, final Object object) {
        Logger.SENDER.sendMessage("(H|" + prefix + ") " + object);
    }

    public static void msg(final Object object) {
        msg("", object);
    }
}