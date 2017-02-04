package com.pdp11.qhacks.hackhouse;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    // list of items
    ArrayList<String> items;
    ItemAdapter adapter;
    ListView lView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        items = new ArrayList<>();
        adapter = new ItemAdapter(items, this);
        lView = (ListView) findViewById(R.id.item_list_view);
        lView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // add item to list
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pop-up dialog box for user to enter the to-do name
                final AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Enter Item");
                // Set up the input
                final EditText input = new EditText(ListActivity.this);
                // Specify the type of input expected
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // The next few lines gives the input area the focus so the user can type. It also brings up the keyboard.
                input.requestFocus();
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                // Set up the buttons
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        items.add(input.getText().toString());
                        adapter.notifyDataSetChanged();
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0); // Hides keyboard
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        dialog.cancel();    // User clicks cancel, dialog box goes away
                    }
                });
                builder.show(); // Shows the dialog box.
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



}
