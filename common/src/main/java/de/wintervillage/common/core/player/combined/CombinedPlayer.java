package de.wintervillage.common.core.player.combined;

import de.wintervillage.common.core.player.WinterVillagePlayer;
import net.luckperms.api.model.user.User;

public record CombinedPlayer(User user, WinterVillagePlayer winterVillagePlayer) {

}
