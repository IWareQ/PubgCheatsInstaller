package ru.iwareq.pci;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

public class Util {

	public static Intent createAndroidRootIntent(boolean data) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			var scheme = "content://com.android.externalstorage.documents/document/primary%3AAndroid%2F" + (data ? "data" : "obb");

			var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(scheme));
			return intent;
		}

		return null;
	}

	public static void renameData(boolean toOfficialName) {
		var document = DocumentFile.fromTreeUri(SafRootHelper.getContext(), SafRootHelper.getData());
		if (document != null) {
			Util.rename(toOfficialName, document);
		}
	}

	public static void renameObb(boolean toOfficialName) {
		var document = DocumentFile.fromTreeUri(SafRootHelper.getContext(), SafRootHelper.getObb());
		if (document != null) {
			Util.rename(toOfficialName, document);
		}
	}

	private static void rename(boolean toOfficialName, DocumentFile document) {
		var nextDocument = document.findFile(toOfficialName ? "com.tencent.igg" : "com.tencent.ig");
		if (nextDocument != null) {
			nextDocument.renameTo(toOfficialName ? "com.tencent.ig" : "com.tencent.igg");
		}
	}
}
