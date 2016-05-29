package com.example.paulina;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;


public class CategoriesAdapter extends ArrayAdapter<Category> {

    private Activity activity;

    public CategoriesAdapter(Activity context, List<Category> objects) {
        super(context, R.layout.app_item, 0, objects);
        activity = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Category ai = getItem(position);
        final Intent startIntent = new Intent(activity, Playground.class);

        View.OnClickListener onClicked = new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (ai.isAvailable()) {
                    startIntent.putExtra("Category", ai.getCategory());
                    activity.startActivityForResult(startIntent, 2);
                }
            }
        };

        View rootView = convertView;
        if (rootView == null) {
            rootView = activity.getLayoutInflater().inflate(R.layout.app_item, null);

        }
        rootView.setOnClickListener(onClicked);
        ImageView appIcon = (ImageView) rootView.findViewById(R.id.appIcon);
        TextView appLabel = (TextView) rootView.findViewById(R.id.appLabel);
        Typeface font = Typeface.createFromAsset(activity.getAssets(), "fonts/ChickenButt.ttf");
        appLabel.setTypeface(font);
        ProgressBar appRating = (ProgressBar) rootView.findViewById(R.id.progressbar1);
        appRating.setMax(ai.getNumberOfWords());
        appRating.setProgress(ai.getNumberOfGuessedWords());

        if (ai.isAvailable()) {
            int id = activity.getResources().getIdentifier(ai.getCategory().toLowerCase(), "drawable", activity.getPackageName());
            if (id != 0) {
                Drawable value = activity.getResources().getDrawable(id);
                if (value != null) appIcon.setImageDrawable(value);
            } else {
                setDefaultImage(appIcon);
            }

        } else {
            setDefaultImage(appIcon);
        }
        appLabel.setText(ai.getCategoryAlias().toUpperCase());
        appIcon.setOnClickListener(onClicked);

        return rootView;
    }

    private void setDefaultImage(ImageView appIcon) {
        int idDownload = activity.getResources().getIdentifier("download", "drawable", activity.getPackageName());
        Drawable value = activity.getResources().getDrawable(idDownload);
        if (value != null) appIcon.setImageDrawable(value);
    }
}
