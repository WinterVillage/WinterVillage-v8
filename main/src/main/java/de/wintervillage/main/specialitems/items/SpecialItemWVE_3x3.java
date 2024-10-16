package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SpecialItemWVE_3x3 extends SpecialItem {

    private ArrayList<Player> btWait = new ArrayList<>();

    public SpecialItemWVE_3x3(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("WVE: 3x3"), Material.ENCHANTED_BOOK, 1, true);
        this.setItem(item);
        this.setNameStr("wve_3x3");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;

        Player player = event.getPlayer();
        if(EnchantmentUtils.hasWVEnchantment(player.getInventory().getItemInMainHand(), "3x3")) {

            if(!btWait.contains(player)) {
                breakBigTool(player, event.getBlock(), 1);
            }

        }
    }

    public void breakBigTool(Player p, Block b, int radius) {
        float facingYaw = p.getLocation().getYaw()+180;
        float facingPitch = p.getLocation().getPitch();

        if(!btWait.contains(p))
            btWait.add(p);

        for(double a = 0; a < 2*Math.PI; a+=Math.PI/4) {
            double diffX = 0, diffY = 0, diffZ=0;

            if(facingPitch>=-45 && facingPitch<=45) {
                if((facingYaw>45 && facingYaw<135) || (facingYaw>225 && facingYaw<305)) {
                    diffZ = Math.round(Math.cos(a)*radius);
                    diffY = Math.round(Math.sin(a)*radius);
                } else if((facingYaw<=45 || facingYaw>=305) || (facingYaw>135 && facingYaw<225)) {
                    diffX = Math.round(Math.cos(a)*radius);
                    diffY = Math.round(Math.sin(a)*radius);
                }
            } else {
                diffX = Math.round(Math.cos(a)*radius);
                diffZ = Math.round(Math.sin(a)*radius);
            }

            Location locBlock = new Location(b.getWorld(), b.getX()+diffX, b.getY()+diffY, b.getZ()+diffZ);

            if(!b.getWorld().getBlockAt(locBlock).isLiquid() && b.getWorld().getBlockAt(locBlock).getType()!=Material.BEDROCK) {
                p.breakBlock(b.getWorld().getBlockAt(locBlock));
            }
        }

        btWait.remove(p);
    }

}
