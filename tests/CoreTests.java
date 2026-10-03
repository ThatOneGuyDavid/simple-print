package com.local.simpleprint;

import java.util.Arrays;
import java.util.Random;

/** Run on the JVM without an Android device; checks real production geometry/packets. */
public final class CoreTests {
	static int checks;
	static void check(boolean condition, String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}
	static void rejected(Runnable action, String message) {
		try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; }
		throw new AssertionError(message);
	}
	public static void main(String[] args) {
		PrintGeometry normal = new PrintGeometry(100, 40, true, false);
		check(normal.left == 130 && normal.blockWidth == 124, "border centered");
		check(normal.padding == 12 && normal.outputHeight == 80, "equal padding plus feed margins");
		PrintGeometry plain = new PrintGeometry(100, 40, false, false);
		check(plain.left == 8 && plain.padding == 0, "borderless at eight pixels");
		PrintGeometry landscape = new PrintGeometry(1000, 80, true, true);
		check(landscape.left == 140 && landscape.outputHeight == 1040, "long rotated line along paper");
		PrintGeometry landscapePlain = new PrintGeometry(1000, 80, false, true);
		check(landscapePlain.left == 8 && landscapePlain.outputHeight == 1016, "borderless rotation");
		check(PrintGeometry.normalTextWidth(true) == 344, "border wrap width");
		check(PrintGeometry.normalTextWidth(false) == 368, "plain wrap width");
		new PrintGeometry(1000, 344, true, true);
		rejected(() -> new PrintGeometry(1000, 345, true, true), "too many lines rejected");
		rejected(() -> new PrintGeometry(20000, 50, false, true), "memory cap rejected, not scaled");
		rejected(() -> TinyPrinter.buildJob(new byte[8], 8, 1, false), "non-printer width rejected");
		byte[] pixels = new byte[384 * 451];
		Random rng = new Random(19);
		for (int x = 0; x < 384; x++) {
			pixels[384 + x] = 1;
			pixels[768 + x] = (byte) (x % 2);
		}
		for (int i = 1152; i < pixels.length; i++) pixels[i] = (byte) rng.nextInt(2);
		for (boolean rotated : new boolean[]{false, true}) {
			byte[] job = TinyPrinter.buildJob(pixels, 384, 451, rotated);
			int offset = 0, rows = 0, speeds = 0, rawRows = 0, rleRows = 0;
			while (offset < job.length) {
				check((job[offset] & 255) == 0x51 && (job[offset + 1] & 255) == 0x78, "frame header");
				int opcode = job[offset + 2] & 255;
				int size = (job[offset + 4] & 255) | ((job[offset + 5] & 255) << 8);
				byte[] data = Arrays.copyOfRange(job, offset + 6, offset + 6 + size);
				int crc = 0;
				for (byte b : data) {
					crc ^= b & 255;
					for (int i = 0; i < 8; i++) crc = ((crc << 1) ^ ((crc & 128) == 0 ? 0 : 7)) & 255;
				}
				check((job[offset + 6 + size] & 255) == crc && (job[offset + 7 + size] & 255) == 255, "frame CRC");
				if (opcode == 0xA4) check(Arrays.equals(data, new byte[]{0x35}), "darkness 5");
				if (opcode == 0xAF) check(Arrays.equals(data, new byte[]{0x1C, 0x25}), "energy 9500");
				if (opcode == 0xBD) speeds++;
				if (opcode == 0xA2 || opcode == 0xBF) {
					byte[] decoded = new byte[384];
					if (opcode == 0xA2) {
						rawRows++;
						check(data.length == 48, "raw width");
						for (int x = 0; x < 384; x++) decoded[x] = (byte) ((data[x / 8] >> (x % 8)) & 1);
					} else {
						rleRows++;
						int x = 0;
						for (byte b : data) {
							int count = b & 127;
							Arrays.fill(decoded, x, x + count, (byte) ((b & 128) == 0 ? 0 : 1));
							x += count;
						}
						check(x == 384, "RLE width");
					}
					byte[] expected = rows < 451
						? Arrays.copyOfRange(pixels, rows * 384, (rows + 1) * 384) : new byte[384];
					check(Arrays.equals(decoded, expected), "image preserved, extra feed completely white");
					rows++;
				}
				if (opcode == 0xA1) check(rows == (rotated ? 569 : 451), "extra feed precedes existing final feed");
				offset += size + 8;
			}
			check(rows == (rotated ? 569 : 451) && speeds == 3 && rawRows > 0 && rleRows > 0,
				"exact 118 extra rows only in rotated mode, complete job including periodic speed");
		}
		System.out.println("PASS: " + checks + " geometry, limit, CRC, darkness and raster checks");
	}
}
