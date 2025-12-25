package com.boltstorms.phantomball.tools;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Android-safe GIF decoder (pure Java + libGDX Pixmap).
 * No javax.imageio, no java.awt.
 *
 * Usage from AndroidGifDecoder:
 *   LegacyGifDecoder.Result r = LegacyGifDecoder.decodeToFrames(is);
 *   for each r.frames[i], r.delays[i] -> build TextureRegions
 */
public final class LegacyGifDecoder {

    private LegacyGifDecoder() {}

    public static final class Result {
        public final Array<Pixmap> frames;
        public final Array<Float> delays; // seconds per frame

        public Result(Array<Pixmap> frames, Array<Float> delays) {
            this.frames = frames;
            this.delays = delays;
        }
    }

    /** Decode GIF into individual Pixmap frames + per-frame delay (seconds). */
    public static Result decodeToFrames(InputStream is) {
        Decoder dec = new Decoder();
        int status = dec.read(is);
        if (status != Decoder.STATUS_OK) {
            throw new RuntimeException("GIF decode failed. Status=" + status);
        }

        int n = dec.getFrameCount();
        Array<Pixmap> frames = new Array<>(n);
        Array<Float> delays = new Array<>(n);

        for (int i = 0; i < n; i++) {
            Pixmap pm = dec.getFrame(i);
            int ms = dec.getDelay(i); // milliseconds
            float sec = Math.max(0.01f, ms / 1000f);

            // IMPORTANT: clone the pixmap because decoder reuses internal buffers
            Pixmap copy = new Pixmap(pm.getWidth(), pm.getHeight(), Pixmap.Format.RGBA8888);
            copy.drawPixmap(pm, 0, 0);

            frames.add(copy);
            delays.add(sec);
        }

        return new Result(frames, delays);
    }

    // =====================================================================================
    // Internal decoder (adapted from the pure-Java/libGDX Pixmap-based GifDecoder you had)
    // =====================================================================================

    private static final class Decoder {

        /** File read status: No errors. */
        public static final int STATUS_OK = 0;
        /** File read status: Error decoding file (may be partially decoded) */
        public static final int STATUS_FORMAT_ERROR = 1;
        /** File read status: Unable to open source. */
        public static final int STATUS_OPEN_ERROR = 2;

        private static final int MAX_STACK_SIZE = 4096;

        private InputStream in;
        private int status;

        private int width;  // full image width
        private int height; // full image height

        private boolean gctFlag;
        private int gctSize;
        private int loopCount = 1;

        private int[] gct;
        private int[] lct;
        private int[] act;

        private int bgIndex;
        private int bgColor;
        private int lastBgColor;

        private int pixelAspect;

        private boolean lctFlag;
        private boolean interlace;
        private int lctSize;

        private int ix, iy, iw, ih;
        private int lrx, lry, lrw, lrh;

        private DixieMap image;
        private DixieMap lastPixmap;

        private final byte[] block = new byte[256];
        private int blockSize = 0;

        private int dispose = 0;
        private int lastDispose = 0;

        private boolean transparency = false;
        private int delay = 0;
        private int transIndex;

        private short[] prefix;
        private byte[] suffix;
        private byte[] pixelStack;
        private byte[] pixels;

        private Vector<GifFrame> frames;
        private int frameCount;

        private static final class DixieMap extends Pixmap {
            DixieMap(int w, int h, Pixmap.Format f) {
                super(w, h, f);
            }

            DixieMap(int[] data, int w, int h, Pixmap.Format f) {
                super(w, h, f);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int pxl_ARGB8888 = data[x + y * w];
                        int pxl_RGBA8888 =
                                ((pxl_ARGB8888 >> 24) & 0x000000ff) | ((pxl_ARGB8888 << 8) & 0xffffff00);
                        drawPixel(x, y, pxl_RGBA8888);
                    }
                }
            }

