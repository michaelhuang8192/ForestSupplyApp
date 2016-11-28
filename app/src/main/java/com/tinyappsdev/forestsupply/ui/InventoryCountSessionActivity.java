package com.tinyappsdev.forestsupply.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.data.CountRecord;
import com.tinyappsdev.forestsupply.data.InventoryCount;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseActivity;
import com.tinyappsdev.forestsupply.ui.BaseUI.LazyAdapter;
import com.tinyappsdev.forestsupply.ui.InventoryFragment.InventoryItemInfoFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.provider.CalendarContract.CalendarCache.URI;

public class InventoryCountSessionActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private LazyRecyclerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_count_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new LazyRecyclerAdapter(
                this,
                R.layout.content_inventory_count_session_item,
                null
        );
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setUri(URI.parse("/InventoryCount/getDocsByUser"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void openInventoryCountSession(long _id, String name) {
        Intent intent = new Intent(this, InventoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("SessionId", _id);
        bundle.putString("SessionName", name);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.sessionItemCount) TextView sessionItemCount;
        @BindView(R.id.sessionName) TextView sessionName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ApiPageResult {
        public int total;
        public InventoryCount[] docs;
    }

    class LazyRecyclerAdapter extends LazyAdapter {
        public LazyRecyclerAdapter(Context context, int resourceId, Uri uri) {
            super(context, resourceId, uri, ApiPageResult.class);
        }

        @Override
        public RecyclerView.ViewHolder createViewHolder(View view) {
            final ViewHolder viewHolder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InventoryCount item = (InventoryCount) getItem(viewHolder.getAdapterPosition());
                    if(item == null) return;

                    openInventoryCountSession(item.getId(), item.getName());
                }
            });
            return viewHolder;
        }

        @Override
        public void renderViewHolder(RecyclerView.ViewHolder _holder, int position, Object data) {
            InventoryCount item = (InventoryCount) data;
            ViewHolder holder = (ViewHolder) _holder;

            if(item == null) {
                holder.sessionItemCount.setText("");
                holder.sessionName.setText("");
            } else {
                holder.sessionItemCount.setText(item.getProductCount() + "");
                holder.sessionName.setText(item.getName() + "");
            }
        }

        @Override
        protected PageResult parseResult(Object result) {
            PageResult pageResult = new PageResult();
            pageResult.rows = ((ApiPageResult)result).docs;
            pageResult.total = ((ApiPageResult)result).total;
            return pageResult;
        }
    }

}
