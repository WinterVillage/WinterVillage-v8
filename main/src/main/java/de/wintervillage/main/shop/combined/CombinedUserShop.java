package de.wintervillage.main.shop.combined;

import de.wintervillage.main.shop.Shop;
import net.luckperms.api.model.user.User;

public record CombinedUserShop(User owner, Shop shop) {
}
