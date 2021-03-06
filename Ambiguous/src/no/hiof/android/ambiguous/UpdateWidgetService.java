package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.MainActivity;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	private static final String LOG = "Widget Service";

	private static int number = 0;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG, "Called");

		number = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getInt("WIN", 0);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				MyWidgetProvider.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
		Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

		for (int widgetId : allWidgetIds) {
			;

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_layout);
			Log.w("Widget", String.valueOf(number));
			// Set the text
			remoteViews.setTextViewText(R.id.widget_layout_textview,
					"Total victories: " + String.valueOf(number));

			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(),
					MainActivity.class);
			clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			clickIntent.setAction(Intent.ACTION_MAIN);

			PendingIntent pendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 1, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			remoteViews.setOnClickPendingIntent(R.id.widget_layout_button,
					pendingIntent);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		stopSelf();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
