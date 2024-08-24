package de.wintervillage.main.plot.combined;

import net.luckperms.api.model.user.User;

import java.util.List;

public record PlotUsers(User owner, List<User> members) {
}
