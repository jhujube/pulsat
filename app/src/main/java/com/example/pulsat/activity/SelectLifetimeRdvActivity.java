package com.example.pulsat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulsat.R;

public class SelectLifetimeRdvActivity extends AppCompatActivity {
    private Integer lifetimeSelected;
    private RadioButton rb0,rb1,rb7,rb14,rb21,rb31,rb62,rb183,rb365;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        lifetimeSelected = data.getIntExtra("lifetimeSelected",0);
        setContentView(R.layout.activity_selectrdvlifetime);

        rb0 = findViewById(R.id.radioButton0);
        rb1 = findViewById(R.id.radioButton1);
        rb7 = findViewById(R.id.radioButton7);
        rb14 = findViewById(R.id.radioButton14);
        rb21 = findViewById(R.id.radioButton21);
        rb31 = findViewById(R.id.radioButton31);
        rb62 = findViewById(R.id.radioButton62);
        rb183 = findViewById(R.id.radioButton183);
        rb365 = findViewById(R.id.radioButton365);
        checkButton();


        Button bt_cancel = findViewById(R.id.bt_cancel);
        bt_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                finishActivity(Activity.RESULT_OK);
            }
        });
        Button bt_ok = findViewById(R.id.bt_ok);
        bt_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("rdvLifetime", lifetimeSelected);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        lifetimeSelected = 0;
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton1:
                if (checked)
                    lifetimeSelected = 1;
                    break;
            case R.id.radioButton7:
                if (checked)
                    lifetimeSelected = 7;
                    break;
            case R.id.radioButton14:
                if (checked)
                    lifetimeSelected = 14;
                break;
            case R.id.radioButton31:
                if (checked)
                    lifetimeSelected = 31;
                break;
            case R.id.radioButton62:
                if (checked)
                    lifetimeSelected = 62;
                break;
            case R.id.radioButton183:
                if (checked)
                    lifetimeSelected = 183;
                break;
            case R.id.radioButton365:
                if (checked)
                    lifetimeSelected = 365;
                break;
            case R.id.radioButton21:
                if (checked)
                    lifetimeSelected = 21;
                break;
        }
    }
    private void checkButton(){
        switch (lifetimeSelected){
            case 0:
                rb0.setChecked(true);
            break;
            case 1:
                rb1.setChecked(true);
                break;
            case 7:
                rb7.setChecked(true);
                break;
            case 14:
                rb14.setChecked(true);
                break;
            case 21:
                rb21.setChecked(true);
                break;
            case 31:
                rb31.setChecked(true);
                break;
            case 62:
                rb62.setChecked(true);
                break;
            case 183:
                rb183.setChecked(true);
                break;
            case 365:
                rb365.setChecked(true);
                break;
        }
    }
}
