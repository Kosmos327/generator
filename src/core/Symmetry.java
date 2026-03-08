package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for mirroring model parts along the X axis.
 *
 * <p>Minecraft humanoid models (arms, legs, etc.) are typically symmetric
 * about the Y-Z plane. Rather than defining both sides manually, use
 * {@link #mirrorPart} to generate the mirrored counterpart automatically.
 *
 * <h3>Naming convention</h3>
 * Parts that follow the convention {@code "left_*"} are renamed to {@code "right_*"}
 * and vice-versa during mirroring. Parts with neither prefix receive a {@code "_mirrored"}
 * suffix so they remain uniquely named.
 */
public final class Symmetry {

    private Symmetry() {}

    // -----------------------------------------------------------------------
    // Part mirroring
    // -----------------------------------------------------------------------

    /**
     * Returns a mirror image of the given part reflected across the Y-Z plane (X = 0).
     *
     * <p>All cubes are mirrored via {@link CubeSpec#mirrored()}.
     * The pivot X is negated. Child parts are recursively mirrored.
     *
     * @param part source part; must not be {@code null}
     * @return new {@link PartSpec} that is the mirror of {@code part}
     */
    public static PartSpec mirrorPart(PartSpec part) {
        PoseSpec src = part.getDefaultPose();
        PoseSpec mirroredPose = new PoseSpec(
            -src.getPivotX(), src.getPivotY(), src.getPivotZ(),
             src.getRotX(),  -src.getRotY(),  -src.getRotZ()
        );

        PartSpec.Builder builder = new PartSpec.Builder(mirrorName(part.getName()))
            .pose(mirroredPose);

        for (CubeSpec cube : part.getCubes()) {
            builder.cube(cube.mirrored());
        }

        for (PartSpec child : part.getChildren()) {
            builder.child(mirrorPart(child));
        }

        return builder.build();
    }

    /**
     * Returns a new list containing each part in {@code parts} followed immediately
     * by its mirrored counterpart.
     *
     * <p>Parts that already have a {@code "right_"} prefix are skipped to avoid
     * double-mirroring when the list contains both sides.
     *
     * @param parts source part list; must not be {@code null}
     * @return new list with originals and their mirrors interleaved
     */
    public static List<PartSpec> mirrorAll(List<PartSpec> parts) {
        List<PartSpec> result = new ArrayList<>();
        for (PartSpec part : parts) {
            result.add(part);
            if (!part.getName().startsWith("right_")) {
                result.add(mirrorPart(part));
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Cube helpers
    // -----------------------------------------------------------------------

    /**
     * Returns a mirrored copy of a single cube without building an entire part.
     *
     * @param cube cube to mirror
     * @return mirrored cube
     */
    public static CubeSpec mirrorCube(CubeSpec cube) {
        return cube.mirrored();
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Applies naming conventions when mirroring:
     * <ul>
     *   <li>{@code "left_foo"} → {@code "right_foo"}</li>
     *   <li>{@code "right_foo"} → {@code "left_foo"}</li>
     *   <li>{@code "foo"} → {@code "foo_mirrored"}</li>
     * </ul>
     */
    static String mirrorName(String name) {
        if (name.startsWith("left_")) {
            return "right_" + name.substring(5);
        } else if (name.startsWith("right_")) {
            return "left_" + name.substring(6);
        } else {
            return name + "_mirrored";
        }
    }
}
