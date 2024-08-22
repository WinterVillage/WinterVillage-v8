package de.wintervillage.common.paper.util;

import org.bukkit.Location;

/**
 * Represents a 2D bounding box defined by minimum and maximum X and Z coordinates.
 */
public class BoundingBox2D {

    private double minX, minZ, maxX, maxZ;

    /**
     * Default constructor initializes the bounding box with undefined coordinates using
     * {@link Double#NaN} to indicate that the bounding box is not yet defined.
     */
    public BoundingBox2D() {
        this.minX = Double.NaN;
        this.minZ = Double.NaN;
        this.maxX = Double.NaN;
        this.maxZ = Double.NaN;
    }

    /**
     * Constructs a bounding box with specified minimum and maximum coordinates
     *
     * @param minX the minimum X coordinate
     * @param minZ the minimum Z coordinate
     * @param maxX the maximum X coordinate
     * @param maxZ the maximum Z coordinate
     */
    public BoundingBox2D(
            double minX,
            double minZ,
            double maxX,
            double maxZ
    ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    /**
     * Gets the minimum X coordinate of the bounding box.
     *
     * @return the minimum X coordinate
     */
    public double getMinX() {
        return this.minX;
    }

    /**
     * Sets the minimum X coordinate of the bounding box.
     *
     * @param minX the minimum X coordinate
     */
    public void setMinX(double minX) {
        this.minX = minX;
    }

    /**
     * Gets the minimum Z coordinate of the bounding box.
     *
     * @return the minimum Z coordinate
     */
    public double getMinZ() {
        return this.minZ;
    }

    /**
     * Sets the minimum Z coordinate of the bounding box.
     *
     * @param minZ the minimum Z coordinate
     */
    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    /**
     * Gets the maximum X coordinate of the bounding box.
     *
     * @return the maximum X coordinate
     */
    public double getMaxX() {
        return this.maxX;
    }

    /**
     * Sets the maximum X coordinate of the bounding box.
     *
     * @param maxX the maximum X coordinate
     */
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    /**
     * Gets the maximum Z coordinate of the bounding box.
     *
     * @return the maximum Z coordinate
     */
    public double getMaxZ() {
        return this.maxZ;
    }

    /**
     * Sets the maximum Z coordinate of the bounding box.
     *
     * @param maxZ the maximum Z coordinate
     */
    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    /**
     * Checks if the bounding box is defined.
     *
     * @return true if the bounding box is defined, false otherwise
     */
    public boolean isDefined() {
        return !Double.isNaN(this.minX) && !Double.isNaN(this.minZ) && !Double.isNaN(this.maxX) && !Double.isNaN(this.maxZ);
    }

    /**
     * Checks if the bounding box contains the given point.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return true if the bounding box contains the point, false otherwise
     */
    public boolean contains(int x, int z) {
        return x >= Math.min(this.minX, this.maxX) && x <= Math.max(this.minX, this.maxX)
                && z >= Math.min(this.minZ, this.maxZ) && z <= Math.max(this.minZ, this.maxZ);
    }

    /**
     * Checks if the bounding box contains the given location.
     *
     * @param location {@link Location} to check
     * @return true if the bounding box contains the location, false otherwise
     */
    public boolean contains(Location location) {
        return this.contains(location.getBlockX(), location.getBlockZ());
    }

    /**
     * Checks if the bounding box contains the given bounding box.
     *
     * @param other {@link BoundingBox2D} to check
     * @return true if the bounding box contains the other bounding box, false otherwise
     */
    public boolean intersects(BoundingBox2D other) {
        return Math.max(this.minX, this.maxX) >= Math.min(other.minX, other.maxX)
                && Math.min(this.minX, this.maxX) <= Math.max(other.minX, other.maxX)
                && Math.max(this.minZ, this.maxZ) >= Math.min(other.minZ, other.maxZ)
                && Math.min(this.minZ, this.maxZ) <= Math.max(other.minZ, other.maxZ);
    }

    /**
     * Calculates and returns the area of the bounding box.
     *
     * @return the area of the bounding box
     */
    public double getArea() {
        if (!this.isDefined()) return 0;
        return (this.maxX - this.minX) * (this.maxZ - this.minZ);
    }

    /**
     * Calculates and returns the X coordinate of the bounding box's center.
     *
     * @return the center X coordinate of the bounding box
     */
    public double getCenterX() {
        return (this.minX + this.maxX) / 2;
    }

    /**
     * Calculates and returns the Z coordinate of the bounding box's center.
     *
     * @return the center Z coordinate of the bounding box
     */
    public double getCenterZ() {
        return (this.minZ + this.maxZ) / 2;
    }

    /**
     * Calculates and returns the width of the bounding box along the X axis.
     *
     * @return the width of the bounding box along the X axis
     */
    public double getWidthX() {
        if (!this.isDefined()) return 0;
        return Math.abs(this.maxX - this.minX);
    }

    /**
     * Calculates and returns the width of the bounding box along the Z axis.
     *
     * @return the width of the bounding box along the Z axis
     */
    public double getWidthZ() {
        if (!this.isDefined()) return 0;
        return Math.abs(this.maxZ - this.minZ);
    }

    @Override
    public String toString() {
        return "BoundingBox2D{" + "minX=" + this.minX + ", minZ=" + this.minZ + ", maxX=" + this.maxX + ", maxZ=" + this.maxZ + '}';
    }
}
