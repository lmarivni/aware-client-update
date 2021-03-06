package com.aware.plugin.smokeregistration;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.fragment.app.FragmentActivity;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import java.sql.Date;
import java.util.Calendar;

public class DateAndTimePicker extends FragmentActivity {
    public static String EXTRA_LABEL = "label";

    TimePickerDialog time_picker;
    DatePickerDialog date_picker;
    Button btnOK, btnCancel;
    EditText time, date;

    String loadedDate = "";
    String loadedTime = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getStringExtra(EXTRA_LABEL) != null && getIntent().getStringExtra(EXTRA_LABEL).length() > 0) {
            final String lines[] = getIntent().getStringExtra(EXTRA_LABEL).split("\n");
            loadedDate = lines[0];
            loadedTime = lines[1];
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_register_smoking_event);

        btnOK = (Button) findViewById(R.id.btn_OK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create new value
                ContentValues data = new ContentValues();
                data.put(Provider.Smoke_Events.DEVICE_ID, Aware.getSetting(DateAndTimePicker.this, Aware_Preferences.DEVICE_ID));
                data.put(Provider.Smoke_Events.TIMESTAMP, System.currentTimeMillis());
                data.put(Provider.Smoke_Events.DATE_OF_SMOKE_EVENT, date.getText().toString());
                data.put(Provider.Smoke_Events.TIME_OF_SMOKE_EVENT, time.getText().toString());
                data.put(Provider.Smoke_Events.IS_DELETED, false);
                //Insert new value in database and view
                DateAndTimePicker.this.getContentResolver().insert(Provider.Smoke_Events.CONTENT_URI, data);
                //Check if edit operation
                if (loadedDate.length() > 0 && loadedTime.length() > 0){
                    //Delete old row from view
                    final int count = DateAndTimePicker.this.getContentResolver().delete(Provider.Smoke_Events.CONTENT_URI, Provider.Smoke_Events.DATE_OF_SMOKE_EVENT + " LIKE '" + loadedDate + "' AND " + Provider.Smoke_Events.TIME_OF_SMOKE_EVENT + " LIKE '" + loadedTime + "' AND " + Provider.Smoke_Events.IS_DELETED + " = 0", null);
                    //Insert new row in database with old data and is_deleted = 1
                    ContentValues data_old = new ContentValues();
                    data_old.put(Provider.Smoke_Events.DEVICE_ID, Aware.getSetting(DateAndTimePicker.this, Aware_Preferences.DEVICE_ID));
                    data_old.put(Provider.Smoke_Events.TIMESTAMP, System.currentTimeMillis());
                    data_old.put(Provider.Smoke_Events.DATE_OF_SMOKE_EVENT, loadedDate);
                    data_old.put(Provider.Smoke_Events.TIME_OF_SMOKE_EVENT, loadedTime);
                    data_old.put(Provider.Smoke_Events.IS_DELETED, true);
                    for (int i = count; i != 0; i--) {
                        DateAndTimePicker.this.getContentResolver().insert(Provider.Smoke_Events.CONTENT_URI, data_old);
                    }
                    //adapter.changeCursor(mContext.getContentResolver().query(Uri.parse("content://" + mContext.getPackageName() + ".provider.smokeregistration/smoke_events"), null, null, null, Provider.Smoke_Events.DATE + " DESC, " + Provider.Smoke_Events.TIME + " DESC"));
                }
                finish();
            }
        });

        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        time = (EditText) findViewById(R.id.editTime);
        final Calendar cldr = Calendar.getInstance();
        if (loadedTime.length() > 0) {
            time.setText(loadedTime);
        } else {
            int hour = cldr.get(Calendar.HOUR_OF_DAY);
            int minutes = cldr.get(Calendar.MINUTE);
            if (hour < 10) {
                if (minutes < 10) {
                    time.setText("0" + hour + ":0" + minutes);
                } else {
                    time.setText("0" + hour + ":" + minutes);
                }
            } else {
                if (minutes < 10) {
                    time.setText(hour + ":0" + minutes);
                } else {
                    time.setText(hour + ":" + minutes);
                }
            }
        }
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                time_picker = new TimePickerDialog(DateAndTimePicker.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                if (sHour < 10) {
                                    if (sMinute < 10) {
                                        time.setText("0" + sHour + ":0" + sMinute);
                                    } else {
                                        time.setText("0" + sHour + ":" + sMinute);
                                    }
                                } else {
                                    if (sMinute < 10) {
                                        time.setText(sHour + ":0" + sMinute);
                                    } else {
                                        time.setText(sHour + ":" + sMinute);
                                    }
                                }
                            }
                        }, hour, minutes, true);
                time_picker.show();
            }
        });

        date = (EditText) findViewById(R.id.editDate);
        if (loadedDate.length() > 0) {
            date.setText(loadedDate);
        } else {
            int year = cldr.get(Calendar.YEAR);
            int month = cldr.get(Calendar.MONTH) + 1;
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            date.setText(year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
        }
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                date_picker = new DatePickerDialog(DateAndTimePicker.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month = month+1;
                                date.setText(year + "-" + String.format("%02d", month) + "-" + String.format("%02d", dayOfMonth));
                            }
                        },  cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH));
                date_picker.show();
            }
        });
    }
}