            void getPixelsInt(int[] out, int offset, int stride, int x, int y, int w, int h) {
                ByteBuffer bb = getPixels();
                for (int yy = y; yy < y + h; yy++) {
                    int _offset = offset;
                    for (int xx = x; xx < x + w; xx++) {
                        int pxl = bb.getInt(4 * (xx + yy * w));
                        out[_offset++] = ((pxl >> 8) & 0x00ffffff) | ((pxl << 24) & 0xff000000);
                    }
                    offset += stride;
                }
            }
        }

        private static final class GifFrame {
            final DixieMap image;
            final int delayMs;

            GifFrame(DixieMap im, int del) {
                image = im;
                delayMs = del;
            }
        }

        public int getDelay(int n) {
            int d = -1;
            if (n >= 0 && n < frameCount) d = frames.elementAt(n).delayMs;
            return d;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public DixieMap getFrame(int n) {
            if (frameCount <= 0) return null;
            n = n % frameCount;
            return frames.elementAt(n).image;
        }

        public int getLoopCount() {
            return loopCount;
        }

        public int read(InputStream is) {
            init();
            if (is != null) {
                in = is;
                readHeader();
                if (!err()) {
                    readContents();
                    if (frameCount < 0) status = STATUS_FORMAT_ERROR;
                }
            } else {
                status = STATUS_OPEN_ERROR;
            }

            try { is.close(); } catch (Exception ignored) {}
            return status;
        }

        private void init() {
            status = STATUS_OK;
            frameCount = 0;
            frames = new Vector<>();
            gct = null;
            lct = null;
        }

        private boolean err() {
            return status != STATUS_OK;
        }

        private int readByte() {
            int curByte = 0;
            try {
                curByte = in.read();
            } catch (Exception e) {
                status = STATUS_FORMAT_ERROR;
            }
            return curByte;
        }

        private int readBlock() {
            blockSize = readByte();
            int n = 0;
            if (blockSize > 0) {
                try {
                    int count;
                    while (n < blockSize) {
                        count = in.read(block, n, blockSize - n);
                        if (count == -1) break;
                        n += count;
                    }
                } catch (Exception e) {
                    status = STATUS_FORMAT_ERROR;
                }
                if (n < blockSize) status = STATUS_FORMAT_ERROR;
            }
            return n;
        }

        private int[] readColorTable(int ncolors) {
            int nbytes = 3 * ncolors;
            int[] tab = null;
            byte[] c = new byte[nbytes];
            int n = 0;
            try {
                n = in.read(c);
            } catch (Exception e) {
                status = STATUS_FORMAT_ERROR;
            }
            if (n < nbytes) {
                status = STATUS_FORMAT_ERROR;
            } else {
                tab = new int[256];
                int i = 0;
                int j = 0;
                while (i < ncolors) {
                    int r = (c[j++]) & 0xff;
                    int g = (c[j++]) & 0xff;
                    int b = (c[j++]) & 0xff;
                    tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }
            return tab;
        }

        private void readHeader() {
            StringBuilder id = new StringBuilder();
            for (int i = 0; i < 6; i++) id.append((char) readByte());
            if (!id.toString().startsWith("GIF")) {
                status = STATUS_FORMAT_ERROR;
                return;
            }
            readLSD();
            if (gctFlag && !err()) {
                gct = readColorTable(gctSize);
                bgColor = gct[bgIndex];
            }
        }

        private void readLSD() {
            width = readShort();
            height = readShort();
            int packed = readByte();
            gctFlag = (packed & 0x80) != 0;
            gctSize = 2 << (packed & 7);
            bgIndex = readByte();
            pixelAspect = readByte();
        }

        private int readShort() {
            return readByte() | (readByte() << 8);
        }

        private void readContents() {
            boolean done = false;
            while (!(done || err())) {
                int code = readByte();
                switch (code) {
                    case 0x2C:
                        readImage();
                        break;
                    case 0x21:
                        code = readByte();
                        switch (code) {
                            case 0xF9:
                                readGraphicControlExt();
                                break;
                            case 0xFF:
                                readBlock();
                                StringBuilder app = new StringBuilder();
                                for (int i = 0; i < 11; i++) app.append((char) block[i]);
                                if ("NETSCAPE2.0".contentEquals(app)) readNetscapeExt();
                                else skip();
                                break;
                            default:
                                skip();
                                break;
                        }
                        break;
                    case 0x3B:
                        done = true;
                        break;
                    case 0x00:
                    default:
                        status = STATUS_FORMAT_ERROR;
                        break;
                }
            }
        }

        private void readGraphicControlExt() {
            readByte(); // block size
            int packed = readByte();
            dispose = (packed & 0x1c) >> 2;
            if (dispose == 0) dispose = 1;
            transparency = (packed & 1) != 0;
            delay = readShort() * 10; // ms
            transIndex = readByte();
            readByte(); // terminator
        }

        private void readNetscapeExt() {
            do {
                readBlock();
                if (block[0] == 1) {
                    int b1 = block[1] & 0xff;
                    int b2 = block[2] & 0xff;
                    loopCount = (b2 << 8) | b1;
                }
            } while (blockSize > 0 && !err());
        }

        private void readImage() {
            ix = readShort();
            iy = readShort();
            iw = readShort();
            ih = readShort();
            int packed = readByte();

            lctFlag = (packed & 0x80) != 0;
            interlace = (packed & 0x40) != 0;
            lctSize = (int) Math.pow(2, (packed & 0x07) + 1);

            if (lctFlag) {
                lct = readColorTable(lctSize);
                act = lct;
            } else {
                act = gct;
                if (bgIndex == transIndex) bgColor = 0;
            }

            int save = 0;
            if (transparency && act != null) {
                save = act[transIndex];
                act[transIndex] = 0;
            }

            if (act == null) {
                status = STATUS_FORMAT_ERROR;
                return;
            }

            decodeImageData();
            skip();

            if (err()) return;

            frameCount++;

            image = new DixieMap(width, height, Pixmap.Format.RGBA8888);
            setPixels();

            frames.addElement(new GifFrame(image, delay));

            if (transparency && act != null) act[transIndex] = save;

            resetFrame();
        }

        private void decodeImageData() {
            int nullCode = -1;
            int npix = iw * ih;

            if (pixels == null || pixels.length < npix) pixels = new byte[npix];
            if (prefix == null) prefix = new short[MAX_STACK_SIZE];
            if (suffix == null) suffix = new byte[MAX_STACK_SIZE];
            if (pixelStack == null) pixelStack = new byte[MAX_STACK_SIZE + 1];

            int data_size = readByte();
            int clear = 1 << data_size;
            int end_of_information = clear + 1;
            int available = clear + 2;
            int old_code = nullCode;
            int code_size = data_size + 1;
            int code_mask = (1 << code_size) - 1;

            for (int code = 0; code < clear; code++) {
                prefix[code] = 0;
                suffix[code] = (byte) code;
            }

            int datum = 0, bits = 0, count = 0, first = 0, top = 0;
            int pi = 0, bi = 0;

            for (int i = 0; i < npix; ) {
                if (top == 0) {
                    if (bits < code_size) {
                        if (count == 0) {
                            count = readBlock();
                            if (count <= 0) break;
                            bi = 0;
                        }
                        datum += (block[bi] & 0xff) << bits;
                        bits += 8;
                        bi++;
                        count--;
                        continue;
                    }

                    int code = datum & code_mask;
                    datum >>= code_size;
                    bits -= code_size;

                    if (code > available || code == end_of_information) break;

                    if (code == clear) {
                        code_size = data_size + 1;
                        code_mask = (1 << code_size) - 1;
                        available = clear + 2;
                        old_code = nullCode;
                        continue;
                    }

                    if (old_code == nullCode) {
                        pixelStack[top++] = suffix[code];
                        old_code = code;
                        first = code;
                        continue;
                    }

                    int in_code = code;
                    if (code == available) {
                        pixelStack[top++] = (byte) first;
                        code = old_code;
                    }

                    while (code > clear) {
                        pixelStack[top++] = suffix[code];
                        code = prefix[code];
                    }

                    first = suffix[code] & 0xff;

                    if (available >= MAX_STACK_SIZE) break;

                    pixelStack[top++] = (byte) first;
                    prefix[available] = (short) old_code;
                    suffix[available] = (byte) first;
                    available++;

                    if ((available & code_mask) == 0 && available < MAX_STACK_SIZE) {
                        code_size++;
                        code_mask = (1 << code_size) - 1;
                    }

                    old_code = in_code;
                }

                top--;
                pixels[pi++] = pixelStack[top];
                i++;
            }

            for (int i = pi; i < npix; i++) pixels[i] = 0;
        }

        private void setPixels() {
            int[] dest = new int[width * height];

            if (lastDispose > 0) {
                if (lastDispose == 3) {
                    int n = frameCount - 2;
                    if (n > 0) lastPixmap = getFrame(n - 1);
                    else lastPixmap = null;
                }

                if (lastPixmap != null) {
                    lastPixmap.getPixelsInt(dest, 0, width, 0, 0, width, height);

                    if (lastDispose == 2) {
                        int c = transparency ? 0 : lastBgColor;
                        for (int i = 0; i < lrh; i++) {
                            int n1 = (lry + i) * width + lrx;
                            int n2 = n1 + lrw;
                            for (int k = n1; k < n2; k++) dest[k] = c;
                        }
                    }
                }
            }

            int pass = 1;
            int inc = 8;
            int iline = 0;

            for (int i = 0; i < ih; i++) {
                int line = i;

                if (interlace) {
                    if (iline >= ih) {
                        pass++;
                        switch (pass) {
                            case 2: iline = 4; break;
                            case 3: iline = 2; inc = 4; break;
                            case 4: iline = 1; inc = 2; break;
                        }
                    }
                    line = iline;
                    iline += inc;
                }

                line += iy;
                if (line < height) {
                    int k = line * width;
                    int dx = k + ix;
                    int dlim = dx + iw;
                    if (k + width < dlim) dlim = k + width;
                    int sx = i * iw;

                    while (dx < dlim) {
                        int index = pixels[sx++] & 0xff;
                        int c = act[index];
                        if (c != 0) dest[dx] = c;
                        dx++;
                    }
                }
            }

            image = new DixieMap(dest, width, height, Pixmap.Format.RGBA8888);
        }

        private void resetFrame() {
            lastDispose = dispose;
            lrx = ix;
            lry = iy;
            lrw = iw;
            lrh = ih;
            lastPixmap = image;
            lastBgColor = bgColor;
            dispose = 0;
            transparency = false;
            delay = 0;
            lct = null;
        }

        private void skip() {
            do {
                readBlock();
            } while (blockSize > 0 && !err());
        }
    }
}
