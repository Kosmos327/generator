package core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * A reusable UV texture atlas that supports shelf-packing allocation and PNG export.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * TextureAtlas atlas = new TextureAtlas(64, 32);
 *
 * // Allocate a 16x8 region for the head faces
 * int[] uv = atlas.allocate("head", 16, 8, ColorSpec.WHITE);
 * // uv[0] = U offset, uv[1] = V offset
 *
 * // Paint arbitrary pixels
 * atlas.setPixel(0, 0, new ColorSpec(0.8f, 0.6f, 0.4f));
 *
 * // Serialize to disk
 * atlas.writePNG("src/generated/example_texture.png");
 * }</pre>
 *
 * <h3>Shelf packing</h3>
 * Regions are allocated left-to-right, top-to-bottom using a shelf algorithm.
 * When a region does not fit on the current shelf it is moved to the next shelf.
 * This keeps the atlas compact without requiring a full 2-D bin-packing solver.
 */
public final class TextureAtlas {

    // -----------------------------------------------------------------------
    // Allocated region descriptor
    // -----------------------------------------------------------------------

    /** An immutable record of a successfully allocated UV region. */
    public static final class Region {

        private final String name;
        private final int    u;
        private final int    v;
        private final int    width;
        private final int    height;

        Region(String name, int u, int v, int width, int height) {
            this.name   = name;
            this.u      = u;
            this.v      = v;
            this.width  = width;
            this.height = height;
        }

        public String getName()   { return name; }
        public int    getU()      { return u; }
        public int    getV()      { return v; }
        public int    getWidth()  { return width; }
        public int    getHeight() { return height; }

        @Override
        public String toString() {
            return String.format("Region(%s, u=%d, v=%d, %dx%d)", name, u, v, width, height);
        }
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final int            atlasWidth;
    private final int            atlasHeight;
    private final BufferedImage  image;
    private final List<Region>   regions = new ArrayList<>();

    /** Cursor position for shelf packing. */
    private int cursorU     = 0;
    private int cursorV     = 0;
    private int shelfHeight = 0;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new blank atlas of the given dimensions.
     * The image is initialised to fully transparent black.
     *
     * @param width  atlas width  in pixels (must be > 0)
     * @param height atlas height in pixels (must be > 0)
     */
    public TextureAtlas(int width, int height) {
        if (width  <= 0) throw new IllegalArgumentException("Atlas width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("Atlas height must be > 0");
        this.atlasWidth  = width;
        this.atlasHeight = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    // -----------------------------------------------------------------------
    // UV allocation
    // -----------------------------------------------------------------------

    /**
     * Allocates a rectangular region inside the atlas using shelf packing.
     *
     * <p>The region is filled with {@code fillColor} immediately after allocation.
     * Passing {@code null} for {@code fillColor} leaves the region transparent.
     *
     * @param name      logical name for the region (for debugging / lookup)
     * @param w         region width  in pixels
     * @param h         region height in pixels
     * @param fillColor color used to pre-fill the region, or {@code null}
     * @return the allocated {@link Region}; never {@code null}
     * @throws IllegalStateException if the atlas has no room for the requested region
     */
    public Region allocate(String name, int w, int h, ColorSpec fillColor) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Region dimensions must be > 0");
        }

        // Advance to next shelf if this region doesn't fit on the current one
        if (cursorU + w > atlasWidth) {
            cursorU      = 0;
            cursorV     += shelfHeight;
            shelfHeight  = 0;
        }

        if (cursorV + h > atlasHeight) {
            throw new IllegalStateException(
                String.format("Atlas is full — cannot allocate region '%s' (%dx%d) "
                    + "at cursor (%d,%d) in atlas (%dx%d)",
                    name, w, h, cursorU, cursorV, atlasWidth, atlasHeight));
        }

        Region region = new Region(name, cursorU, cursorV, w, h);
        regions.add(region);

        // Fill region if a color is provided
        if (fillColor != null) {
            fillRegion(cursorU, cursorV, w, h, fillColor);
        }

        // Advance cursor
        cursorU    += w;
        shelfHeight = Math.max(shelfHeight, h);

        return region;
    }

    /**
     * Allocates a region without pre-filling (leaves pixels transparent).
     *
     * @param name logical name
     * @param w    width in pixels
     * @param h    height in pixels
     * @return the allocated {@link Region}
     */
    public Region allocate(String name, int w, int h) {
        return allocate(name, w, h, null);
    }

