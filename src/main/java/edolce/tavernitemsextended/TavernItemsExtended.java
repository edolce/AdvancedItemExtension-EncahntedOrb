package edolce.tavernitemsextended;

import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class TavernItemsExtended extends JavaPlugin implements Listener, CommandExecutor {

    public static TavernItemsExtended instance;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        instance=this;
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this,this);
        getCommand("enchantOrb").setExecutor(this);
        //Check if Item in config is valid


    }

    @Override
    public void onDisable() {
    }


    //Trigger when Special Item Used on another item
    @EventHandler
    public void onEnchantGrabberGrabEnchants(InventoryClickEvent event){
        ItemStack handItem= event.getCursor();
        ItemStack clickedItem= event.getCurrentItem();
        //Check if inventory is clicked
        if(event.getSlot()==-1) return;

        //check if both item are not null
        if(handItem==null || clickedItem==null) return;

        //check if handitem is the actual item
        if(!isEnchantGrabber(handItem)) return;

        //Check if target item has some enchants
        if(!hasSomeEnchants(clickedItem)) return;

        //Get all enchants
        HashMap<String,Integer> enchants = getAllEnchants(clickedItem);


        event.setCancelled(true);

        //Sostituire ItemCon Special Item After
        //getItemWithNoEnchants(clickedItem);

        if(event.getWhoClicked().getInventory().addItem(getEnchantGrabberWithStoredEnchants(enchants)).size()!=0){
            event.getWhoClicked().sendMessage(getConfig().getString("inventory-full-message"));
            return;
        }
        event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
        getItemWithNoEnchants(clickedItem);

//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                getItemWithNoEnchants(clickedItem);
//                this.cancel();
//            }
//        }.runTaskTimer(getInstance(),3,1);




    }



    //Trigger when Special AFTER Item Used on another item
    @EventHandler
    public void onEnchantGrabberApplyEnchants(InventoryClickEvent event){
        ItemStack handItem= event.getCursor();
        ItemStack clickedItem= event.getCurrentItem();

        //Check if inventory is clicked
        if(event.getSlot()==-1) return;
        if(clickedItem==null) return;
        if(clickedItem.getType()==Material.AIR) return;

        //check if both item are not null
        if(handItem == null) return;

        //check if handitem is the actual item
        if(!isSpecialAfterItem(handItem)) return;

        //check if clickedItem is not the EncahntGrabber item
        if(isEnchantGrabber(clickedItem)) return;

        //check if clickedItem is not the EncahntStored item
        if(isSpecialAfterItem(clickedItem)) return;

        event.setCancelled(true);

        //Get all enchants
        HashMap<String,Integer> enchants = getAllStoredEnchants(handItem);

        //set Encahnts to item
        try{
            event.setCurrentItem(getEnchantedItem(clickedItem,enchants));

            //Check if is more than one
            if(handItem.getAmount()>1){
                handItem.setAmount(handItem.getAmount()-1);
            }else {
                event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
            }
        }catch (IllegalArgumentException e){
            event.getWhoClicked().sendMessage(getConfig().getString("incompatible-message"));
        }




    }


    /*
    * Remove all enchants from the item and get the item back
     */
    private ItemStack getItemWithNoEnchants(ItemStack clickedItem) {

        for (Enchantment enchantment : clickedItem.getEnchantments().keySet()) {
            clickedItem.removeEnchantment(enchantment);
//            System.out.println("Rimosso enchant");
        }

        for (String enchantment : AEAPI.getEnchantmentsOnItem(clickedItem).keySet()) {
            clickedItem.setItemMeta(AEAPI.removeEnchantment(clickedItem,enchantment).getItemMeta());
//            System.out.println("Rimosso enchant");
        }

        return clickedItem;
    }


    private HashMap<String, Integer> getAllStoredEnchants(ItemStack handItem) {
        HashMap<String, Integer> enchants = new HashMap<>();
        PersistentDataContainer container = handItem.getItemMeta().getPersistentDataContainer();

        for(NamespacedKey key:container.getKeys()){
            if(key.getKey().contains("enchant")) enchants.put(key.getKey().split("\\.")[1],container.get(key,PersistentDataType.INTEGER));
        }

        return enchants;
    }

    private ItemStack getEnchantedItem(ItemStack clickedItem, HashMap<String, Integer> enchants) {


        for(Map.Entry<String,Integer> entry:enchants.entrySet()){
            NamespacedKey enchantmentKey = NamespacedKey.minecraft(entry.getKey());
            if(Enchantment.getByKey(enchantmentKey)!=null) {
//                System.out.printf("[%s]-Lv:%s\n",entry.getKey(),entry.getValue());
                clickedItem.addEnchantment(Enchantment.getByKey(enchantmentKey),entry.getValue());
            }else {
//                System.out.printf("[%s]-Lv:%s\n",entry.getKey(),entry.getValue());
                clickedItem = AEAPI.applyEnchant(entry.getKey(),entry.getValue(),clickedItem);
            }
        }

        return clickedItem;
    }

    private boolean isSpecialAfterItem(ItemStack specialItem) {

        //get info from config
        if(specialItem.getItemMeta()==null) return false;
        PersistentDataContainer container= specialItem.getItemMeta().getPersistentDataContainer();
        if(!container.has(new NamespacedKey(this, "detection"),PersistentDataType.INTEGER)) return false;
        return container.get(new NamespacedKey(this, "detection"),PersistentDataType.INTEGER)==1;
    }

    private HashMap<String, Integer> getAllEnchants(ItemStack clickedItem) {
        HashMap<String,Integer> enchants = new HashMap<>();

        for(Map.Entry<Enchantment,Integer> entry:clickedItem.getEnchantments().entrySet()){
            String enchantName = entry.getKey().getKey().getKey();
            enchants.put(enchantName,entry.getValue());
        }

        enchants.putAll(AEAPI.getEnchantmentsOnItem(clickedItem));

        return enchants;
    }


    /*
    * Check if the target item has some enchants inside (vanilla or AE)
     */
    private boolean hasSomeEnchants(ItemStack handItem) {

//        System.out.println("IMMAGAZZINO GLI ENCHANTS");
        for(Map.Entry<Enchantment,Integer> entry:handItem.getEnchantments().entrySet()){
//            System.out.printf("[%s]-Lv:%s\n",entry.getKey().getKey().getKey(),entry.getValue());
        }
        for(Map.Entry<String,Integer> entry:AEAPI.getEnchantmentsOnItem(handItem).entrySet()){
//            System.out.printf("[%s]-Lv:%s\n",entry.getKey(),entry.getValue());
        }

        return handItem.getEnchantments().size()+AEAPI.getEnchantmentsOnItem(handItem).size()!=0;
    }


    /*
    * Check if the item is an item enchant Grabber
     */
    private boolean isEnchantGrabber(ItemStack specialItem){

        //get info from config
        if(specialItem.getItemMeta()==null) return false;
        PersistentDataContainer container= specialItem.getItemMeta().getPersistentDataContainer();
        if(!container.has(new NamespacedKey(this, "detection"),PersistentDataType.INTEGER)) return false;
        return container.get(new NamespacedKey(this, "detection"),PersistentDataType.INTEGER)==0;
    }


    //get Special Item
    private ItemStack getSpecialItem(){
        ItemStack specialItem = new ItemStack(ConfigUtil.getMaterial());

        ItemMeta meta = specialItem.getItemMeta();

        meta.setDisplayName(ConfigUtil.getDisplayName());
        meta.setLore(ConfigUtil.getPreLore());
        meta.setCustomModelData(ConfigUtil.getModelData());

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(this, "detection"), PersistentDataType.INTEGER, 0);
        container.set(new NamespacedKey(this, "uniqueness"), PersistentDataType.INTEGER, random.nextInt(10000));

        specialItem.setItemMeta(meta);

        return specialItem;
    }


    /*
    * Get Enchant Grabber with stored enchants
     */
    private ItemStack getEnchantGrabberWithStoredEnchants(HashMap<String,Integer> enchants){
        ItemStack specialItem = new ItemStack(ConfigUtil.getMaterial());

        ItemMeta meta = specialItem.getItemMeta();

        meta.setDisplayName(ConfigUtil.getDisplayName());

        //BUILD NEW LORE

        List<String> enchantTemplate = ConfigUtil.getEnchantLines();
        List<String> enchantsLore = new ArrayList<>();

        //Create the enchants list
        for(Map.Entry<String,Integer> entry: enchants.entrySet()){
            List<String> miniEnchantLore = new ArrayList<>();
            for (String s: enchantTemplate) {
                s = s.replace("<enchant-name>", entry.getKey());
                s = s.replace("<level>", entry.getValue().toString());
                miniEnchantLore.add(ColorUtils.translateColorCodes(s));
            }

            enchantsLore.addAll(miniEnchantLore);
        }

        List<String> finalLore = ConfigUtil.getStandardPostLore();
        finalLore.addAll(enchantsLore);

        meta.setLore(finalLore);
        meta.setCustomModelData(ConfigUtil.getModelData());

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(this, "detection"), PersistentDataType.INTEGER, 1);

        for(Map.Entry<String,Integer> entry: enchants.entrySet()) {


            container.set(new NamespacedKey(this, "enchant."+entry.getKey()), PersistentDataType.INTEGER, entry.getValue());
        }

        specialItem.setItemMeta(meta);

        return specialItem;
    }

    public static TavernItemsExtended getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return false;
        if(!sender.isOp()) return false;

        if(args.length==1){
            if(Objects.equals(args[0], "reload")){
                this.saveDefaultConfig();
                sender.sendMessage("[NatureRegen]: Config Reloaded");
                return true;
            }
            return false;
        }


        ((Player) sender).getInventory().addItem(getSpecialItem());
        return true;
    }
}
