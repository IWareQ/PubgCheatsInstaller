package ru.iwareq.pci;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.iwareq.pci.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private final ActivityResultLauncher<Intent> handleDeleteApk = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				Util.renameData(true);
				Util.renameObb(true);

				this.startActivity(Util.createInstallIntent(SafRootHelper.getApkInstallPath()));
			});

	private final ActivityResultLauncher<Intent> handleSelectedFile = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					var resultIntent = result.getData();
					if (resultIntent == null || resultIntent.getData() == null) {
						Log.d(TAG, "Выбраный файл не может быть null");
						return;
					}

					SafRootHelper.setApkInstallPath(resultIntent.getData().getLastPathSegment().replace("primary:", "/"));

					Util.renameData(false);
					Util.renameObb(false);

					this.handleDeleteApk.launch(Util.createDeleteIntent());
				}
			});

	private final ActivityResultLauncher<Intent> handleObbUri = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				var uriObb = result.getData().getData();
				this.takeUriPermission(uriObb);
				SafRootHelper.setObb(uriObb);
			});

	private final ActivityResultLauncher<Intent> handleDataUri = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				var uriData = result.getData().getData();
				this.takeUriPermission(uriData);
				SafRootHelper.setData(uriData);

				this.handleObbUri.launch(Util.createAndroidRootIntent(false));
			});

	private final ActivityResultLauncher<Intent> handleAllFilesAccessPermission =
			this.registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(), result ->
							this.checkAndroidRootPermissions());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		var binding = ActivityMainBinding.inflate(this.getLayoutInflater());
		this.setContentView(binding.getRoot());

		if (!Util.isPubgInstalled(this.getPackageManager())) {
			new MaterialAlertDialogBuilder(this)
					.setTitle(this.getString(R.string.pubg_not_installed_title))
					.setMessage(this.getString(R.string.pubg_not_installed_text))
					.setPositiveButton(this.getString(R.string.pubg_not_installed_positive_text), (dialog, which) -> this.finish())
					.create().show();
		} else {
			this.requestPermissions();

			SafRootHelper.setContext(this);

			binding.selectApkFile.setOnClickListener(listener -> {
				var apkPicker = new Intent(Intent.ACTION_GET_CONTENT);
				apkPicker.setType("application/*");
				this.handleSelectedFile.launch(apkPicker);
			});
		}
	}

	private void requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (!Environment.isExternalStorageManager()) {
				try {
					var uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
					var intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
					this.handleAllFilesAccessPermission.launch(intent);
				} catch (Exception exception) {
					var intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
					intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
					this.handleAllFilesAccessPermission.launch(intent);
				}
			} else {
				this.checkAndroidRootPermissions();
			}
		} else {
			ActivityCompat.requestPermissions(this, new String[]{
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
			}, 1);
		}
	}

	private void checkAndroidRootPermissions() {
		var permissions = this.getContentResolver().getPersistedUriPermissions();
		if (permissions.size() == 2) {
			SafRootHelper.setData(permissions.get(0).getUri());
			SafRootHelper.setObb(permissions.get(1).getUri());
		} else {
			this.handleDataUri.launch(Util.createAndroidRootIntent(true));
		}
	}

	private void takeUriPermission(Uri uri) {
		var resolver = this.getContentResolver();

		var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
		resolver.takePersistableUriPermission(uri, flags);
	}
}
