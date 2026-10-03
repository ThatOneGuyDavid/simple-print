package com.local.simpleprint;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

final class TinyPrinter {
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final int CHUNK_SIZE = 180;
	private static final long CHUNK_DELAY_MS = 4;

	void print(BluetoothDevice device, byte[] pixels, int width, int height) throws Exception {
		byte[] job = buildJob(pixels, width, height);
		try (BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID)) {
			socket.connect();
			OutputStream output = socket.getOutputStream();
			for (int offset = 0; offset < job.length; offset += CHUNK_SIZE) {
				int count = Math.min(CHUNK_SIZE, job.length - offset);
				output.write(job, offset, count);
				output.flush();
				Thread.sleep(CHUNK_DELAY_MS);
			}
		}
	}

	static byte[] buildJob(byte[] pixels, int width, int height) {
		if (width != 384 || height < 1 || (long) width * height != pixels.length) throw new IllegalArgumentException("Invalid raster");
		ByteArrayOutputStream job = new ByteArrayOutputStream();
		write(job, packet(0xA4, new byte[]{0x35})); // Maximum darkness: level 5
		write(job, packet(0xAF, new byte[]{0x1C, 0x25})); // 9500: X6H/X5H reference profile
		write(job, packet(0xBE, new byte[]{0x01}));
		write(job, packet(0xBD, new byte[]{0x0A}));
		for (int row = 0; row < height; row++) {
			byte[] rle = rleLine(pixels, row * width, width);
			byte[] raw = packLine(pixels, row * width, width);
			write(job, rle.length <= raw.length ? packet(0xBF, rle) : packet(0xA2, raw));
			if ((row + 1) % 200 == 0) write(job, packet(0xBD, new byte[]{0x0A}));
		}
		write(job, packet(0xA1, new byte[]{(byte) 0x90, 0x00, 0x11}));
		write(job, packet(0xA3, new byte[]{0x00}));
		return job.toByteArray();
	}

	private static byte[] rleLine(byte[] pixels, int start, int width) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int color = pixels[start] == 0 ? 0 : 1;
		int run = 1;
		for (int x = 1; x < width; x++) {
			int next = pixels[start + x] == 0 ? 0 : 1;
			if (next == color) run++;
			else { writeRun(out, color, run); color = next; run = 1; }
		}
		writeRun(out, color, run);
		return out.toByteArray();
	}

	private static void writeRun(ByteArrayOutputStream out, int color, int count) {
		while (count > 127) { out.write((color << 7) | 127); count -= 127; }
		if (count > 0) out.write((color << 7) | count);
	}

	private static byte[] packLine(byte[] pixels, int start, int width) {
		byte[] packed = new byte[width / 8];
		for (int x = 0; x < width; x++) {
			if (pixels[start + x] != 0) packed[x / 8] |= (byte) (1 << (x % 8));
		}
		return packed;
	}

	private static byte[] packet(int command, byte[] payload) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0x51); out.write(0x78); out.write(command); out.write(0x00);
		out.write(payload.length & 0xFF); out.write((payload.length >> 8) & 0xFF);
		write(out, payload); out.write(crc8(payload)); out.write(0xFF);
		return out.toByteArray();
	}

	private static int crc8(byte[] data) {
		int crc = 0;
		for (byte value : data) {
			crc ^= value & 0xFF;
			for (int bit = 0; bit < 8; bit++) crc = (crc & 0x80) != 0 ? ((crc << 1) ^ 0x07) & 0xFF : (crc << 1) & 0xFF;
		}
		return crc;
	}

	private static void write(ByteArrayOutputStream out, byte[] data) {
		out.write(data, 0, data.length);
	}
}
