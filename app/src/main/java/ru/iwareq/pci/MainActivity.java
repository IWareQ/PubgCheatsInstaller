package ru.iwareq.pci;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import ru.iwareq.pci.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private final ActivityResultLauncher<Intent> handleSelectedFile = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					var resultIntent = result.getData();
					if (resultIntent == null || resultIntent.getData() == null) {
						Log.d(TAG, "Result intent or data uri is null");
						return;
					}

					var uriData = resultIntent.getData();
					Log.e(TAG, String.valueOf(uriData));
				}
			});

	private final ActivityResultLauncher<Intent> handleObbUri = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				var resolver = this.getContentResolver();

				var uriData = result.getData().getData();
				var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
				resolver.takePersistableUriPermission(uriData, flags);
				this.registerSafRoot(uriData, false);
			});


	private final ActivityResultLauncher<Intent> handleDataUri = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				var resolver = this.getContentResolver();

				var uriData = result.getData().getData();
				var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
				resolver.takePersistableUriPermission(uriData, flags);

				var permissions = resolver.getPersistedUriPermissions();
				Log.e(TAG, "handleDataUri size: " + permissions.size());
				this.registerSafRoot(uriData, true);
			});


	private final ActivityResultLauncher<Intent> handleAllFilesAccesssPermission = this.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result ->
					this.checkAndroidRootPermissions());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		var binding = ActivityMainBinding.inflate(this.getLayoutInflater());
		this.setContentView(binding.getRoot());

		this.requestPermissions();

		//		binding.getObbAccess.setOnClickListener(listener -> this.startAndroidAccessDirectory("obb"));

		//		binding.getDataAccess.setOnClickListener(listener -> this.startAndroidAccessDirectory("data"));

		binding.selectApkFile.setOnClickListener(listener -> {
			var permissions = this.getContentResolver().getPersistedUriPermissions();
			Log.e(TAG, "size: " + permissions.size());
			//			rename(true);
			/*var apkPicker = new Intent(Intent.ACTION_GET_CONTENT);
			apkPicker.setType("application/*");
			this.handleSelectedFile.launch(apkPicker);*/
		});
	}

	private void requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
			try {
				var uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
				var intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
				this.handleAllFilesAccesssPermission.launch(intent);
			} catch (Exception exception) {
				var intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
				intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
				this.handleAllFilesAccesssPermission.launch(intent);
			}
		} else {
			ActivityCompat.requestPermissions(this, new String[]{
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
			}, 1);
		}

		this.checkAndroidRootPermissions();
	}

	private void checkAndroidRootPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			var permissions = this.getContentResolver().getPersistedUriPermissions();
			Log.e(TAG, "size: " + permissions.size());
			if (permissions.size() == 4) {
				SafRootHolder.setData(permissions.get(0).getUri());
				SafRootHolder.setRenamedData(permissions.get(1).getUri());

				SafRootHolder.setObb(permissions.get(2).getUri());
				SafRootHolder.setRenamedObb(permissions.get(3).getUri());
			} else {
				this.handleDataUri.launch(this.getAndroidRootIntent(true, true));
			}

			Log.e(TAG, String.valueOf(permissions));
		}
	}

	private void registerSafRoot(Uri uri, boolean data) {
	/*	var resolver = this.getContentResolver();

		var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
		resolver.takePersistableUriPermission(uri, flags);

*/
		var permissions = this.getContentResolver().getPersistedUriPermissions();
		Log.e(TAG, "registerSafRoot size: " + permissions.size());
		if (uri.toString().endsWith(".ig")) {
			if (data) {
				SafRootHolder.setData(uri);
				this.renameData(false);
				this.handleDataUri.launch(this.getAndroidRootIntent(true, false));
			} else {
				SafRootHolder.setObb(uri);
				this.renameObb(false);
				this.handleObbUri.launch(this.getAndroidRootIntent(false, false));
			}
		} else {
			if (data) {
				SafRootHolder.setRenamedData(uri);
				this.renameData(true);
				this.handleObbUri.launch(this.getAndroidRootIntent(false, true));
			} else {
				SafRootHolder.setRenamedObb(uri);
				this.renameObb(true);
			}
		}
	}

	private void renameData(boolean toOfficialName) {
		var treeUriData = !toOfficialName ? SafRootHolder.getData() : SafRootHolder.getRenamedData();
		var dataDocument = DocumentFile.fromTreeUri(this, treeUriData);
		if (dataDocument != null) {
			dataDocument.renameTo(toOfficialName ? "com.tencent.ig" : "com.tencent.igg");
		}
	}

	private void renameObb(boolean toOfficialName) {
		var treeUriObb = !toOfficialName ? SafRootHolder.getObb() : SafRootHolder.getRenamedObb();
		var obbDocument = DocumentFile.fromTreeUri(this, treeUriObb);
		if (obbDocument != null) {
			obbDocument.renameTo(toOfficialName ? "com.tencent.ig" : "com.tencent.igg");
		}
	}

	private Intent getAndroidRootIntent(boolean data, boolean official) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, this.getUriAndroidRoot(data, official));
			return intent;
		}

		return null;
	}

	private Uri getUriAndroidRoot(boolean data, boolean official) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			var manager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);

			var intent = manager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();

			var startDir = "Android/" + (data ? "data" : "obb") + (official ? "/com.tencent.ig" : "/com.tencent.igg");

			var uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");

			var scheme = uri.toString();

			Log.d(TAG, "INITIAL_URI scheme: " + scheme);

			scheme = scheme.replace("/root/", "/document/");

			startDir = startDir.replace("/", "%2F");

			scheme += "%3A" + startDir;

			uri = Uri.parse(scheme);

			Log.d(TAG, "uri: " + uri.toString());
			return (Uri) uri;
		}

		return Uri.EMPTY;
	}
}
