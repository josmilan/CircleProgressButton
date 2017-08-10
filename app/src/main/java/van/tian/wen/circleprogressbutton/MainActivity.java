package van.tian.wen.circleprogressbutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircleProgressButton circleProgressButton = (CircleProgressButton) findViewById(R.id.circleProgressButton);
        CircleProgressButton circleProgressButton1 = (CircleProgressButton) findViewById(R.id.circleProgressButton1);

        circleProgressButton.setText("Hello");

        circleProgressButton1.setText("Hello");
        circleProgressButton1.setDrawArc(false);

    }
}
