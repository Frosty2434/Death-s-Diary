package DeathNoteBuild.deathNote;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;

public final class DeathNote extends JavaPlugin implements Listener {
    private String PlayerName= "";
    private String causeOfDeath= "";
    public static ItemStack ID;
    public static ItemMeta ID_meta;
    public static ArrayList<String> id_lore = new ArrayList<>();
    private int bar = 0;
    private Long time_mult;
    private Long long_time;
    private String kira = "";
    private Player victim;
    private ShapedRecipe recipe;
    private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON,20,2);
    private final PotionEffect wither = new PotionEffect(PotionEffectType.WITHER,20,2);
    private final PotionEffect starve = new PotionEffect(PotionEffectType.HUNGER,160,255);
    private final PotionEffect darkness = new PotionEffect(PotionEffectType.DARKNESS,20000,2);
    private final PotionEffect mining_fatigue = new PotionEffect(PotionEffectType.MINING_FATIGUE,20000,2);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        ID = new ItemStack(Material.PAPER);
        ID_meta = ID.getItemMeta();
        ItemManager.CreateDeathDiary();
        recipe = new ShapedRecipe(new NamespacedKey(this,"DeathDiary"), ItemManager.DeathDiary);
        recipe.shape("NWN","SBS","NNN");
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('W', Material.WHITE_DYE);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('B', Material.WRITABLE_BOOK);
        this.getCommand("givediary").getExecutor();
        this.getCommand("removediary").getExecutor();
        this.getCommand("addrecipe").getExecutor();
        this.getCommand("removerecipe").getExecutor();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("giveDiary")){
            if(sender.isOp()){
                ChooseUser(sender, args);
                sender.sendMessage(ChatColor.BOLD+""+ChatColor.RED+"Death's Diary Has Been Dropped!");
                Objects.requireNonNull(Bukkit.getPlayer(kira)).getInventory().addItem(ItemManager.DeathDiary);

            }
            else{
                Bukkit.getPlayer(sender.getName()).sendMessage("You Are Not Allowed To Do That!!");
            }
        }
        if(command.getName().equalsIgnoreCase("removeDiary")){
            if(sender.isOp()&& !Objects.equals(args[0], sender.getName())){
                Objects.requireNonNull(Bukkit.getPlayer(kira)).getInventory().removeItem(ItemManager.DeathDiary);
                RemoveUser(sender,args);
                sender.sendMessage(ChatColor.BOLD+""+ChatColor.RED+"That Person No Longer Has The Diary!");
            }
            else if(!sender.isOp()&&Objects.equals(args[0], sender.getName())){
                Objects.requireNonNull(Bukkit.getPlayer(kira)).getInventory().removeItem(ItemManager.DeathDiary);
                RemoveUser(sender,args);
                sender.sendMessage(ChatColor.BOLD+""+ChatColor.BLACK+"You No Longer Have The Diary!");


            }
            else if(sender.isOp()&&Objects.equals(args[0], sender.getName())){
                Objects.requireNonNull(Bukkit.getPlayer(kira)).getInventory().removeItem(ItemManager.DeathDiary);
                RemoveUser(sender,args);
                sender.sendMessage(ChatColor.BOLD+""+ChatColor.BLACK+"You No Longer Have The Diary!");


            }
            else if(!sender.isOp()&&!Objects.equals(args[0], sender.getName())){
                sender.sendMessage(ChatColor.BOLD+""+ChatColor.RED+ "You Do Not have Permission To Do That!");
            }



        }
        if(command.getName().equalsIgnoreCase("removeRecipe")){
            sender.sendMessage("§4Recipe Removed!");
            Bukkit.removeRecipe(recipe.getKey());

        }
        if(command.getName().equalsIgnoreCase("addRecipe")){
            sender.sendMessage("§aRecipe Added!");
            Bukkit.addRecipe(recipe);

        }
        return  true;
    }

    public void ChooseUser(CommandSender sender, String[] args){
        kira = args[0];
    }

    public void RemoveUser(CommandSender sender, String[] args){
        kira = "";
    }



    @EventHandler
    public void onPlayerEditBook(PlayerInteractEvent e) {
        if(Objects.requireNonNull(e.getItem()).getType().equals(Material.WRITTEN_BOOK)) {
            BookMeta itembookmeta = (BookMeta) e.getItem().getItemMeta();
            if(itembookmeta.getDisplayName().equals(ItemManager.bookMeta.getDisplayName())&&itembookmeta.getLore().getFirst().equals(ItemManager.bookMeta.getLore().getFirst())&&itembookmeta.hasPages()&&itembookmeta.getTitle().equals("DeathDiary")){
                    String bookPage = itembookmeta.getPage(1);
                    //When "after" is used when it comes to choosing the time of death
                    if(bookPage.contains("will die from")&&bookPage.contains("after")&&!bookPage.contains("within")&&!bookPage.contains("in")){
                        String[] parts = bookPage.split("will die from");
                        String[] death_details = parts[1].split("after");
                        String[] time = death_details[1].split(" ");
                        if(bookPage.contains("seconds")|| bookPage.contains("second")){
                            time_mult=20L;
                        }
                        else if(bookPage.contains("minutes")|| bookPage.contains("minute")){
                            time_mult=1200L;
                        }
                        else if(bookPage.contains("hours")|| bookPage.contains("hour")){
                            time_mult=72000L;

                        }
                        PlayerName = parts[0].trim();
                        causeOfDeath = death_details[0].trim();
                        try{
                            long_time = Long.parseLong(time[1])*time_mult;
                            if(!PlayerName.equals("everyone")&&e.getPlayer().getInventory().contains(ID)){
                                SearchVictim();
                                ChooseDeathType(e);
                                e.getItem().setType(Material.WRITABLE_BOOK);

                            }
                            else{
                                e.getPlayer().sendMessage("§4ID Not Found!");
                                e.getItem().setType(Material.WRITABLE_BOOK);
                            }
                        }
                        catch (NumberFormatException ex) {
                            long_time = 800L;
                            SearchVictim();
                            ChooseDeathType(e);
                            e.getItem().setType(Material.WRITABLE_BOOK);

                        }

                    }
                    //When "in" is used when it comes to choosing the time of death
                    else if(bookPage.contains("will die from")&&!bookPage.contains("after")&&!bookPage.contains("within")&&bookPage.contains("in")&&Bukkit.getPlayer(PlayerName) != null){
                        String[] parts = bookPage.split("will die from");
                        String[] death_details = parts[1].split("in");
                        String[] time = death_details[1].split(" ");
                        if(bookPage.contains("seconds")|| bookPage.contains("second")){
                            time_mult=20L;
                        }
                        else if(bookPage.contains("minutes")|| bookPage.contains("minute")){
                            time_mult=1200L;
                        }
                        else if(bookPage.contains("hours")|| bookPage.contains("hour")){
                            time_mult=72000L;

                        }
                        PlayerName = parts[0].trim();
                        causeOfDeath = death_details[0].trim();
                        try{
                            long_time = Long.parseLong(time[1])*time_mult;
                            if(!PlayerName.equals("everyone")&&e.getPlayer().getInventory().contains(ID)){
                                SearchVictim();
                                ChooseDeathType(e);
                                e.getItem().setType(Material.WRITABLE_BOOK);

                            }
                            else{
                                e.getPlayer().sendMessage("§4ID Not Found!");
                                e.getItem().setType(Material.WRITABLE_BOOK);
                            }

                        } catch (NumberFormatException ex) {
                            long_time = 800L;

                        }
                        SearchVictim();
                        ChooseDeathType(e);
                    }
                    //When there is only a cause of death and name in the book
                    else if(bookPage.contains("will die from")&&!bookPage.contains("after")&&!bookPage.contains("in")&&!bookPage.contains("within")&&Bukkit.getPlayer(PlayerName) != null){
                        String[] parts = bookPage.split("will die from");
                        PlayerName = parts[0].trim();
                        causeOfDeath = parts[1].trim();
                        if(!PlayerName.equals("everyone")&&e.getPlayer().getInventory().contains(ID)){
                            SearchVictim();
                            long_time = 800L;
                            ChooseDeathType(e);
                            e.getItem().setType(Material.WRITABLE_BOOK);
                        }
                        else{
                            e.getPlayer().sendMessage("§4ID Not Found!");
                            e.getItem().setType(Material.WRITABLE_BOOK);
                        }



                    }
                    //When there is only a name in the book
                    else if(!bookPage.contains("will die from")&&!bookPage.contains("after")&&!bookPage.contains("in")&&!bookPage.contains("within")&&Bukkit.getPlayer(PlayerName) != null){
                        PlayerName = bookPage.trim();
                        if(!PlayerName.equals("everyone")&&e.getPlayer().getInventory().contains(ID)){
                            SearchVictim();
                            long_time = 800L;
                            Bukkit.getScheduler().runTaskLater(this, () -> {
                                if (!victim.isDead()) {
                                    victim.setHealth(0);
                                }
                                e.getItem().setType(Material.WRITABLE_BOOK);


                            }, long_time);
                        }
                        else{
                            e.getPlayer().sendMessage("§4ID Not Found!");
                            e.getItem().setType(Material.WRITABLE_BOOK);
                        }
                    }

            }
            else{
                e.getItem().setType(Material.WRITABLE_BOOK);
                e.getPlayer().sendMessage("§4Wrong Format!");
                e.getPlayer().playSound(e.getPlayer(), Sound.BLOCK_END_PORTAL_SPAWN,1.0f,1.0f);
            }

        }





    }

    public void SearchVictim(){
        if(Objects.requireNonNull(Bukkit.getPlayer(PlayerName)).isOnline()&&(!Objects.requireNonNull(Bukkit.getPlayer(PlayerName)).isDead())&&Bukkit.getPlayer(PlayerName)!=null){
            victim=Bukkit.getPlayer(PlayerName);
        }
    }
    public void ClearSurvivability(){
        victim.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        victim.removePotionEffect(PotionEffectType.REGENERATION);
        victim.removePotionEffect(PotionEffectType.RESISTANCE);
        victim.getInventory().clear();
    }
    public void ChooseMassDeathType(PlayerInteractEvent e){
        if (causeOfDeath.equals("Meteor Shower")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Location nearplayer = player.getLocation().add(5f, 50f, 5f);
                        FallingBlock meteor = player.getWorld().spawnFallingBlock(nearplayer, Material.DEEPSLATE.createBlockData());
                        meteor.setVelocity(new Vector(0, -1, 0));
                        meteor.setVisualFire(true);
                        Bukkit.getScheduler().runTaskLater(DeathNote.this, () -> {
                            meteor.getWorld().createExplosion(meteor.getLocation(), 20f);
                            meteor.remove();


                        }, 36L);

                    }

                }
            }.runTaskTimer(this, 0L, 120L);
        }

    }

    public void ChooseDeathType(PlayerInteractEvent e) {
        if (causeOfDeath.isEmpty() || causeOfDeath.equals("heart attack")&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (!victim.isDead()) {
                    victim.setHealth(0);
                }


            }, long_time);

        } else if (causeOfDeath.equals("burning") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }
                        ClearSurvivability();
                        victim.setFireTicks(20);
                        victim.getLocation().getBlock().setType(Material.FIRE);
                    }
                }.runTaskTimer(this, 0L, 20L);
            }, long_time);



        } else if (causeOfDeath.equals("freezing") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                ClearSurvivability();
                victim.setFreezeTicks(2000000000);
            },long_time);


        } else if (causeOfDeath.equals("starvation") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }
                        ClearSurvivability();
                        victim.addPotionEffect(starve);
                    }
                }.runTaskTimer(this, 0L, 20L);
            }, long_time);


        } else if (causeOfDeath.equals("poisoning") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }
                        ClearSurvivability();
                        victim.addPotionEffect(poison);
                        victim.addPotionEffect(wither);
                    }
                }.runTaskTimer(this, 0L, 20L);
            }, long_time);

        } else if (causeOfDeath.equals("drowning") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Location below_player1 = victim.getLocation().subtract(0, 1, 0);
            Location below_player2 = victim.getLocation().subtract(0, 2, 0);
            Location below_player3 = victim.getLocation().subtract(0, 3, 0);
            Location above_player2 = victim.getLocation();
            Bukkit.getScheduler().runTaskLater(this, () -> {
                victim.setWalkSpeed(0);
                below_player1.getBlock().setType(Material.AIR);
                below_player2.getBlock().setType(Material.AIR);
                below_player3.getBlock().setType(Material.AIR);
                victim.teleport(below_player3);
                ClearSurvivability();
                victim.addPotionEffect(darkness);
                victim.addPotionEffect(mining_fatigue);
                above_player2.getBlock().setType(Material.GRASS_BLOCK);
                below_player1.getBlock().setType(Material.WATER);
                victim.setWalkSpeed(0.2f);
            }, long_time);
        } else if (causeOfDeath.equals("accidental death") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }
                        ClearSurvivability();
                        victim.setHealth(1);
                    }
                }.runTaskTimer(this, 0L, 40L);
            }, long_time);


        } else if (causeOfDeath.equals("explosion") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                ClearSurvivability();
                victim.getWorld().createExplosion(victim.getLocation(), 10.0f);
            }, long_time);




        } else if (causeOfDeath.equals("big explosion") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                ClearSurvivability();
                victim.getWorld().createExplosion(victim.getLocation(), 50.0f);
            }, long_time);



        } else if (causeOfDeath.equals("very big explosion") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                ClearSurvivability();
                victim.getWorld().createExplosion(victim.getLocation(), 100.0f);
            }, long_time);



        }
        else if (causeOfDeath.equals("impalement") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())&&e.getPlayer().getInventory().contains(ID)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }

                        Location above = victim.getLocation().add(0, 4, 0);
                        ClearSurvivability();
                        FallingBlock dripstone = above.getBlock().getWorld().spawnFallingBlock(above, Material.POINTED_DRIPSTONE.createBlockData());
                        dripstone.canHurtEntities();
                        dripstone.setDamagePerBlock(100);


                    }
                }.runTaskTimer(this, 0L, 20L);
            }, long_time);



        }
        else if (causeOfDeath.equals("fall") && !victim.isDead()&&ID.getItemMeta().getLore().contains(victim.getName())&&e.getPlayer().getInventory().contains(ID)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                ClearSurvivability();
                victim.teleport(victim.getLocation().add(0,300,0));
            }, long_time);



        }

        else if (causeOfDeath.equals("compression") && !victim.isDead()&&e.getPlayer().getInventory().contains(ID)&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }

                        Location above = victim.getLocation().add(0, 4, 0);
                        ClearSurvivability();
                        FallingBlock anvil = above.getBlock().getWorld().spawnFallingBlock(above, Material.ANVIL.createBlockData());
                        anvil.canHurtEntities();
                        anvil.setDamagePerBlock(100);


                    }
                }.runTaskTimer(this, 0L, 20L);
            }, long_time);


        }
        else if (causeOfDeath.equals("disease") && !victim.isDead()&&e.getPlayer().getInventory().contains(ID)&&ID.getItemMeta().getLore().contains(victim.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isDead()) {
                            cancel();
                        }
                        ClearSurvivability();
                        victim.addPotionEffect(wither);



                    }
                }.runTaskTimer(this, 0L, 20L);
            }, long_time);
        }
    }
    @EventHandler
    public void StealID(PlayerToggleSneakEvent e){
        if(e.getPlayer().getInventory().contains(ItemManager.DeathDiary)){
            if(!e.getPlayer().getInventory().contains(ID)){
                if(!id_lore.contains(e.getPlayer().getName())){
                    id_lore.add(e.getPlayer().getName());
                }
                ID_meta.setLore(id_lore);
                ID.setItemMeta(ID_meta);
                e.getPlayer().getInventory().addItem(ID);
            }
            new BukkitRunnable(){
                @Override
                public void run(){
                    if(e.getPlayer().isSneaking()){
                        for(Entity entity: e.getPlayer().getWorld().getNearbyEntities(e.getPlayer().getLocation(), 2f,2f,2f)){
                            if(entity instanceof Player player&&!entity.equals(e.getPlayer())){
                                if(bar!=100){
                                    bar+=20;
                                }
                                else{
                                    e.getPlayer().getInventory().removeItem(ID);
                                    if(!id_lore.contains(player.getName())){
                                        id_lore.add(player.getName());
                                    }
                                    ID_meta.setLore(id_lore);
                                    ID.setItemMeta(ID_meta);
                                    e.getPlayer().getInventory().addItem(ID);
                                    e.getPlayer().sendMessage("§a ID Acquired");
                                    e.getPlayer().playSound(e.getPlayer(),Sound.ENTITY_PLAYER_LEVELUP,1.0f,1.0f);
                                    bar=0;
                                    cancel();
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(this,0,20);
        }

    }


}