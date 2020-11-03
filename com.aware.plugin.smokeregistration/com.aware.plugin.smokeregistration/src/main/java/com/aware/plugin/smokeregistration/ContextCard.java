package com.aware.plugin.smokeregistration;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {

    public ContextCard() {
    }

    private ListView smoking_events;
    private SmokeEventsAdapter adapter;

    @Override
    public View getContextCard(final Context context) {
        //Load card layout
        View card = LayoutInflater.from(context).inflate(R.layout.smokeregistration_card, null);

        final Button register_smoking_event = (Button) card.findViewById(R.id.register_smoking_event);

        smoking_events = (ListView) card.findViewById(R.id.smoking_events_list);

        smoking_events.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true); //allow scrolling of this view
                return false;
            }
        });

        adapter = new SmokeEventsAdapter(context, context.getContentResolver().query(Uri.parse("content://" + context.getPackageName() + ".provider.smokeregistration/smoke_events"), null, null, null, Provider.Smoke_Events.DATE + " DESC, " + Provider.Smoke_Events.TIME + " DESC"), true);
        smoking_events.setAdapter(adapter);

        ViewGroup.LayoutParams params = smoking_events.getLayoutParams();
        params.height = 800;
        smoking_events.setLayoutParams(params);

        register_smoking_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerSmokeEvent = new Intent(context, DateAndTimePicker.class);
                registerSmokeEvent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(registerSmokeEvent);
            }
        });

        //Return the card to AWARE/apps
        return card;
    }

    private class SmokeEventsAdapter extends CursorAdapter {
        private Context mContext;

        SmokeEventsAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.smoking_event_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView label = (TextView) view.findViewById(R.id.smoking_event_label);
            final Button edit = (Button) view.findViewById(R.id.smoking_event_edit);
            final Button delete = (Button) view.findViewById(R.id.smoking_event_del);

            final String labeltxt = cursor.getString(cursor.getColumnIndex(Provider.Smoke_Events.DATE)) + "\n" + cursor.getString(cursor.getColumnIndex(Provider.Smoke_Events.TIME));
            label.setText(labeltxt);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editDateOrTime = new Intent(mContext, DateAndTimePicker.class);
                    editDateOrTime.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    editDateOrTime.putExtra(DateAndTimePicker.EXTRA_LABEL, labeltxt);
                    mContext.startActivity(editDateOrTime);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.getContentResolver().delete(Uri.parse("content://" + mContext.getPackageName() + ".provider.smokeregistration/smoke_events"), Provider.Smoke_Events.TIMESTAMP + " LIKE '" + labeltxt + "'", null);
                    adapter.changeCursor(mContext.getContentResolver().query(Uri.parse("content://" + mContext.getPackageName() + ".provider.smokeregistration/smoke_events"), null, null, null, Provider.Smoke_Events.TIMESTAMP + " ASC"));
                }
            });
        }
    }
}