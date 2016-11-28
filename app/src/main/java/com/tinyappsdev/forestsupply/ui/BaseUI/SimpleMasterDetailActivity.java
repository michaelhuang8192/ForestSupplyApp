package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;


public abstract class SimpleMasterDetailActivity extends BaseActivity implements
        SearchView.OnQueryTextListener,
        SimpleMasterDetailInterface {

    public static final String SEARCH_FRAGMENT_TAG = "SearchFragmentTag";

    protected long mCurItemId;
    protected Object mItem;
    protected FragmentPagerAdapter mFragmentPagerAdapter;
    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;
    private String mLastSearchQuery;
    private boolean mIsSearchActive;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private Handler mHandler;
    private Runnable mSearchDelay;
    private boolean mIsResultNeeded;
    protected ApiCallClient.Result mItemLoader;

    protected abstract void loadItem(long _id);
    protected abstract Fragment createSearchFragment();
    protected abstract Object loadItemFromJson(String json);
    protected abstract String dumpItemToJson(Object item);
    protected abstract FragmentPagerAdapter createFragmentPagerAdapter(FragmentManager fragmentManager);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_master_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHandler = new Handler();
        mSearchDelay = new Runnable() {
            @Override
            public void run() {
                sendMessage(R.id.SimpleMasterDetailOnSearchQueryChange);
            }
        };

        mIsResultNeeded = true;

        mFragmentPagerAdapter = createFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mTabLayout = (TabLayout)findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        if(savedInstanceState != null) {
            mCurItemId = savedInstanceState.getLong("mCurItemId");
            mItem = loadItemFromJson(savedInstanceState.getString("mItem"));

            if(mCurItemId != 0 && mItem == null)
                changeItem(mCurItemId);

        } else {
            Bundle bundle = getIntent().getExtras();
            long _id = bundle != null ? bundle.getLong("itemId", 0) : 0;

            if(_id == 0) {
                showSearchResults();
                sendMessage(R.id.SimpleMasterDetailOnSearchQueryChange);

            } else
                changeItem(_id);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mCurItemId", mCurItemId);
        outState.putString("mItem", dumpItemToJson(mItem));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.list: {
                showSearchResults();
                sendMessage(R.id.SimpleMasterDetailOnSearchQueryChange);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSearchResults() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == null) {
            fragment = createSearchFragment();
            fragmentTransaction.add(R.id.content_simple_master_detail, fragment, SEARCH_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(fragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_simple_master_detail, menu);

        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                showSearchResults();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchView.setQuery(mLastSearchQuery, false);
                    }
                });

                mIsSearchActive = true;

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if(mCurItemId != 0) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
                    if (fragment != null) {
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.hide(fragment);
                        fragmentTransaction.commit();
                    }
                }

                mLastSearchQuery = mSearchView.getQuery().toString();
                mIsSearchActive = false;
                mHandler.removeCallbacks(mSearchDelay);

                return true;
            }
        });

        return true;
    }

    @Override
    public String getSearchQuery() {
        return mSearchView.getQuery().toString();
    }

    @Override
    public Object getItem() {
        return mItem;
    }

    @Override
    public void selectItem(long _id) {
        changeItem(_id);
        mSearchItem.collapseActionView();
    }

    @Override
    public void setResult(Object item) {
        if(!isResultNeeded()) return;

        String js = dumpItemToJson(item);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("js", js);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean isResultNeeded() {
        return mIsResultNeeded;
    }

    public void changeItem(long _id) {
        if(_id == mCurItemId && (mItem != null || mItemLoader != null)) return;
        if(mItemLoader != null) { mItemLoader.cancel(); mItemLoader = null; };

        mCurItemId = _id;
        mItem = null;

        if(_id == 0) {
            sendMessage(R.id.SimpleMasterDetailOnItemUpdate);
            return;
        }

        loadItem(mCurItemId);
    }

    protected  void onLoadedItem(Object item) {
        mItem = item;
        sendMessage(R.id.SimpleMasterDetailOnItemUpdate);
    }

    protected  void selectItem(long _id, Object item) {
        mCurItemId = _id;
        mItem = item;
        mSearchItem.collapseActionView();
        sendMessage(R.id.SimpleMasterDetailOnItemUpdate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if(mItemLoader != null) mItemLoader.cancel();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mHandler.removeCallbacks(mSearchDelay);
        if(mIsSearchActive) mHandler.postDelayed(mSearchDelay, 300);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return onQueryTextSubmit(newText);
    }

}
