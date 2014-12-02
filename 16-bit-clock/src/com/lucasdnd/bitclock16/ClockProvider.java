package com.lucasdnd.bitclock16;

import java.util.Calendar;

import com.lucasdnd.bitclock16.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.text.format.Time;
import android.widget.RemoteViews;

public class ClockProvider extends AppWidgetProvider {
	
	public static String CLOCK_UPDATE = "com.lucasdnd.bitclock16.CLOCK_UPDATE";
	public static String SWITCH_COLORS_ACTION = "com.lucasdnd.bitclock16.SWITCH_COLORS";
	private static int canvasSize = 384;
	private static int dotSize = 12;
	
	private static boolean isWhiteColor = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		super.onReceive(context, intent);

		// Get the widget manager and ids for this widget provider, then call the shared clock update method.
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    
	    // Clock Update Event
		if (CLOCK_UPDATE.equals(intent.getAction())) {
		    int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
		    for (int appWidgetID: ids) {
				updateClock(context, appWidgetManager, appWidgetID);
		    }
		}
		
		// Touch Event
		if(SWITCH_COLORS_ACTION.equals(intent.getAction())) {
			int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
		    for (int appWidgetID: ids) {
		    	isWhiteColor = !isWhiteColor;
		    	updateClock(context, appWidgetManager, appWidgetID);
		    }
		}
	}
	
	private PendingIntent createClockTickIntent(Context context) {
		Intent intent = new Intent(CLOCK_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
	}
	private PendingIntent createColorSwitchIntent(Context context) {
		Intent intent = new Intent(SWITCH_COLORS_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
	}
	
	@Override
	public void onDisabled(Context context) {
		
		super.onDisabled(context);
		
		// Stop the Timer
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockTickIntent(context));	
	}
	
	@Override 
	public void onEnabled(Context context) {
		
		super.onEnabled(context);
		
		// Create the Timer
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MILLISECOND, 1318);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1318, createClockTickIntent(context));
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i = 0; i < appWidgetIds.length; i++) {
			
			int appWidgetId = appWidgetIds[i];
			
			// Get the layout for the App Widget and attach an on-click listener to the button
			AppWidgetProviderInfo appInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
			RemoteViews views = new RemoteViews(context.getPackageName(), appInfo.initialLayout);
			
			// Update The clock label using a shared method
			updateClock(context, appWidgetManager, appWidgetId);
			
			// Touch Intent
			PendingIntent p = createColorSwitchIntent(context);
	        views.setOnClickPendingIntent(R.id.image, p);
	        
	        // Tell the AppWidgetManager to perform an update on the current app widget	        
	        appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	public static void updateClock(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		
		// Get a reference to our Remote View
		AppWidgetProviderInfo appInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		RemoteViews views = new RemoteViews(context.getPackageName(), appInfo.initialLayout);
		
		// Update the time text
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		int hour = today.hour;
		int minute = today.minute;
		int second = today.second;
		int time = second + (minute * 60) + (hour * 60 * 60);
		
		// Draw the dots
		Bitmap bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setStyle(Paint.Style.FILL);
		
		System.out.println("time = " + time);
		System.out.println("bits = " + Integer.toBinaryString(time));
		
		for (int i = 0; i < 16; i++) {
			if (((time >> i) & 1) == 1) {
				p.setColor(Color.argb(255, 242, 242, 242));
			} else {
				p.setColor(Color.argb(128, 64, 64, 64));
			}
			canvas.drawCircle(dotSize + ((dotSize * 10) * (i % 4)), dotSize + ((dotSize * 10) * ((i / 4) % 4)), dotSize, p);
		}
		
		views.setImageViewBitmap(R.id.image, bitmap); 		
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}
