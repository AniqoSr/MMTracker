package net.justforfun.MMTracker.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.justforfun.MMTracker.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class MMTrackerPlaceholder extends PlaceholderExpansion {
    private final Main plugin;
    private final FileConfiguration yamlConfig;
    private final boolean debug;

    public MMTrackerPlaceholder(Main plugin) {
        this.plugin = plugin;
        this.yamlConfig = plugin.getDatabase().getYamlConfig();
        this.debug = plugin.getConfigManager().isDebug();
    }

    @NotNull
    public String getIdentifier() {
        return "mmtrack";
    }

    @NotNull
    public String getAuthor() {
        return "JFF";
    }

    @NotNull
    public String getVersion() {
        return "1.0";
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params == null || params.isEmpty()) {
            return "None";
        }

        String[] parts = params.split("_");
        if (parts.length != 3) {
            return "None";
        }

        String type = parts[0];
        String identifier = parts[1];
        int rank;

        try {
            rank = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "None";
        }

        if (this.debug) {
            this.plugin.getLogger().info("Parsing placeholder: type=" + type + ", identifier=" + identifier + ", rank=" + rank);
        }

        switch (type.toLowerCase()) {
            case "topname":
                return getTopName(identifier, rank);
            case "topdamage":
                return getTopDamage(identifier, rank);
            default:
                return "None";
        }
    }

    private String getTopName(String identifier, int rank) {
        String path = "id." + identifier + ".totaldamage.players." + rank;
        String name = this.yamlConfig.getString(path + ".Name", "None");
        if (this.debug) {
            this.plugin.getLogger().info("Fetching top name for " + identifier + " rank " + rank + ": " + name);
        }
        return name;
    }

    private String getTopDamage(String identifier, int rank) {
        String path = "id." + identifier + ".totaldamage.players." + rank;
        int damage = this.yamlConfig.getInt(path + ".Damage", 0);
        if (this.debug) {
            this.plugin.getLogger().info("Fetching top damage for " + identifier + " rank " + rank + ": " + damage);
        }
        return String.valueOf(damage);
    }
}
