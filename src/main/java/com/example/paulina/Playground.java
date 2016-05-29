package com.example.paulina;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;


public class
Playground extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 5;
    private final Activity thisActivity = this;
    private final int NUMBER_OF_LETTERS = 26;
    private boolean[] charsAlreadyWritten = new boolean[Character.MAX_VALUE];
    private TextView categoryName;
    private TextView wordToGuess;
    private TextView infoTrials;
    private TextView infoUsedTrials;
    private Boolean TWO_PLAYERS_MODE;
    private ArrayList<SingleChar> singleWordList;
    private String singleWord;
    private String category;
    private boolean wordGuessed = false;
    private int nrOfTrials;
    private int uniqueLettersGuessed;
    private boolean GAME_FINISHED = false;
    private Integer mode; // EASY / MEDIUM / HARD


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);
        categoryName = (TextView) findViewById(R.id.textView);
        wordToGuess = (TextView) findViewById(R.id.textView2);
        infoTrials = (TextView) findViewById(R.id.textView4);
        infoUsedTrials = (TextView) findViewById(R.id.textView3);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ChickenButt.ttf");
        wordToGuess.setTypeface(font);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            singleWord = savedInstanceState.getString("singleWord");
            category = savedInstanceState.getString("category");
            nrOfTrials = Math.min(savedInstanceState.getInt("nrOfTrials"), NUMBER_OF_LETTERS - 1);
            uniqueLettersGuessed = savedInstanceState.getInt("uniqueLettersGuessed");
            charsAlreadyWritten = savedInstanceState.getBooleanArray("charsAlreadyWritten");
            singleWordList = savedInstanceState.getParcelableArrayList("singleWordList");
            TWO_PLAYERS_MODE = savedInstanceState.getBoolean("TWO_PLAYERS_MODE");
            setMode();
            adjustViewAfterRestored();

        } else {
            if (extras != null) {
                category = extras.getString("Category");
                if (category != null) {
                    if (category.equals("bluetooth")) {
                        TWO_PLAYERS_MODE = true;
                        String text = "Two players mode";
                        categoryName.setText(text);
                        singleWord = extras.getString("wordToGuess");
                        singleWordList = new ArrayList<>();
                        for (int j = 0; j < singleWord.length(); j++) {
                            char charToAdd = singleWord.charAt(j);
                            SingleChar c = new SingleChar(charToAdd == ' ', charToAdd); //space must be ignored
                            singleWordList.add(c);
                        }
                    } else {
                        TWO_PLAYERS_MODE = false;
                        findWordInCategory();
                        categoryName.setText(category.toUpperCase());

                    }
                    setMode();
                    setNumberOfTrials();
                    displayGameStatus();
                    SharedPreferences categoriesList = getSharedPreferences(category.toLowerCase(), 0);
                    categoriesList.getString(category.toLowerCase(), "");
                    categoryName.setTypeface(font);
                    drawWord();

                } else {
                    this.finish();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        Bundle b = new Bundle();
        b.putString("singleWord", singleWord);
        b.putString("category", category);
        b.putInt("nrOfTrials", nrOfTrials);
        b.putInt("uniqueLettersGuessed", uniqueLettersGuessed);
        b.putBooleanArray("charsAlreadyWritten", charsAlreadyWritten);
        b.putBoolean("TWO_PLAYERS_MODE", TWO_PLAYERS_MODE);
        b.putParcelableArrayList("singleWordList", singleWordList);
        savedInstanceState.putAll(b);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView iw = (ImageView) findViewById(R.id.imageView);
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iw.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category", category);
        setResult(wordGuessed ? RESULT_OK : RESULT_CANCELED, returnIntent);
        finish();
    }

    private void adjustViewAfterRestored() {

        categoryName.setText(category.toUpperCase());
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ChickenButt.ttf");
        categoryName.setTypeface(font);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int j = 0; j < singleWord.length(); j++) {
            SpannableString text = new SpannableString(Character.toString(singleWordList.get(j).getLetter()));
            int c2;
            if (singleWordList.get(j).getAvailable()) {
                c2 = Color.argb(0xff, 0x00, 0x00, 0x00);
            } else {
                c2 = Color.argb(0x00, 0x00, 0x00, 0x00);
            }
            text.setSpan(new ForegroundColorSpan(c2), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(text);
        }
        wordToGuess.setText(builder, TextView.BufferType.SPANNABLE);
        String s1 = "Number of trials:" + nrOfTrials;
        infoTrials.setText(s1);
        String s2 = "Number of used trials: " + uniqueLettersGuessed;
        infoUsedTrials.setText(s2);
//        (not supported for API 16)infoUsedTrials.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        infoTrials.setText(s1);
//        infoTrials.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);

        if (uniqueLettersGuessed > 0 && !GAME_FINISHED) {
            redrawNotGuessed();
        }

    }

    private void displayGameStatus() {
        String s1 = "Number of trials:" + nrOfTrials;
        infoTrials.setText(s1);
        String s2 = "Number of used trials: " + uniqueLettersGuessed;
        infoUsedTrials.setText(s2);
        infoUsedTrials.setText(s2);
        infoTrials.setText(s1);
    }

    private void showLooserView() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        Button bLeft = (Button) findViewById(R.id.button);
        bLeft.setClickable(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoserImage();
                replaceButtonFunctionality(false);

            }
        }, 2000);

    }

    private void replaceButtonFunctionality(boolean winGame) {
        Button bLeft = (Button) findViewById(R.id.button);
        if (TWO_PLAYERS_MODE) {
            bLeft.setText(R.string.come_back);
            bLeft.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent comeBack = new Intent(thisActivity, RootActivity.class);
                            comeBack.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            comeBack.putExtra("Category", category);
                            startActivity(comeBack);
                        }
                    }
            );
        } else {
            bLeft.setText(R.string.try_again);
            bLeft.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent tryAgain = new Intent(thisActivity, Playground.class);
                            tryAgain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            tryAgain.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            tryAgain.putExtra("Category", category);
                            startActivity(tryAgain);
                        }
                    }
            );
        }
        String winner = winGame ? "Winner" : "Looser";
        Button bRight = (Button) findViewById(R.id.button2);
        String text = winner + " " + getString(R.string.share);
        bRight.setText(text);
        bRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    private void showLoserImage() {
        ImageView iw = (ImageView) findViewById(R.id.imageView);
        int looserId = getResources().getIdentifier("looser", "drawable", getPackageName());
        Drawable value = getResources().getDrawable(looserId);
        iw.setImageDrawable(value);
    }

    private void setMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.mode = Integer.parseInt(preferences.getString("Difficulty", "0"));

    }

    private boolean searchLetter(char c) {
        boolean letterFound = false;
        for (int i = 0; i < singleWordList.size(); i++) {
            SingleChar letter = singleWordList.get(i);
            if (Character.toString(letter.getLetter()).toLowerCase().equals(Character.toString(c).toLowerCase())) {
                letter.setAvailable(true);
                letterFound = true;
            }
        }
        return letterFound;
    }

    private void drawWord() {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int j = 0; j < singleWord.length(); j++) {
            SpannableString text = new SpannableString(Character.toString(singleWordList.get(j).getLetter()));
            int c2;
            if (singleWordList.get(j).getAvailable()) {
                c2 = Color.argb(0xff, 0x00, 0x00, 0x00);
            } else {
                c2 = Color.argb(0x00, 0x00, 0x00, 0x00);
            }
            text.setSpan(new ForegroundColorSpan(c2), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(text);
        }
        wordToGuess.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public void insertLetterClicked(View v) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = thisActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.guess_dialog, null);
        alert.setView(view);

        EditText letterInsertion = (EditText) view.findViewById(R.id.wordToGuess);
        letterInsertion.setHintTextColor(Color.BLACK);
        letterInsertion.setFocusable(true);
        letterInsertion.setFocusableInTouchMode(true); // user touches widget on phone with touch screen
        letterInsertion.setClickable(true);


        final AlertDialog dialog = alert.create();

        letterInsertion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });


        letterInsertion.addTextChangedListener(new TextWatcher() {
            EditText letterInsertion = (EditText) view.findViewById(R.id.wordToGuess);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (letterInsertion.getText().toString().equals("Enter letter"))
                    letterInsertion.setText("");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (letterInsertion.getText().length() == 1) {
                    Button letterAccepted = (Button) findViewById(R.id.button);
                    letterAccepted.setEnabled(false);
                    String text = letterInsertion.getText().toString();
                    if (text.length() != 1) {
                        Toast.makeText(thisActivity.getBaseContext(), text + " too long",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (searchLetter(text.charAt(0))) {
                            drawWord();
                        } else {
                            if (!charsAlreadyWritten[text.charAt(0)]) {
                                uniqueLettersGuessed++;
                            }
                            redrawNotGuessed();
                        }
                        charsAlreadyWritten[text.charAt(0)] = true;
                        if (checkIfGameFinished() == 1 && !GAME_FINISHED) {
                            showWinnerView();
                        } else if (checkIfGameFinished() == -1) {
                            GAME_FINISHED = true;
                            showLooserView();
                            makeAllLettersAvailable();
                            drawWord();
                        }

                    }
                    displayGameStatus();
                    letterAccepted.setEnabled(true);
                    dialog.hide();
                }

            }
        });

        dialog.show();
    }

    public void guessClicked(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = thisActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.guess_dialog, null);
        alert.setView(view);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText et = (EditText) view.findViewById(R.id.wordToGuess);
                String sw = singleWord.toLowerCase();
                Editable text = et.getText();
                if (sw.equals(text.toString().trim().toLowerCase())) {
                    makeAllLettersAvailable();
                    showWinnerView();
                    drawWord();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Not correct", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled - come back to activity and continue guessing
            }
        });
        final AlertDialog dialog = alert.create();

        EditText et = (EditText) view.findViewById(R.id.wordToGuess);

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });


        dialog.show();

    }

    private void showWinnerImage() {
        ImageView iw = (ImageView) findViewById(R.id.imageView);
        int winnerId = getResources().getIdentifier("winner", "drawable", getPackageName());
        Drawable value = getResources().getDrawable(winnerId);
        iw.setImageDrawable(value);

    }

    private void makeAllLettersAvailable() {
        for (int i = 0; i < singleWordList.size(); i++) {
            SingleChar letter = singleWordList.get(i);
            letter.setAvailable(true);
        }

    }

    private int checkIfGameFinished() {
        int gameFinished = 1; //positive value means game won; negative means game lost
        for (int i = 0; i < singleWordList.size(); i++) {
            SingleChar letter = singleWordList.get(i);
            if (!letter.getAvailable()) {
                gameFinished = 0; //game not won, but not lost yet
            }
        }
        if (gameFinished == 0) {
            if (nrOfTrials <= uniqueLettersGuessed) {
                gameFinished = -1; // game lost
            }
        }
        return gameFinished;
    }

    private void showWinnerView() {
        GAME_FINISHED = true;
        wordGuessed = true;
        showWinnerImage();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);


        replaceButtonFunctionality(true);
        saveQtyGuessedToSP();
    }

    private void saveQtyGuessedToSP() {
        SharedPreferences categoriesList = getSharedPreferences("categoriesList", 0);
        SharedPreferences.Editor catListEditor = categoriesList.edit();
        int guessedWords = categoriesList.getInt(category.toLowerCase() + "int", 0);
        catListEditor.putInt(category + "int", guessedWords + 1);
        catListEditor.apply();
    }

    private void findWordInCategory() {

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
            int index = 0;
            String catName = jArray.getJSONObject(index).getString("name").toLowerCase();
            while (!catName.equals(category)) {
                index++;
                catName = jArray.getJSONObject(index).getString("name").toLowerCase();

            }
            JSONArray words = jArray.getJSONObject(index).getJSONArray("wordsArray");
            Random r = new Random(System.currentTimeMillis());
            int i = r.nextInt(words.length() - 1);
            singleWord = words.getJSONObject(i).getString("word");
            singleWordList = new ArrayList<>();

            for (int j = 0; j < singleWord.length(); j++) {
                char charToAdd = singleWord.charAt(j);
                SingleChar c = new SingleChar(charToAdd == ' ', charToAdd); //space must be ignored
                singleWordList.add(c);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNumberOfTrials() {
        int uniqueChars = countUniqueCharacters();
        if (mode == 0) {
            nrOfTrials = Math.min((int) (uniqueChars * 1.2), NUMBER_OF_LETTERS - 1);
        } else if (mode == 1) {
            nrOfTrials = Math.min((int) (uniqueChars * 0.7), NUMBER_OF_LETTERS - 1);
        } else if (mode == 2) {
            nrOfTrials = Math.min((int) (uniqueChars * 0.5), NUMBER_OF_LETTERS - 1);
        }

    }

    public int countUniqueCharacters() {
        boolean[] isItThere = new boolean[Character.MAX_VALUE];
        for (int i = 0; i < singleWord.length(); i++) {
            isItThere[singleWord.charAt(i)] = true;
        }
        int count = 0;
        for (boolean b : isItThere) {
            if (b) count++;
        }
        return count;
    }

    private void redrawNotGuessed() {
        ImageView myImageView = (ImageView) findViewById(R.id.imageView);
        int width = (int) (myImageView.getWidth() * 0.9);
        int height = myImageView.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.MAGENTA);
        myImageView.setImageBitmap(bitmap);
        Paint paintEye = new Paint(Paint.ANTI_ALIAS_FLAG);
        double ratioOfMistakes = uniqueLettersGuessed * 19.0 / nrOfTrials;
        int mistakes = (int) Math.ceil(ratioOfMistakes);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setShadowLayer(3, 4, 1, Color.parseColor("#eadb9f"));
        paint.setShader(new LinearGradient(0, 0, 0, height, Color.parseColor("#997a00"), Color.parseColor("#ffff00"), Shader.TileMode.CLAMP));
        paintEye.setColor(Color.BLACK);
        paintEye.setStrokeWidth(4);
        switch (mistakes) {
            case 19:

            case 18:

            case 17:

            case 16:
                canvas.drawLine(width * 80 / 100, height * 60 / 100, width * 85 / 100, height * 65 / 100, paint); //reka prawa

            case 15:
                canvas.drawLine(width * 80 / 100, height * 60 / 100, width * 75 / 100, height * 65 / 100, paint); //reka lewa

            case 14:
                canvas.drawLine(width * 80 / 100, height * 80 / 100, width * 90 / 100, height * 90 / 100, paint);//noga od gory w prawo

            case 13:
                canvas.drawLine(width * 80 / 100, height * 80 / 100, width * 70 / 100, height * 90 / 100, paint);//noga od gory w lewo

            case 12:
                canvas.drawLine(width * 80 / 100, height * 20 / 100 + 2 * (height * 10 / 100), width * 80 / 100, height * 80 / 100, paint); //tulow
            case 11:
                canvas.drawCircle(width * 80 / 100, height * 20 / 100 + (height * 10 / 100), height * 10 / 100, paint); //glowa

            case 10:
                canvas.drawLine(width * 80 / 100, height * 10 / 100, width * 80 / 100, height * 20 / 100, paint);//od stelazu do glowy

            case 9:
                canvas.drawLine(width * 50 / 100, height * 10 / 100, width * 80 / 100, height * 10 / 100, paint);// stelaz w prawo p3

            case 8:
                canvas.drawLine(width * 30 / 100, height * 10 / 100, width * 50 / 100, height * 10 / 100, paint);// stelaz w prawo p2

            case 7:
                canvas.drawLine(width * 10 / 100, height * 10 / 100, width * 30 / 100, height * 10 / 100, paint);// stelaz w prawo p1

            case 6:
                canvas.drawLine(width * 10 / 100, height * 90 / 100, width * 10 / 100, height * 10 / 100, paint);// stelaz p4

            case 5:
                canvas.drawLine(width * 10 / 100, height * 50 / 100, width * 10 / 100, height * 30 / 100, paint);// stelaz p3

            case 4:
                canvas.drawLine(width * 10 / 100, height * 70 / 100, width * 10 / 100, height * 50 / 100, paint);// stelaz p2

            case 3:
                canvas.drawLine(width * 10 / 100, height * 90 / 100, width * 10 / 100, height * 70 / 100, paint);// stelaz part 1

            case 2:
                canvas.drawLine(width * 10 / 100, height * 90 / 100, width * 20 / 100, height * 99 / 100, paint);//podnozek, w prawo od gory

            case 1: {
                canvas.drawLine(width * 10 / 100, height * 90 / 100, width / 100, height * 99 / 100, paint); //podnozek, w lewo od gory
                if (mistakes > 16) {
                    canvas.drawCircle(width * 80 / 100 - height * 3 / 100, height * 20 / 100 + (height * 10 / 100) - height * 5 / 100,
                            height * 2 / 100, paintEye); //oko lewe}

                    if (mistakes > 17) {
                        canvas.drawCircle(width * 80 / 100 + height * 3 / 100, height * 20 / 100 + (height * 10 / 100) - height * 5 / 100,
                                height * 2 / 100, paintEye);
                        if (mistakes > 18) {
                            canvas.drawLine(width * 80 / 100 - height * 3 / 100, height * 20 / 100 + (height * 10 / 100) + height * 5 / 100,
                                    width * 80 / 100 + height * 3 / 100, height * 20 / 100 + (height * 10 / 100) + height * 5 / 100, paintEye);
                        }
                    }
                }
            }
        }
    }

    private class SingleChar implements Parcelable {
        public final Creator<SingleChar> CREATOR = new Creator<SingleChar>() {
            public SingleChar createFromParcel(Parcel in) {
                return new SingleChar(in);
            }

            public SingleChar[] newArray(int size) {
                return new SingleChar[size];
            }
        };
        Boolean available;
        char letter;

        public SingleChar(Boolean available, char letter) {
            this.available = available;
            this.letter = letter;
        }

        private SingleChar(Parcel in) {
            available = in.readByte() != 0;
            letter = (char) in.readInt();
        }

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }

        public char getLetter() {
            return letter;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (available ? 1 : 0));
            dest.writeInt((int) letter);
        }
    }


}


