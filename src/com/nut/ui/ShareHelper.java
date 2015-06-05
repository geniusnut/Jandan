package com.nut.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import com.nut.Jandan.Utility.Utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yw07 on 15-6-5.
 */
public class ShareHelper {

	public static Intent createResolvedIntent(Intent intent, ResolveInfo info) {
		final Intent intentNew = new Intent(intent);
		intentNew.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
		intentNew.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		return intentNew;
	}

	public static boolean launchActivity(Context context, Intent intent) {
		try {
			context.startActivity(intent);
			return true;
		} catch (Throwable e) {
			//	showErrorMessage(context, e);
			return false;
		}
	}

	public static void launchResolvedActivity(Context context, Intent intent, ResolveInfo info) {
		launchActivity(context, createResolvedIntent(intent, info));
	}

	public static List<ResolveInfo> queryIntentActivities(Context context, Intent intent, int flags) {
		final PackageManager pm = context.getPackageManager();
		try {
			return pm.queryIntentActivities(intent, flags);
		} catch (RuntimeException e) {
			// ignore exception: Package manager has died
			return new ArrayList<ResolveInfo>();
		}
	}

	public interface OnActivityConfirm {
		public abstract void confirm(Intent intent, ResolveInfo info);
	}

	public static void showMenu(final Context context, final Intent intent, View anchor) {
		final List<ResolveInfo> items = queryIntentActivities(context, intent, 0);
		final String action = intent.getAction();

		final PackageManager pm = context.getPackageManager();
		final SharedPreferences pref = context.getSharedPreferences("activity_menu", 0);

		Utilities.safeSort(items, new Comparator<ResolveInfo>() {
			private final ResolveInfo.DisplayNameComparator mComparator = new ResolveInfo.DisplayNameComparator(pm);
			private final String mMyPackage = context.getPackageName();

			public int compare(ResolveInfo info1, ResolveInfo info2) {
				final boolean self1 = mMyPackage.equals(info1.activityInfo.packageName);
				final boolean self2 = mMyPackage.equals(info2.activityInfo.packageName);
				int ret = pref.getInt(info2.activityInfo.name, self2 ? 10 : 0) - pref.getInt(info1.activityInfo.name, self1 ? 10 : 0);
				if (ret == 0) {
					final int r1 = self1 ? 0 : 1;
					final int r2 = self2 ? 0 : 1;
					ret = r1 - r2;
					if (ret == 0)
						ret = mComparator.compare(info1, info2);
				}
				return ret;
			}
		});

		final MenuItem.OnMenuItemClickListener clickListener = new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				final ResolveInfo info = items.get(item.getItemId());
				// confirm.confirm(intent, info);
				launchResolvedActivity(context, intent, info);
				final int count = pref.getInt(info.activityInfo.name, 0);
				pref.edit().putInt(info.activityInfo.name, count + 1).commit();
				return false;
			}
		};

		int count = items.size();
		if (count < 1)
			return;
		PopupMenu menu = new PopupMenu(context, anchor);
		//menu.inflate(R.menu.popup);
		// use reflact to
		try {
			Field field = menu.getClass().getDeclaredField("mPopup");
			field.setAccessible(true);
			Object menuPopupHelper = field.get(menu);
			Class<?> cls = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
			Method method = cls.getDeclaredMethod("setForceShowIcon", new Class[]{boolean.class});
			method.setAccessible(true);
			method.invoke(menuPopupHelper, new Object[]{true});

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < count; i++) {
			final ResolveInfo info = items.get(i);
			CharSequence title = info.loadLabel(pm);
			if (title == null) title = info.activityInfo.name;
			final MenuItem item = menu.getMenu().add(Menu.NONE, i, Menu.NONE, title);


			item.setIcon(info.loadIcon(pm));
			item.setOnMenuItemClickListener(clickListener);
		}
		menu.show();
		// context.startActivity(Intent.createChooser(intent, "share"));
	}
}
