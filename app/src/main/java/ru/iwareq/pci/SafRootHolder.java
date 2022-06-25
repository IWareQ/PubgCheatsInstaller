package ru.iwareq.pci;

import android.net.Uri;

public class SafRootHolder {

	private static Uri data, renamedData;
	private static Uri obb, renamedObb;

	public static Uri getData() {
		return SafRootHolder.data;
	}

	public static void setData(Uri data) {
		SafRootHolder.data = data;
	}

	public static Uri getRenamedData() {
		return SafRootHolder.renamedData;
	}

	public static void setRenamedData(Uri renamedData) {
		SafRootHolder.renamedData = renamedData;
	}

	public static Uri getObb() {
		return SafRootHolder.obb;
	}

	public static void setObb(Uri obb) {
		SafRootHolder.obb = obb;
	}

	public static Uri getRenamedObb() {
		return SafRootHolder.renamedObb;
	}

	public static void setRenamedObb(Uri renamedObb) {
		SafRootHolder.renamedObb = renamedObb;
	}
}
