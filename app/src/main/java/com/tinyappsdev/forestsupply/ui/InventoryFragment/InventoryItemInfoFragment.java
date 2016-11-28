package com.tinyappsdev.forestsupply.ui.InventoryFragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.data.CountRecord;
import com.tinyappsdev.forestsupply.data.Product;
import com.tinyappsdev.forestsupply.data.UOM;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseFragment;
import com.tinyappsdev.forestsupply.ui.BaseUI.InventoryActivityInterface;
import com.tinyappsdev.forestsupply.ui.BaseUI.LazyAdapter;
import com.tinyappsdev.forestsupply.ui.BaseUI.SimpleCustomDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.provider.CalendarContract.CalendarCache.URI;


public class InventoryItemInfoFragment extends BaseFragment<InventoryActivityInterface> {

    @BindView(R.id.prodAlu) TextView mProdAlu;
    @BindView(R.id.prodDesc) TextView mProdDesc;

    private Unbinder mUnbinder;

    private RecyclerView mRecyclerView;
    private LazyRecyclerAdapter mAdapter;

    public InventoryItemInfoFragment() {
    }

    public static InventoryItemInfoFragment newInstance() {
        InventoryItemInfoFragment fragment = new InventoryItemInfoFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_inventory_item_info, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CountRecordDialog.newInstance().show(
                        InventoryItemInfoFragment.this.getFragmentManager(),
                        CountRecordDialog.class.getSimpleName()
                );
            }
        });

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mAdapter = new LazyRecyclerAdapter(
                this.getContext(),
                R.layout.fragment_inventory_item_info_item,
                null
        );
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        updateUI();
    }

    @Override
    protected void onMessage(Message msg) {
        switch(msg.what) {
            case R.id.SimpleMasterDetailOnItemUpdate: {
                updateUI();
                break;
            }
        }
    }

    protected void updateUI() {
        Product product = (Product)mActivity.getItem();

        if(product == null) {
            mProdAlu.setText("");
            mProdDesc.setText("");
            mAdapter.setUri(null);
        } else {
            mProdAlu.setText(product.getManufacturerNo());
            mProdDesc.setText(product.getDescription());
            mAdapter.setUri(
                    URI.parse("/InventoryCount/getCountRecords?sortDirection=-1").buildUpon()
                            .appendQueryParameter("inventoryCountId", mActivity.getSessionId() + "")
                            .appendQueryParameter("productNum", product.getProductNum())
                            .appendQueryParameter("ts", product.getProductNum())
                    .build()
            );
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.recordQuantity) TextView recordQuantity;
        @BindView(R.id.recordTime) TextView recordTime;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ApiPageResult {
        public int total;
        public CountRecord[] docs;
    }

    class LazyRecyclerAdapter extends LazyAdapter {
        public LazyRecyclerAdapter(Context context, int resourceId, Uri uri) {
            super(context, resourceId, uri, ApiPageResult.class);
        }

        @Override
        public RecyclerView.ViewHolder createViewHolder(View view) {
            final ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void renderViewHolder(RecyclerView.ViewHolder _holder, int position, Object data) {
            CountRecord item = (CountRecord) data;
            ViewHolder holder = (ViewHolder) _holder;

            if(item == null) {
                holder.recordQuantity.setText("");
                holder.recordTime.setText("");
            } else {
                holder.recordQuantity.setText(String.format("%+d%s", item.getQuantity(), item.getUom()));
                holder.recordTime.setText(DateUtils.getRelativeTimeSpanString(item.getCreatedTime()));
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

    public static class CountRecordDialog extends SimpleCustomDialog<InventoryActivityInterface> {
        @BindView(R.id.uom) Spinner uom;
        @BindView(R.id.quantity) TextView quantity;

        public static CountRecordDialog newInstance() {
            Bundle args = new Bundle();
            CountRecordDialog fragment = new CountRecordDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateCustomView(Bundle savedInstanceState,
                                       AlertDialog.Builder builder,
                                       LayoutInflater inflater,
                                       ViewGroup parent)
        {
            View view = inflater.inflate(R.layout.dialog_count_record, parent);
            ButterKnife.bind(this, view);


            Product product = (Product)mActivity.getItem();
            if(product != null && product.getUomList() != null) {
                String[] uoms = new String[product.getUomList().size()];
                int i = 0;
                for(UOM uom : product.getUomList())
                    uoms[i] = uom.getName();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this.getContext(), android.R.layout.simple_spinner_item, uoms
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                uom.setAdapter(adapter);
            }

            return view;
        }

        @Override
        public void onConfirm() {
            Product product = (Product)mActivity.getItem();
            if(product == null || product.getUomList() == null) return;

            CountRecord countRecord = new CountRecord();

            try {
                countRecord.setQuantity(Integer.parseInt(quantity.getText().toString()));
            } catch(NumberFormatException e) {
                return;
            }
            countRecord.setUom(uom.getSelectedItem().toString());
            countRecord.setProductNum(product.getProductNum());
            countRecord.setInventoryCountId(mActivity.getSessionId());

            mActivity.addCountRecord(countRecord);
        }
    }

}
