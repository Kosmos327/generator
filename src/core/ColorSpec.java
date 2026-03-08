package core;

/**
 * An immutable RGBA color value used throughout the generator.
 * All channels are stored as floats in the range [0.0, 1.0].
 */
public final class ColorSpec {

    public static final ColorSpec WHITE  = new ColorSpec(1f, 1f, 1f, 1f);
    public static final ColorSpec BLACK  = new ColorSpec(0f, 0f, 0f, 1f);
    public static final ColorSpec RED    = new ColorSpec(1f, 0f, 0f, 1f);
    public static final ColorSpec GREEN  = new ColorSpec(0f, 1f, 0f, 1f);
    public static final ColorSpec BLUE   = new ColorSpec(0f, 0f, 1f, 1f);

    private final float r;
    private final float g;
    private final float b;
    private final float a;

    /**
     * Creates a fully opaque color.
     *
     * @param r red channel   [0.0 – 1.0]
     * @param g green channel [0.0 – 1.0]
     * @param b blue channel  [0.0 – 1.0]
     */
    public ColorSpec(float r, float g, float b) {
        this(r, g, b, 1f);
    }

    /**
     * Creates a color with explicit alpha.
     *
     * @param r red   [0.0 – 1.0]
     * @param g green [0.0 – 1.0]
     * @param b blue  [0.0 – 1.0]
     * @param a alpha [0.0 – 1.0]
     */
    public ColorSpec(float r, float g, float b, float a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    /**
     * Creates a ColorSpec from a packed 0xAARRGGBB integer.
     *
     * @param argb packed ARGB color
     * @return new ColorSpec
     */
    public static ColorSpec fromARGB(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >>  8) & 0xFF) / 255f;
        float b = ( argb        & 0xFF) / 255f;
        return new ColorSpec(r, g, b, a);
    }

    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }
    public float getA() { return a; }

    /** Returns this color as a packed 0xAARRGGBB integer. */
    public int toARGB() {
        int ai = Math.round(a * 255f);
        int ri = Math.round(r * 255f);
        int gi = Math.round(g * 255f);
        int bi = Math.round(b * 255f);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Returns a new ColorSpec linearly interpolated between this and {@code other}.
     *
     * @param other target color
     * @param t     interpolation factor [0.0 – 1.0]
     * @return interpolated ColorSpec
     */
    public ColorSpec lerp(ColorSpec other, float t) {
        float tc = clamp(t);
        return new ColorSpec(
            r + (other.r - r) * tc,
            g + (other.g - g) * tc,
            b + (other.b - b) * tc,
            a + (other.a - a) * tc
        );
    }

    @Override
    public String toString() {
        return String.format("ColorSpec(r=%.3f, g=%.3f, b=%.3f, a=%.3f)", r, g, b, a);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
