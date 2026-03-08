package core;

import java.util.List;

/**
 * Static analysis utilities for {@link ModelSpec} instances.
 *
 * <p>These helpers are used by the generator to report statistics and by
 * model definitions to validate their output before generation begins.
 */
public final class ModelAnalysis {

    private ModelAnalysis() {}

    // -----------------------------------------------------------------------
    // Counting
    // -----------------------------------------------------------------------

    /**
     * Returns the total number of cubes across all parts (including nested children)
     * in the given model.
     *
     * @param spec model to analyse; must not be {@code null}
     * @return total cube count
     */
    public static int countCubes(ModelSpec spec) {
        int total = 0;
        for (PartSpec part : spec.getRootParts()) {
            total += countCubesInPart(part);
        }
        return total;
    }

    /**
     * Returns the total number of parts (including nested children)
     * in the given model.
     *
     * @param spec model to analyse; must not be {@code null}
     * @return total part count
     */
    public static int countParts(ModelSpec spec) {
        int total = 0;
        for (PartSpec part : spec.getRootParts()) {
            total += countPartsInPart(part);
        }
        return total;
    }

    /**
     * Returns the maximum depth of the part hierarchy.
     * A model with only root parts (no children) has depth 1.
     *
     * @param spec model to analyse; must not be {@code null}
     * @return maximum hierarchy depth
     */
    public static int maxDepth(ModelSpec spec) {
        int max = 0;
        for (PartSpec part : spec.getRootParts()) {
            max = Math.max(max, depthOf(part));
        }
        return max;
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    /**
     * Validates that the model satisfies minimum structural requirements.
     *
     * <p>Checks performed:
     * <ul>
     *   <li>Model name is non-empty.</li>
     *   <li>At least one root part exists.</li>
     *   <li>All part names are unique within the model.</li>
     *   <li>Texture dimensions are positive.</li>
     * </ul>
     *
     * @param spec model to validate; must not be {@code null}
     * @throws IllegalStateException if any check fails
     */
    public static void validate(ModelSpec spec) {
        if (spec.getName() == null || spec.getName().isEmpty()) {
            throw new IllegalStateException("ModelSpec name must not be empty");
        }
        if (spec.getTextureWidth() <= 0 || spec.getTextureHeight() <= 0) {
            throw new IllegalStateException("ModelSpec texture dimensions must be > 0");
        }
        if (spec.getRootParts().isEmpty()) {
            throw new IllegalStateException(
                "ModelSpec '" + spec.getName() + "' has no root parts");
        }
        checkUniqueNames(spec);
    }

    /**
     * Returns a human-readable summary string for logging / debugging.
     *
     * @param spec model to summarise; must not be {@code null}
     * @return multi-line summary
     */
    public static String summarise(ModelSpec spec) {
        return String.format(
            "Model   : %s%n"
          + "Detail  : %s%n"
          + "Texture : %dx%d%n"
          + "Parts   : %d%n"
          + "Cubes   : %d%n"
          + "Depth   : %d",
            spec.getName(),
            spec.getDetailLevel(),
            spec.getTextureWidth(), spec.getTextureHeight(),
            countParts(spec),
            countCubes(spec),
            maxDepth(spec));
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private static int countCubesInPart(PartSpec part) {
        int count = part.getCubes().size();
        for (PartSpec child : part.getChildren()) {
            count += countCubesInPart(child);
        }
        return count;
    }

    private static int countPartsInPart(PartSpec part) {
        int count = 1;
        for (PartSpec child : part.getChildren()) {
            count += countPartsInPart(child);
        }
        return count;
    }

    private static int depthOf(PartSpec part) {
        int max = 0;
        for (PartSpec child : part.getChildren()) {
            max = Math.max(max, depthOf(child));
        }
        return max + 1;
    }

    private static void checkUniqueNames(ModelSpec spec) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (PartSpec part : spec.getRootParts()) {
            collectNames(part, seen);
        }
    }

    private static void collectNames(PartSpec part, java.util.Set<String> seen) {
        if (!seen.add(part.getName())) {
            throw new IllegalStateException(
                "Duplicate part name '" + part.getName() + "' in model");
        }
        for (PartSpec child : part.getChildren()) {
            collectNames(child, seen);
        }
    }
}
