package de.wintervillage.main.plot;

import de.wintervillage.common.paper.util.BoundingBox2D;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class Plot {

    private String name, uniqueId;
    private Date creation;
    private UUID owner;

    private BoundingBox2D boundingBox;

    private List<UUID> members;

    public Plot() { }

    public Plot(String name, String uniqueId, Date creation, UUID owner, BoundingBox2D boundingBox, List<UUID> members) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.creation = creation;
        this.owner = owner;
        this.boundingBox = boundingBox;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public BoundingBox2D getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox2D boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Plot{" +
                "name='" + name + '\'' +
                ", creation=" + creation +
                ", owner=" + owner +
                ", boundingBox=" + boundingBox +
                ", members=" + members +
                '}';
    }
}
