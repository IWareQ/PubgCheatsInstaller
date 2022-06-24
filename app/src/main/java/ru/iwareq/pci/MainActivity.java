package ru.iwareq.pci;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import ru.iwareq.pci.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

	private final ActivityResultLauncher<Intent> openFileManagerLauncher = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					var data = result.getData().getData();
					Log.d("openFileManagerLauncher", data.toString());
				}
			});

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		var binding = ActivityMainBinding.inflate(this.getLayoutInflater());
		this.setContentView(binding.getRoot());

		binding.testButton.setOnClickListener(listener -> {
			var apkPicker = new Intent(Intent.ACTION_GET_CONTENT);
			apkPicker.setType("application/apk");
			this.openFileManagerLauncher.launch(apkPicker);
		});
	}
}
