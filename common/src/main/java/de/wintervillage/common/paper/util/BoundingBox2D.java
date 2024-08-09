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

    public boolean contains(int x, int z) {
        return x >= Math.min(this.minX, this.maxX) && x <= Math.max(this.minX, this.maxX)
                && z >= Math.min(this.minZ, this.maxZ) && z <= Math.max(this.minZ, this.maxZ);
    }

    public boolean intersects(BoundingBox2D other) {
        return Math.max(this.minX, this.maxX) >= Math.min(other.minX, other.maxX)
                && Math.min(this.minX, this.maxX) <= Math.max(other.minX, other.maxX)
                && Math.max(this.minZ, this.maxZ) >= Math.min(other.minZ, other.maxZ)
                && Math.min(this.minZ, this.maxZ) <= Math.max(other.minZ, other.maxZ);
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
        return "BoundingBox2D{" + "minX=" + this.minX + ", minZ=" + this.minZ + ", maxX=" + this.maxX + ", maxZ=" + this.maxZ + '}';
    }
}
