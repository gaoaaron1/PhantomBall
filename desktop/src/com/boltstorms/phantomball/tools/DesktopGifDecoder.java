package com.boltstorms.phantomball.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DesktopGifDecoder implements IGifDecoder {

    @Override
    public GifDecoder.GIFAnimation decode(Animation.PlayMode playMode, InputStream is) {
        try {
            List<Frame> decoded = readGifWithDisposal(is);

            Array<TextureRegion> regions = new Array<>();
            Array<Texture> textures = new Array<>();
            float totalDelay = 0f;

            for (Frame f : decoded) {
                Pixmap pm = bufferedImageToPixmap(f.canvasSnapshot);

                Texture tex = new Texture(pm);
                tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

                textures.add(tex);
                regions.add(new TextureRegion(tex));

                totalDelay += f.delaySeconds;

                pm.dispose();
            }

            float frameDuration = decoded.size() > 0 ? totalDelay / decoded.size() : 0.1f;
            return new GifDecoder.GIFAnimation(regions, textures, frameDuration);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decode GIF (desktop)", e);
        }
    }

    // -------------------- compositing / disposal --------------------

    private static class Frame {
        BufferedImage canvasSnapshot;
        float delaySeconds;

        Frame(BufferedImage snapshot, float delaySeconds) {
            this.canvasSnapshot = snapshot;
            this.delaySeconds = delaySeconds;
        }
    }

    private static class GifMeta {
        int left, top, frameW, frameH;
        float delaySeconds;
        String disposal;
    }

    private static List<Frame> readGifWithDisposal(InputStream in) throws Exception {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) throw new IllegalStateException("No GIF ImageReader found.");

        ImageReader reader = readers.next();
        ImageInputStream stream = ImageIO.createImageInputStream(in);
        reader.setInput(stream, false);

        int numFrames = reader.getNumImages(true);

        int canvasW = -1, canvasH = -1;
        IIOMetadata streamMeta = reader.getStreamMetadata();
        if (streamMeta != null) {
            Node root = streamMeta.getAsTree(streamMeta.getNativeMetadataFormatName());
            int[] wh = parseLogicalScreenSize(root);
            canvasW = wh[0];
            canvasH = wh[1];
        }
        if (canvasW <= 0 || canvasH <= 0) {
            BufferedImage first = reader.read(0);
            canvasW = first.getWidth();
            canvasH = first.getHeight();
        }

        BufferedImage master = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMaster = master.createGraphics();
        gMaster.setComposite(AlphaComposite.SrcOver);

        List<Frame> out = new ArrayList<>();
        BufferedImage previousBackup = null;

        for (int i = 0; i < numFrames; i++) {
            BufferedImage frameImg = reader.read(i);
            GifMeta meta = parseFrameMeta(reader.getImageMetadata(i));

            if ("restoreToPrevious".equals(meta.disposal)) {
                previousBackup = deepCopy(master);
            } else {
                previousBackup = null;
            }

            gMaster.drawImage(frameImg, meta.left, meta.top, null);

            out.add(new Frame(deepCopy(master), meta.delaySeconds));

            if ("restoreToBackgroundColor".equals(meta.disposal)) {
                Graphics2D g = master.createGraphics();
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(meta.left, meta.top, meta.frameW, meta.frameH);
                g.dispose();
            } else if ("restoreToPrevious".equals(meta.disposal) && previousBackup != null) {
                Graphics2D g = master.createGraphics();
                g.setComposite(AlphaComposite.Src);
                g.drawImage(previousBackup, 0, 0, null);
                g.dispose();
            }
        }

        gMaster.dispose();
        stream.close();
        reader.dispose();

        return out;
    }

    private static int[] parseLogicalScreenSize(Node root) {
        int w = -1, h = -1;
        Node n = findNode(root, "LogicalScreenDescriptor");
        if (n != null) {
            NamedNodeMap a = n.getAttributes();
            Node wn = a.getNamedItem("logicalScreenWidth");
            Node hn = a.getNamedItem("logicalScreenHeight");
            if (wn != null) w = Integer.parseInt(wn.getNodeValue());
            if (hn != null) h = Integer.parseInt(hn.getNodeValue());
        }
        return new int[]{w, h};
    }

    private static GifMeta parseFrameMeta(IIOMetadata meta) {
        GifMeta m = new GifMeta();
        m.left = 0; m.top = 0; m.frameW = 0; m.frameH = 0;
        m.delaySeconds = 0.1f;
        m.disposal = "none";

        if (meta == null) return m;

        String fmt = meta.getNativeMetadataFormatName();
        Node root = meta.getAsTree(fmt);

        Node imgDesc = findNode(root, "ImageDescriptor");
        if (imgDesc != null) {
            NamedNodeMap a = imgDesc.getAttributes();
            Node ln = a.getNamedItem("imageLeftPosition");
            Node tn = a.getNamedItem("imageTopPosition");
            Node wn = a.getNamedItem("imageWidth");
            Node hn = a.getNamedItem("imageHeight");
            if (ln != null) m.left = Integer.parseInt(ln.getNodeValue());
            if (tn != null) m.top  = Integer.parseInt(tn.getNodeValue());
            if (wn != null) m.frameW = Integer.parseInt(wn.getNodeValue());
            if (hn != null) m.frameH = Integer.parseInt(hn.getNodeValue());
        }

        Node gce = findNode(root, "GraphicControlExtension");
        if (gce != null) {
            NamedNodeMap a = gce.getAttributes();
            Node delay = a.getNamedItem("delayTime");
            if (delay != null) {
                int cs = Integer.parseInt(delay.getNodeValue());
                m.delaySeconds = Math.max(0.01f, cs / 100f);
            }
            Node disp = a.getNamedItem("disposalMethod");
            if (disp != null) m.disposal = disp.getNodeValue();
        }

        return m;
    }

    private static Node findNode(Node root, String name) {
        if (root == null) return null;
        if (name.equals(root.getNodeName())) return root;
        for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
            Node hit = findNode(c, name);
            if (hit != null) return hit;
        }
        return null;
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return copy;
    }

    // -------------------- BufferedImage -> Pixmap --------------------

    private static Pixmap bufferedImageToPixmap(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xff;
                int r = (argb >>> 16) & 0xff;
                int g = (argb >>>  8) & 0xff;
                int b = (argb       ) & 0xff;

                int rgba = Color.rgba8888(r / 255f, g / 255f, b / 255f, a / 255f);

                // If you ever see upside-down on desktop, change to: pm.drawPixel(x, h - 1 - y, rgba);
                pm.drawPixel(x, y, rgba);
            }
        }

        return pm;
    }
}
