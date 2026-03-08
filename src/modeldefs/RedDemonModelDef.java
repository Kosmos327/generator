package modeldefs;

import core.CubeSpec;
import core.DetailLevel;
import core.ModelSpec;
import core.PartSpec;
import core.PoseSpec;

/**
 * Model definition for the Red Demon boss, inspired by the anime "Seven Deadly Sins".
 *
 * <p>The demon stands approximately three player-heights tall (~98 model units) with
 * a wide, muscular silhouette: a broad torso, thick arms and legs, large curved horns,
 * and — deliberately — no tail.  The head is kept smaller than the torso to reinforce
 * the creature's massive, brutish frame.
 *
 * <h3>Cube budget (approximate)</h3>
 * <ul>
 *   <li>{@link DetailLevel#HIGH}   – ~1508 cubes (full detail)</li>
 *   <li>{@link DetailLevel#MEDIUM} – ~214  cubes (balanced)</li>
 *   <li>{@link DetailLevel#LOW}    – ~58   cubes  (coarse silhouette)</li>
 * </ul>
 *
 * <h3>Part hierarchy</h3>
 * <pre>
 * head       (root) — smaller head for brute silhouette
 *   left_horn        — 8-segment upward arc, curves outward
 *   right_horn       — mirror of left_horn
 * torso      (root) — very wide chest with shoulder pads and neck stub
 * left_arm   (root)
 *   left_forearm
 *     left_hand
 * right_arm  (root)
 *   right_forearm
 *     right_hand
 * left_leg   (root)
 *   left_lower_leg
 *     left_foot
 * right_leg  (root)
 *   right_lower_leg
 *     right_foot
 * </pre>
 *
 * <h3>Adding a new model definition</h3>
 * <ol>
 *   <li>Create a new file in {@code src/modeldefs/}.</li>
 *   <li>Declare {@code package modeldefs;}.</li>
 *   <li>Implement {@code public static ModelSpec build(DetailLevel detail)}.</li>
 *   <li>Register it in {@code ForgeModelGenerator.java}.</li>
 * </ol>
 */
public final class RedDemonModelDef {

