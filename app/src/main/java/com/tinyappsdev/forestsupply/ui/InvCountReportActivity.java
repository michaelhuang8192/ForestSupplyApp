package com.tinyappsdev.forestsupply.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinyappsdev.forestsupply.AppGlobal;
import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.data.InventoryCount;
import com.tinyappsdev.forestsupply.data.InventoryCountEmployee;
import com.tinyappsdev.forestsupply.data.Product;
import com.tinyappsdev.forestsupply.data.User;
import com.tinyappsdev.forestsupply.helper.TinyMap;
import com.tinyappsdev.forestsupply.helper.TinyUtils;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.provider.CalendarContract.CalendarCache.URI;

public class InvCountReportActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter mAdapter;
    private long mInvCountId;
    private int mTeamId;
    private String mInvCountName;
    private boolean mShowUnmatched;
    private List<Map> mRecords;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_count_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mInvCountId = bundle.getLong("SessionId");
            mInvCountName = bundle.getString("SessionName");
            mTeamId = bundle.getInt("TeamId");
            getSupportActionBar().setTitle("Report - " + mInvCountName);
        }
        if(savedInstanceState != null)
            mShowUnmatched = savedInstanceState.getBoolean("mShowUnmatched");

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MyRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        loadRecords();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mShowUnmatched", mShowUnmatched);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inv_count_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_refresh: {
                loadRecords();
                return true;
            }
            case R.id.showALL: {
                mShowUnmatched = false;
                item.setChecked(true);
                showRecords();
                return true;
            }
            case R.id.showUnmatched: {
                mShowUnmatched = true;
                item.setChecked(true);
                showRecords();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected List<Map> filterResult() {
        if(!mShowUnmatched || mRecords == null) return mRecords;

        List<Map> records = new ArrayList();
        for(Map record : mRecords) {
            if(TinyMap.AsTinyMap(record).getBoolean("matched")) continue;
            records.add(record);
        }

        return records;
    }

    protected void showRecords() {
        if(mRecords == null) {
            loadRecords();
        } else {
            mAdapter.setRecords(filterResult());
        }
    }

    protected void loadRecords() {
        if(mResult != null) mResult.cancel();
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/InventoryCount/getCountReport?inventoryCountId=" + mInvCountId,
                null,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                            mAdapter.setRecords(null);
                        } else {
                            mRecords = map.getTinyList("docs").list();
                            mAdapter.setRecords(filterResult());
                        }
                    }
                }
        );
    }

    protected void showInvCountProduct(String prodNum) {
        if(mResult != null) mResult.cancel();
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                Uri.parse("/InventoryCountProduct/getDocByNum").buildUpon()
                        .appendQueryParameter("inventoryCountId", mInvCountId + "")
                        .appendQueryParameter("productNum", prodNum)
                        .build().toString(),
                null,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else {
                            openInvCountProduct(map.getLong("_id"));
                        }
                    }
                }
        );
    }

    public void openInvCountProduct(long _id) {
        Intent intent = new Intent(this, InventoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("SessionId", mInvCountId);
        bundle.putString("SessionName", mInvCountName);
        bundle.putInt("TeamId", mTeamId);
        bundle.putLong("itemId", _id);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.prodNum) TextView prodNum;
        @BindView(R.id.team1) TextView team1;
        @BindView(R.id.team2) TextView team2;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    class MyRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {
        List<Map> mRecords;

        public void setRecords(List<Map> records) {
            mRecords = records;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.content_inv_count_report_item, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TinyMap map = getItem(viewHolder.getAdapterPosition());
                    if(map == null) return;

                    showInvCountProduct(map.getString("num"));
                }
            });
            return viewHolder;
        }

        public TinyMap getItem(int position) {
            if(mRecords == null || position < 0 || position >= mRecords.size()) return null;
            return TinyMap.AsTinyMap(mRecords.get(position));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TinyMap map = getItem(position);
            if(map == null) {
                holder.prodNum.setText("");
                holder.team1.setText("");
                holder.team2.setText("");
            } else {
                holder.prodNum.setText(map.getString("num"));
                holder.team1.setText(map.getString("team1"));
                holder.team2.setText(map.getString("team2"));
            }
        }

        @Override
        public int getItemCount() {
            return mRecords == null ? 0 : mRecords.size();
        }

    }

}
