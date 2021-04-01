package com.js.androidmartialarts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity {




    enum State{SIGNUP,LOGIN}

    private State state;

    private EditText username,password,anonymous;
    private RadioButton passenger,driver;
    private RadioGroup radioGroup;
    private Button login,onetimelogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(ParseUser.getCurrentUser()!=null){
//            ParseUser.logOut();
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }


        username=findViewById(R.id.txtusername);
        password=findViewById(R.id.txtpassword);
        anonymous=findViewById(R.id.txtanonymous);
        passenger=findViewById(R.id.radiopassenger);
        driver=findViewById(R.id.radiodriver);
        radioGroup=findViewById(R.id.radioGroup);
        login=findViewById(R.id.loginbutton);
        onetimelogin=findViewById(R.id.btnonetime);

        state=State.SIGNUP;

        onetimelogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(anonymous.getText().toString().equals("Driver")||anonymous.getText().toString().equals("Passenger")){
                    if(ParseUser.getCurrentUser()==null){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user!=null && e==null){
                                    Toast.makeText(MainActivity.this, "We have anonymous user logged!", Toast.LENGTH_SHORT).show();
                                    user.put("as",anonymous.getText().toString());
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            transitionToPassengerActivity();
                                            transitionToDriverRequestListActivity();
                                        }
                                    });
                                }
                            }
                        });
                    } else{
                        Toast.makeText(MainActivity.this, "Another User Logged!Please wait...", Toast.LENGTH_SHORT).show();
                        ParseUser.logOut();
                    }
                } else{
                    Toast.makeText(MainActivity.this, "Are you a driver or passenger?", Toast.LENGTH_SHORT).show();
                }
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state==State.SIGNUP){
                    if(driver.isChecked()==false && passenger.isChecked()==false){

                        Toast.makeText(MainActivity.this, "Are you a driver or passenger", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ParseUser appUser=new ParseUser();
                    appUser.setUsername(username.getText().toString());
                    appUser.setPassword(password.getText().toString());
                    if(driver.isChecked()){
                        appUser.put("as","Driver");
                    } else if(passenger.isChecked()){
                        appUser.put("as","Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Toast.makeText(MainActivity.this, "Signed In successful", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();

                            }

                        }
                    });
                } else if(state==State.LOGIN){
                    if(driver.isChecked()==false && passenger.isChecked()==false){

                        Toast.makeText(MainActivity.this, "Are you a driver or passenger", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {

                            if(user!=null && e==null){
                                Toast.makeText(MainActivity.this, "User logged in successful", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();

                            }
                        }
                    });

                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.login_item:

                if(state==State.SIGNUP){
                    state=State.LOGIN;
                    item.setTitle("signup");
                    login.setText("login");

                } else if(state==State.LOGIN){

                    state=State.SIGNUP;
                    item.setTitle("login");
                    login.setText("sign up");

                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){

                Intent intent=new Intent(MainActivity.this,MapsActivity.class);
                startActivity(intent);

            }
        }
    }

    private void transitionToDriverRequestListActivity(){
        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent=new Intent(this,DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }

}