    private RedDemonModelDef() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Builds and returns a {@link ModelSpec} for the Red Demon boss entity.
     *
     * <p>At {@link DetailLevel#LOW} only a coarse outline is produced.
     * At {@link DetailLevel#HIGH} the model reaches its full cube budget of ~1508 cubes.
     *
     * @param detail requested detail level
     * @return fully constructed {@link ModelSpec}
     */
    public static ModelSpec build(DetailLevel detail) {

        // m drives the subdivision density of every body part.
        //   LOW    = 1  →  ~58  cubes  (coarse silhouette)
        //   MEDIUM = 2  →  ~214 cubes  (balanced detail)
        //   HIGH   = 4  → ~1508 cubes  (full cube budget, 1500–3000 target)
        final int m = (detail == DetailLevel.HIGH)   ? 4
                    : (detail == DetailLevel.MEDIUM) ? 2 : 1;

        // Build bottom-up so children exist before they are passed to parents.

        // Hands
        PartSpec leftHand  = buildHand("left_hand",  m);
        PartSpec rightHand = buildHand("right_hand", m);

        // Forearms — hold hands as children
        PartSpec leftForearm  = buildForearm("left_forearm",  leftHand,  m);
        PartSpec rightForearm = buildForearm("right_forearm", rightHand, m);

        // Upper arms — root parts, pivot at shoulder position in model space
        PartSpec leftArm  = buildArm("left_arm",   20f, leftForearm,  m);
        PartSpec rightArm = buildArm("right_arm", -20f, rightForearm, m);

        // Feet
        PartSpec leftFoot  = buildFoot("left_foot",  m);
        PartSpec rightFoot = buildFoot("right_foot", m);

        // Lower legs — hold feet as children
        PartSpec leftLowerLeg  = buildLowerLeg("left_lower_leg",  leftFoot,  m);
        PartSpec rightLowerLeg = buildLowerLeg("right_lower_leg", rightFoot, m);

        // Upper legs — root parts, pivot at hip position in model space
        PartSpec leftLeg  = buildLeg("left_leg",   8f, leftLowerLeg,  m);
        PartSpec rightLeg = buildLeg("right_leg", -8f, rightLowerLeg, m);

        // Head (with large curved horns as children)
        PartSpec head  = buildHead(m);

        // Torso (includes neck stub, chest, mid-section, abdomen, shoulder pads)
        PartSpec torso = buildTorso(m);

        return new ModelSpec.Builder("RedDemon")
                .textureWidth(512)
                .textureHeight(256)
                .detailLevel(detail)
                .part(head)
                .part(torso)
                .part(leftArm)
                .part(rightArm)
                .part(leftLeg)
                .part(rightLeg)
                .build();
    }

    // -----------------------------------------------------------------------
    // Private part builders
    // -----------------------------------------------------------------------

    /**
     * Head: sits above the torso pivot (negative Y).  Intentionally smaller than
     * the body to create a massive-brute silhouette.  Two curved horns are added
     * as child parts.
     *
     * <pre>
     *   pivot at (0, 0, 0) — neck-top / model origin
     *   main volume  : −8..+8  in X (16 wide)
     *                  −14..0  in Y (14 tall, above pivot)
     *                  −7..+7  in Z (14 deep)
     *   brow ridge   : front-top overhang (16 × 2 × 4)
     *   jaw          : lower-chin protrusion (12 × 3 × 5)
     * </pre>
     */
    private static PartSpec buildHead(int m) {
        int s = Math.max(1, m / 2);

        PartSpec leftHorn  = buildHorn("left_horn",  true,  m);
        PartSpec rightHorn = buildHorn("right_horn", false, m);

        PartSpec.Builder b = new PartSpec.Builder("head")
                .pose(new PoseSpec(0f, 0f, 0f));

        // Main head volume (16 × 14 × 14)
        addGrid(b, -8f, -14f, -7f, 16f, 14f, 14f,  m,     m,     m,      0,   0);

        // Brow ridge — front-top overhang (16 × 2 × 4)
        addGrid(b, -8f, -16f, -7f, 16f,  2f,  4f,  m,     s,     s,     64,   0);

        // Jaw / lower-chin protrusion (12 × 3 × 5)
        addGrid(b, -6f,  -2f, -7f, 12f,  3f,  5f,  m,     s,     m,     64,  32);

        return b.child(leftHorn).child(rightHorn).build();
    }

    /**
     * A large curved horn built from 8 cube segments arranged in an upward arc.
     * The left horn curves up and to the model's left (+X); the right horn
     * is its mirror image.
     *
     * @param name part name
     * @param left {@code true} for the left horn, {@code false} for the right
     * @param m    grid multiplier (unused for horns; segments are always the same)
     */
    private static PartSpec buildHorn(String name, boolean left, int m) {
        // Segment data defined for the LEFT (+X) side: { x, y, z, sizeX, sizeY, sizeZ }.
        // All positions are relative to the head pivot.
        // For the right horn each x is mirrored: xRight = −x − sizeX.
        float[][] segs = {
            {  4f, -18f, -2f,  4f, 5f, 4f },   // base — emerges from the top of the head
            {  6f, -23f, -2f,  4f, 5f, 3f },   // first outward sweep
            {  9f, -28f, -2f,  4f, 5f, 3f },   // continuing the arc
            { 12f, -33f, -1f,  3f, 5f, 3f },   // mid-arc
            { 14f, -38f, -1f,  3f, 4f, 2f },   // curving back inward slightly
            { 15f, -42f, -1f,  3f, 4f, 2f },   // nearing the tip
            { 15f, -46f,  0f,  2f, 3f, 2f },   // upper tip
            { 14f, -49f,  0f,  2f, 3f, 2f },   // pointed tip
        };

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(PoseSpec.ZERO);

        for (float[] seg : segs) {
            float x = left ? seg[0] : (-seg[0] - seg[3]);
            b.cube(new CubeSpec(x, seg[1], seg[2],
                                seg[3], seg[4], seg[5],
                                128, 0));
        }

        return b.build();
    }

    /**
     * Torso: very wide and bulky; the visual centerpiece of the model.
     * Includes a neck stub above the pivot, a wide chest, a mid section,
     * an abdomen, and — as inline geometry — shoulder pads on both sides.
     * No tail geometry is added.
     *
     * <pre>
     *   pivot at (0, 0, 0) — same reference as head
     *   neck stub    : −4..+4   Y: −6..0   (fills gap between head and chest)
     *   chest        : −16..+16  Y: 0..10   (32 wide — widest section)
     *   mid torso    : −15..+15  Y: 10..22
     *   abdomen      : −14..+14  Y: 22..30  (slightly narrower toward the waist)
     *   shoulder pads: X: 16..28 / −28..−16  Y: 0..8  (protrude from the chest)
     * </pre>
     */
    private static PartSpec buildTorso(int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder("torso")
                .pose(new PoseSpec(0f, 0f, 0f));

        // Neck stub (8 × 6 × 8) — above pivot, bridging head to chest
        addGrid(b,  -4f, -6f,  -4f,  8f,  6f,  8f,  s,     m,     s,      0,  64);

        // Chest — widest section (32 × 10 × 16)
        addGrid(b, -16f,  0f,  -8f, 32f, 10f, 16f,  m + 1, m,     m,      0,  80);

        // Mid torso (30 × 12 × 14)
        addGrid(b, -15f, 10f,  -7f, 30f, 12f, 14f,  m,     m,     m,      0,  96);

        // Abdomen — narrowing toward the waist (28 × 8 × 12)
        addGrid(b, -14f, 22f,  -6f, 28f,  8f, 12f,  m,     s,     m,      0, 112);

        // Left shoulder pad (12 × 8 × 14) — protrudes from the left of the chest
        addGrid(b,  16f,  0f,  -7f, 12f,  8f, 14f,  m,     s + 1, m,    192,   0);

        // Right shoulder pad (mirror of left)
        addGrid(b, -28f,  0f,  -7f, 12f,  8f, 14f,  m,     s + 1, m,    192,   0);

        return b.build();
    }

    /**
     * Upper arm — very thick for the muscular silhouette.
     *
     * <pre>
     *   pivot : (pivotX, 4, 0) in model space — shoulder joint
     *   volume: −6..+6  Y: 0..30  Z: −6..+6  (12 × 30 × 12)
     *   elbow spike: backward protrusion at Y ≈ 20
     * </pre>
     *
     * @param name    part name
     * @param pivotX  shoulder X in model space (+20 = left arm, −20 = right arm)
     * @param forearm already-built forearm child
     * @param m       grid multiplier
     */
    private static PartSpec buildArm(String name, float pivotX,
                                     PartSpec forearm, int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(pivotX, 4f, 0f));

        // Main upper-arm volume (12 × 30 × 12)
        addGrid(b, -6f,  0f,  -6f, 12f, 30f, 12f,  m,     m * 2, m,    256,   0);

        // Elbow-spike protrusion — backward-facing (Z−), at ≈ elbow height
        addGrid(b, -3f, 20f, -10f,  6f,  6f,  4f,  s,     s,     s,    192,  64);

        return b.child(forearm).build();
    }

    /**
     * Forearm — child of an upper arm; pivot is at the elbow joint.
     *
     * <pre>
     *   pivot : (0, 28, 0) relative to arm — elbow joint
     *   volume: −5..+5  Y: 0..22  Z: −5..+5  (10 × 22 × 10)
     * </pre>
     *
     * @param name part name
     * @param hand already-built hand child
     * @param m    grid multiplier
     */
    private static PartSpec buildForearm(String name, PartSpec hand, int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 28f, 0f));

