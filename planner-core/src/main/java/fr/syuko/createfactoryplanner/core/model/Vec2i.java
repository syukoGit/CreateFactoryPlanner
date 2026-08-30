package fr.syuko.createfactoryplanner.core.model;

public record Vec2i(int x, int y) {

    public static final Vec2i ORIGIN = new Vec2i(0, 0);

    public Vec2i plus(Vec2i other) {
        return new Vec2i(Math.addExact(x, other.x), Math.addExact(y, other.y));
    }

    public Vec2i minus(Vec2i other) {
        return new Vec2i(Math.subtractExact(x, other.x), Math.subtractExact(y, other.y));
    }
}
