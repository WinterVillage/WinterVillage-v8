package de.wintervillage.main.util;

public class BoundingBox2D {

    private double minX, minZ, maxX, maxZ;

    public BoundingBox2D() {
        this(0, 0, 0, 0);
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

    public double getMinZ() {
        return this.minZ;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMaxZ() {
        return this.maxZ;
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