        // Forearm volume (10 × 22 × 10)
        addGrid(b, -5f, 0f, -5f, 10f, 22f, 10f,  m,  m + s,  m,  256,  64);

        return b.child(hand).build();
    }

    /**
     * Hand — child of a forearm; pivot is at the wrist joint.
     *
     * <pre>
     *   pivot : (0, 22, 0) relative to forearm — wrist joint
     *   volume: −5..+5  Y: 0..8  Z: −4..+4  (10 × 8 × 8)  — large clawed hands
     * </pre>
     *
     * @param name part name
     * @param m    grid multiplier
     */
    private static PartSpec buildHand(String name, int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 22f, 0f));

        // Hand volume (10 × 8 × 8)
        addGrid(b, -5f, 0f, -4f, 10f, 8f, 8f,  m,  s,  m,  256, 128);

        return b.build();
    }

    /**
     * Upper leg — very thick for the muscular silhouette.
     *
     * <pre>
     *   pivot : (pivotX, 30, 0) in model space — hip joint
     *   volume: −7..+7  Y: 0..24  Z: −7..+7  (14 × 24 × 14)
     *   knee guard: forward protrusion at Y ≈ 20
     * </pre>
     *
     * @param name     part name
     * @param pivotX   hip X in model space (+8 = left leg, −8 = right leg)
     * @param lowerLeg already-built lower-leg child
     * @param m        grid multiplier
     */
    private static PartSpec buildLeg(String name, float pivotX,
                                     PartSpec lowerLeg, int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(pivotX, 30f, 0f));

        // Main upper-leg volume (14 × 24 × 14)
        addGrid(b, -7f,  0f,  -7f, 14f, 24f, 14f,  m,     m * 2, m,    384,   0);

        // Knee-guard protrusion — forward-facing (Z−), at ≈ knee height
        addGrid(b, -3f, 20f, -10f,  6f,  6f,  4f,  s,     s,     s,    384,  64);

        return b.child(lowerLeg).build();
    }

    /**
     * Lower leg — child of an upper leg; pivot is at the knee joint.
     *
     * <pre>
     *   pivot : (0, 24, 0) relative to upper leg — knee joint
     *   volume: −6..+6  Y: 0..22  Z: −6..+6  (12 × 22 × 12)
     * </pre>
     *
     * @param name part name
     * @param foot already-built foot child
     * @param m    grid multiplier
     */
    private static PartSpec buildLowerLeg(String name, PartSpec foot, int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 24f, 0f));

        // Lower-leg volume (12 × 22 × 12)
        addGrid(b, -6f, 0f, -6f, 12f, 22f, 12f,  m,  m + s,  m,  384,  80);

        return b.child(foot).build();
    }

    /**
     * Foot — child of a lower leg; pivot is at the ankle joint.
     * Extended forward (Z−) for a large, stable demon foot.
     *
     * <pre>
     *   pivot : (0, 22, 0) relative to lower leg — ankle joint
     *   volume: −7..+7  Y: 0..8  Z: −14..+6  (14 × 8 × 20)
     * </pre>
     *
     * @param name part name
     * @param m    grid multiplier
     */
    private static PartSpec buildFoot(String name, int m) {
        int s = Math.max(1, m / 2);

        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 22f, 0f));

        // Foot volume (14 × 8 × 20) — extends forward
        addGrid(b, -7f, 0f, -14f, 14f, 8f, 20f,  m + 1,  s,  m + 1,  384, 128);

        return b.build();
    }

    // -----------------------------------------------------------------------
    // Grid helper
    // -----------------------------------------------------------------------

    /**
     * Adds a 3-D grid of {@code nx × ny × nz} equal-sized cubes that
     * collectively fill the axis-aligned box
     * {@code [x, x+w] × [y, y+h] × [z, z+d]}.
     * All cubes in the grid share the same {@code (u, v)} UV offset.
     *
     * <p>This is the primary mechanism for hitting the cube-budget target:
     * larger {@code nx/ny/nz} values at {@link DetailLevel#HIGH} subdivide
     * each body-part volume into more (smaller) cubes, adding muscle-definition
     * detail without manually listing thousands of individual boxes.
     *
     * @param b  builder to receive the cubes
     * @param x  box-origin X relative to the part pivot
     * @param y  box-origin Y relative to the part pivot
     * @param z  box-origin Z relative to the part pivot
     * @param w  total box width  (X axis, must be &gt; 0)
     * @param h  total box height (Y axis, must be &gt; 0)
     * @param d  total box depth  (Z axis, must be &gt; 0)
     * @param nx number of subdivisions along X (must be ≥ 1)
     * @param ny number of subdivisions along Y (must be ≥ 1)
     * @param nz number of subdivisions along Z (must be ≥ 1)
     * @param u  texture U offset shared by all cubes in this grid
     * @param v  texture V offset shared by all cubes in this grid
     */
    private static void addGrid(PartSpec.Builder b,
                                float x,  float y,  float z,
                                float w,  float h,  float d,
                                int   nx, int   ny, int   nz,
                                int   u,  int   v) {
        float cw = w / nx;
        float ch = h / ny;
        float cd = d / nz;
        for (int ix = 0; ix < nx; ix++) {
            for (int iy = 0; iy < ny; iy++) {
                for (int iz = 0; iz < nz; iz++) {
                    b.cube(new CubeSpec(
                            x + ix * cw,
                            y + iy * ch,
                            z + iz * cd,
                            cw, ch, cd,
                            u, v));
                }
            }
        }
    }
}
