package pe.chalk.watermarker;

import pe.chalk.watermarker.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;


public class MainActivity extends Activity {
    private SystemUiHider mSystemUiHider;
    private ImageView imageContent;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable(){ @Override public void run(){ MainActivity.this.mSystemUiHider.hide(); } };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);
        if(this.getActionBar() != null) this.getActionBar().hide();

        final View contentView = this.findViewById(R.id.fullscreen_content);
        final View controlsView = this.findViewById(R.id.fullscreen_content_controls);

        this.imageContent = (ImageView) this.findViewById(R.id.image_content);

        this.mSystemUiHider = SystemUiHider.getInstance(this, contentView, SystemUiHider.FLAG_HIDE_NAVIGATION);
        this.mSystemUiHider.setup();
        this.mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            private int mControlsHeight;
            private int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
                    controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                } else {
                    if (this.mControlsHeight == 0) this.mControlsHeight = controlsView.getHeight();
                    if (this.mShortAnimTime == 0)
                        this.mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                    controlsView.animate().translationY(visible ? 0 : this.mControlsHeight).setDuration(this.mShortAnimTime);
                }

                if (visible) delayedHide(3000);
            }
        });

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.mSystemUiHider.toggle();
            }
        });

        this.findViewById(R.id.dummy_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MainActivity.this.delayedHide(3000);
                return false;
            }
        });

        final ImageView watermark = (ImageView) this.findViewById(R.id.watermark);
        ((SeekBar) this.findViewById(R.id.alpha_seek)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                watermark.setAlpha(progress / (seekBar.getMax() + 0f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        this.delayedHide(100);
    }

    private void delayedHide(int delayMillis){
        this.mHideHandler.removeCallbacks(this.mHideRunnable);
        this.mHideHandler.postDelayed(this.mHideRunnable, delayMillis);
    }

    public void selectImage(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        this.startActivityForResult(intent, 19132);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 19132 && resultCode == Activity.RESULT_OK){
            if(data == null) return;

            try{
                this.imageContent.setImageURI(data.getData());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
