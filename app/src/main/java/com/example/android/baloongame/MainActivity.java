package com.example.android.baloongame;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.baloongame.utils.HighScoreHelper;
import com.example.android.baloongame.utils.SoundHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Balloon.BalloonListener{
    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 1000;
    private static final int MAX_ANIMATION_DURATION = 8000;
    private static final int NUMBER_OF_PINS = 5;
    private static final int BALLOONS_PER_LEVEL = 10;

    private ViewGroup mContentView;
    private int mScreenWidth, mScreenHeight;

    private TextView mLevelView, mScoreView;
    private Button mGoButton;
    private int[] colorArray = new int[3];

    private int mLevel,mScore, mPinsUsed ;
    private boolean mPlaying;
    private boolean mGameStopped = true;
    private List<ImageView> mPinImages = new ArrayList<>();
    private List<Balloon> mBalloons = new ArrayList<>();
    private int mBalloonsPopped;

    private SoundHelper mSoundHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);

        mContentView = (ViewGroup) findViewById(R.id.activity_main);
        setToFullScreen();

        final ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if(viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();
                }
            });
        }
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToFullScreen();
            }
        });

        colorArray[0] = Color.argb(255,255,0,0);
        colorArray[1] = Color.argb(255, 0 ,255, 0);
        colorArray[2] = Color.argb(255, 0, 0, 255);

        mScoreView = (TextView) findViewById(R.id.score_display);
        mLevelView = (TextView) findViewById(R.id.level_display);

        mPinImages.add((ImageView) findViewById(R.id.pushpin1));
        mPinImages.add((ImageView) findViewById(R.id.pushpin2));
        mPinImages.add((ImageView) findViewById(R.id.pushpin3));
        mPinImages.add((ImageView) findViewById(R.id.pushpin4));
        mPinImages.add((ImageView) findViewById(R.id.pushpin5));
        mGoButton = (Button) findViewById(R.id.go_button);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPlaying){
                    gameOver(false);
                }else if(mGameStopped){
                    startGame();
                }else {
                    startLevel();
                }
            }
        });
        updateDisplay();
        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);
    }

    private void setToFullScreen(){
        ViewGroup rootElement = (ViewGroup) findViewById(R.id.activity_main);
        rootElement.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSoundHelper.pauseMusic();
    }

    private void startGame(){
        mScore = 0;
        mLevel = 0;
        mPinsUsed = 0;
        for(ImageView pin : mPinImages){
            pin.setImageResource(R.drawable.pin);
        }
        mGameStopped = false;
        startLevel();
        mSoundHelper.playMusic();
    }

    private void startLevel(){
        mLevel++;
        updateDisplay();
        BalloonLauncher launcher = new BalloonLauncher();
        launcher.execute(mLevel);
        mPlaying = true;
        mBalloonsPopped = 0;
        mGoButton.setText("Stop game");
    }

    private void finishLevel(){
        Toast.makeText(this, String.format("You finished level %d",mLevel),Toast.LENGTH_SHORT).show();
        mPlaying = false;
        mGoButton.setText(String.format("Start level %d", mLevel+1));
    }

    private void gameOver(boolean allPinsUsed) {
        Toast.makeText(this, "Game over!", Toast.LENGTH_LONG).show();
        mSoundHelper.pauseMusic();
        for(Balloon balloon : mBalloons){
            mContentView.removeView(balloon);
            balloon.setPopped(true);
        }
        mBalloons.clear();
        mPlaying = false;
        mGameStopped = true;
        mGoButton.setText("Start game");

        if(allPinsUsed){
            if(HighScoreHelper.isTopScore(this, mScore)){
                HighScoreHelper.setTopScore(this, mScore);
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("High Score !");
                b.setMessage(String.format("Your new High score is : %d",mScore));
                b.setPositiveButton("OK",null);
                AlertDialog a = b.create();
                a.show();
            }
        }
    }

    private void launchBalloon(int x){
        Random r = new Random();
        Balloon b = new Balloon(MainActivity.this, colorArray[r.nextInt(3)]);
        mBalloons.add(b);
        b.setX(x);
        b.setY(mScreenHeight+b.getHeight());
        mContentView.addView(b);
        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel*1000));
        b.releaseBalloon(mScreenHeight, duration);
    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {
        mBalloonsPopped++;
        mSoundHelper.playSound();
        mContentView.removeView(balloon);
        mBalloons.remove(balloon);
        if(userTouch){
            mScore++;
        }else {
            mPinsUsed++;
            if(mPinsUsed<=mPinImages.size()){
                mPinImages.get(mPinsUsed-1).setImageResource(R.drawable.pin_off);
            }
            if(mPinsUsed == NUMBER_OF_PINS){
                gameOver(true);
                return;
            }else {
                Toast.makeText(this, "Missed that one", Toast.LENGTH_SHORT).show();
            }
        }
        updateDisplay();
        if(mBalloonsPopped == BALLOONS_PER_LEVEL){
            finishLevel();
        }
    }

    private void updateDisplay(){
        mScoreView.setText(String.valueOf(mScore));
        mLevelView.setText(String.valueOf(mLevel));
    }

    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void>{

        @Override
        protected Void doInBackground(Integer... params) {
            int level = params[0];
            int maxDelay = Math.max(MIN_ANIMATION_DELAY, (MAX_ANIMATION_DELAY-((level-1)*500)));
            int minDelay = maxDelay/2;
            int balloonLaunched = 0;
            while(mPlaying && balloonLaunched < BALLOONS_PER_LEVEL){
                Random random = new Random(new Date().getTime());
                int xPosition = random.nextInt(mScreenWidth-200);
                publishProgress(xPosition);
                balloonLaunched++;

            int delay = random.nextInt(minDelay)+minDelay;
                try{
                    Thread.sleep(delay);
                }catch (InterruptedException e){
                    e.printStackTrace();;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchBalloon(xPosition);
        }
    }

}
