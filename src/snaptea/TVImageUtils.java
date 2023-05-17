package snaptea;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.typedarrays.Uint8ClampedArray;

/**
 * Image utils for TVImage.
 */
public class TVImageUtils {

    /**
     * Returns the decoded RGBA shorts of given image.
     */
    public static short[] getShortsRGBA(TVImage anImg)
    {
        // If HTMLImageElement, convert to canvas
        if (anImg._img != null) anImg.convertToCanvas();

        // Get image data and convert to bytes
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D) anImg._canvas.getContext("2d");
        ImageData idata = cntx.getImageData(0, 0, anImg.getPixWidth(), anImg.getPixHeight());
        Uint8ClampedArray ary8C = idata.getData();
        int len = ary8C.getLength();
        short[] rgba = new short[len];
        for (int i = 0; i < len; i++)
            rgba[i] = ary8C.get(i);
        return rgba;
    }

    /**
     * Sets the decoded RGBA shorts of given image.
     */
    public static void putShortsRGBA(TVImage anImg, short[] rgba)
    {
        // If HTMLImageElement, convert to canvas
        if (anImg._img != null) anImg.convertToCanvas();

        // Get image data and convert to bytes
        int len = rgba.length;
        Uint8ClampedArray ary8C = Uint8ClampedArray.create(len);
        for (int i = 0; i < len; i++) ary8C.set(i, rgba[i]);
        ImageData imageData = createImageData(ary8C, anImg.getPixWidth(), anImg.getPixHeight());
        CanvasRenderingContext2D renderContext2D = (CanvasRenderingContext2D) anImg._canvas.getContext("2d");
        renderContext2D.putImageData(imageData, 0, 0, 0, 0, anImg.getPixWidth(), anImg.getPixHeight());
    }

    /**
     * Returns the decoded Alpha bytes of given image.
     */
    public static short[] getShortsAlpha(TVImage anImg)
    {
        // If HTMLImageElement, convert to canvas
        if (anImg._img != null) anImg.convertToCanvas();

        // Get image data and convert to bytes
        CanvasRenderingContext2D renderContext2D = (CanvasRenderingContext2D) anImg._canvas.getContext("2d");
        ImageData imageData = renderContext2D.getImageData(0, 0, anImg.getPixWidth(), anImg.getPixHeight());
        Uint8ClampedArray ary8C = imageData.getData();
        int len = ary8C.getLength(), len2 = len / 4;
        short[] alpha = new short[len2];
        for (int i = 0, j = 3; i < len2; i++, j += 4)
            alpha[i] = ary8C.get(j);
        return alpha;
    }

    /**
     * Creates an ImageData for Uint8ClampedArray and image width/height.
     */
    @org.teavm.jso.JSBody(params = {"theAry", "aW", "aH"}, script = "return new ImageData(theAry, aW, aH);")
    static native ImageData createImageData(Uint8ClampedArray theAry, int aW, int aH);

    /**
     * Emboss a source image according to a bump map, both in ARGB integer array form.
     * Bump map is assumed to to be (2*radius x 2*radius) pixels larger than the source
     * to compensate for edge conditions of both the blur and the emboss convolutions.
     * Code adapted from Graphics Gems IV - Fast Embossing Effects on Raster Image Data (by John Schlag)
     */
    public static void emboss(short[] srcPix, short[] bumpPix, int srcW, int srcH, int radius, double azimuth, double altitude)
    {
        // Get basic info
        int rad = Math.abs(radius);
        int bumpW = srcW + 2 * rad;

        // Normalized light source vector
        double pixScale = 255.9;
        int Lx = (int) (Math.cos(azimuth) * Math.cos(altitude) * pixScale);
        int Ly = (int) (Math.sin(azimuth) * Math.cos(altitude) * pixScale);
        int Lz = (int) (Math.sin(altitude) * pixScale);

        // Constant z component of surface normal
        int Nz = 3 * 255 / rad, Nz2 = Nz * Nz;
        int NzLz = Nz * Lz, background = Lz;

        // Declare vars for source/bump offsets
        int soff = 0, boff = bumpW * rad + rad;

        // Shade the pixels based on bump height & light source location
        for (int y = 0; y < srcH; y++, boff += 2 * rad) {
            for (int x = 0; x < srcW; x++) {

                // Normal calculation from alpha sample of bump map of surrounding pixels
                int b_0_0 = bumpPix[boff - bumpW - 1], b_0_1 = bumpPix[boff - bumpW], b_0_2 = bumpPix[boff - bumpW + 1];
                int b_1_0 = bumpPix[boff - 1], b_1_2 = bumpPix[boff + 1];
                int b_2_0 = bumpPix[boff + bumpW - 1], b_2_1 = bumpPix[boff + bumpW], b_2_2 = bumpPix[boff + bumpW + 1];
                int Nx = b_0_0 + b_1_0 + b_2_0 - b_0_2 - b_1_2 - b_2_2;
                int Ny = b_2_0 + b_2_1 + b_2_2 - b_0_0 - b_0_1 - b_0_2;

                // If negative, negate everything
                if (radius < 0) {
                    Nx = -Nx;
                    Ny = -Ny;
                }

                // Calculate shade: If normal isn't normal, calculate shade
                int shade = background;
                if (Nx != 0 || Ny != 0) {
                    int NdotL = Nx * Lx + Ny * Ly + NzLz;
                    if (NdotL < 0) shade = 0;
                    else shade = (int) (NdotL / Math.sqrt(Nx * Nx + Ny * Ny + Nz2));
                }

                // scale each rgb sample by shade
                int r = (srcPix[soff] * shade) >> 8;
                srcPix[soff++] = (short) r;
                int g = (srcPix[soff] * shade) >> 8;
                srcPix[soff++] = (short) g;
                int b = (srcPix[soff] * shade) >> 8;
                srcPix[soff++] = (short) b;
                soff++;
                boff++;
            }
        }
    }
}