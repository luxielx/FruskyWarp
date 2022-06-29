package SolarWarp;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by ADMIN on 2/23/2018.
 */
@SuppressWarnings("deprecation")
public class Main extends JavaPlugin {
    public Essentials ess;
    public static HashMap<UUID, Long> hasm = new HashMap<>();

    @Override
    public void onEnable() {
        ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");


    }


    @Override
    public void onDisable() {

    }


    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        try {
            if (cmd.getName().equalsIgnoreCase("warp")) {

                if (sender.hasPermission("solarwarp.admin") && args.length > 1) {
                    if(!ess.getWarps().getList().contains(args[1])) return false;
                    if (Bukkit.getPlayer(args[0]) != null) {
                        Player spec = Bukkit.getPlayer(args[0]);
                        teleportToWarp(spec, args[1]);
                    }
                    if (sender instanceof Player) {
                        if(!ess.getWarps().getList().contains(args[0])) return false;

                        teleportToWarp((Player) sender, args[0]);
                    }
                }
                if (sender instanceof Player) {

                    if (args.length > 0) {
                        if(!ess.getWarps().getList().contains(args[0])) return false;

                        if (((Player) sender).isOnGround()) {
                            teleportToWarp((Player) sender, args[0]);
                        } else {
                            new BukkitRunnable() {
                                int c = 0;

                                @Override
                                public void run() {
                                    if (c >= 20) this.cancel();
                                    if (((Player) sender).isOnGround()) {
                                        teleportToWarp((Player) sender, args[0]);
                                        this.cancel();
                                    }
                                    c++;

                                }
                            }.runTaskTimer(this, 5, 5);
                        }
                    } else {
                        if (sender instanceof Player)
                            ((Player) sender).chat("/essentials:warp");

                    }


                }

            } else if (cmd.getName().equalsIgnoreCase("spawn")) {
                if (sender.hasPermission("solarwarp.admin") && args.length > 0) {
                    if (Bukkit.getPlayer(args[0]) != null) {
                        Player spec = Bukkit.getPlayer(args[0]);
                        teleportToWarp(spec , "spawn");
                    }
                    if (sender instanceof Player) {
                        teleportToWarp((Player) sender, "spawn");
                    }
                }
                if (sender instanceof Player) {
                    if (((Player) sender).isOnGround()) {
                        teleportToWarp((Player) sender, "spawn");
                    } else {
                        new BukkitRunnable() {
                            int c = 0;

                            @Override
                            public void run() {
                                if (c >= 20) this.cancel();
                                if (((Player) sender).isOnGround()) {
                                    teleportToWarp((Player) sender, "spawn");
                                    this.cancel();
                                }
                                c++;

                            }
                        }.runTaskTimer(this, 5, 5);
                    }

                }
            }
            return true;
        } catch (Exception e) {

        }
        return true;
    }


    public void teleportToWarp(Player player, String warp) {
        if (!player.hasPermission("solar.warp")) {
            player.chat("/essentials:warp " + warp);
            return;
        }
        boolean allow = false;
        if (hasm.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - hasm.get(player.getUniqueId()) >= 10000) {
                allow = true;
                hasm.put(player.getUniqueId(), System.currentTimeMillis());
            } else {
                player.sendMessage(ChatColor.RED + "You have to wait " + (int) ((10000 - (System.currentTimeMillis() - hasm.get(player.getUniqueId())) )/ 1000) + " seconds to use this again");
            }
        } else {
            hasm.put(player.getUniqueId(), System.currentTimeMillis());
            allow = true;
        }
        if(!allow) return;
        new BukkitRunnable() {
            int tptime = 10;
            int counter = 0;
            Location prev = player.getLocation();
            double health = player.getHealth();

            @Override
            public void run() {
                if (player.getHealth() < health) {
                    player.sendMessage(ChatColor.RED + "Teleportation has been canceled while you got hit!");
                    if (hasm.containsKey(player.getUniqueId())) hasm.remove(player.getUniqueId());
                    this.cancel();
                }
                if (prev.getWorld() == player.getWorld()) {
                    prev.setY(player.getLocation().getY());
                    if (prev.distance(player.getLocation()) >= 0.1) {
                        player.sendMessage(ChatColor.RED + "Teleportation has been canceled while you moved!");
                        if (hasm.containsKey(player.getUniqueId())) hasm.remove(player.getUniqueId());
                        this.cancel();
                    }
                } else {
                    this.cancel();
                }

                if (counter % 2 == 0) {
                    createHeliX(player.getLocation());
                    createHeliY(player.getLocation());
                    player.sendMessage(ChatColor.GRAY + "Teleporting in " + ChatColor.RED + (tptime * 2 - counter) / 2 + ChatColor.GRAY + "...");
                }
                if (counter >= tptime * 2) {
                    try {
                        player.teleport(ess.getWarps().getWarp(warp), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
                    } catch (WarpNotFoundException e) {

                        e.printStackTrace();
                    } catch (InvalidWorldException e) {
                        e.printStackTrace();
                    }

                    this.cancel();
                }
                prev = player.getLocation();
                counter++;
            }
        }.runTaskTimer(this, 0, 10);

    }

    public void createHeliX(Location loc) {
        double radius = 0.8;
        new BukkitRunnable() {
            double y = 0;

            @Override
            public void run() {
                if (y > 3)
                    this.cancel();

                double x = radius * Math.cos(y);
                double z = radius * Math.sin(y);
                loc.getWorld().spigot().playEffect(loc.clone().add(x, y, z), Effect.FLAME, 0, 0, 0, 0, 0, 0, 1, 30);
                loc.getWorld().spigot().playEffect(loc.clone().add(z, y, x), Effect.FLAME, 0, 0, 0, 0, 0, 0, 1, 30);
                y += 0.1;

            }
        }.runTaskTimerAsynchronously(this, 0, 1);
    }

    public void createHeliY(Location loc) {
        double radius = -0.8;
        new BukkitRunnable() {
            double y = 0;

            @Override
            public void run() {
                if (y > 3)
                    this.cancel();
                double x = radius * Math.cos(y);
                double z = radius * Math.sin(y);
                loc.getWorld().spigot().playEffect(loc.clone().add(x, y, z), Effect.FLAME, 0, 0, 0, 0, 0, 0, 1, 30);
                loc.getWorld().spigot().playEffect(loc.clone().add(z, y, x), Effect.FLAME, 0, 0, 0, 0, 0, 0, 1, 30);


                y += 0.1;

            }
        }.runTaskTimerAsynchronously(this, 0, 1);
    }


//    public static void sendTitleBar(Player p, String title, String subtitle, int fadein, int fadeout) {
//        CraftPlayer cp = (CraftPlayer) p;
//        PacketPlayOutTitle packettitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
//                IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + title + "\"}"), 0, fadein, fadeout);
//        cp.getHandle().playerConnection.sendPacket(packettitle);
//
//        PacketPlayOutTitle packetsubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
//                IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + subtitle + "\"}"), 0, fadein, fadeout);
//        cp.getHandle().playerConnection.sendPacket(packetsubtitle);
//    }
//
//    public static void sendActionBar(Player p, String msg) {
//
//        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(msg).create());
//
//
//    }

    public static ArrayList<Player> getNearbyPlayerAsync(Location player, double radius) {
        World world = player.getWorld();
        ArrayList<Player> list = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (online.getWorld().getName().equalsIgnoreCase(world.getName())) {
                if (online.getLocation().distance(player) > radius) {
                    list.remove(online);
                }
            }
        }
        return list;
    }

}
