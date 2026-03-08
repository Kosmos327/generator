# Minecraft Forge Model Generator

A reusable, architecture-first Java code generator that produces Minecraft Forge `EntityModel` source files and texture atlas PNGs from lightweight model definitions.

---

## Purpose

This repository contains **only** the generator. It is not tied to any single mob. The generator is a reusable engine designed to support future creation of:

- Demons
- Seven Deadly Sins characters
- Holy knights
- Bosses
- Humanoids, monsters, and weapons

New model definitions can be added at any time by creating a single file in `src/modeldefs/` without touching the core engine.

---

## Project Structure

```
src/
├── ForgeModelGenerator.java    ← Entry point / orchestrator
│
├── core/                       ← Reusable engine types (never mob-specific)
│   ├── ModelSpec.java          ← Complete model specification
│   ├── PartSpec.java           ← Named part (bone) with cubes and children
│   ├── CubeSpec.java           ← Single cube: position, size, UV
│   ├── PoseSpec.java           ← Default pivot and rotation for a part
│   ├── TextureAtlas.java       ← UV allocation + PNG export
│   ├── DetailLevel.java        ← LOD enum: LOW / MEDIUM / HIGH
│   ├── ColorSpec.java          ← Immutable RGBA color value
│   ├── ModelAnalysis.java      ← Validation and statistics
│   ├── Noise.java              ← Value noise (1D / 2D / 3D / fractal)
│   └── Symmetry.java           ← Mirror helpers for symmetric models
│
├── modeldefs/                  ← One file per model definition
│   └── ExampleModelDef.java    ← Minimal proof-of-concept model
│
└── generated/                  ← Output folder (git-ignored)
    ├── ExampleEntityModel.java ← Generated Forge EntityModel source
    └── exampleentity_texture.png ← Generated texture atlas
```

---

## How to Add a New Model

1. Create a new file in `src/modeldefs/`, e.g. `DemonModelDef.java`:

```java
package modeldefs;

import core.*;

public final class DemonModelDef {

    private DemonModelDef() {}

    public static ModelSpec build(DetailLevel detail) {
        PartSpec head = new PartSpec.Builder("head")
            .pose(new PoseSpec(0f, 0f, 0f))
            .cube(new CubeSpec(-4f, -8f, -4f, 8f, 8f, 8f, 0, 0))
            .build();

        return new ModelSpec.Builder("DemonEntity")
            .textureWidth(64)
            .textureHeight(64)
            .detailLevel(detail)
            .part(head)
            .build();
    }
}
```

2. In `ForgeModelGenerator.java`, change:

```java
ModelSpec spec = ExampleModelDef.build(detail);
```

to:

```java
ModelSpec spec = DemonModelDef.build(detail);
```

3. Run the generator. The output appears in `src/generated/`.

---

## How to Run the Generator

### Compile

```bash
javac -sourcepath src -d out $(find src -name "*.java" | grep -v generated)
```

### Run

```bash
java -cp out ForgeModelGenerator
```

### Output

Generated files are written to `src/generated/`:

| File | Description |
|------|-------------|
| `<ModelName>Model.java` | Forge `EntityModel` subclass ready to copy into your mod |
| `<modelname>_texture.png` | Texture atlas showing allocated UV regions |

---

## Core API Quick Reference

### `ModelSpec`

Top-level model container. Built with `ModelSpec.Builder`:

```java
ModelSpec spec = new ModelSpec.Builder("MyEntity")
    .textureWidth(64)
    .textureHeight(64)
    .detailLevel(DetailLevel.HIGH)
    .part(somePart)
    .build();
```

### `PartSpec`

Named bone with geometry. Built with `PartSpec.Builder`:

```java
PartSpec arm = new PartSpec.Builder("left_arm")
    .pose(new PoseSpec(-5f, 2f, 0f))
    .cube(new CubeSpec(-3f, -2f, -2f, 4f, 12f, 4f, 40, 16))
    .build();
```

### `CubeSpec`

A single oriented box:

```java
// CubeSpec(x, y, z, sizeX, sizeY, sizeZ, u, v)
new CubeSpec(-4f, -8f, -4f, 8f, 8f, 8f, 0, 0)
```

### `Symmetry`

Mirror a part across the Y-Z plane:

```java
PartSpec rightArm = Symmetry.mirrorPart(leftArm); // renames left_ → right_
```

### `Noise`

Procedural variation:

```java
float bump = Noise.noise2D(x * 0.1f, y * 0.1f);
```

### `DetailLevel`

Scale complexity to the requested LOD inside `build()`:

```java
if (detail == DetailLevel.HIGH) {
    // add extra detail cubes
}
```

---

## Dependencies

- **Java 8+** — no external libraries required.
- `javax.imageio` (JDK standard) is used for PNG export.
- The generated source targets **Minecraft Forge 1.20.x** (Parchment / Mojmaps naming).

---

## License

This generator is provided as-is for Minecraft mod development use.
