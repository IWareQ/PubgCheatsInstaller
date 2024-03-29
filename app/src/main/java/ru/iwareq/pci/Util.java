package ru.iwareq.pci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;

public class Util {

	private static final String TENCENT_PACKAGE = "com.tencent.ig";

	public static boolean isPubgInstalled(PackageManager packageManager) {
		try {
			packageManager.getPackageInfo(TENCENT_PACKAGE, 0);
			return true;
		} catch (PackageManager.NameNotFoundException exception) {
			return false;
		}
	}

	public static Intent createDeleteIntent() {
		var intent = new Intent(Intent.ACTION_DELETE);
		intent.setData(Uri.parse("package:" + TENCENT_PACKAGE));
		return intent;
	}

	public static Intent createInstallIntent(String apkPath) {
		var storage = Environment.getExternalStorageDirectory().toString().replace("file:///", "");
		var destination = storage + apkPath;

		var contentUri = FileProvider.getUriForFile(
				SafRootHelper.getContext(),
				BuildConfig.APPLICATION_ID + ".provider",
				new File(destination)
		);
		var install = new Intent(Intent.ACTION_VIEW);
		install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		install.setData(contentUri);
		return install;
	}

	public static Intent createAndroidRootIntent(boolean data) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			var scheme = "content://com.android.externalstorage.documents/document/primary%3AAndroid%2F" + (data ? "data" : "obb");

			var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(scheme));
			return intent;
		}

		return null;
	}

	public static void renameAllTo(boolean toOfficialName) {
		var dataDocument = DocumentFile.fromTreeUri(SafRootHelper.getContext(), SafRootHelper.getData());
		var obbDocument = DocumentFile.fromTreeUri(SafRootHelper.getContext(), SafRootHelper.getObb());
		if (dataDocument != null && obbDocument != null) {
			Util.renameTo(toOfficialName, dataDocument);
			Util.renameTo(toOfficialName, obbDocument);
		}
	}

	private static void renameTo(boolean toOfficialName, DocumentFile document) {
		var nextDocument = document.findFile(toOfficialName ? TENCENT_PACKAGE + "g" : TENCENT_PACKAGE);
		if (nextDocument != null) {
			nextDocument.renameTo(toOfficialName ? TENCENT_PACKAGE : TENCENT_PACKAGE + "g");
		}
	}
}
