package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The top-level specification for a single model.
 *
 * <p>A {@code ModelSpec} aggregates everything needed to generate a Forge model:
 * <ul>
 *   <li>A unique name used for the generated class name.</li>
 *   <li>The list of root {@link PartSpec} objects (the model's bone hierarchy).</li>
 *   <li>A {@link TextureAtlas} that tracks UV regions for this model.</li>
 *   <li>The {@link DetailLevel} at which the model was built.</li>
 * </ul>
 *
 * <p>Build instances with {@link Builder}:
 * <pre>{@code
 * ModelSpec spec = new ModelSpec.Builder("ExampleEntity")
 *     .textureWidth(64)
 *     .textureHeight(32)
 *     .detailLevel(DetailLevel.MEDIUM)
 *     .part(headPart)
 *     .part(bodyPart)
 *     .build();
 * }</pre>
 */
public final class ModelSpec {

    private final String          name;
    private final int             textureWidth;
    private final int             textureHeight;
    private final DetailLevel     detailLevel;
    private final List<PartSpec>  rootParts;
    private final TextureAtlas    textureAtlas;

    private ModelSpec(Builder builder) {
        this.name          = builder.name;
        this.textureWidth  = builder.textureWidth;
        this.textureHeight = builder.textureHeight;
        this.detailLevel   = builder.detailLevel;
        this.rootParts     = Collections.unmodifiableList(new ArrayList<>(builder.rootParts));
        this.textureAtlas  = new TextureAtlas(builder.textureWidth, builder.textureHeight);
    }

    public String          getName()          { return name; }
    public int             getTextureWidth()  { return textureWidth; }
    public int             getTextureHeight() { return textureHeight; }
    public DetailLevel     getDetailLevel()   { return detailLevel; }
    public List<PartSpec>  getRootParts()     { return rootParts; }
    public TextureAtlas    getTextureAtlas()  { return textureAtlas; }

    @Override
    public String toString() {
        return String.format("ModelSpec(name=%s, texture=%dx%d, detail=%s, rootParts=%d)",
            name, textureWidth, textureHeight, detailLevel, rootParts.size());
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static final class Builder {

        private final String         name;
        private int                  textureWidth  = 64;
        private int                  textureHeight = 32;
        private DetailLevel          detailLevel   = DetailLevel.MEDIUM;
        private final List<PartSpec> rootParts     = new ArrayList<>();

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("ModelSpec name must not be empty");
            }
            this.name = name;
        }

        public Builder textureWidth(int w) {
            if (w <= 0) throw new IllegalArgumentException("textureWidth must be > 0");
            this.textureWidth = w;
            return this;
        }

        public Builder textureHeight(int h) {
            if (h <= 0) throw new IllegalArgumentException("textureHeight must be > 0");
            this.textureHeight = h;
            return this;
        }

        public Builder detailLevel(DetailLevel level) {
            this.detailLevel = (level != null) ? level : DetailLevel.MEDIUM;
            return this;
        }

        public Builder part(PartSpec part) {
            if (part != null) rootParts.add(part);
            return this;
        }

        public ModelSpec build() {
            return new ModelSpec(this);
        }
    }
}
