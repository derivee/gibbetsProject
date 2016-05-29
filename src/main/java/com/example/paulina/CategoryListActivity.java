package com.example.paulina;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CategoryListActivity extends Activity {

    private List<Category> data;
    public HashMap<Integer, Category> categories;
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("FIRST_RUN_INDICATOR", 0);
        readFromJsonFile();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ALREADY_RUN", true);
        editor.apply();


        setContentView(R.layout.main_layout);
        loadData();
        ListView categoriesList = (ListView) findViewById(R.id.mainList);
        ListAdapter categoriesAdapter = new CategoriesAdapter(this, data);
        categoriesList.setAdapter(categoriesAdapter);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent incomingIntent) {
        if (requestCode == 2) {
            if (incomingIntent != null) {
                Bundle b = incomingIntent.getExtras();
                String lastPlayedCategory = b.getString("category").toLowerCase();
                int i = 0;
                if (resultCode == RESULT_OK) {
                    while (!lastPlayedCategory.equals(categories.get(i).getCategory())) {
                        i++;
                    }
                    if (i < categories.size()) {
                        Category item = categories.get(i);
                        item.setNumberOfGuessedWords(item.getNumberOfGuessedWords() + 1); //found matching category
                        loadData();
                        ListView lv = (ListView) findViewById(R.id.mainList);
                        ListAdapter a = new CategoriesAdapter(this, data);
                        lv.setAdapter(a);
                    }

                }
            }
        }
    }


    private void readFromJsonFile() {

        SharedPreferences categoriesList = getSharedPreferences("categoriesList", 0);
        SharedPreferences.Editor catListEditor = categoriesList.edit();
        categories = new HashMap<>();
        InputStream inputStream = getResources().openRawResource(R.raw.json_categories);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("categories");
            for (int k = 0; k < jArray.length(); k++) {
                String catName = jArray.getJSONObject(k).getString("name").toLowerCase();
                boolean available = true;
                catListEditor.putBoolean(catName, available);
                SharedPreferences settings = getSharedPreferences("FIRST_RUN_INDICATOR", 0);
                boolean firstLaunch = !(settings.getBoolean("ALREADY_RUN", false));
                if (firstLaunch) {
                    catListEditor.putInt(catName + "int", 0);
                }

                SharedPreferences givenCategory = getSharedPreferences(catName.toLowerCase(), 0);
                SharedPreferences.Editor editor = givenCategory.edit();

                int guessedWords = categoriesList.getInt(catName + "int", 0);

                categories.put(k, new Category(catName, available,
                        jArray.getJSONObject(k).getJSONArray("wordsArray").length(), guessedWords));
                Gson gson = new Gson();
                String jsonFavorites = gson.toJson(jArray.get(k).toString());
                editor.putString(catName, jsonFavorites);
                editor.apply();
                catListEditor.apply();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadData() {
        data = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            Category c = new Category(categories.get(i).getCategory(), categories.get(i).isAvailable(),
                    categories.get(i).getNumberOfWords(), categories.get(i).getNumberOfGuessedWords());
            data.add(c);
        }
    }

}