    // -----------------------------------------------------------------------
    // Pixel operations
    // -----------------------------------------------------------------------

    /**
     * Sets a single pixel.
     *
     * @param x     X coordinate (0-based, must be within atlas bounds)
     * @param y     Y coordinate (0-based, must be within atlas bounds)
     * @param color color to write
     */
    public void setPixel(int x, int y, ColorSpec color) {
        checkBounds(x, y);
        image.setRGB(x, y, color.toARGB());
    }

    /**
     * Reads a single pixel.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return color at that position
     */
    public ColorSpec getPixel(int x, int y) {
        checkBounds(x, y);
        return ColorSpec.fromARGB(image.getRGB(x, y));
    }

    /**
     * Fills a rectangular region with a solid color.
     *
     * @param u     left edge
     * @param v     top edge
     * @param w     width
     * @param h     height
     * @param color fill color
     */
    public void fillRegion(int u, int v, int w, int h, ColorSpec color) {
        int argb = color.toARGB();
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int px = u + dx;
                int py = v + dy;
                if (px < atlasWidth && py < atlasHeight) {
                    image.setRGB(px, py, argb);
                }
            }
        }
    }

    /**
     * Draws a 1-pixel border around a previously allocated region.
     * Useful for visualising UV tiles during development.
     *
     * @param region region to outline
     * @param color  border color
     */
    public void drawBorder(Region region, ColorSpec color) {
        int argb = color.toARGB();
        int u = region.getU();
        int v = region.getV();
        int w = region.getWidth();
        int h = region.getHeight();

        for (int dx = 0; dx < w; dx++) {
            safeSet(u + dx, v,         argb);
            safeSet(u + dx, v + h - 1, argb);
        }
        for (int dy = 0; dy < h; dy++) {
            safeSet(u,         v + dy, argb);
            safeSet(u + w - 1, v + dy, argb);
        }
    }

    // -----------------------------------------------------------------------
    // PNG export
    // -----------------------------------------------------------------------

    /**
     * Writes the atlas image to a PNG file at the given path.
     * Parent directories are created automatically if they do not exist.
     *
     * @param path target file path (e.g. {@code "src/generated/my_texture.png"})
     * @throws IOException if the file cannot be written
     */
    public void writePNG(String path) throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created) {
                throw new IOException("Could not create directory: " + parent.getAbsolutePath());
            }
        }
        boolean written = ImageIO.write(image, "PNG", file);
        if (!written) {
            throw new IOException("No PNG ImageWriter found — cannot write atlas to: " + path);
        }
    }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    public int          getAtlasWidth()   { return atlasWidth; }
    public int          getAtlasHeight()  { return atlasHeight; }
    public List<Region> getRegions()      { return Collections.unmodifiableList(regions); }
    public int          getRegionCount()  { return regions.size(); }

    /**
     * Returns the first allocated region with the given name, or {@code null}.
     *
     * @param name region name to search for
     * @return matching region or {@code null}
     */
    public Region findRegion(String name) {
        for (Region r : regions) {
            if (r.getName().equals(name)) return r;
        }
        return null;
    }

    /**
     * Returns an approximate count of pixels still available for allocation.
     * The estimate is based on the rows fully below the current shelf; pixels
     * between the shelf cursor and the right edge of the atlas are not counted.
     */
    public int remainingPixels() {
        int nextRowStart = cursorV + shelfHeight;
        return atlasWidth * Math.max(0, atlasHeight - nextRowStart);
    }

    @Override
    public String toString() {
        return String.format("TextureAtlas(%dx%d, regions=%d)", atlasWidth, atlasHeight, regions.size());
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private void checkBounds(int x, int y) {
        if (x < 0 || x >= atlasWidth || y < 0 || y >= atlasHeight) {
            throw new IndexOutOfBoundsException(
                String.format("Pixel (%d,%d) is out of atlas bounds (%dx%d)",
                    x, y, atlasWidth, atlasHeight));
        }
    }

    private void safeSet(int x, int y, int argb) {
        if (x >= 0 && x < atlasWidth && y >= 0 && y < atlasHeight) {
            image.setRGB(x, y, argb);
        }
    }
}
