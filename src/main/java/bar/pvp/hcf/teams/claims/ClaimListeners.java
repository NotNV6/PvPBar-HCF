package bar.pvp.hcf.teams.claims;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.material.Openable;
import org.bukkit.material.Redstone;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ClaimListeners implements Listener {

    private HCTeams main = HCTeams.getInstance();
    private Config mainConfig = main.getMainConfig();
    private Config langConfig = main.getLangConfig();

    @EventHandler
    public void onMove(final PlayerMoveEvent e) {
        if (e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) {
            final Player p = e.getPlayer();
            final Profile profile = Profile.getByUuid(p.getUniqueId());
            final Claim claim = Claim.getProminentClaimAt(e.getTo());
            if (claim != null) {
                if (claim.isInside(e.getTo())) {
                    if (profile.getLastInside() == null) {
                        profile.setLastInside(claim);
                        p.sendMessage(langConfig.getString("FACTION_CLAIM.LEAVE.WILDERNESS"));
                        p.sendMessage(getEnteringMessage(profile, claim));
                        return;
                    }

                    if (profile.getLastInside().getTeam() != claim.getTeam()) {
                        if (profile.getLastInside().isInside(e.getTo()) && !profile.getLastInside().isGreaterThan(claim)) {
                            return;
                        }
                        p.sendMessage(getLeavingMessage(profile, profile.getLastInside()));
                        p.sendMessage(getEnteringMessage(profile, claim));
                    }

                    profile.setLastInside(claim);
                } else {
                    if (profile.getLastInside() != null && profile.getLastInside() == claim) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (profile.getLastInside() != null && profile.getLastInside() == claim) {
                                    p.sendMessage(getLeavingMessage(profile, claim));
                                    p.sendMessage(langConfig.getString("FACTION_CLAIM.ENTER.WILDERNESS"));
                                    profile.setLastInside(null);
                                }
                            }
                        }.runTaskLater(main, 1L);
                    }
                }
            } else {
                if (profile.getLastInside() != null) {
                    p.sendMessage(getLeavingMessage(profile, profile.getLastInside()));
                    p.sendMessage(langConfig.getString("FACTION_CLAIM.ENTER.WILDERNESS"));
                    profile.setLastInside(profile.getLastInside());
                    profile.setLastInside(null);
                }
            }

        }
    }

    private String getLeavingMessage(Profile profile, Claim inside) {
        if (profile.getFaction() != null && profile.getFaction() == inside.getTeam()) {
            return langConfig.getString("FACTION_CLAIM.LEAVE.FRIENDLY").replace("%FACTION%", inside.getTeam().getName());
        } else if (profile.getFaction() != null && inside.getTeam() instanceof PlayerTeam && profile.getFaction().getAllies().contains(inside.getTeam())) {
            return langConfig.getString("FACTION_CLAIM.LEAVE.ALLY").replace("%FACTION%", inside.getTeam().getName());
        } else if (!(inside.getTeam() instanceof PlayerTeam)) {
            SystemTeam systemFaction = (SystemTeam) inside.getTeam();
            if (systemFaction.isDeathban()) {
                return langConfig.getString("FACTION_CLAIM.LEAVE.SYSTEM_FACTION_DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
            } else {
                return langConfig.getString("FACTION_CLAIM.LEAVE.SYSTEM_FACTION_NON-DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
            }
        } else {
            return langConfig.getString("FACTION_CLAIM.LEAVE.ENEMY").replace("%FACTION%", inside.getTeam().getName());
        }
    }

    private String getEnteringMessage(Profile profile, Claim inside) {
        if (profile.getFaction() != null && profile.getFaction() == inside.getTeam()) {
            return langConfig.getString("FACTION_CLAIM.ENTER.FRIENDLY").replace("%FACTION%", inside.getTeam().getName());
        } else if (profile.getFaction() != null && inside.getTeam() instanceof PlayerTeam && profile.getFaction().getAllies().contains(inside.getTeam())) {
            return langConfig.getString("FACTION_CLAIM.ENTER.ALLY").replace("%FACTION%", inside.getTeam().getName());
        } else if (!(inside.getTeam() instanceof PlayerTeam)) {
            SystemTeam systemFaction = (SystemTeam) inside.getTeam();
            if (systemFaction.isDeathban()) {
                return langConfig.getString("FACTION_CLAIM.ENTER.SYSTEM_FACTION_DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
            } else {
                Player player = Bukkit.getPlayer(profile.getUuid());
                if (player != null) {
                    player.setHealth(((CraftPlayer)player).getMaxHealth());
                }
                return langConfig.getString("FACTION_CLAIM.ENTER.SYSTEM_FACTION_NON-DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
            }
        } else {
            return langConfig.getString("FACTION_CLAIM.ENTER.ENEMY").replace("%FACTION%", inside.getTeam().getName());
        }
    }

    @EventHandler
    public void onPlayerInteractClaim(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().equals(Team.getWand(main))) {
            event.setCancelled(true);

            Profile profile = Profile.getByUuid(player.getUniqueId());
            ClaimProfile claimProfile = profile.getClaimProfile();

            if (claimProfile == null) {
                if (profile.getFaction() != null) {
                    claimProfile = new ClaimProfile(player, profile.getFaction());
                } else {
                    return;
                }
            }

            final Team team = claimProfile.getTeam();
            if (team instanceof PlayerTeam) {
                PlayerTeam playerFaction = (PlayerTeam) team;
                if (playerFaction.getLeader() != player.getUniqueId() && !playerFaction.getOfficers().contains(player.getUniqueId()) && !player.hasPermission("hcteams.admin")) {
                    player.getInventory().removeItem(Team.getWand(main));
                    claimProfile.removePillars();
                    return;
                }
            }

            if (event.getAction().name().contains("BLOCK")) {
                final Material material = Material.valueOf(mainConfig.getString("FACTION_CLAIMING.CLAIM_PILLAR.TYPE"));
                final int data = mainConfig.getInteger("FACTION_CLAIMING.CLAIM_PILLAR.DATA");
                final Location location = event.getClickedBlock().getLocation();
                claimProfile.setResetClicked(false);

                int toDisplay = 0;
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    toDisplay = 1;
                } else {
                    if (!team.getClaims().isEmpty() && team instanceof PlayerTeam && !team.isNearBorder(location)) {
                        player.sendMessage(langConfig.getString("ERROR.MUST_CLAIM_CLOSER"));
                        return;
                    }
                }

                final ClaimPillar pillar = claimProfile.getPillars()[toDisplay];
                final String message = (langConfig.getString("FACTION_CLAIM.SET_POSITION_" + (toDisplay + 1)).replace("%X_POS%", location.getBlockX() + "").replace("%Z_POS%", location.getBlockZ() + ""));

                boolean b;

                for(Claim claim : Claim.getClaims()) {
                    if (claim.isInside(location) && (!(team instanceof SystemTeam))) {
                        player.sendMessage(langConfig.getString("ERROR.CANNOT_CLAIM_HERE"));
                        return;
                    }
                    if (claim.isNearby(location, mainConfig.getInteger("FACTION_CLAIMING.BUFFER")) && claim.getTeam() != team && (!(team instanceof SystemTeam))) {
                        player.sendMessage(langConfig.getString("ERROR.CANNOT_CLAIM_BUFFER"));
                        return;
                    }
                }

                if (toDisplay == 1) {
                    final ClaimProfile finalClaimProfile = claimProfile;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ClaimPillar secondPillar = finalClaimProfile.getPillars()[0];
                            ClaimPillar firstPillar = new ClaimPillar(player, location);

                            if (secondPillar != null) {
                                Location cornerOne = firstPillar.getLocation();
                                Location cornerTwo = secondPillar.getLocation();
                                Location cornerThree = new Location(cornerOne.getWorld(), cornerOne.getBlockX(), 0, cornerTwo.getBlockZ());

                                int width = (int) cornerThree.distance(cornerOne) + 1;
                                int length = (int) cornerThree.distance(cornerTwo) + 1;

                                if (width < mainConfig.getInteger("FACTION_CLAIMING.MIN_SIZE") || length < mainConfig.getInteger("FACTION_CLAIMING.MIN_SIZE")) {
                                    player.sendMessage(langConfig.getString("ERROR.CLAIM_TOO_SMALL"));
                                    return;
                                }

                                player.sendMessage(message);
                                player.sendMessage(langConfig.getString("FACTION_CLAIM.BOTH_POSITIONS_SET").replace("%COST%", calculateCosts(firstPillar, secondPillar) + "").replace("%LENGTH%", length + "").replace("%WIDTH%", width + "").replace("%TOTAL_BLOCKS%", length * width + ""));
                            } else {
                                player.sendMessage(message);
                            }

                            if (pillar != null) {
                                if (pillar.getLocation().equals(location)) {
                                    return;
                                } else {
                                    pillar.remove();
                                }
                            }

                            finalClaimProfile.getPillars()[1] = firstPillar.show(material, data);
                        }
                    }.runTaskLaterAsynchronously(main, 1L);
                } else {
                    ClaimPillar secondPillar = claimProfile.getPillars()[1];
                    ClaimPillar firstPillar = new ClaimPillar(player, location);

                    if (secondPillar != null) {
                        Location cornerOne = firstPillar.getLocation();
                        Location cornerTwo = secondPillar.getLocation();
                        Location cornerThree = new Location(cornerOne.getWorld(), cornerOne.getBlockX(), 0, cornerTwo.getBlockZ());
                        int width = (int) cornerThree.distance(cornerOne) + 1;
                        int length = (int) cornerThree.distance(cornerTwo) + 1;

                        if (width < mainConfig.getInteger("FACTION_CLAIMING.MIN_SIZE") || length < mainConfig.getInteger("FACTION_CLAIMING.MIN_SIZE")) {
                            player.sendMessage(langConfig.getString("ERROR.CLAIM_TOO_SMALL"));
                            return;
                        }

                        player.sendMessage(message);
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.BOTH_POSITIONS_SET").replace("%COST%", calculateCosts(firstPillar, secondPillar) + "").replace("%LENGTH%", length + "").replace("%WIDTH%", width + "").replace("%TOTAL_BLOCKS%", length * width + ""));
                    } else {
                        player.sendMessage(message);
                    }

                    if (pillar != null) {
                        if (pillar.getLocation().equals(location)) {
                            return;
                        } else {
                            pillar.remove();
                        }
                    }

                    claimProfile.getPillars()[0] = firstPillar.show(material, data);
                }
                return;
            }


            if (event.getAction() == Action.LEFT_CLICK_AIR && player.isSneaking()) {

                if (claimProfile.getPillars()[0] == null || claimProfile.getPillars()[1] == null) {
                    player.sendMessage(langConfig.getString("ERROR.INVALID_SELECTION"));
                    return;
                }

                ClaimPillar firstPillar = claimProfile.getPillars()[0];
                ClaimPillar secondPillar = claimProfile.getPillars()[1];
                Location cornerOne = firstPillar.getLocation();
                Location cornerTwo = secondPillar.getLocation();
                Location cornerThree = new Location(cornerOne.getWorld(), cornerOne.getBlockX(), 0, cornerTwo.getBlockZ());
                Location cornerFour = new Location(cornerOne.getWorld(), cornerTwo.getBlockX(), 0, cornerOne.getBlockZ());

                if (team instanceof PlayerTeam) {
                    Claim.getClaims().forEach(claim -> {
                        if (claim.overlaps(firstPillar.getLocation().getBlockX(), firstPillar.getLocation().getBlockZ(), secondPillar.getLocation().getBlockX(), secondPillar.getLocation().getBlockZ())) {
                            player.sendMessage(langConfig.getString("ERROR.CANNOT_OVERCLAIM"));
                            return;
                        }
                        if (claim.isNearby(cornerOne, mainConfig.getInteger("FACTION_CLAIMING.BUFFER")) && claim.getTeam() != team) {
                            player.sendMessage(langConfig.getString("ERROR.CANNOT_CLAIM_BUFFER"));
                            return;
                        }
                        if (claim.isNearby(cornerTwo, mainConfig.getInteger("FACTION_CLAIMING.BUFFER")) && claim.getTeam() != team) {
                            player.sendMessage(langConfig.getString("ERROR.CANNOT_CLAIM_BUFFER"));
                            return;
                        }
                        if (claim.isNearby(cornerThree, mainConfig.getInteger("FACTION_CLAIMING.BUFFER")) && claim.getTeam() != team) {
                            player.sendMessage(langConfig.getString("ERROR.CANNOT_CLAIM_BUFFER"));
                            return;
                        }
                        if (claim.isNearby(cornerFour, mainConfig.getInteger("FACTION_CLAIMING.BUFFER")) && claim.getTeam() != team) {
                            player.sendMessage(langConfig.getString("ERROR.CANNOT_CLAIM_BUFFER"));
                        }
                    });
                }

                int price = calculateCosts(firstPillar, secondPillar);

                if (team instanceof PlayerTeam) {
                    PlayerTeam playerFaction = (PlayerTeam) team;

                    if (playerFaction.getBalance() < price && !player.hasPermission("hcteams.admin")) {
                        player.sendMessage(langConfig.getString("ERROR.FACTION_NOT_ENOUGH_MONEY"));
                        return;
                    }

                    playerFaction.setBalance(playerFaction.getBalance() - price);
                    playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_CLAIM_LAND").replace("%PLAYER%", player.getName()));
                }

                new Claim(team, new int[]{firstPillar.getLocation().getBlockX(), secondPillar.getLocation().getBlockX(), firstPillar.getLocation().getBlockZ(), secondPillar.getLocation().getBlockZ()}, firstPillar.getLocation().getWorld().getName());
                claimProfile.removePillars();
                player.getInventory().remove(Team.getWand(main));
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (claimProfile.getPillars()[0] == null && claimProfile.getPillars()[1] == null) {
                    player.sendMessage(langConfig.getString("ERROR.INVALID_SELECTION"));
                    return;
                }
                if (claimProfile.isResetClicked()) {
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.SELECTION_RESET"));
                    claimProfile.setResetClicked(false);
                    claimProfile.removePillars();
                } else {
                    claimProfile.setResetClicked(true);
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.CLICK_TO_RESET_SELECTION"));
                }
            }
        }
    }

    private int calculateCosts(ClaimPillar firstPillar, ClaimPillar secondPillar) {
        Location cornerOne = firstPillar.getLocation();
        Location cornerTwo = secondPillar.getLocation();
        Location cornerThree = new Location(cornerOne.getWorld(), cornerOne.getBlockX(), 0, cornerTwo.getBlockZ());
        int width = (int) cornerThree.distance(cornerOne) + 1;
        int length = (int) cornerThree.distance(cornerTwo) + 1;
        return (int) (width * length * mainConfig.getDouble("FACTION_CLAIMING.PRICE_MULTIPLIER"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().getInventory().removeItem(Team.getWand(main));
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity().getType() == EntityType.ITEM_FRAME) {
            Player player = (Player) event.getDamager();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.isInAdminMode()) {
                return;
            }

            Claim claim = Claim.getProminentClaimAt(event.getEntity().getLocation());
            if (claim != null) {
                Team team = claim.getTeam();

                if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                    return;
                }
                
                if (team instanceof SystemTeam) {
                    SystemTeam systemFaction = (SystemTeam) team;
                    if (systemFaction.getName().equalsIgnoreCase("Warzone")) {
                        return;
                    }
                }

                if (profile.getFaction() == null || !team.equals(profile.getFaction())) {
                    if (team instanceof SystemTeam) {
                        SystemTeam systemFaction = (SystemTeam) team;
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                    } else {
                        if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team))
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                        else
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));

                    }
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        Claim claim = Claim.getProminentClaimAt(event.getEntity().getLocation());
        if (claim != null) {
            Team team = claim.getTeam();

            if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) return;


            if (profile.getFaction() == null || !team.equals(profile.getFaction())) {
                if (team instanceof SystemTeam) {
                    SystemTeam systemFaction = (SystemTeam) team;
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                } else {
                    if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team))
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                    else
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));

                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        Block block = event.getBlock();
        if (Claim.getProminentClaimAt(block.getLocation()) == null) {
            event.getBlocks().forEach(other -> {
                if (Claim.getProminentClaimAt(other.getLocation()) != null) {
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        Block block = event.getBlock();
        if (Claim.getProminentClaimAt(block.getLocation()) == null) {
            if (Claim.getProminentClaimAt(event.getRetractLocation()) != null) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.isInAdminMode()) return;

            Claim claim = Claim.getProminentClaimAt(event.getEntity().getLocation());
            if (claim != null) {
                Team team = claim.getTeam();

                if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) return;


                if (profile.getFaction() == null || !team.equals(profile.getFaction())) {
                    if (team instanceof SystemTeam) {
                        SystemTeam systemFaction = (SystemTeam) team;
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                    } else {
                        if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team))
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                        else
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getItemMeta().hasDisplayName()) {
            if (event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals(Team.getWand(main).getItemMeta().getDisplayName())) {
                event.getItemDrop().remove();

                ClaimProfile profile = Profile.getByUuid(event.getPlayer().getUniqueId()).getClaimProfile();
                if (profile != null) {
                    Arrays.stream(profile.getPillars()).forEach(pillar -> {
                        if(pillar != null) pillar.remove();
                    });
                }
            }
        }
    }

    @EventHandler
    public void onExplodeEvent(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamageInSafezone(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damager;

            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player)
                    damager = (Player) projectile.getShooter();
                else return;

            } else return;

            Player damaged = (Player) event.getEntity();
            Profile damagerProfile = Profile.getByUuid(damager.getUniqueId());

            Claim damagerClaim = Claim.getProminentClaimAt(damager.getLocation());
            if (!damagerProfile.isInAdminMode() && damagerClaim != null && damagerClaim.isInside(damager.getLocation()) && damagerClaim.getTeam() instanceof SystemTeam) {
                SystemTeam systemFaction = (SystemTeam) damagerClaim.getTeam();
                if (!(systemFaction.isDeathban())) {
                    damager.sendMessage(langConfig.getString("FACTION_CLAIM.DAMAGER_IN_SAFEZONE").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + ""));
                    event.setCancelled(true);
                    return;
                }
            }

            Claim damagedClaim = Claim.getProminentClaimAt(damaged.getLocation());
            if (!damagerProfile.isInAdminMode() && damagedClaim != null && damagedClaim.getTeam() instanceof SystemTeam) {
                SystemTeam systemFaction = (SystemTeam) damagedClaim.getTeam();
                if (!(systemFaction.isDeathban())) {
                    damager.sendMessage(langConfig.getString("FACTION_CLAIM.DAMAGED_IN_SAFEZONE").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + ""));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        Claim claim = Claim.getProminentClaimAt(event.getBlock().getLocation());
        if (claim != null) {
            Team team = claim.getTeam();

            if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                return;
            }

            if (team instanceof SystemTeam) {
                SystemTeam systemFaction = (SystemTeam) team;
                if (systemFaction.getName().equalsIgnoreCase("Warzone")) {
                    return;
                }
            }

            if (!team.equals(profile.getFaction())) {
                if (team instanceof SystemTeam) {
                    SystemTeam systemFaction = (SystemTeam) team;
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                } else {
                    if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                    } else {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        Claim claim = Claim.getProminentClaimAt(event.getBlock().getLocation());
        if (claim != null) {
            Team team = claim.getTeam();

            if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                return;
            }

            if (team instanceof SystemTeam) {
                SystemTeam systemFaction = (SystemTeam) team;
                if (systemFaction.getName().equalsIgnoreCase("Warzone")) {
                    return;
                }
            }

            if (!team.equals(profile.getFaction())) {
                if (team instanceof SystemTeam) {
                    SystemTeam systemFaction = (SystemTeam) team;
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                } else {
                    if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                    } else {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        Claim claim = Claim.getProminentClaimAt(event.getBlockClicked().getLocation());
        if (claim != null) {
            Team team = claim.getTeam();

            if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                return;
            }
            if (team instanceof SystemTeam) {
                SystemTeam systemFaction = (SystemTeam) team;
                if (systemFaction.getName().equalsIgnoreCase("Warzone")) {
                    return;
                }
            }
            if (!team.equals(profile.getFaction())) {
                if (team instanceof SystemTeam) {
                    SystemTeam systemFaction = (SystemTeam) team;
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                } else {
                    if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                    } else {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        Claim claim = Claim.getProminentClaimAt(event.getBlockClicked().getLocation());
        if (claim != null) {
            Team team = claim.getTeam();

            if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                return;
            }

            if (team instanceof SystemTeam) {
                SystemTeam systemFaction = (SystemTeam) team;
                if (systemFaction.getName().equalsIgnoreCase("Warzone")) {
                    return;
                }
            }

            if (!team.equals(profile.getFaction())) {
                if (team instanceof SystemTeam) {
                    SystemTeam systemFaction = (SystemTeam) team;
                    player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                } else {
                    if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                    } else {
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        if (event.getRightClicked() instanceof Hanging) {
            Entity entity = event.getRightClicked();

            Claim claim = Claim.getProminentClaimAt(entity.getLocation());
            if (claim != null) {
                Team team = claim.getTeam();

                if (profile.isInAdminMode()) {
                    return;
                }

                if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                    return;
                }

                if (profile.getFaction() == null || !team.equals(profile.getFaction())) {
                    if (team instanceof SystemTeam) {
                        SystemTeam systemFaction = (SystemTeam) team;
                        player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                    } else {
                        if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ALLY").replace("%FACTION%", team.getName()));
                        } else {
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_BUILD_ENEMY").replace("%FACTION%", team.getName()));
                        }
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof ContainerBlock || block.getState().getData() instanceof Redstone || block.getState().getData() instanceof Openable) {
                Claim claim = Claim.getProminentClaimAt(block.getLocation());
                if (claim != null) {
                    Team team = claim.getTeam();

                    if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                        return;
                    }

                    if (!team.equals(profile.getFaction())) {
                        if (team instanceof SystemTeam) {
                            SystemTeam systemFaction = (SystemTeam) team;
                            player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_INTERACT_SYSTEM").replace("%COLOR%", systemFaction.getColor() + "").replace("%FACTION%", systemFaction.getName()));
                        } else {
                            if (team instanceof PlayerTeam && profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                                player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_INTERACT_ALLY").replace("%FACTION%", team.getName()));
                            } else {
                                player.sendMessage(langConfig.getString("FACTION_CLAIM.CANNOT_INTERACT_ENEMY").replace("%FACTION%", team.getName()));
                            }
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPhsycialInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInAdminMode()) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            Claim claim = Claim.getProminentClaimAt(block.getLocation());
            if (claim != null) {
                Team team = claim.getTeam();

                if (team instanceof PlayerTeam && ((PlayerTeam) team).isRaidable()) {
                    return;
                }

                if (!team.equals(profile.getFaction())) {
                    if (!(team instanceof SystemTeam)) {
                        event.setCancelled(true);
                    } else {
                        if (block.getType() == Material.SOIL) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryInreact(InventoryMoveItemEvent event) {
        if (event.getItem() == Team.getWand(main)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInreact(InventoryClickEvent event) {
        if (event.getInventory().contains(Team.getWand(main))) {
            event.getInventory().remove(Team.getWand(main));
            ClaimProfile profile = Profile.getByUuid(event.getWhoClicked().getUniqueId()).getClaimProfile();
            if (profile != null) {
                Arrays.stream(profile.getPillars()).forEach(pillar -> {
                    if(pillar != null) pillar.remove();
                });
            }
        }
        if (event.getClickedInventory() != null) {
            if (event.getClickedInventory().getHolder() instanceof Player) {
                Player player = (Player) event.getClickedInventory().getHolder();
                ClaimProfile profile = Profile.getByUuid(player.getUniqueId()).getClaimProfile();
                if (profile != null) {
                    Arrays.stream(profile.getPillars()).forEach(pillar -> {
                        if(pillar != null) pillar.remove();
                    });
                }
            }
            event.getClickedInventory().remove(Team.getWand(main));
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL && event.getEntity() instanceof Monster) {
            Claim claim = Claim.getProminentClaimAt(event.getLocation());
            if (claim != null) {
                if (claim.getTeam() instanceof SystemTeam) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
