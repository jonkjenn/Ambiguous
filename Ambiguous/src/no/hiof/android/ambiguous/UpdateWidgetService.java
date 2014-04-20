package no.hiof.android.ambiguous;

import java.util.Random;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	  private static final String LOG = "de.vogella.android.widget.example";
	  private SQLiteDatabase db;
	  private static int number = 0;

	  @Override
	  public void onStart(Intent intent, int startId) {
	    Log.i(LOG, "Called");
	    // create some random data
	    this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
	    Cursor c = db.rawQuery("SELECT win FROM Statistics WHERE id = " +
	    		"(SELECT id FROM Statistics ORDER BY id DESC LIMIT 1)", null);
	    if(c.moveToFirst()){
	    	number = c.getInt(c.getColumnIndex("win"));
	    }
	    c.close();

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
//	      // create some random data if 
//	      int number = (new Random().nextInt(100));

	      RemoteViews remoteViews = new RemoteViews(this
	          .getApplicationContext().getPackageName(),
	          R.layout.widget_layout);
	      Log.w("Widget", String.valueOf(number));
	      // Set the text
	      remoteViews.setTextViewText(R.id.widget_layout_textview,
	          "Total victories: " + String.valueOf(number));

	      // Register an onClickListener
	      Intent clickIntent = new Intent(this.getApplicationContext(),
	          MyWidgetProvider.class);

	      clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
	          allWidgetIds);

	      PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
	          PendingIntent.FLAG_UPDATE_CURRENT);
	      remoteViews.setOnClickPendingIntent(R.id.widget_layout_textview, pendingIntent);
	      appWidgetManager.updateAppWidget(widgetId, remoteViews);
	    }
	    stopSelf();

	    super.onStart(intent, startId);
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	    return null;
	  }
	} 
