package com.local.simpleprint;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
	private static final int BLUETOOTH_PERMISSION_REQUEST = 10;
	private final List<BluetoothDevice> devices = new ArrayList<>();
	private final ExecutorService worker = Executors.newSingleThreadExecutor();
	private Spinner printerSpinner;
	private Button printButton;
	private TextView statusText;
	private EditText textInput;
	private CheckBox borderCheck;

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_main);
		printerSpinner = findViewById(R.id.printerSpinner);
		printButton = findViewById(R.id.printButton);
		statusText = findViewById(R.id.statusText);
		textInput = findViewById(R.id.textInput);
		borderCheck = findViewById(R.id.borderCheck);
		printButton.setOnClickListener(this::printTest);
		if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
			loadPairedPrinters();
		} else {
			requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
		super.onRequestPermissionsResult(requestCode, permissions, results);
		if (requestCode == BLUETOOTH_PERMISSION_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
			loadPairedPrinters();
		} else {
			showStatus("Bluetooth permission is required");
		}
	}

	private void loadPairedPrinters() {
		BluetoothManager manager = getSystemService(BluetoothManager.class);
		BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
		if (adapter == null || !adapter.isEnabled()) {
			showStatus("Turn on Bluetooth first");
			return;
		}
		Set<BluetoothDevice> bonded = adapter.getBondedDevices();
		devices.clear();
		devices.addAll(bonded);
		devices.sort(Comparator.comparing(device -> safeName(device).toLowerCase()));
		List<String> labels = new ArrayList<>();
		for (BluetoothDevice device : devices) labels.add(safeName(device));
		printerSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
		for (int i = 0; i < devices.size(); i++) {
			if (safeName(devices.get(i)).toLowerCase().startsWith("x5h")) {
				printerSpinner.setSelection(i);
				break;
			}
		}
		showStatus(devices.isEmpty() ? "Pair the printer in Settings first" : "Ready");
	}

	private String safeName(BluetoothDevice device) {
		String name = device.getName();
		return name == null || name.isBlank() ? device.getAddress() : name;
	}

	private void printTest(View ignored) {
		String text = textInput.getText().toString().trim();
		if (text.isEmpty()) {
			showStatus("Enter some text first");
			return;
		}
		int index = printerSpinner.getSelectedItemPosition();
		if (index < 0 || index >= devices.size()) {
			showStatus("Select a paired printer");
			return;
		}
		BluetoothDevice device = devices.get(index);
		int selectedTextSize = 32;
		if (((android.widget.RadioButton) findViewById(R.id.sizeSmall)).isChecked()) selectedTextSize = 24;
		if (((android.widget.RadioButton) findViewById(R.id.sizeLarge)).isChecked()) selectedTextSize = 42;
		final int textSize = selectedTextSize;
		boolean bold = ((android.widget.RadioButton) findViewById(R.id.styleBold)).isChecked();
		boolean border = borderCheck.isChecked();
		printButton.setEnabled(false);
		showStatus("Printing...");
		worker.execute(() -> {
			try {
				byte[] raster = TextRenderer.render(text, textSize, bold, border);
				new TinyPrinter().print(device, raster, TextRenderer.WIDTH, raster.length / TextRenderer.WIDTH);
				runOnUiThread(() -> showStatus("Printed"));
			} catch (Exception error) {
				android.util.Log.e("SimplePrint", "Print failed", error);
				runOnUiThread(() -> showStatus("Failed: " + error.getClass().getSimpleName()));
			} finally {
				runOnUiThread(() -> printButton.setEnabled(true));
			}
		});
	}

	private void showStatus(String text) {
		statusText.setText(text);
	}

	@Override
	protected void onDestroy() {
		worker.shutdownNow();
		super.onDestroy();
	}
}
