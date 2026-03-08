package modeldefs;

import core.CubeSpec;
import core.DetailLevel;
import core.ModelSpec;
import core.PartSpec;
import core.PoseSpec;

/**
 * Model definition for the Red Demon boss, inspired by the anime "Seven Deadly Sins".
 *
 * <p>A massive, top-heavy demon with a strong recognisable silhouette: huge
 * shoulders, large curved horns, thick arms that reach mid-thigh, and powerful
 * shorter legs.  Deep crimson skin, glowing yellow eyes, black horns, bone-white
 * teeth and dark-grey claws.
 *
 * <h3>Coordinate system (Minecraft Y-down)</h3>
 * <pre>
 *   Y =  0  : model origin — neck / head-torso junction
 *   Y = -7  : top of head (7 above origin)
 *   Y =  0  : torso starts here
 *   Y = +14 : hip joint (torso ends here)
 *   Y = +21 : knee joint  (hip + 7)
 *   Y = +27 : ankle joint (knee + 6)
 *   Y = +31 : bottom of feet (ankle + 4)  — ground contact
 *   Total height: 7 + 14 + 7 + 6 + 4 = 38 units
 * </pre>
 *
 * <h3>Cube budget (approximate)</h3>
 * <ul>
 *   <li>{@link DetailLevel#LOW}    – ~110  cubes (coarse silhouette)</li>
 *   <li>{@link DetailLevel#MEDIUM} – ~3 800 cubes (full sculpted detail)</li>
 *   <li>{@link DetailLevel#HIGH}   – ~3 800 cubes (same as MEDIUM)</li>
 * </ul>
 *
 * <h3>Part hierarchy</h3>
 * <pre>
 * head       (root) — 7H × 14W × 12D
 *   left_horn        — 12-segment arc, curves outward and backward
 *   right_horn       — mirror of left_horn
 * torso      (root) — neck, chest, abdomen, back, shoulder pads
 * left_arm   (root) — shoulder pivot at (+13, +1, 0)
 *   left_forearm
 *     left_hand
 * right_arm  (root) — shoulder pivot at (−13, +1, 0)
 *   right_forearm
 *     right_hand
 * left_leg   (root) — hip pivot at (+5, +14, 0)
 *   left_lower_leg
 *     left_foot
 * right_leg  (root) — hip pivot at (−5, +14, 0)
 *   right_lower_leg
 *     right_foot
 * </pre>
 */
public final class RedDemonModelDef {

