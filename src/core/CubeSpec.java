package core;

/**
 * Defines a single cube within a model part.
 *
 * <p>Coordinates follow Minecraft's convention:
 * <ul>
 *   <li>Origin is relative to the parent part's pivot.</li>
 *   <li>Y-axis points downward.</li>
 *   <li>UV origin is the top-left corner of the cube's texture region.</li>
 * </ul>
 */
public final class CubeSpec {

    private final float x;
    private final float y;
    private final float z;
    private final float sizeX;
    private final float sizeY;
    private final float sizeZ;
    private final int u;
    private final int v;
    private final boolean mirror;

    /**
     * @param x      X position of the cube (relative to pivot)
     * @param y      Y position of the cube (relative to pivot)
     * @param z      Z position of the cube (relative to pivot)
     * @param sizeX  Width  (X axis)
     * @param sizeY  Height (Y axis)
     * @param sizeZ  Depth  (Z axis)
     * @param u      Texture U offset
     * @param v      Texture V offset
     * @param mirror Whether to mirror the UV mapping (for mirrored limbs)
     */
    public CubeSpec(float x, float y, float z,
                    float sizeX, float sizeY, float sizeZ,
                    int u, int v,
                    boolean mirror) {
        this.x      = x;
        this.y      = y;
        this.z      = z;
        this.sizeX  = sizeX;
        this.sizeY  = sizeY;
        this.sizeZ  = sizeZ;
        this.u      = u;
        this.v      = v;
        this.mirror = mirror;
    }

    /** Convenience constructor without mirroring (mirror = false). */
    public CubeSpec(float x, float y, float z,
                    float sizeX, float sizeY, float sizeZ,
                    int u, int v) {
        this(x, y, z, sizeX, sizeY, sizeZ, u, v, false);
    }

    public float getX()     { return x; }
    public float getY()     { return y; }
    public float getZ()     { return z; }
    public float getSizeX() { return sizeX; }
    public float getSizeY() { return sizeY; }
    public float getSizeZ() { return sizeZ; }
    public int   getU()     { return u; }
    public int   getV()     { return v; }
    public boolean isMirror() { return mirror; }

    /**
     * Returns a mirrored copy of this cube (mirrored = true, X negated and width negated
     * so the cube sits on the opposite side of the pivot).
     */
    public CubeSpec mirrored() {
        return new CubeSpec(-x - sizeX, y, z, sizeX, sizeY, sizeZ, u, v, true);
    }

    @Override
    public String toString() {
        return String.format(
            "CubeSpec(pos=[%.1f,%.1f,%.1f], size=[%.1f,%.1f,%.1f], uv=[%d,%d], mirror=%b)",
            x, y, z, sizeX, sizeY, sizeZ, u, v, mirror);
    }
}
