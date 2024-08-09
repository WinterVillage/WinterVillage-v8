package de.wintervillage.main.plot;

import de.wintervillage.common.paper.util.BoundingBox2D;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface Plot {

    /**
     * Unique identifier of the plot
     * @return {@link UUID} uniqueId
     */
    UUID uniqueId();

    /**
     * Name of the plot
     * @return {@link String} name
     */
    String name();

    /**
     * Date of creation
     * @return {@link Date} creation
     */
    Date created();

    /**
     * Owner of the plot
     * @return {@link UUID} owner
     */
    UUID owner();

    /**
     * Applies the owner to the plot
     * @param uuid {@link UUID} owner
     */
    void owner(UUID uuid);

    /**
     * Bounding box of the plot
     * @return {@link BoundingBox2D} boundingBox
     */
    BoundingBox2D boundingBox();

    /**
     * Applies the bounding box to the plot
     * @param boundingBox {@link BoundingBox2D} boundingBox
     */
    void boundingBox(BoundingBox2D boundingBox);

    /**
     * Members of the plot
     * @return {@link List} of {@link UUID} members
     */
    List<UUID> members();

    /**
     * Adds a member to the plot
     * @param uuid {@link UUID} member
     */
    void addMember(UUID uuid);
}
