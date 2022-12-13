package edolce.tavernitemsextended;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {

    public static List<String> getPreLore(){
        //aply Hex code
        List<String> list = TavernItemsExtended.getInstance().getConfig().getStringList("item-info.lore");
        List<String> parsedList = new ArrayList<>();

        for (String s:list){
            s=ColorUtils.translateColorCodes(s);
            parsedList.add(s);
        }

        return parsedList;
    }

    public static List<String> getStandardPostLore(){
        //aply Hex code
        List<String> list = TavernItemsExtended.getInstance().getConfig().getStringList("enchant-storage-style.new-lore");
        List<String> parsedList = new ArrayList<>();

        for (String s:list){
            s=ColorUtils.translateColorCodes(s);
            parsedList.add(s);
        }

        return parsedList;
    }

    public static String getDisplayName(){
        return ColorUtils.translateColorCodes(TavernItemsExtended.getInstance().getConfig().getString("item-info.display-name"));
    }

    public static Material getMaterial(){
        return Material.valueOf(TavernItemsExtended.getInstance().getConfig().getString("item-info.material"));
    }

    public static int getModelData(){
        return TavernItemsExtended.getInstance().getConfig().getInt("item-info.model-data");
    }

    public static List<String> getEnchantLines(){
        return TavernItemsExtended.getInstance().getConfig().getStringList("enchant-storage-style.enchants-list-line");
    }


}
