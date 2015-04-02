package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * User: ZhangYafei(261957725@qq.com)
 * Date: 2015-03-26
 * Time: 10:02
 */
public class NewActivity extends Activity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.testlayout);

        Button bt = (Button) findViewById(R.id.ok);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewActivity.this, MainActivity.class);
                startActivity(intent);
//                System.out.println("OK!!");
            }
        });

    }


}
