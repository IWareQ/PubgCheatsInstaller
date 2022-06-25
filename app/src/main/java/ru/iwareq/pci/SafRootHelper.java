package ru.iwareq.pci;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

public class SafRootHelper {

	private static AppCompatActivity context;

	private static Uri data, obb;

	public static AppCompatActivity getContext() {
		return SafRootHelper.context;
	}

	public static void setContext(AppCompatActivity context) {
		SafRootHelper.context = context;
	}

	public static Uri getData() {
		return SafRootHelper.data;
	}

	public static void setData(Uri data) {
		SafRootHelper.data = data;
	}

	public static Uri getObb() {
		return SafRootHelper.obb;
	}

	public static void setObb(Uri obb) {
		SafRootHelper.obb = obb;
	}
}
