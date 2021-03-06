package com.herenow.fase1.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;

import com.herenow.fase1.Activities.BrowserActivity;
import com.herenow.fase1.Activities.CardsActivity;
import com.herenow.fase1.Activities.ConnectToWifi;
import com.herenow.fase1.Activities.WeaconListActivity;
import com.herenow.fase1.BusStop.NewBusStop;
import com.herenow.fase1.R;
import com.herenow.fase1.Wifi.LogInManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import parse.WeaconParse;
import util.myLog;

/**
 * Created by Milenko on 17/07/2015.
 */
public abstract class Notifications {
    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
    public static HashMap<String, WeaconParse> weaconsLaunchedTable; //For not relaunche the same weacon {obid, WeaconParse}
    static String tag = "noti";
    private static ArrayList<WeaconParse> showedNotifications; //list of weacosn currently showed in a notification
    private static NotificationManager mNotificationManager;
    private static Context ctx;
    private static int mIdNoti = 103;

    private static int currentId = 1;
    private final static BroadcastReceiver receiverDeleteNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                myLog.add("<<<<<<<<<<<<Has borrado una notif>>>>", tag);
                currentId = currentId + 2;
                showedNotifications = new ArrayList<>();

            } catch (Exception e) {
                myLog.add("---ERROR<<<<<<<<<<<<Has borrado una notif>>>>" + e.getMessage());
            }
            context.unregisterReceiver(this);
        }
    };
    private static PendingIntent pendingDeleteIntent;

    public static void Initialize(Context act) {
        ctx = act;
        weaconsLaunchedTable = new HashMap<>();
        showedNotifications = new ArrayList<>(); //Weacons showed in notification
        mNotificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private static NotificationCompat.Builder buildSingleNotification
            (WeaconParse we, Intent resultIntent, PendingIntent resultPendingIntent, boolean sound, boolean anyFetchable) {
        NotificationCompat.Builder notif;

        Intent refreshIntent = new Intent("popo");
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(ctx, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);

        NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_silence, "Turn Off", resultPendingIntent);//TODO to create the silence intent

//        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);

        notif = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_stat_name_hn)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(we.getName())
                .setContentText(we.getType())
                .setAutoCancel(true)
                .setTicker("Weacon detected\n" + we.getName())
                .setDeleteIntent(pendingDeleteIntent)
                .addAction(actionSilence);

        if (anyFetchable) notif.addAction(actionRefresh);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }

        //Airport Buttons
        if (we.isAirport()) {
            Intent arrivalsIntent = new Intent(resultIntent);
            arrivalsIntent.putExtra("typeOfAiportCard", "Arrivals");
            PendingIntent pendingArrivals = PendingIntent.getActivity(ctx, 1, arrivalsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action DepartureAction = new NotificationCompat.Action(R.drawable.ic_flight_takeoff_white_24dp, "Departures", resultPendingIntent);
            NotificationCompat.Action ArrivalAction = new NotificationCompat.Action(R.drawable.ic_flight_land_white_24dp, "Arrivals", pendingArrivals);
            notif.addAction(DepartureAction)
                    .addAction(ArrivalAction);
        }

        //ZARA APP button
        if (we.getName().equals("ZARA")) {
            final String appPackageName = "com.inditex.zara"; // getPackageName() from Context or Activity object
            Intent getAppIntent;

            try {
                getAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            } catch (android.content.ActivityNotFoundException anfe) {
                getAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                myLog.add("app no found: " + anfe.getLocalizedMessage());
            }

            PendingIntent pendingGetApp = PendingIntent.getActivity(ctx, 1, getAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action getAppAction = new NotificationCompat.Action(R.drawable.ic_market, "Get App", pendingGetApp);
            notif.addAction(getAppAction);
        }

        //Call Waitress
        if (we.getType().equals("restaurant")) {
            //TODO replace
            Intent connectToWifi = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=hola"));
            PendingIntent pendingWIFIConnect = PendingIntent.getActivity(ctx, 1, connectToWifi, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action getAppAction = new NotificationCompat.Action(R.drawable.ic_waiter, "Waiter", pendingWIFIConnect);
            notif.addAction(getAppAction);
        }

        //WIFI APP button
        if (we.getName().startsWith("Conj")) {
            //TODO replace, doesn't do anything
//            Intent connectToWifi = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=hola"));_
            Intent connectToWifi = new Intent(ctx, ConnectToWifi.class);
            PendingIntent pendingWIFIConnect = PendingIntent.getService(ctx, 1, connectToWifi, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action getAppAction = new NotificationCompat.Action(R.drawable.ic_wifi, "Connect", pendingWIFIConnect);
            notif.addAction(getAppAction);
        }

        // Bus stop
        if (we.getType().equals("bus_stop")) {
//            formatter form = new formatter(we.getFetchedElements());
//            NewBusStop busStop = null;
//            if (we.near(parameters.stCugat, 20)) {
//                busStop = (NewBusStopStCg) we.getFetchedElements().get(0);
//            } else if (we.near(parameters.santiago, 20)) {
//                busStop = (NewBusStopSantiago) we.getFetchedElements().get(0);
//
//            }
//            notif.setContentText("BUS STOP. " + we.getOneLineSummary());
            NewBusStop busStop = (NewBusStop) we.getFetchedElements().get(0);
            notif.setContentText("BUS STOP. " + busStop.summarizeAllLines());

            //InboxStyle
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(we.getName());
            inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");

//            StringBuilder sb = new StringBuilder();
//            for (SpannableString s : form.summarizeByOneLine()) {
//                inboxStyle.addLine(s);
//                sb.append("   " + s + "\n");
//            }

            StringBuilder sb = new StringBuilder();
            for (SpannableString s : busStop.summarizeByOneLine()) {
                inboxStyle.addLine(s);
                sb.append("   " + s + "\n");
            }

            notif.setStyle(inboxStyle);
            myLog.notificationMultiple(we.getName(), sb.toString(), "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));
        } else {
            //Bigtext style
            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
            textStyle.setBigContentTitle(we.getName());
            textStyle.bigText(we.getMessage());
            textStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");
            notif.setStyle(textStyle);

            myLog.notificationMultiple(we.getName(), we.getMessage(), "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));

        }

        notif.setContentIntent(resultPendingIntent);

        return notif;
    }

    public static void showNotification(ArrayList<WeaconParse> notificables, boolean sound, boolean anyFetchable) {
        try {
            if (notificables.size() > 0) {
                if (notificables.size() == 1) {
                    sendOneWeacon(notificables.get(0), sound, anyFetchable);
                } else {
                    sendSeveralWeacons(notificables, sound, anyFetchable);
                }
            } else {
                mNotificationManager.cancel(mIdNoti);
                myLog.add("Borrada la notifcacion porque no estamos en área de ninguno.", "LIM");

            }
        } catch (Exception e) {
            myLog.add("---Errod en shownotif: " + e.getLocalizedMessage() + "\n" + Log.getStackTraceString(e));
        }
    }

    private static void sendSeveralWeacons(ArrayList<WeaconParse> notificables, boolean sound, boolean anyFetchable) {

        Intent refreshIntent = new Intent("popo");
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(ctx, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);

        NotificationCompat.Builder notif;
        Collections.reverse(notificables);

        Intent resultIntent;
        TaskStackBuilder stackBuilder;
        PendingIntent resultPendingIntent;

        String msg = Integer.toString(notificables.size()) + " weacons around you";

        notif = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_stat_name_dup)
                .setLargeIcon(notificables.get(0).getLogoRounded())
                .setContentTitle(msg)
                .setContentText(notificables.get(0).getName() + " and others.")
                .setAutoCancel(true)
                .setDeleteIntent(pendingDeleteIntent)
                .setTicker(msg);

        if (anyFetchable) notif.addAction(actionRefresh);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setVibrate(new long[]{0, 300, 150, 400, 100})
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
//            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(msg);
        inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");

        StringBuilder sb = new StringBuilder();
        for (WeaconParse weacon : notificables) {
            inboxStyle.addLine(weacon.getOneLineSummary());
            sb.append("  " + weacon.getOneLineSummary() + "\n");
        }

        notif.setStyle(inboxStyle);
        resultIntent = new Intent(ctx, WeaconListActivity.class);

        stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(WeaconListActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setContentIntent(resultPendingIntent);

        myLog.notificationMultiple(msg, sb.toString(), "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));
        mNotificationManager.notify(mIdNoti, notif.build());
    }

    public static void sendOneWeacon(WeaconParse we, boolean sound, boolean anyFetchable) {
        try {
            Intent resultIntent;
            TaskStackBuilder stackBuilder;
            PendingIntent resultPendingIntent;
            NotificationCompat.Builder notification;
            Class<?> cls;
            myLog.add("estanmos en send one weacon");
            if (we.isBrowser()) {
                cls = BrowserActivity.class;
            } else {
                myLog.add("Y vamos a usar la activudad cards activity");
                cls = CardsActivity.class;
            }

            resultIntent = new Intent(ctx, cls)
                    .putExtra("wUrl", we.getUrl())
                    .putExtra("wName", we.getName())
                    .putExtra("wLogo", we.getLogoRounded())
                    .putExtra("wComapanyDataObId", we.getCompanyDataObjectId())
                    .putExtra("wCards", we.getCards())
                    .putExtra("typeOfAiportCard", "Departures")
                    .putExtra("wWeaconObId", we.getObjectId());

            stackBuilder = TaskStackBuilder.create(ctx);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(resultIntent);
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT); //Todo solve the stack for going back from cards

            notification = buildSingleNotification(we, resultIntent, resultPendingIntent, sound, anyFetchable);
            mNotificationManager.notify(mIdNoti, notification.build());
        } catch (Exception e) {
            myLog.add("---error en send one notif" + e.getLocalizedMessage());
        }
    }

    public static void notifyContabilidad(String contabilidadString) {
        NotificationCompat.Builder notif;

        notif = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_media_pause)
//                .setLargeIcon(we.getLogoRounded())
                .setContentTitle("weacons table")
//                .setContentText(we.getType())
                .setAutoCancel(true)
                .setOngoing(false)
//                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
//                .setLights(0xE6D820, 300, 100)
                .setTicker("Weacons situation");
//                .setDeleteIntent(pendingDeleteIntent)
//                .addAction(actionSilence);
        //Bigtext style
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle("Weacon contabilidad");
        textStyle.bigText(contabilidadString);
        notif.setStyle(textStyle);

        mNotificationManager.notify(102, notif.build());

    }

}
