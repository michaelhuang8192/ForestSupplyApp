package com.tinyappsdev.forestsupply.ui.BaseUI;


import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinyappsdev.forestsupply.AppGlobal;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;

import java.util.HashMap;
import java.util.Map;

public abstract class LazyAdapter extends RecyclerView.Adapter {
    static class PageCache {
        ApiCallClient.Result requestResult;
        Object[] rows;
    }

    public static class PageResult {
        public int total;
        public Object[] rows;
    }

    protected Context mContext;
    protected int mTotal;
    protected int mPageSize;
    protected Map<Integer, PageCache> mCache;
    protected int mResourceId;
    protected Uri mUri;
    protected Class mResultClass;

    public LazyAdapter(Context context, int resourceId, Uri uri, Class resultClass) {
        this(context, resourceId, uri, 50, resultClass);
    }

    public LazyAdapter(Context context, int resourceId, Uri uri, int pageSize, Class resultClass) {
        mResourceId = resourceId;
        mContext = context;
        mPageSize = pageSize;
        mResultClass = resultClass;

        mUri = uri;
        newCache(mUri == null ? 0 : 1);
    }

    public void newCache(int total) {
        mTotal = total;
        mCache = new HashMap<Integer, PageCache>();
    }

    public void refresh() {
        if(mTotal <= 0) mTotal = 1;
        mCache.clear();
        notifyDataSetChanged();
    }

    public void setUri(Uri uri) {
        if(uri == mUri || uri != null && uri.equals(mUri)) {
            refresh();
            return;
        }

        mUri = uri;
        newCache(mUri == null ? 0 : 1);
        notifyDataSetChanged();
    }

    public RecyclerView.ViewHolder createViewHolder(View view) {
        return null;
    }

    public void renderViewHolder(RecyclerView.ViewHolder holder, int position, Object data) {

    }

    public void loadMore() {
        if(mTotal == 0 || (mTotal % mPageSize) != 0) return;

        int pageIdx = mTotal / mPageSize;
        PageCache pageCache = mCache.get(pageIdx);
        if(pageCache == null || pageCache.requestResult == null && pageCache.rows == null) {
            requestPage(pageIdx);
        }
    }

    protected Uri buildRequestUri(int pageIdx) {
        return mUri.buildUpon()
                .appendQueryParameter("limit", String.valueOf(mPageSize))
                .appendQueryParameter("skip", String.valueOf(pageIdx * mPageSize))
                .build();
    }

    protected void requestPage(final int pageIdx) {
        if(mUri == null) return;

        PageCache pageCache = mCache.get(pageIdx);
        if(pageCache == null) {
            pageCache = new PageCache();
            mCache.put(pageIdx, pageCache);
        }

        final PageCache _pageCache = pageCache;
        Uri uri = buildRequestUri(pageIdx);
        _pageCache.requestResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                uri.toString(),
                null,
                mResultClass,
                new ApiCallClient.OnResultListener() {
                    @Override
                    public void onResult(ApiCallClient.Result result) {
                        try {
                            if (_pageCache != mCache.get(pageIdx)) return;
                            if (result.error != null || result.data == null) return;

                            setPage(pageIdx, _pageCache, parseResult(result.data));
                        } finally {
                            _pageCache.requestResult = null;
                        }
                    }
                }
        );
    }

    protected abstract PageResult parseResult(Object result);

    protected void setPage(int pageIdx, PageCache _pageCache, PageResult result) {
        //Log.i("PKT", String.format(">>>>%d, %d", result.total, result.rows.length));

        if(result.total < 0 || result.total == mTotal) {
            int lastPageIdx = (mTotal - 1) / mPageSize;
            if(pageIdx <= lastPageIdx || pageIdx == lastPageIdx + 1 && (mTotal % mPageSize) == 0) {
                if(result.rows.length != mPageSize || pageIdx >= lastPageIdx)
                    mTotal = pageIdx * mPageSize + result.rows.length;

                _pageCache.rows = result.rows;
                notifyDataSetChanged();
            }
        } else {
            newCache(result.total);
            PageCache pageCache = new PageCache();
            pageCache.rows = result.rows;
            mCache.put(pageIdx, pageCache);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mResourceId, parent, false);
        return createViewHolder(view);
    }

    public Object getItem(int position) {
        if(position < 0 || position >= mTotal) return null;

        int pageIdx = position / mPageSize;
        int rowIdx = position % mPageSize;
        PageCache pageCache = mCache.get(pageIdx);
        if(pageCache == null || pageCache.rows == null) return null;

        return pageCache.rows[rowIdx];
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object data = null;
        if(position >= 0 && position < mTotal) {
            int pageIdx = position / mPageSize;
            int rowIdx = position % mPageSize;
            PageCache pageCache = mCache.get(pageIdx);
            if(pageCache == null || pageCache.requestResult == null && pageCache.rows == null) {
                requestPage(pageIdx);
            } else if(pageCache.rows != null && rowIdx < pageCache.rows.length) {
                data = pageCache.rows[rowIdx];
            }
        }

        renderViewHolder(holder, position, data);
    }

    @Override
    public int getItemCount() {
        return mTotal;
    }

}
