package core;

/**
 * Provides simple, deterministic pseudo-random noise functions useful for
 * procedural model variation (e.g. bumps, irregular sizing, color variation).
 *
 * <h3>Implementation</h3>
 * Uses a value-noise approach based on integer hashing so there are no external
 * dependencies and results are reproducible given the same inputs.
 */
public final class Noise {

    private Noise() {}

    // -----------------------------------------------------------------------
    // 1-D noise
    // -----------------------------------------------------------------------

    /**
     * Returns a smooth noise value in [0.0, 1.0] for a 1-D input.
     *
     * @param x input coordinate
     * @return noise value in [0.0, 1.0]
     */
    public static float noise1D(float x) {
        int   xi  = fastFloor(x);
        float xf  = x - xi;
        float u   = fade(xf);

        float a = valueAt(xi);
        float b = valueAt(xi + 1);

        return lerp(a, b, u);
    }

    // -----------------------------------------------------------------------
    // 2-D noise
    // -----------------------------------------------------------------------

    /**
     * Returns a smooth noise value in [0.0, 1.0] for a 2-D input.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return noise value in [0.0, 1.0]
     */
    public static float noise2D(float x, float y) {
        int xi = fastFloor(x);
        int yi = fastFloor(y);
        float xf = x - xi;
        float yf = y - yi;
        float u = fade(xf);
        float v = fade(yf);

        float aa = valueAt(xi,     yi);
        float ba = valueAt(xi + 1, yi);
        float ab = valueAt(xi,     yi + 1);
        float bb = valueAt(xi + 1, yi + 1);

        return lerp(lerp(aa, ba, u), lerp(ab, bb, u), v);
    }

    // -----------------------------------------------------------------------
    // 3-D noise
    // -----------------------------------------------------------------------

    /**
     * Returns a smooth noise value in [0.0, 1.0] for a 3-D input.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return noise value in [0.0, 1.0]
     */
    public static float noise3D(float x, float y, float z) {
        int xi = fastFloor(x);
        int yi = fastFloor(y);
        int zi = fastFloor(z);
        float xf = x - xi;
        float yf = y - yi;
        float zf = z - zi;
        float u = fade(xf);
        float v = fade(yf);
        float w = fade(zf);

        float aaa = valueAt(xi,     yi,     zi);
        float baa = valueAt(xi + 1, yi,     zi);
        float aba = valueAt(xi,     yi + 1, zi);
        float bba = valueAt(xi + 1, yi + 1, zi);
        float aab = valueAt(xi,     yi,     zi + 1);
        float bab = valueAt(xi + 1, yi,     zi + 1);
        float abb = valueAt(xi,     yi + 1, zi + 1);
        float bbb = valueAt(xi + 1, yi + 1, zi + 1);

        float x1 = lerp(aaa, baa, u);
        float x2 = lerp(aba, bba, u);
        float x3 = lerp(aab, bab, u);
        float x4 = lerp(abb, bbb, u);

        return lerp(lerp(x1, x2, v), lerp(x3, x4, v), w);
    }

    // -----------------------------------------------------------------------
    // Fractal / octave wrapper
    // -----------------------------------------------------------------------

    /**
     * Returns a fractal (multi-octave) noise value by summing several layers
     * of {@link #noise2D} at different frequencies and amplitudes.
     *
     * @param x        X coordinate
     * @param y        Y coordinate
     * @param octaves  number of octaves (more = more detail)
     * @param lacunarity frequency multiplier per octave (typically 2.0)
     * @param gain       amplitude multiplier per octave (typically 0.5)
     * @return noise value, approximately in [0.0, 1.0]
     */
    public static float fractal2D(float x, float y, int octaves, float lacunarity, float gain) {
        float value     = 0f;
        float amplitude = 0.5f;
        float frequency = 1f;
        for (int i = 0; i < octaves; i++) {
            value     += noise2D(x * frequency, y * frequency) * amplitude;
            frequency *= lacunarity;
            amplitude *= gain;
        }
        return Math.max(0f, Math.min(1f, value));
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /** Smooth step (Ken Perlin's fade curve: 6t^5 - 15t^4 + 10t^3). */
    private static float fade(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static int fastFloor(float f) {
        int i = (int) f;
        return (f < i) ? i - 1 : i;
    }

    /** Hashes a single integer to a value in [0.0, 1.0]. */
    private static float valueAt(int x) {
        return (hash(x) & 0xFFFF) / 65535f;
    }

    /** Hashes two integers to a value in [0.0, 1.0]. */
    private static float valueAt(int x, int y) {
        return (hash(x ^ (y * 31)) & 0xFFFF) / 65535f;
    }

    /** Hashes three integers to a value in [0.0, 1.0]. */
    private static float valueAt(int x, int y, int z) {
        return (hash(x ^ (y * 31) ^ (z * 1009)) & 0xFFFF) / 65535f;
    }

    /** Simple integer hash (Wang hash variant). */
    private static int hash(int n) {
        n = (n ^ 61) ^ (n >>> 16);
        n = n + (n << 3);
        n = n ^ (n >>> 4);
        n = n * 0x27d4eb2d;
        n = n ^ (n >>> 15);
        return n;
    }
}
