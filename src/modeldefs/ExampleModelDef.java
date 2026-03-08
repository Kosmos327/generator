package modeldefs;

import core.CubeSpec;
import core.DetailLevel;
import core.ModelSpec;
import core.PartSpec;
import core.PoseSpec;

/**
 * A minimal model definition used solely to exercise and prove the generator architecture.
 *
 * <p>This class defines a simple two-part model (head + body) with a 64×32 texture.
 * It is intentionally kept small and self-contained. Future model definitions —
 * demons, holy knights, bosses, etc. — should each live in their own file in this
 * {@code modeldefs} package and implement the same {@code build(DetailLevel)} pattern.
 *
 * <h3>Adding a new model definition</h3>
 * <ol>
 *   <li>Create a new file in {@code src/modeldefs/}, e.g. {@code DemonModelDef.java}.</li>
 *   <li>Declare a {@code package modeldefs;} header.</li>
 *   <li>Implement a {@code public static ModelSpec build(DetailLevel detail)} method.</li>
 *   <li>Register it in {@code ForgeModelGenerator.java} to generate output for it.</li>
 * </ol>
 */
public final class ExampleModelDef {

    private ExampleModelDef() {}

    /**
     * Builds and returns a minimal valid {@link ModelSpec} for the example entity.
     *
     * <p>At {@link DetailLevel#LOW} only the head is generated.
     * At {@link DetailLevel#MEDIUM} and above, the body is also included.
     *
     * @param detail requested detail level
     * @return fully constructed {@link ModelSpec}
     */
    public static ModelSpec build(DetailLevel detail) {

        // --- Head -----------------------------------------------------------
        PartSpec head = new PartSpec.Builder("head")
            .pose(new PoseSpec(0f, 0f, 0f))
            .cube(new CubeSpec(-4f, -8f, -4f, 8f, 8f, 8f, 0, 0))
            .build();

        ModelSpec.Builder modelBuilder = new ModelSpec.Builder("ExampleEntity")
            .textureWidth(64)
            .textureHeight(32)
            .detailLevel(detail)
            .part(head);

        // --- Body (MEDIUM and HIGH only) ------------------------------------
        if (detail != DetailLevel.LOW) {
            PartSpec body = new PartSpec.Builder("body")
                .pose(new PoseSpec(0f, 0f, 0f))
                .cube(new CubeSpec(-4f, 0f, -2f, 8f, 12f, 4f, 16, 16))
                .build();
            modelBuilder.part(body);
        }

        return modelBuilder.build();
    }
}
