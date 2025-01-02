package DeathNoteBuild.deathNote;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import java.util.ArrayList;
public class ItemManager {
    public static ItemStack DeathDiary;
    public static BookMeta bookMeta;
    public static void CreateDeathDiary(){
        DeathDiary = new ItemStack(Material.WRITABLE_BOOK);
        bookMeta = (BookMeta) DeathDiary.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§4The Worst Murder Weapon In The History Of Mankind.");
        bookMeta.setDisplayName("§8DeathDiary");
        bookMeta.setLore(lore);
        DeathDiary.setItemMeta(bookMeta);



    }

}