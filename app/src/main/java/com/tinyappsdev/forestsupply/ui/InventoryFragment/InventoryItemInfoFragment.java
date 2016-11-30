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
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.tinyappsdev.forestsupply.AppGlobal;
import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.data.CountRecord;
import com.tinyappsdev.forestsupply.data.Product;
import com.tinyappsdev.forestsupply.data.UOM;
import com.tinyappsdev.forestsupply.helper.TinyMap;
import com.tinyappsdev.forestsupply.helper.TinyUtils;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseFragment;
import com.tinyappsdev.forestsupply.ui.BaseUI.InventoryActivityInterface;
import com.tinyappsdev.forestsupply.ui.BaseUI.LazyAdapter;
import com.tinyappsdev.forestsupply.ui.BaseUI.SimpleCustomDialog;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.provider.CalendarContract.CalendarCache.URI;


public class InventoryItemInfoFragment extends BaseFragment<InventoryActivityInterface> {

    @BindView(R.id.prodNum) TextView mProdNum;
    @BindView(R.id.prodAlu) TextView mProdAlu;
    @BindView(R.id.prodDesc) TextView mProdDesc;
    @BindView(R.id.prodOnHand) TextView mProdOnHand;
    @BindView(R.id.prodCountRecord) TextView mProdCountRecord;

    protected ApiCallClient.Result mStatLoader;
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
                CountRecordDialog.newInstance(0L, 0, null).show(
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

    protected void loadStat(final String productNum) {
        if(mStatLoader != null) mStatLoader.cancel();
        mStatLoader = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                Uri.parse("/InventoryCount/getProductCountStat").buildUpon()
                        .appendQueryParameter("productNum", productNum)
                        .appendQueryParameter("inventoryCountId", mActivity.getSessionId() + "")
                        .appendQueryParameter("teamId", mActivity.getTeamId() + "")
                        .build().toString(),
                null,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getActivity(), R.string.error_occurred);
                        } else {
                            TinyMap.TinyList list = map.getTinyList("docs");
                            if(list == null || list.list().size() == 0) return;
                            String[] stats = new String[list.list().size()];
                            int i = 0;
                            for(Map _uomStat: (List<Map>)list.list()) {
                                TinyMap uomStat = TinyMap.AsTinyMap(_uomStat);
                                stats[i++] = String.format(
                                        "%d%s",
                                        uomStat.getInt("quantity"),
                                        uomStat.getString("_id")
                                );
                            }
                            mProdCountRecord.setText(TextUtils.join(", ", stats));
                        }
                    }
                }
        );
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
        if(mStatLoader != null) mStatLoader.cancel();

        if(product == null) {
            mProdNum.setText("");
            mProdAlu.setText("");
            mProdDesc.setText("");
            mProdOnHand.setText("");
            mProdCountRecord.setText("");
            mAdapter.setUri(null);
        } else {
            mProdNum.setText(product.getProductNum() + "");
            mProdAlu.setText(product.getManufacturerNo());
            mProdDesc.setText(product.getDescription());
            mAdapter.setUri(
                    Uri.parse("/InventoryCount/getCountRecords?sortDirection=-1").buildUpon()
                            .appendQueryParameter("inventoryCountId", mActivity.getSessionId() + "")
                            .appendQueryParameter("teamId", mActivity.getTeamId() + "")
                            .appendQueryParameter("productNum", product.getProductNum())
                            .appendQueryParameter("ts", product.getProductNum())
                    .build()
            );
            mProdOnHand.setText(product.getOnHand() + "");
            mProdCountRecord.setText("");
            loadStat(product.getProductNum());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.recordQuantity) TextView recordQuantity;
        @BindView(R.id.recordTime) TextView recordTime;
        @BindView(R.id.recordUser) TextView recordUser;
        @BindView(R.id.recordEdit) ImageButton recordEdit;

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
            viewHolder.recordEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CountRecord item = (CountRecord) getItem(viewHolder.getAdapterPosition());
                    if(item == null) return;

                    CountRecordDialog.newInstance(item.getId(), item.getQuantity(), item.getUom())
                            .show(
                                    InventoryItemInfoFragment.this.getFragmentManager(),
                                    CountRecordDialog.class.getSimpleName()
                            );
                }
            });
            return viewHolder;
        }

        @Override
        public void renderViewHolder(RecyclerView.ViewHolder _holder, int position, Object data) {
            CountRecord item = (CountRecord) data;
            ViewHolder holder = (ViewHolder) _holder;

            if(item == null) {
                holder.recordQuantity.setText("");
                holder.recordTime.setText("");
                holder.recordUser.setText("");
            } else {
                holder.recordQuantity.setText(String.format("%+d%s", item.getQuantity(), item.getUom()));
                holder.recordTime.setText(DateUtils.getRelativeTimeSpanString(item.getCreatedTime()));
                holder.recordUser.setText(item.getUserName());
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
        @BindView(R.id.uom) Spinner viewUom;
        @BindView(R.id.quantity) TextView viewQuantity;

        String mUom;
        long mId;
        int mQuantity;

        public static CountRecordDialog newInstance(Long _id, int quantity, String uom) {
            Bundle args = new Bundle();
            args.putLong("_id", _id);
            args.putInt("quantity", quantity);
            args.putString("uom", uom);
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

            Bundle args = getArguments();
            if(args != null) {
                mId = args.getLong("_id");
                mUom = args.getString("uom");
                mQuantity = args.getInt("quantity");
            }

            Product product = (Product)mActivity.getItem();
            if(product != null && product.getUomList() != null) {
                String[] uoms = new String[product.getUomList().size()];
                int i = 0;
                int k = 0;
                for(UOM uom : product.getUomList()) {
                    uoms[i] = uom.getName();
                    if(uoms[i] == mUom) k = i;
                    i++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this.getContext(), android.R.layout.simple_spinner_item, uoms
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewUom.setAdapter(adapter);

                if(mId != 0) {
                    builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CountRecord countRecord = new CountRecord();
                            countRecord.setId(mId);
                            countRecord.setInventoryCountId(mActivity.getSessionId());
                            mActivity.deleteCountRecord(countRecord);
                        }
                    });
                }

                if(savedInstanceState == null) {
                    viewUom.setSelection(k);
                    viewQuantity.setText(mQuantity == 0 ? "" : mQuantity + "");
                }
            }

            return view;
        }

        @Override
        public void onConfirm() {
            Product product = (Product)mActivity.getItem();
            if(product == null || product.getUomList() == null) return;

            CountRecord countRecord = new CountRecord();
            countRecord.setId(mId);

            try {
                countRecord.setQuantity(Integer.parseInt(viewQuantity.getText().toString()));
            } catch(NumberFormatException e) {
                return;
            }
            countRecord.setUom(viewUom.getSelectedItem().toString());
            countRecord.setProductNum(product.getProductNum());
            countRecord.setInventoryCountId(mActivity.getSessionId());

            if(mId == 0)
                mActivity.addCountRecord(countRecord);
            else
                mActivity.editCountRecord(countRecord);
        }
    }

}
