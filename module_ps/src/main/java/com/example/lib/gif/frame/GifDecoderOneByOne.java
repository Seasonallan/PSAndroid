package com.example.lib.gif.frame;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Gif解码器
 * @author liao
 */
public class GifDecoderOneByOne extends Thread {

	public boolean isDestroy = false;

	private InputStream in;

	public int width; // full currentImage width
	public int height; // full currentImage height
	private boolean gctFlag; // global color.xml table used
	private int gctSize; // size of global color.xml table

	private int[] gct; // global color.xml table
	private int[] lct; // local color.xml table
	private int[] act; // active color.xml table

	private int bgIndex; // background color.xml index
	private int bgColor; // background color.xml
	private int lastBgColor; // previous bg color.xml
	private int pixelAspect; // pixel aspect ratio

	private boolean lctFlag; // local color.xml table flag
	private boolean interlace; // interlace flag
	private int lctSize; // local color.xml table size

	private int ix, iy, iw, ih; // current currentImage rectangle
	private int lrx, lry, lrw, lrh;

	private byte[] block = new byte[256]; // current data block
	private int blockSize = 0; // block size

	// last graphic control extension info
	private int dispose = 0;
	// 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
	private int lastDispose = 0;
	private boolean transparency = false; // use transparent color.xml
	private int delay = 0; // delay in milliseconds
	private int transIndex; // transparent color.xml index

	private static final int MaxStackSize = 4096;
	// max decoder pixel stack size

	// LZW decoder working arrays
	private short[] prefix;
	private byte[] suffix;
	private byte[] pixelStack;
	private byte[] pixels;

	private String strFileName = null;

	public void setGifImage(String strFileName) {
		this.strFileName = strFileName;
	}


	public void run() {
		gct = null;
		lct = null;
		try {
			in = new FileInputStream(strFileName);
			startReadBytes();
			if (in != null)
				in.close();
			in = null;
		} catch (Exception ex) {
		}
	}

	private void startReadBytes() throws Exception {
		readHeader();
		// read GIF file content blocks
		while (isDestroy == false) {
			int code = read();
			switch (code) {
				case 0x2C: // currentImage separator
					readImage();
					break;
				case 0x21: // extension
					code = read();
					switch (code) {
						case 0xf9: // graphics control extension
							readGraphicControlExt();
							break;
						case 0xff: // application extension
							readBlock();
							String app = "";
							for (int i = 0; i < 11; i++) {
								app += (char) block[i];
							}
							if (app.equals("NETSCAPE2.0")) {
								readNetscapeExt();
							} else {
								skip(); // don't care
							}
							break;
						default: // uninteresting extension
							skip();
					}
					break;
				case 0x3b: // terminator
					in = new FileInputStream(strFileName);
					readHeader();
					break;
				case 0x00: // bad byte, but keep going and see what happens
					break;
				default:
					break;
			}
		}
	}


	public int getDelay(){
		return delay;
	}

	Bitmap currentBitmap, nextBitmap;
	long currentTime;
	boolean empty;
	int position = -1;
	public Bitmap getFrame(int position) {
		this.position = position;
		if (currentBitmap == null){
			return null;
		}
		if (currentTime <= 0){
			currentTime = System.currentTimeMillis();
			return currentBitmap;
		}
		if (System.currentTimeMillis() - currentTime > delay){
			if (nextBitmap == null){
				notifyThread();
				return currentBitmap;
			}
			currentBitmap.recycle();
			currentBitmap = null;
			currentBitmap = nextBitmap;
			nextBitmap = null;
			currentTime = System.currentTimeMillis();
			notifyThread();
			return currentBitmap;
		}
		return currentBitmap;
	}

	private void notifyThread(){
		synchronized(this){
			notify();
		}
	}
	private void waitThread() throws InterruptedException {
		synchronized(this){
			wait();
		}
	}

