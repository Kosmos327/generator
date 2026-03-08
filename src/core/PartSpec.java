package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a named part (bone) of a model.
 *
 * <p>A part contains:
 * <ul>
 *   <li>One or more {@link CubeSpec} objects describing its geometry.</li>
 *   <li>A {@link PoseSpec} describing its default pivot and rotation.</li>
 *   <li>Optional child parts that inherit this part's transform.</li>
 * </ul>
 *
 * <p>Build instances with {@link Builder}:
 * <pre>{@code
 * PartSpec head = new PartSpec.Builder("head")
 *     .pose(new PoseSpec(0, 0, 0))
 *     .cube(new CubeSpec(-4, -8, -4, 8, 8, 8, 0, 0))
 *     .build();
 * }</pre>
 */
public final class PartSpec {

    private final String        name;
    private final PoseSpec      defaultPose;
    private final List<CubeSpec> cubes;
    private final List<PartSpec> children;

    private PartSpec(Builder builder) {
        this.name        = builder.name;
        this.defaultPose = builder.defaultPose;
        this.cubes       = Collections.unmodifiableList(new ArrayList<>(builder.cubes));
        this.children    = Collections.unmodifiableList(new ArrayList<>(builder.children));
    }

    public String         getName()        { return name; }
    public PoseSpec       getDefaultPose() { return defaultPose; }
    public List<CubeSpec> getCubes()       { return cubes; }
    public List<PartSpec> getChildren()    { return children; }

    /** Total number of cubes in this part (not including children). */
    public int cubeCount() {
        return cubes.size();
    }

    @Override
    public String toString() {
        return String.format("PartSpec(name=%s, cubes=%d, children=%d)",
            name, cubes.size(), children.size());
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static final class Builder {

        private final String        name;
        private PoseSpec            defaultPose = PoseSpec.ZERO;
        private final List<CubeSpec> cubes    = new ArrayList<>();
        private final List<PartSpec> children = new ArrayList<>();

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("PartSpec name must not be empty");
            }
            this.name = name;
        }

        public Builder pose(PoseSpec pose) {
            this.defaultPose = (pose != null) ? pose : PoseSpec.ZERO;
            return this;
        }

        public Builder cube(CubeSpec cube) {
            if (cube != null) cubes.add(cube);
            return this;
        }

        public Builder child(PartSpec child) {
            if (child != null) children.add(child);
            return this;
        }

        public PartSpec build() {
            return new PartSpec(this);
        }
    }
}
