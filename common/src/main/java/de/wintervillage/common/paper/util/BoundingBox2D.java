package de.wintervillage.common.paper.util;

public class BoundingBox2D {

    private double minX, minZ, maxX, maxZ;

    public BoundingBox2D() {
    }

    public BoundingBox2D(double minX, double minZ, double maxX, double maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    public double getMinX() {
        return this.minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinZ() {
        return this.minZ;
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMaxZ() {
        return this.maxZ;
    }

    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    public boolean contains(double x, double z) {
        return x >= this.minX && x <= this.maxX
                && z >= this.minZ && z <= this.maxZ;
    }

    public boolean intersects(BoundingBox2D other) {
        return this.maxX >= other.minX && this.minX <= other.maxX && this.maxZ >= other.minZ && this.minZ <= other.maxZ;
    }

    public double getArea() {
        return (this.maxX - this.minX) * (this.maxZ - this.minZ);
    }

    public double getCenterX() {
        return (this.minX + this.maxX) / 2;
    }

    public double getCenterZ() {
        return (this.minZ + this.maxZ) / 2;
    }

    public double getWidthX() {
        return Math.abs(this.maxX - this.minX);
    }

    public double getWidthZ() {
        return Math.abs(this.maxZ - this.minZ);
    }

    @Override
    public String toString() {
        return "BoundingBox2D{" +
                "minX=" + minX +
                ", minZ=" + minZ +
                ", maxX=" + maxX +
                ", maxZ=" + maxZ +
                '}';
    }
}
