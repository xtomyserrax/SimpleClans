package net.sacredlabyrinth.phaed.simpleclans.commands;

import net.sacredlabyrinth.phaed.simpleclans.*;
import static net.sacredlabyrinth.phaed.simpleclans.SimpleClans.lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.List;

import static net.sacredlabyrinth.phaed.simpleclans.SimpleClans.lang;

/**
 * @author phaed
 */
public class AllyCommand {
    public AllyCommand() {
    }

    /**
     * Execute the command
     *
     * @param player
     * @param arg
     */
    public void execute(Player player, String[] arg) {
        SimpleClans plugin = SimpleClans.getInstance();

        if (!plugin.getPermissionsManager().has(player, "simpleclans.leader.ally")) {
            ChatBlock.sendMessage(player, ChatColor.RED + lang("insufficient.permissions",player));
            return;
        }

        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);

        if (cp == null) {
            ChatBlock.sendMessage(player, ChatColor.RED + lang("not.a.member.of.any.clan",player));
            return;
        }

        Clan clan = cp.getClan();

        if (!clan.isVerified()) {
            ChatBlock.sendMessage(player, ChatColor.RED + lang("clan.is.not.verified",player));
            return;
        }      

        if (arg.length != 2) {
            ChatBlock.sendMessage(player, ChatColor.RED + MessageFormat.format(lang("usage.ally",player), plugin.getSettingsManager().getCommandClan()));
            return;
        }

        if (clan.getSize() < plugin.getSettingsManager().getClanMinSizeToAlly()) {
            ChatBlock.sendMessage(player, ChatColor.RED + MessageFormat.format(lang("minimum.to.make.alliance",player), plugin.getSettingsManager().getClanMinSizeToAlly()));
            return;
        }

        String action = arg[0];
        Clan ally = plugin.getClanManager().getClan(arg[1]);

        if (ally == null) {
            ChatBlock.sendMessage(player, ChatColor.RED + lang("no.clan.matched",player));
            return;
        }

        if (!ally.isVerified()) {
            ChatBlock.sendMessage(player, ChatColor.RED + lang("cannot.ally.with.an.unverified.clan",player));
            return;
        }

        if (action.equals(lang("add",player))) {
            if (!plugin.getPermissionsManager().has(player, RankPermission.ALLY_ADD, PermissionLevel.LEADER, true)) {
            	return;
            }

            if (clan.isAlly(ally.getTag())) {
                ChatBlock.sendMessage(player, ChatColor.RED + lang("your.clans.are.already.allies",player));
                return;
            }

            int maxAlliances = plugin.getSettingsManager().getClanMaxAlliances();
            if (maxAlliances != -1) {
                if (clan.getAllies().size() >= maxAlliances) {
                    ChatBlock.sendMessage(player, lang("your.clan.reached.max.alliances", player));
                    return;
                }
                if (ally.getAllies().size() >= maxAlliances) {
                    ChatBlock.sendMessage(player, lang("other.clan.reached.max.alliances", player));
                    return;
                }
            }

            List<ClanPlayer> onlineLeaders = Helper.stripOffLinePlayers(clan.getLeaders());

            if (onlineLeaders.isEmpty()) {
                ChatBlock.sendMessage(player, ChatColor.RED + lang("at.least.one.leader.accept.the.alliance",player));
                return;
            }

            plugin.getRequestManager().addAllyRequest(cp, ally, clan);
            ChatBlock.sendMessage(player, ChatColor.AQUA + MessageFormat.format(lang("leaders.have.been.asked.for.an.alliance",player), ally.getName()));
        } else if (action.equals(lang("remove",player))) {
            if (!plugin.getPermissionsManager().has(player, RankPermission.ALLY_REMOVE, PermissionLevel.LEADER, true)) {
            	return;
            }
        	
            if (!clan.isAlly(ally.getTag())) {
                ChatBlock.sendMessage(player, ChatColor.RED + lang("your.clans.are.not.allies",player));
                return;
            }

            clan.removeAlly(ally);
            ally.addBb(cp.getName(), ChatColor.AQUA + MessageFormat.format(lang("has.broken.the.alliance"), clan.getName(), ally.getName()), false);
            clan.addBb(cp.getName(), ChatColor.AQUA + MessageFormat.format(lang("has.broken.the.alliance"), cp.getName(), ally.getName()));
        } else {
            ChatBlock.sendMessage(player, ChatColor.RED + MessageFormat.format(lang("usage.ally",player), plugin.getSettingsManager().getCommandClan()));
        }
    }
}
