package com.tinyappsdev.forestsupply.ui.InventoryFragment;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.data.Product;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseFragment;
import com.tinyappsdev.forestsupply.ui.BaseUI.InventoryActivityInterface;
import com.tinyappsdev.forestsupply.ui.BaseUI.LazyAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventorySearchFragment extends BaseFragment<InventoryActivityInterface> {

    private RecyclerView mRecyclerView;
    private LazyRecyclerAdapter mAdapter;
    private String mQuery;

    private Uri GETDOCS_URI;

    public static InventorySearchFragment newInstance() {
        Bundle args = new Bundle();
        InventorySearchFragment fragment = new InventorySearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory_search, container, false);

        GETDOCS_URI = new Uri.Builder()
                .appendEncodedPath("InventoryCountProduct/getDocs")
                .appendQueryParameter("inventoryCountId", mActivity.getSessionId() + "")
                .appendQueryParameter("sortDirection", "-1")
                .build();

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager =
                        (LinearLayoutManager)mRecyclerView.getLayoutManager();
                int total = linearLayoutManager.getItemCount();
                int last = linearLayoutManager.findLastVisibleItemPosition();

                if(mAdapter != null && total - last < 15) mAdapter.loadMore();
            }
        });
        mAdapter = new LazyRecyclerAdapter(
                this.getContext(),
                R.layout.fragment_inventory_search_item,
                GETDOCS_URI
        );
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.clearOnScrollListeners();
    }

    @Override
    protected void onMessage(Message msg) {
        switch(msg.what) {
            case R.id.SimpleMasterDetailOnSearchQueryChange: {
                loadList(mActivity.getSearchQuery());
                break;
            }
        }
    }

    protected void loadList(String query) {
        if (query == null || query.isEmpty()) {
            if(mQuery != null) {
                mAdapter.setUri(GETDOCS_URI);
                mQuery = null;
            }
        } else if(!query.equals(mQuery)) {
            mQuery = query;
            Uri uri = new Uri.Builder()
                    .appendEncodedPath("InventoryCountProduct/search")
                    .appendQueryParameter("inventoryCountId", mActivity.getSessionId() + "")
                    .appendQueryParameter("terms", query).build();
            mAdapter.setUri(uri);
            
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.prodNum) TextView prodNum;
        @BindView(R.id.prodAlu) TextView prodAlu;
        @BindView(R.id.prodDesc) TextView prodDesc;
        @BindView(R.id.prodOnHand) TextView prodOnHand;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ApiPageResult {
        public int total;
        public Product[] docs;
    }

    class LazyRecyclerAdapter extends LazyAdapter {

        public LazyRecyclerAdapter(Context context, int resourceId, Uri uri) {
            super(context, resourceId, uri, 25, ApiPageResult.class);
        }

        @Override
        public RecyclerView.ViewHolder createViewHolder(View view) {
            final ViewHolder viewHolder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product product = (Product)getItem(viewHolder.getAdapterPosition());
                    if(product == null) return;
                    mActivity.selectItem(product.getId());
                }
            });

            return viewHolder;
        }

        @Override
        public void renderViewHolder(RecyclerView.ViewHolder holder, int position, Object data) {
            Product product = (Product) data;
            ViewHolder viewHolder = (ViewHolder) holder;

            if(product == null) {
                viewHolder.prodNum.setText("");
                viewHolder.prodAlu.setText("");
                viewHolder.prodDesc.setText("");
                viewHolder.prodOnHand.setText("");
            } else {
                viewHolder.prodNum.setText(product.getProductNum());
                viewHolder.prodAlu.setText(product.getManufacturerNo());
                viewHolder.prodDesc.setText(product.getDescription());
                viewHolder.prodOnHand.setText(product.getOnHand() + "");
            }

        }

        @Override
        protected PageResult parseResult(Object result) {
            LazyAdapter.PageResult pageResult = new LazyAdapter.PageResult();
            pageResult.rows = ((ApiPageResult)result).docs;
            pageResult.total = ((ApiPageResult)result).total;
            return pageResult;
        }
    }

}
