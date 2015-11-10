package com.nandi.imagesearch.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nandi.imagesearch.FullScreenImageActivity;
import com.nandi.imagesearch.MainActivity;
import com.nandi.imagesearch.model.Data;
import com.nandi.imagesearch.model.DataList;
import com.nandi.imagesearch.network.CustomRequest;
import com.nandi.imagesearch.service.CustomService;
import com.nandi.imagesearch.R;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nandi_000 on 08-11-2015.
 */
public class PhotoAdapter extends ArrayAdapter<Data> implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private List<Data> imagesDataList;
    private Context context;
    private final int layoutResource;
    private final int gridSize;
    private int lastScrollState = SCROLL_STATE_IDLE;
    SpiceManager spiceManager;
    private boolean scrollCheck = true;
    private String inputText;
    private int pageNo = 0;

    public PhotoAdapter(Context context, int resource, List<Data> dataList,
                        int gridSize, String input) {
        super(context, resource, dataList);
        this.context = context;
        this.layoutResource = resource;
        this.imagesDataList = dataList;
        this.gridSize = gridSize;
        this.spiceManager = new SpiceManager(CustomService.class);
        this.spiceManager.start(getContext());
        this.inputText = input;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Data imageData = imagesDataList.get(position);
        Uri uri = Uri.parse(imageData.link);
        Picasso.with(context).load(uri).placeholder(R.mipmap.ic_launcher).resize(gridSize,gridSize)
                .into(holder.imageView);
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //Toast.makeText(context, "state change End", Toast.LENGTH_SHORT).show();
        if (scrollState != SCROLL_STATE_FLING && lastScrollState == SCROLL_STATE_FLING
                && view.getLastVisiblePosition()==getCount()-1 && scrollCheck) {
            //Toast.makeText(context, "call", Toast.LENGTH_SHORT).show();
            // avoid duplicate calls
            scrollCheck = false;
            // call next page data
            makePaginationCall();
        }
        lastScrollState = scrollState;
    }

    private void makePaginationCall() {
        CustomRequest req = new CustomRequest(++pageNo, inputText);
        spiceManager.execute(req, inputText+pageNo, DurationInMillis.ONE_MINUTE, new PaginationRequestListener());
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    /*
     * Navigate to full screen activity open selecting the image
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Data data = imagesDataList.get(position);
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra("selectedImgData", data);
        context.startActivity(intent);
    }

    static class ViewHolder {
        ImageView imageView;
    }

    class PaginationRequestListener implements RequestListener<DataList> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(DataList dataList) {
            int size = dataList.data.size();
            // if size is 0, no more pages left
            if(size == 0) {
                scrollCheck = false;
            } else {
                List<Data> contributorsData = dataList.data;
                List<Data> newImageData = new ArrayList<Data>();
                for(Data contributor: contributorsData) {
                    String link = contributor.link;
                    if(MainActivity.validate(link)) {
                        newImageData.add(contributor);
                    }
                }
                imagesDataList.addAll(newImageData);
                // notify adapter of new data
                notifyDataSetChanged();
                scrollCheck = true;
            }
        }
    }
}
