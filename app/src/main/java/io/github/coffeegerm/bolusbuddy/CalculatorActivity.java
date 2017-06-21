package io.github.coffeegerm.bolusbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CalculatorActivity extends AppCompatActivity {

    private static final String TAG = "CalculatorActivity";

    Toolbar mToolbar;
    FloatingActionButton fab;
    Button carbsBarButton;
    // Shared Preferences cast globally for access in methods.
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    // MainActivity views
    private TextView carbsTv;
    private TextView insulinTv;
    // Carbs bar views
    private CardView carbsBar;
    private EditText carbsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        final View mView = this.findViewById(android.R.id.content);

        // SharedPreferences for storing and accessing user designated bolusRatio
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();

        // Sets up toolbar and hides title
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        carbsTv = (TextView) findViewById(R.id.carbs_tv);
        insulinTv = (TextView) findViewById(R.id.insulin_tv);

        // updates insulin to default values
        updateInsulin();

        // Carbs bar items
        carbsBar = (CardView) findViewById(R.id.carbs_bar);
        carbsEditText = (EditText) findViewById(R.id.carbs_bar_et);
        carbsBarButton = (Button) findViewById(R.id.carbs_bar_button);
        carbsBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredCarbs = carbsEditText.getText().toString();

                if (enteredCarbs.equals("")) {
                    // do nothing
                    Toast.makeText(CalculatorActivity.this, "Enter number of carbs eaten please", Toast.LENGTH_SHORT).show();
                } else {
                    carbsTv.setText(enteredCarbs);
                    carbsBar.setVisibility(View.GONE);
                    hideKeyboard(mView);
                }
                updateInsulin();
            }
        });

        // fab to show carbs bar
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FAB pressed");

                carbsBar.setVisibility(View.VISIBLE);
                carbsEditText.setText("");
                carbsEditText.requestFocus();
                showKeyboard();
            }
        });


    }

    // Method to update the insulin card
    public void updateInsulin() {
        float bolusRatio = prefs.getFloat("bolusRatio", 0.0f);
        if (bolusRatio == 0.0) {
            Log.i(TAG, "updateInsulin: No bolus ratio found");
            editor.putFloat("bolus_ratio", 10);
        } else {
            float currentCarbs = Float.parseFloat(carbsTv.getText().toString());
            float insulinNeeded = currentCarbs / bolusRatio;
            String insulinString = String.valueOf(insulinNeeded);
            insulinTv.setText(insulinString);
        }
    }

    // Sets up menu to allow for clicking the settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // On settings pressed shows entry to allow changing of bolus ratio
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.bolusRatio) {
            Log.i(TAG, "onOptionsItemSelected: Alert Dialog");
            showDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showKeyboard() {
        // InputMethodManager for manipulating keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(carbsEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideKeyboard(View view) {
        // InputMethodManager for manipulating keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void showDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.bolus_ratio_alert_dialog, null);
        final EditText etBolusRatio = dialogView.findViewById(R.id.dialogBolusRatio);
        Button cancelBtn = dialogView.findViewById(R.id.cancel_button);
        final Button saveBtn = dialogView.findViewById(R.id.save_button);
        mBuilder.setView(dialogView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etBolusRatio.getText().toString().equals("")) {
                    Log.i(TAG, "Nothing entered for bolus ratio");
                    dialog.dismiss();
                } else {
                    Log.i(TAG, "Bolus Ratio Saved");
                    float etBolusRatioChanged = Float.parseFloat(etBolusRatio.getText().toString());
                    editor.putFloat("bolusRatio", etBolusRatioChanged).apply();
                    dialog.dismiss();
                    updateInsulin();
                }
            }
        });
    }
}
