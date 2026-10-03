package com.local.simpleprint;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	private static final String PREFS = "simple_print";
	private static final String LAST_PRINTER = "last_printer";
	private final List<BluetoothDevice> devices = new ArrayList<>();
	private final ExecutorService worker = Executors.newSingleThreadExecutor();
	private Spinner printerSpinner;
	private Spinner sizeSpinner;
	private Spinner styleSpinner;
	private Button printButton;
	private TextView statusText;
	private TextView printerDetails;
	private EditText textInput;
	private CheckBox borderCheck;
	private ImageView previewImage;
	private LinearLayout printPage;
	private LinearLayout printerPage;

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_main);
		printerSpinner = findViewById(R.id.printerSpinner);
		sizeSpinner = findViewById(R.id.sizeSpinner);
		styleSpinner = findViewById(R.id.styleSpinner);
		printButton = findViewById(R.id.printButton);
		statusText = findViewById(R.id.statusText);
		printerDetails = findViewById(R.id.printerDetails);
		textInput = findViewById(R.id.textInput);
		borderCheck = findViewById(R.id.borderCheck);
		previewImage = findViewById(R.id.previewImage);
		printPage = findViewById(R.id.printPage);
		printerPage = findViewById(R.id.printerPage);

		sizeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
			new String[]{"Small", "Medium", "Large"}));
		sizeSpinner.setSelection(1);
		styleSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
			new String[]{"Regular", "Bold"}));

		findViewById(R.id.printTab).setOnClickListener(view -> showPrintPage());
		findViewById(R.id.printerTab).setOnClickListener(view -> showPrinterPage());
		findViewById(R.id.hideKeyboardButton).setOnClickListener(view -> hideKeyboard());
		findViewById(R.id.refreshPrintersButton).setOnClickListener(view -> loadPairedPrinters());
		printButton.setOnClickListener(this::printText);
		borderCheck.setOnCheckedChangeListener((button, checked) -> updatePreview());
		sizeSpinner.setOnItemSelectedListener(previewListener);
		styleSpinner.setOnItemSelectedListener(previewListener);
		textInput.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence text, int start, int before, int count) { updatePreview(); }
			@Override public void afterTextChanged(Editable text) {}
		});
		printerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < devices.size()) {
					BluetoothDevice device = devices.get(position);
					getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(LAST_PRINTER, device.getAddress()).apply();
					printerDetails.setText("Selected: " + safeName(device) + "\nAddress: " + device.getAddress() + "\nStatus: Paired");
				}
			}
			@Override public void onNothingSelected(AdapterView<?> parent) {}
		});

		showPrintPage();
		updatePreview();
		if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
			loadPairedPrinters();
		} else {
			requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST);
		}
	}

	private final AdapterView.OnItemSelectedListener previewListener = new AdapterView.OnItemSelectedListener() {
		@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updatePreview(); }
		@Override public void onNothingSelected(AdapterView<?> parent) {}
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
		super.onRequestPermissionsResult(requestCode, permissions, results);
		if (requestCode == BLUETOOTH_PERMISSION_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
			loadPairedPrinters();
		} else {
			showStatus("Bluetooth permission is required");
		}
	}

	private void showPrintPage() {
		printPage.setVisibility(View.VISIBLE);
		printerPage.setVisibility(View.GONE);
	}

	private void showPrinterPage() {
		hideKeyboard();
		printPage.setVisibility(View.GONE);
		printerPage.setVisibility(View.VISIBLE);
	}

	private void hideKeyboard() {
		InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (keyboard != null) keyboard.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
		textInput.clearFocus();
	}

	private int selectedTextSize() {
		switch (sizeSpinner.getSelectedItemPosition()) {
			case 0: return 24;
			case 2: return 42;
			default: return 32;
		}
	}

	private void updatePreview() {
		if (previewImage == null || sizeSpinner == null || styleSpinner == null || borderCheck == null || textInput == null) return;
		Bitmap preview = TextRenderer.renderBitmap(textInput.getText().toString(), selectedTextSize(),
			styleSpinner.getSelectedItemPosition() == 1, borderCheck.isChecked());
		previewImage.setImageBitmap(preview);
	}

	private void loadPairedPrinters() {
		BluetoothManager manager = getSystemService(BluetoothManager.class);
		BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
		if (adapter == null || !adapter.isEnabled()) {
			printerDetails.setText("Bluetooth is off");
			return;
		}
		Set<BluetoothDevice> bonded = adapter.getBondedDevices();
		devices.clear();
		devices.addAll(bonded);
		devices.sort(Comparator.comparing(device -> safeName(device).toLowerCase()));
		List<String> labels = new ArrayList<>();
		for (BluetoothDevice device : devices) labels.add(safeName(device));
		printerSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
		String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(LAST_PRINTER, "");
		int selection = -1;
		for (int i = 0; i < devices.size(); i++) {
			if (devices.get(i).getAddress().equals(saved)) { selection = i; break; }
			if (selection < 0 && safeName(devices.get(i)).toLowerCase().startsWith("x5h")) selection = i;
		}
		if (selection >= 0) printerSpinner.setSelection(selection);
		if (devices.isEmpty()) printerDetails.setText("No paired Bluetooth devices found");
	}

	private String safeName(BluetoothDevice device) {
		String name = device.getName();
		return name == null || name.isBlank() ? device.getAddress() : name;
	}

	private void printText(View ignored) {
		String content = textInput.getText().toString().trim();
		if (content.isEmpty()) { showStatus("Enter some text first"); return; }
		int index = printerSpinner.getSelectedItemPosition();
		if (index < 0 || index >= devices.size()) { showStatus("Choose a printer on the Printer tab"); return; }
		BluetoothDevice device = devices.get(index);
		int textSize = selectedTextSize();
		boolean bold = styleSpinner.getSelectedItemPosition() == 1;
		boolean border = borderCheck.isChecked();
		hideKeyboard();
		printButton.setEnabled(false);
		showStatus("Printing...");
		worker.execute(() -> {
			Bitmap bitmap = null;
			try {
				bitmap = TextRenderer.renderBitmap(content, textSize, bold, border);
				byte[] raster = TextRenderer.rasterize(bitmap);
				new TinyPrinter().print(device, raster, bitmap.getWidth(), bitmap.getHeight());
				runOnUiThread(() -> showStatus("Printed"));
			} catch (Exception error) {
				android.util.Log.e("SimplePrint", "Print failed", error);
				runOnUiThread(() -> showStatus("Failed: " + error.getClass().getSimpleName()));
			} finally {
				if (bitmap != null) bitmap.recycle();
				runOnUiThread(() -> printButton.setEnabled(true));
			}
		});
	}

	private void showStatus(String text) { statusText.setText(text); }

	@Override
	protected void onDestroy() {
		worker.shutdownNow();
		super.onDestroy();
	}
}
