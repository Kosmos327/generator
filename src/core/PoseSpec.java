package core;

/**
 * Describes the default pose of a model part: its pivot point and initial rotation.
 *
 * <p>Rotations are stored in radians and applied in the order X → Y → Z.
 * The pivot point is the origin around which the part rotates, expressed in
 * model-space units (1 unit = 1/16th of a Minecraft block).</p>
 */
public final class PoseSpec {

    /** A zero-rotation, zero-offset pose (suitable as a default). */
    public static final PoseSpec ZERO = new PoseSpec(0, 0, 0, 0, 0, 0);

    private final float pivotX;
    private final float pivotY;
    private final float pivotZ;
    private final float rotX;
    private final float rotY;
    private final float rotZ;

    /**
     * @param pivotX pivot X in model units
     * @param pivotY pivot Y in model units
     * @param pivotZ pivot Z in model units
     * @param rotX   rotation around X axis (radians)
     * @param rotY   rotation around Y axis (radians)
     * @param rotZ   rotation around Z axis (radians)
     */
    public PoseSpec(float pivotX, float pivotY, float pivotZ,
                    float rotX, float rotY, float rotZ) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.rotX   = rotX;
        this.rotY   = rotY;
        this.rotZ   = rotZ;
    }

    /** Convenience: offset only, no rotation. */
    public PoseSpec(float pivotX, float pivotY, float pivotZ) {
        this(pivotX, pivotY, pivotZ, 0, 0, 0);
    }

    public float getPivotX() { return pivotX; }
    public float getPivotY() { return pivotY; }
    public float getPivotZ() { return pivotZ; }
    public float getRotX()   { return rotX; }
    public float getRotY()   { return rotY; }
    public float getRotZ()   { return rotZ; }

    /** Returns true if all rotations are zero. */
    public boolean hasRotation() {
        return rotX != 0f || rotY != 0f || rotZ != 0f;
    }

    @Override
    public String toString() {
        return String.format(
            "PoseSpec(pivot=[%.1f,%.1f,%.1f], rot=[%.4f,%.4f,%.4f])",
            pivotX, pivotY, pivotZ, rotX, rotY, rotZ);
    }
}
