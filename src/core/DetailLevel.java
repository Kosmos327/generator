package core;

/**
 * Defines the level of detail used when generating a model.
 * Higher detail levels produce more cubes and finer texture regions.
 * Model definitions can use this to scale complexity at generation time.
 */
public enum DetailLevel {

    /** Minimal geometry — suitable for distant LOD or simple mobs. */
    LOW,

    /** Balanced geometry — the default for most models. */
    MEDIUM,

    /** Maximum geometry — used for boss or showcase models. */
    HIGH
}
