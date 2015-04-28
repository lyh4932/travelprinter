package com.travel.testdemo1;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import com.google.zxing.client.android.CaptureActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button BnScan = (Button) findViewById(R.id.twoscan);
        BnScan.setOnClickListener(new View.OnClickListener(){
         @Override
          public void onClick(View view){
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivity(intent);
           }
        }
         );

        Button BnPrint = (Button) findViewById(R.id.PrintTick);
        BnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConsoleActivity.class);
                startActivity(intent);
            }
        });
   
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
