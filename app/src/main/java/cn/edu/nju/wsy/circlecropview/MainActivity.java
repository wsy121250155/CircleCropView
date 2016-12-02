package cn.edu.nju.wsy.circlecropview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (CircleWindowView) findViewById(R.id.imageView);
        resultIv = (ImageView) findViewById(R.id.resultIv);
    }

    private CircleWindowView imageView;
    private ImageView resultIv;

    public void crop(View view) {
        resultIv.setImageBitmap(imageView.getCrop());
    }
}
