package com.example.paulina;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class RootActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button bStartGame;
        Button bTeacher;
        Button bStudent;
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ChickenButt.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        bStartGame = (Button) findViewById(R.id.imageButton2);
        bTeacher = (Button) findViewById(R.id.bluetooth_button_teacher);
        bStudent = (Button) findViewById(R.id.bluetooth_button_student);

        bStartGame.setTypeface(font);
        bTeacher.setTypeface(font);
        bStudent.setTypeface(font);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer timeCode = Integer.parseInt(preferences.getString("Notifications", "0"));

        inflater.inflate(R.menu.menu_root, menu);
        if (timeCode > 0) {
            scheduleNotification(getNotification(), timeCode);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(android.R.id.content, new PreferencesRootFragment());
            transaction.addToBackStack(null);
            transaction.commit();
                   }
        return true;
    }

    public void startOnePlayerMode(View v) {
        AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
        v.startAnimation(buttonClick);
        Intent i = new Intent(this, CategoryListActivity.class);
        startActivity(i);
    }


    public void connectViaBluetooth(View v) {
        Intent launchBluetoothActions = new Intent(this, TwoPlayersActivity.class);
        switch (v.getId()) {
            case R.id.bluetooth_button_teacher:
                launchBluetoothActions.putExtra("role", "teacher");
                break;
            case R.id.bluetooth_button_student:
                launchBluetoothActions.putExtra("role", "student");
                break;
            default:
                throw new RuntimeException("Unknown button ID");
        }
        startActivity(launchBluetoothActions);

    }


    private void scheduleNotification(Notification notification, int timeCode) {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        if (timeCode == 1) {
            cal.set(Calendar.HOUR_OF_DAY, 10);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 20);
        }
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, cal.getTimeInMillis(), 1000 * 24 * 60 * 60, pendingIntent);
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText("Daily delay notification");
        builder.setSmallIcon(R.drawable.ic_launcher);
        return builder.build();
    }

}