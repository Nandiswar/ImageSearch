package com.nandi.imagesearch;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.nandi.imagesearch.model.Data;
import com.nandi.imagesearch.network.CustomRequest;
import com.nandi.imagesearch.R;
import com.nandi.imagesearch.adapter.PhotoAdapter;
import com.nandi.imagesearch.model.DataList;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nandi_000 on 08-11-2015.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private CustomRequest req;
    private static final int PHOTO_WIDTH = 150;
    private static final int MIN_COLUMNS = 2;
    private static final int MAX_COLUMNS = 4;
    private PhotoAdapter photoAdapter;
    private GridView imageGrid;
    private int gridSize;
    private String input;
    private boolean handleOrientationChange;
    private DataList handleOrientationData;
    private AutoCompleteTextView autoCompleteTextView;
    private static List<String> searchList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews(); // initialize views
    }

    private void bindViews() {

        imageGrid = (GridView) findViewById(R.id.imagesGrid);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        // auto complete text view initialization
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.enterText);
        searchList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, searchList);
        adapter.setNotifyOnChange(true);
        autoCompleteTextView.setAdapter(adapter);

        gridSize = sizeColumnsToFit(imageGrid, PHOTO_WIDTH, MIN_COLUMNS, MAX_COLUMNS);
        searchButton.setOnClickListener(this);
    }

    /*
    @returns the column width dynamically based on device screens
     */
    private int sizeColumnsToFit(GridView imageGrid, int photoWidth, int minColumns, int maxColumns) {

        Display display = getWindowManager().getDefaultDisplay();

        int screenWidth;
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;

        int numColumns = screenWidth / photoWidth;
        numColumns = Math.min(numColumns, maxColumns);
        numColumns = Math.max(numColumns, minColumns);
        int remainingSpace = screenWidth - numColumns * photoWidth;
        int columnWidth = photoWidth + remainingSpace / numColumns;

        imageGrid.setNumColumns(numColumns);
        imageGrid.setColumnWidth(columnWidth);

        return columnWidth;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //getSpiceManager().execute(req, "github", DurationInMillis.ONE_MINUTE, new CustomRequestListener());
    }

    @Override
    public void onClick(View v) {
        // close keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // read the text entered in edit text
        input = autoCompleteTextView.getText().toString();
        if(input!=null && input.length()!=0 && !input.equals("")) {
            // create a request using input text
            req = new CustomRequest(0, input);
            // execute the service to fetch the data and tie up a custom request listener
            getSpiceManager().execute(req, input, DurationInMillis.ONE_MINUTE, new CustomRequestListener());

            //add input string to search list
            addToSearchList(input);
        }
    }

    /*
     * creating a list of strings entered by user
     */
    private void addToSearchList(String input) {
        if(!searchList.contains(input)) {
            searchList.add(input);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //write search list to file
        writeToFile();
    }

    private void writeToFile() {
        try {
            FileOutputStream output = openFileOutput("searchHistory.txt",MODE_PRIVATE);
            DataOutputStream dataOutputStream = new DataOutputStream(output);
            dataOutputStream.writeInt(searchList.size()); // Save line count
            for(String line : searchList) { // Save lines
                dataOutputStream.writeUTF(line);
            }
            dataOutputStream.flush(); // Flush stream ...
            dataOutputStream.close(); // ... and close.
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //read search  history from file
        searchList = readFile();
    }

    private List<String> readFile() {
        try {
            List<String> searchList = new ArrayList<String>();
            FileInputStream input = openFileInput("lines.txt"); // Open input stream
            DataInputStream dataInputStream = new DataInputStream(input);
            int prevSize = dataInputStream.readInt(); // Read line count
            for (int i=0;i<prevSize;i++) { // Read lines
                String line = dataInputStream.readUTF();
                searchList.add(line);
            }
            dataInputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            return searchList;
        }
        //return searchList;
    }

    @Override
    protected void onDestroy() {
        // delete the cache
        getSpiceManager().removeDataFromCache(DataList.class);
        super.onDestroy();
    }

    /*
     * Custom request listener invoked upon executing the api call
     */
    public final class CustomRequestListener implements RequestListener<DataList> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(MainActivity.this, "Please check your data connection!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(DataList contributors) {
            //Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
            updateView(contributors);
        }
    }

    /*
     * handle the device orientation
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(handleOrientationChange) {
            handleOrientationChange = false;
            updateView(handleOrientationData);
        }
    }

    /*
     * Update the gridview using the data collected and photo adpater
     */
    private void updateView(DataList contributors) {
        handleOrientationData = contributors;
        List<Data> contributorsData = contributors.data;
        List<Data> dataList = new ArrayList<Data>();
        handleOrientationChange = true;

        if(contributorsData.size() == 0) {
            Toast.makeText(MainActivity.this, "No images found. Please try again!", Toast.LENGTH_SHORT).show();
        } else {
            for(Data contributor: contributorsData) {
                String link = contributor.link;
                if(validate(link)) {
                    dataList.add(contributor);
                }
            }

            photoAdapter = new PhotoAdapter(this, R.layout.grid_view_item, dataList, gridSize, input);
            imageGrid.setAdapter(photoAdapter);
            imageGrid.setOnScrollListener(photoAdapter);
            imageGrid.setOnItemClickListener(photoAdapter);
        }
    }

    /*
     * check the image link if its empty and avoid gif images
     */
    public static boolean validate(String link) {
        if(link!=null && link.length()!=0 && !link.equals("")
                && (link.endsWith(".jpg")||link.endsWith(".png"))) {
            return true;
        }
        return false;
    }
}