    private RedDemonModelDef() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Builds and returns a {@link ModelSpec} for the Red Demon boss entity.
     *
     * <p>Subdivision multiplier {@code m} drives every grid's density:
     * <ul>
     *   <li>LOW    : m = 1 → coarse silhouette (~110 cubes)</li>
     *   <li>MEDIUM : m = 6 → full sculpted detail (~3 800 cubes)</li>
     *   <li>HIGH   : m = 6 → same as MEDIUM</li>
     * </ul>
     *
     * @param detail requested detail level
     * @return fully constructed {@link ModelSpec}
     */
    public static ModelSpec build(DetailLevel detail) {

        final int m = (detail == DetailLevel.LOW) ? 1 : 6;
        final int s = Math.max(1, m / 2);   // secondary density  (3 at MEDIUM)

        // Build bottom-up so children exist before they are passed to parents.

        PartSpec leftHand    = buildHand("left_hand",   m, s);
        PartSpec rightHand   = buildHand("right_hand",  m, s);

        PartSpec leftForearm  = buildForearm("left_forearm",  leftHand,  m, s);
        PartSpec rightForearm = buildForearm("right_forearm", rightHand, m, s);

        PartSpec leftArm  = buildUpperArm("left_arm",   13f, leftForearm,  m, s);
        PartSpec rightArm = buildUpperArm("right_arm", -13f, rightForearm, m, s);

        PartSpec leftFoot  = buildFoot("left_foot",  m, s);
        PartSpec rightFoot = buildFoot("right_foot", m, s);

        PartSpec leftShin  = buildShin("left_lower_leg",  leftFoot,  m, s);
        PartSpec rightShin = buildShin("right_lower_leg", rightFoot, m, s);

        PartSpec leftLeg  = buildThigh("left_leg",   5f, leftShin,  m, s);
        PartSpec rightLeg = buildThigh("right_leg", -5f, rightShin, m, s);

        PartSpec head  = buildHead(m, s);
        PartSpec torso = buildTorso(m, s);

        return new ModelSpec.Builder("RedDemon")
                .textureWidth(1024)
                .textureHeight(1024)
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
    // HEAD
    // -----------------------------------------------------------------------

    /**
     * Demon head — sculpted with distinct anatomical regions.
     *
     * <ul>
     *   <li><b>Skull</b> — main 14W × 7H × 12D volume with forehead surface and crown cap.</li>
     *   <li><b>Brow</b> — hard ridge overhang and paired brow bumps above the eyes.</li>
     *   <li><b>Cheeks / temples</b> — outward masses on each side of the skull.</li>
     *   <li><b>Eye sockets</b> — deep recessed cubes with glowing-yellow inserts behind them.</li>
     *   <li><b>Jaw</b> — upper snout + lower jaw body + lateral cheek sides.</li>
     *   <li><b>Teeth</b> — two rows of bone-white tooth cubes (upper and lower).</li>
     *   <li><b>Horns</b> — two large curved horns attached as child parts.</li>
     * </ul>
     *
     * <pre>
     *   pivot  : (0, 0, 0) — model origin / neck-top
     *   skull  : 14W × 7H × 12D  centered  Y = −7..0
     *   crown  : protrudes 2 units above the skull  Y = −9..−7
     *   brow   : overhangs front-top of skull
     *   cheeks : protrude outward from each side
     *   eyes   : deep sockets with glowing-yellow inserts
     *   jaw    : lower snout with demon teeth
     * </pre>
     */
    private static PartSpec buildHead(int m, int s) {
        PartSpec leftHorn  = buildHorn("left_horn",  true,  m);
        PartSpec rightHorn = buildHorn("right_horn", false, m);

        PartSpec.Builder b = new PartSpec.Builder("head")
                .pose(new PoseSpec(0f, 0f, 0f));

        // Main skull (14W × 7H × 12D)
        addGrid(b,  -7f, -7f,  -6f,  14f,  7f, 12f,  m + 1,    m,   m + 1,   0,   0);

        // Forehead surface — bulging layer above brow line
        addGrid(b,  -7f, -8f,  -5f,  14f,  2f,  9f,      m,    2,       m,   0,  32);

        // Brow ridge — hard overhang at forehead
        addGrid(b,  -7f,-8.5f, -7f,  14f,  1.5f,2f,      m,    1,       1,   0,  48);

        // Left brow bump
        addGrid(b,   2f, -9f,  -7f,   4f,  2.5f,2f,      s,    s,       1,  64,   0);
        // Right brow bump
        addGrid(b,  -6f, -9f,  -7f,   4f,  2.5f,2f,      s,    s,       1,  64,   0);

        // Left cheek mass — protrudes outward from skull
        addGrid(b,   4f, -6f,  -5f,   3f,   4f,  3f,  s + 1, s + 1,  s + 1, 64,  32);
        // Right cheek mass
        addGrid(b,  -7f, -6f,  -5f,   3f,   4f,  3f,  s + 1, s + 1,  s + 1, 64,  32);

        // Left temple
        addGrid(b,   6f, -7f,  -4f,   1.5f, 5f,  8f,      1, s + 1,  s + 1, 80,   0);
        // Right temple
        addGrid(b,-7.5f, -7f,  -4f,   1.5f, 5f,  8f,      1, s + 1,  s + 1, 80,   0);

        // Left lateral skull surface
        addGrid(b,   6f, -7f,  -6f,   1.5f, 7f,  2f,      1,     s,       1, 80,  32);
        // Right lateral skull surface
        addGrid(b,-7.5f, -7f,  -6f,   1.5f, 7f,  2f,      1,     s,       1, 80,  32);

        // Left eye socket — deep recess on front face
        addGrid(b,   1f, -6f, -7.5f,  2.5f, 2.5f,1f,  s + 1, s + 1,       1, 96,   0);
        // Right eye socket
        addGrid(b,-3.5f, -6f, -7.5f,  2.5f, 2.5f,1f,  s + 1, s + 1,       1, 96,   0);

        // Left eye — glowing yellow insert (sits behind socket rim)
        addGrid(b, 1.5f,-5.5f, -8f,   1.5f, 1.5f,1f,      s,     s,       1, 112,  0);
        // Right eye — glowing yellow insert
        addGrid(b,  -3f,-5.5f, -8f,   1.5f, 1.5f,1f,      s,     s,       1, 112,  0);

        // Nose bridge
        addGrid(b,  -1f, -5f,  -7f,   2f,   2f,  1f,      1,     s,       1, 128,  0);

        // Upper snout — jaw top
        addGrid(b,  -5f,-3.5f, -7f,  10f,   2f,  5f,      s,     1,       s, 128, 16);

        // Lower jaw body
        addGrid(b,-4.5f,-1.5f, -7f,   9f,  2.5f, 5f,      s,     1,       s, 128, 32);

        // Jaw cheek sides
        addGrid(b, 3.5f, -3f,  -6f,   1f,  3.5f, 4f,      1,     s,       s, 128, 48);
        addGrid(b,-4.5f, -3f,  -6f,   1f,  3.5f, 4f,      1,     s,       s, 128, 48);

        // Upper teeth — bone-white UV region
        addGrid(b,  -4f, -3f,  -8f,   8f,  1.5f, 1f,      m,     1,       1, 144,  0);
        // Lower teeth
        addGrid(b,  -4f,-1.5f, -8f,   8f,  1.5f, 1f,      m,     1,       1, 144, 16);

        // Chin
        addGrid(b,-3.5f,  0f,  -6f,   7f,  1.5f, 4f,      s,     1,       s, 144, 32);

        // Back of skull — thick rear plate
        addGrid(b,  -6f, -7f,   4f,  12f,   6f,  2f,      m, s + 1,       1, 160,  0);

        // Crown — top cap protruding above skull
        addGrid(b,  -6f, -9f,  -4f,  12f,   2f,  8f,      m,     2,       s, 160, 32);

        return b.child(leftHorn).child(rightHorn).build();
    }

    // -----------------------------------------------------------------------
    // HORNS  (12 segments each — curves outward and backward)
    // -----------------------------------------------------------------------

    /**
     * Large curved horn.
     *
     * <p>12 overlapping segments arranged in an upward arc that sweeps outward
     * (+X for left horn) and then curves backward (positive Z).  Segments
     * overlap by ~0.2 units to ensure a continuous surface.
     *
     * @param name part name
     * @param left {@code true} for the left horn, {@code false} for the right
     * @param m    grid multiplier — at LOW only the first 4 segments are used
     */
    private static PartSpec buildHorn(String name, boolean left, int m) {
        // Segment data for the LEFT horn: { x, y, z, sizeX, sizeY, sizeZ }.
        // Positions are relative to the head pivot (model origin).
        // Right horn: x is mirrored → xRight = −x − sizeX.
        final float[][] segs = {
            {  3f,  -9f, -1f,   3f,  3f,   3f  },   //  1 base — emerges from crown
            {  4f, -12f, -0.5f, 2.8f,3f,  2.8f},   //  2 first outward sweep
            {  5.5f,-15f, 0f,   2.5f,3f,  2.5f},   //  3
            {  7f, -18f,  0.5f, 2.5f,3f,  2.3f},   //  4 starts curving backward
            {  8.5f,-21f, 1f,   2f,  3f,  2.2f},   //  5
            {  9.5f,-24f, 1.5f, 2f,  2.8f,2f  },   //  6 mid arc
            { 10.5f,-27f, 2f,   1.8f,2.5f,1.8f},   //  7 narrowing toward tip
            { 11f, -30f,  2.2f, 1.5f,2.5f,1.5f},   //  8
            { 11.5f,-33f, 2.2f, 1.2f,2.2f,1.2f},   //  9
            { 11.5f,-36f, 2f,   1f,  2f,  1f  },   // 10 near tip
            { 11f,  -38f, 1.5f, 0.9f,2f,  0.9f},   // 11
            { 10.5f,-40f, 1f,   0.8f,2f,  0.8f},   // 12 pointed tip
        };

        final int numSegs = (m <= 1) ? 4 : segs.length;
        PartSpec.Builder b = new PartSpec.Builder(name).pose(PoseSpec.ZERO);

        for (int i = 0; i < numSegs; i++) {
            final float[] seg = segs[i];
            final float x = left ? seg[0] : (-seg[0] - seg[3]);
            b.cube(new CubeSpec(x, seg[1], seg[2],
                                seg[3], seg[4], seg[5],
                                192, 0));
        }

        return b.build();
    }

    // -----------------------------------------------------------------------
    // TORSO
    // -----------------------------------------------------------------------

    /**
     * Torso — the widest volume; the visual centerpiece of the model.
     *
     * <pre>
     *   pivot : (0, 0, 0) — model origin
     *   neck       : 8W × 2H × 8D    Y =  0..2
     *   chest      : 18W × 8H × 10D  Y =  0..8   (widest section)
     *   abdomen    : 14W × 6H ×  8D  Y =  8..14  (narrows toward hip)
     *   back slab  : 14W × 8H ×  3D  behind chest
     *   shoulder pads : protrude ±4 beyond the chest sides, Y = −1..8
     * </pre>
     */
    private static PartSpec buildTorso(int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder("torso")
                .pose(new PoseSpec(0f, 0f, 0f));

        // Neck (8W × 2H × 8D)
        addGrid(b,  -4f,  0f, -4f,   8f,  2f,  8f,   s,   1,   s,   0,  64);

        // Chest main (18W × 8H × 10D) — widest section
        addGrid(b,  -9f,  0f, -5f,  18f,  8f, 10f, m+1,   m, m+1,   0,  80);

        // Left pectoral muscle surface (bulges off front face)
        addGrid(b,   0f, 0.5f,-6f,   8f,  5f,  1f,   m,   s,   1,  32,  64);
        // Right pectoral muscle surface
        addGrid(b,  -8f, 0.5f,-6f,   8f,  5f,  1f,   m,   s,   1,  32,  64);

        // Collarbone band — front of upper chest
        addGrid(b,  -8f,-0.5f,-6f,  16f, 1.5f, 1f,   m,   1,   1,  48,  64);

        // Lower chest band — front
        addGrid(b,  -8f,  7f, -6f,  16f, 1.5f, 1f,   m,   1,   1,  48,  72);

        // Abdomen (14W × 6H × 8D)
        addGrid(b,  -7f,  8f, -4f,  14f,  6f,  8f,   m,   m,   m,  64,  64);

        // Abdominal muscle rows — 3 pairs on front face
        addGrid(b,  -5f,  8f, -5f,   4f, 1.5f, 1f,   s,   1,   1,  80,  64);
        addGrid(b,   1f,  8f, -5f,   4f, 1.5f, 1f,   s,   1,   1,  80,  64);
        addGrid(b,  -5f, 10f, -5f,   4f, 1.5f, 1f,   s,   1,   1,  80,  72);
        addGrid(b,   1f, 10f, -5f,   4f, 1.5f, 1f,   s,   1,   1,  80,  72);
        addGrid(b,  -5f, 12f, -5f,   4f, 1.5f, 1f,   s,   1,   1,  80,  80);
        addGrid(b,   1f, 12f, -5f,   4f, 1.5f, 1f,   s,   1,   1,  80,  80);

        // Oblique muscles — sides of abdomen
        addGrid(b,   8f,  5f, -3f,  1.5f, 7f,  6f,   1,   m,   s,  96,  64);
        addGrid(b,-9.5f,  5f, -3f,  1.5f, 7f,  6f,   1,   m,   s,  96,  64);

        // Back muscles — thick slab behind chest
        addGrid(b,  -7f,  0f,  5f,  14f,  8f,  3f,   m,   m,   s, 112,  64);

        // Spinal ridge — centre of back
        addGrid(b,-1.5f,  1f,  7f,   3f,  7f,  1f,   1,   m,   1, 128,  64);

        // Lower back — behind abdomen
        addGrid(b,  -6f,  8f,  4f,  12f,  6f,  3f,   m,   m,   s, 144,  64);

        // Left trapezius — neck-to-shoulder transition
        addGrid(b, 3.5f, -1f, -2f,   5f,  3f,  4f,   s,   s,   s, 160,  64);
        // Right trapezius
        addGrid(b,-8.5f, -1f, -2f,   5f,  3f,  4f,   s,   s,   s, 160,  64);

        // Left lat spread — adds V-taper to silhouette
        addGrid(b,   7f,  3f, -1f,   2f,  7f,  8f,   1,   m,   s, 176,  64);
        // Right lat spread
        addGrid(b,  -9f,  3f, -1f,   2f,  7f,  8f,   1,   m,   s, 176,  64);

        // Left intercostal ribs — side of chest
        addGrid(b,   7f,  0f, -5f,   2f,  6f,  5f,   1,   m,   s, 192,  64);
        // Right intercostal ribs
        addGrid(b,  -9f,  0f, -5f,   2f,  6f,  5f,   1,   m,   s, 192,  64);

        // Left shoulder pad — protrudes from left side of chest: X = 9..14, Y = −1..8
        addGrid(b,   9f, -1f, -5f,   5f,  9f, 10f, s+1, m+1,   m, 208,   0);
        // Left shoulder pad top curve
        addGrid(b,   9f, -2f, -4f,   4f, 1.5f, 8f,   s,   1,   s, 208,  48);

        // Right shoulder pad — mirror of left
        addGrid(b, -14f, -1f, -5f,   5f,  9f, 10f, s+1, m+1,   m, 224,   0);
        // Right shoulder pad top curve
        addGrid(b, -13f, -2f, -4f,   4f, 1.5f, 8f,   s,   1,   s, 224,  48);

        return b.build();
    }

    // -----------------------------------------------------------------------
    // UPPER ARM
    // -----------------------------------------------------------------------

    /**
     * Upper arm — very thick for the muscular silhouette.
     *
     * <pre>
     *   pivot  : (±pivotX, 1, 0) — shoulder joint, top of chest
     *   volume : 6W × 8H × 6D   upper-arm thickness ≈ 6 units
     *   arm tip reaches absolute Y ≈ 18–20 (past mid-thigh at Y = 17.5)
     * </pre>
     *
     * @param name    part name
     * @param pivotX  shoulder X (+13 = left arm, −13 = right arm)
     * @param forearm already-built forearm child
     * @param m       grid multiplier
     * @param s       secondary density
     */
    private static PartSpec buildUpperArm(String name, float pivotX,
                                          PartSpec forearm, int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(pivotX, 1f, 0f));

        // Main upper arm (6W × 8H × 6D)
        addGrid(b,  -3f,  0f, -3f,   6f,  8f,  6f, s+2,   m, s+2,  256,   0);

        // Bicep bulge — front face surface
        addGrid(b,-2.5f, 0.5f,-4f,   5f,  4f,  1f,   s,   s,   1,  256,  64);

        // Tricep mass — back face surface
        addGrid(b,-2.5f, 0.5f, 3f,   5f,  4f,  1f,   s,   s,   1,  256,  80);

        // Deltoid cap — rounds the shoulder top
        addGrid(b,  -3f,-1.5f,-2f,   6f,  2f,  4f,   s,   1,   s,  256,  96);

        // Outer arm detail
        addGrid(b, 2.5f,  1f,-2.5f,  1f,  5f,  5f,   1,   s,   s,  256, 112);

        // Elbow spike — backward protrusion
        addGrid(b,-1.5f,  6f,  3f,   3f,  2f, 2.5f,  s,   1,   1,  272,   0);

        return b.child(forearm).build();
    }

