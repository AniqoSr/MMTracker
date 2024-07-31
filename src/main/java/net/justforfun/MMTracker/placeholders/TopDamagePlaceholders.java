package net.justforfun.MMTracker.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.utils.PlaceholderAPIUtil;

public class TopDamagePlaceholders extends PlaceholderExpansion {
    private PlaceholderAPIUtil placeholderAPIUtil;

    public TopDamagePlaceholders(Main plugin) {
        this.placeholderAPIUtil = new PlaceholderAPIUtil(plugin);
    }

    @Override
    public String getIdentifier() {
        return "mmtrack";
    }

    @Override
    public String getAuthor() {
        return "JFF";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        return placeholderAPIUtil.parsePlaceholder(player, identifier);
    }
}