	/**
	 */
	private void free() {
		if (in != null) {
			try {
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			in = null;
		}

		if (currentBitmap != null) {
			if (!currentBitmap.isRecycled()){
				currentBitmap.recycle();
			}
			currentBitmap = null;
		}
		if (nextBitmap != null) {
			if (!nextBitmap.isRecycled()){
				nextBitmap.recycle();
			}
			nextBitmap = null;
		}
	}

	public void release() {
		isDestroy = true;
		free();
	}


	private int[] dest = null;

	private void setPixels() throws Exception {
// int[] dest = new int[width * height];
		if (dest == null) {
			dest = new int[width * height];
		}

		// copy each source line to the appropriate place in the destination
		int pass = 1;
		int inc = 8;
		int iline = 0;
		for (int i = 0; i < ih; i++) {
			int line = i;
			if (interlace) {
				if (iline >= ih) {
					pass++;
					switch (pass) {
						case 2:
							iline = 4;
							break;
						case 3:
							iline = 2;
							inc = 4;
							break;
						case 4:
							iline = 1;
							inc = 2;
					}
				}
				line = iline;
				iline += inc;
			}
			line += iy;
			if (line < height) {
				int k = line * width;
				int dx = k + ix; // start of line in dest
				int dlim = dx + iw; // end of dest line
				if ((k + width) < dlim) {
					dlim = k + width; // past dest edge
				}
				int sx = i * iw; // start of line in source
				while (dx < dlim) {
					// map color.xml and insert in destination
					int index = ((int) pixels[sx++]) & 0xff;
					int c = act[index];
					if (c != 0) {
						dest[dx] = c;
					}
					dx++;
				}
			}
		}
		if (currentBitmap == null){
			currentBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);  // Config.RGB_565
		}else{
			nextBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);  // Config.RGB_565
			waitThread();
		}
	}


	private void decodeImageData() throws IOException {
		int NullCode = -1;
		int npix = iw * ih;
		int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

		if ((pixels == null) || (pixels.length < npix)) {
			pixels = new byte[npix]; // allocate new pixel array
		}
		if (prefix == null) {
			prefix = new short[MaxStackSize];
		}
		if (suffix == null) {
			suffix = new byte[MaxStackSize];
		}
		if (pixelStack == null) {
			pixelStack = new byte[MaxStackSize + 1];
		}
		// Initialize GIF data stream decoder.
		data_size = read();
		clear = 1 << data_size;
		end_of_information = clear + 1;
		available = clear + 2;
		old_code = NullCode;
		code_size = data_size + 1;
		code_mask = (1 << code_size) - 1;
		for (code = 0; code < clear; code++) {
			prefix[code] = 0;
			suffix[code] = (byte) code;
		}

		// Decode GIF pixel stream.
		datum = bits = count = first = top = pi = bi = 0;
		for (i = 0; i < npix;) {
			if (top == 0) {
				if (bits < code_size) {
					// Load bytes until there are enough bits for a code.
					if (count == 0) {
						// Read a new data block.
						count = readBlock();
						if (count <= 0) {
							break;
						}
						bi = 0;
					}
					datum += (((int) block[bi]) & 0xff) << bits;
					bits += 8;
					bi++;
					count--;
					continue;
				}
				// Get the next code.
				code = datum & code_mask;
				datum >>= code_size;
				bits -= code_size;

				// Interpret the code
				if ((code > available) || (code == end_of_information)) {
					break;
				}
				if (code == clear) {
					// Reset decoder.
					code_size = data_size + 1;
					code_mask = (1 << code_size) - 1;
					available = clear + 2;
					old_code = NullCode;
					continue;
				}
				if (old_code == NullCode) {
					pixelStack[top++] = suffix[code];
					old_code = code;
					first = code;
					continue;
				}
				in_code = code;
				if (code == available) {
					pixelStack[top++] = (byte) first;
					code = old_code;
				}
				while (code > clear) {
					pixelStack[top++] = suffix[code];
					code = prefix[code];
				}
				first = ((int) suffix[code]) & 0xff;
				// Add a new string to the string table,
				if (available >= MaxStackSize) {
					break;
				}
				pixelStack[top++] = (byte) first;
				prefix[available] = (short) old_code;
				suffix[available] = (byte) first;
				available++;
				if (((available & code_mask) == 0) && (available < MaxStackSize)) {
					code_size++;
					code_mask += available;
				}
				old_code = in_code;
			}

			// Pop a pixel off the pixel stack.
			top--;
			pixels[pi++] = pixelStack[top];
			i++;
		}
		for (i = pi; i < npix; i++) {
			pixels[i] = 0; // clear missing pixels
		}
	}

	private int read() throws IOException {
		return in.read();
	}

	private int readBlock() throws IOException {
		blockSize = read();
		int n = 0;
		if (blockSize > 0) {
			int count = 0;
			while (n < blockSize) {
				count = in.read(block, n, blockSize - n);
				if (count == -1) {
					break;
				}
				n += count;
			}
		}
		return n;
	}

	private int[] tab = new int[256];

	private int[] readColorTable(int ncolors) throws IOException {
		int nbytes = 3 * ncolors;
		// int[] tab = null;
		byte[] c = new byte[nbytes];
		int n = in.read(c);
		if (n < nbytes) {

		} else {
			// tab = new int[256]; // max size to avoid bounds checks
			int i = 0;
			int j = 0;
			while (i < ncolors) {
				int r = ((int) c[j++]) & 0xff;
				int g = ((int) c[j++]) & 0xff;
				int b = ((int) c[j++]) & 0xff;
				tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
		}
		return tab;
	}

	private void readGraphicControlExt() throws IOException {
		read(); // block size
		int packed = read(); // packed fields
		dispose = (packed & 0x1c) >> 2; // disposal method
		if (dispose == 0) {
			dispose = 1; // elect to keep old currentImage if discretionary
		}
		transparency = (packed & 1) != 0;
		delay = readShort() * 10; // delay in milliseconds
		if(delay == 0){
			delay = 100;
		}
		transIndex = read(); // transparent color.xml index
		read(); // block terminator
	}

	private void readHeader() throws Exception {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (char) read();
		}
		if (!id.startsWith("GIF")) {
			return;
		}
		readLSD();
		if (gctFlag) {
			gct = readColorTable(gctSize);
			bgColor = gct[bgIndex];
		}
	}

	private void readImage() throws Exception {
		ix = readShort(); // (sub)currentImage position & size
		iy = readShort();
		iw = readShort();
		ih = readShort();
		int packed = read();
		lctFlag = (packed & 0x80) != 0; // 1 - local color.xml table flag
		interlace = (packed & 0x40) != 0; // 2 - interlace flag
		// 3 - sort flag
		// 4-5 - reserved
		lctSize = 2 << (packed & 7); // 6-8 - local color.xml table size
		if (lctFlag) {
			lct = readColorTable(lctSize); // read table
			act = lct; // make local table active
		} else {
			act = gct; // make global table active
			if (bgIndex == transIndex) {
				bgColor = 0;
			}
		}
		int save = 0;
		if (transparency) {
			if (act != null && act.length > 0 && act.length > transIndex) {
				save = act[transIndex];
				act[transIndex] = 0; // set transparent color.xml if specified
			}
		}
		decodeImageData(); // decode pixel data
		skip();
		// url new currentImage to receive frame data
		//currentImage = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		// createImage(width, height);
		setPixels(); // transfer pixel data to currentImage

		if (transparency) {
			act[transIndex] = save;
		}
		resetFrame();

	}

	private void readLSD() throws IOException {
		// logical screen size
		width = readShort();
		height = readShort();
		// packed fields
		int packed = read();
		gctFlag = (packed & 0x80) != 0; // 1 : global color.xml table flag
		// 2-4 : color.xml resolution
		// 5 : gct sort flag
		gctSize = 2 << (packed & 7); // 6-8 : gct size
		bgIndex = read(); // background color.xml index
		pixelAspect = read(); // pixel aspect ratio
	}

	private void readNetscapeExt() throws IOException {
		do {
			readBlock();
			if (block[0] == 1) {
				// loop count sub-block
				int b1 = ((int) block[1]) & 0xff;
				int b2 = ((int) block[2]) & 0xff;
				//loopCount = (b2 << 8) | b1;
			}
		} while (blockSize > 0);
	}

	private int readShort() throws IOException {
		// read 16-bit value, LSB first
		int s = read();
		int f = read();
		int t = s | (f << 8);
		return t;
		//return read() | (read() << 8);
	}

	private void resetFrame() {
		lastDispose = dispose;
		lrx = ix;
		lry = iy;
		lrw = iw;
		lrh = ih;
		lastBgColor = bgColor;
		dispose = 0;
		transparency = false;
	//	delay = 0;
		lct = null;
	}

	/**
	 * Skips variable length blocks up to and including next zero length block.
	 */
	private void skip() throws IOException {
		do {
			readBlock();
		} while (blockSize > 0);
	}
}