    // -----------------------------------------------------------------------
    // FOREARM
    // -----------------------------------------------------------------------

    /**
     * Forearm — child of an upper arm.
     *
     * <pre>
     *   pivot : (0, 8, 0) relative to upper arm — elbow joint
     *   volume: 5W × 5H × 5D  (forearm thickness ≈ 5 units)
     * </pre>
     *
     * @param name part name
     * @param hand already-built hand child
     * @param m    grid multiplier
     * @param s    secondary density
     */
    private static PartSpec buildForearm(String name, PartSpec hand, int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 8f, 0f));

        // Main forearm (5W × 5H × 5D)
        addGrid(b,-2.5f,  0f,-2.5f,  5f,  5f,  5f, s+2,   m, s+2,  288,   0);

        // Forearm extensor — back surface
        addGrid(b,  -2f, 0.5f, 2.5f, 4f, 3.5f, 1f,   s,   s,   1,  288,  64);

        // Forearm flexor — front surface
        addGrid(b,  -2f, 0.5f,-3.5f, 4f, 3.5f, 1f,   s,   s,   1,  288,  80);

        // Wrist detail band
        addGrid(b,-2.5f,  4f,-2.5f,  5f, 1.5f, 5f,   s,   1,   s,  288,  96);

        return b.child(hand).build();
    }

    // -----------------------------------------------------------------------
    // HAND
    // -----------------------------------------------------------------------

    /**
     * Hand — child of a forearm.
     *
     * <pre>
     *   pivot  : (0, 5, 0) relative to forearm — wrist joint
     *   volume : 5W × 3H × 4D  + 4 demon claws + thumb claw
     * </pre>
     *
     * @param name part name
     * @param m    grid multiplier
     * @param s    secondary density
     */
    private static PartSpec buildHand(String name, int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 5f, 0f));

        // Main hand volume (5W × 3H × 4D)
        addGrid(b,-2.5f,  0f, -2f,   5f,  3f,  4f,   m,   s,   m,  304,   0);

        // 4 demon finger claws (dark-grey UV region)
        addGrid(b,-2.5f,  3f, -2f,   1f, 2.5f,0.8f,  1,   s,   1,  304,  48);
        addGrid(b,  -1f,  3f, -2f,   1f, 2.5f,0.8f,  1,   s,   1,  304,  48);
        addGrid(b, 0.5f,  3f, -2f,   1f, 2.5f,0.8f,  1,   s,   1,  304,  48);
        addGrid(b,   2f,  3f, -2f,   1f, 2.5f,0.8f,  1,   s,   1,  304,  48);

        // Thumb claw
        addGrid(b,-3.5f, 0.5f,-1f,   1f, 1.5f, 1f,   1,   1,   1,  304,  64);

        return b.build();
    }

    // -----------------------------------------------------------------------
    // THIGH (upper leg)
    // -----------------------------------------------------------------------

    /**
     * Thigh — root of each leg chain.
     *
     * <pre>
     *   pivot  : (±pivotX, 14, 0) — hip joint in model space
     *   volume : 7W × 7H × 7D
     *   knee cap protrudes forward at the bottom of the thigh
     * </pre>
     *
     * @param name   part name
     * @param pivotX hip X (+5 = left leg, −5 = right leg)
     * @param shin   already-built shin child
     * @param m      grid multiplier
     * @param s      secondary density
     */
    private static PartSpec buildThigh(String name, float pivotX,
                                       PartSpec shin, int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(pivotX, 14f, 0f));

        // Main thigh (7W × 7H × 7D)
        addGrid(b,-3.5f,  0f,-3.5f,  7f,  7f,  7f, s+1,   m, s+1,  320,   0);

        // Quad muscle — front face surface
        addGrid(b,  -3f, 0.5f,-4.5f, 6f, 4.5f, 1f,   s,   s,   1,  320,  64);

        // Hamstring — back face
        addGrid(b,  -3f, 0.5f, 3.5f, 6f, 4.5f, 1f,   s,   s,   1,  320,  80);

        // Inner thigh
        addGrid(b,-4.5f,  1f, -3f,   1f,  5f,  6f,   1,   s,   s,  320,  96);

        // Knee cap / guard
        addGrid(b,-2.5f, 5.5f,-5f,   5f,  2f, 1.5f,  s,   1,   1,  320, 112);

        return b.child(shin).build();
    }

    // -----------------------------------------------------------------------
    // SHIN (lower leg)
    // -----------------------------------------------------------------------

    /**
     * Shin — child of a thigh.
     *
     * <pre>
     *   pivot : (0, 7, 0) relative to thigh — knee joint
     *   volume: 6W × 6H × 6D
     * </pre>
     *
     * @param name part name
     * @param foot already-built foot child
     * @param m    grid multiplier
     * @param s    secondary density
     */
    private static PartSpec buildShin(String name, PartSpec foot, int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 7f, 0f));

        // Main shin (6W × 6H × 6D)
        addGrid(b,  -3f,  0f, -3f,   6f,  6f,  6f, s+1,   m, s+1,  336,   0);

        // Shin plate — tibia bump on front face
        addGrid(b,-1.5f, 0.5f,-4f,   3f,  4f,  1f,   s,   s,   1,  336,  64);

        // Calf mass — back face
        addGrid(b,  -2f,  0f,  3f,   4f,  4f,  1f,   s,   s,   1,  336,  80);

        // Outer shin detail
        addGrid(b, 2.5f,  1f,-2.5f,  1f,  4f,  5f,   1,   s,   s,  336,  96);

        return b.child(foot).build();
    }

    // -----------------------------------------------------------------------
    // FOOT
    // -----------------------------------------------------------------------

    /**
     * Foot — child of a shin.
     *
     * <pre>
     *   pivot : (0, 6, 0) relative to shin — ankle joint
     *   volume: 8W × 4H × 12D — extends forward (negative Z)
     *   bottom of foot is at Y = 4 relative to pivot (absolute Y = 31 — ground)
     *   4 demon toes protrude from the front; heel protrudes at the back
     * </pre>
     *
     * @param name part name
     * @param m    grid multiplier
     * @param s    secondary density
     */
    private static PartSpec buildFoot(String name, int m, int s) {
        PartSpec.Builder b = new PartSpec.Builder(name)
                .pose(new PoseSpec(0f, 6f, 0f));

        // Main foot (8W × 4H × 12D) — extends forward; Y = 0..4 (ground at bottom)
        addGrid(b,  -4f,  0f,-10f,   8f,  4f, 12f,   m,   s,   m,  352,   0);

        // 4 demon toe extensions (dark-grey claws)
        addGrid(b,  -4f,  2f,-12f,  1.5f, 2f,  2f,   1,   1,   1,  352,  64);
        addGrid(b,-1.5f,  2f,-12f,  1.5f, 2f,  2f,   1,   1,   1,  352,  64);
        addGrid(b,   1f,  2f,-12f,  1.5f, 2f,  2f,   1,   1,   1,  352,  64);
        addGrid(b,   3f,  2f,-12f,  1.5f, 2f,  2f,   1,   1,   1,  352,  64);

        // Heel protrusion — back of foot
        addGrid(b,  -3f,  0f,  1.5f, 6f,  4f,  2f,   s,   1,   1,  352,  80);

        // Ankle bone bump — top of foot near ankle
        addGrid(b,  -2f, -1f, -8f,   4f, 1.5f, 4f,   s,   1,   s,  352,  96);

        return b.build();
    }

    // -----------------------------------------------------------------------
    // Grid helper
    // -----------------------------------------------------------------------

    /**
     * Fills the axis-aligned box {@code [x, x+w] × [y, y+h] × [z, z+d]} with
     * a uniform grid of {@code nx × ny × nz} equal-sized {@link CubeSpec}s,
     * all sharing the UV offset {@code (u, v)}.
     *
     * <p>Increasing {@code nx/ny/nz} at higher {@link DetailLevel}s adds
     * muscle-definition without manually specifying thousands of boxes.
     *
     * @param b  builder to receive the cubes
     * @param x  box-origin X (relative to part pivot)
     * @param y  box-origin Y (relative to part pivot)
     * @param z  box-origin Z (relative to part pivot)
     * @param w  total width  (X, must be &gt; 0)
     * @param h  total height (Y, must be &gt; 0)
     * @param d  total depth  (Z, must be &gt; 0)
     * @param nx subdivisions along X (must be ≥ 1)
     * @param ny subdivisions along Y (must be ≥ 1)
     * @param nz subdivisions along Z (must be ≥ 1)
     * @param u  texture U offset (shared by all cubes)
     * @param v  texture V offset (shared by all cubes)
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
