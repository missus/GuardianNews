package com.example.android.guardiannews;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    private NewsAdapter mAdapter;
    private ListView mNewsListView;
    private TextView mEmptyStateTextView;
    private LoaderManager mLoaderManager;
    private String mUri;
    private SwipeRefreshLayout mSwipeContainer;


    public static final String LOG_TAG = NewsActivity.class.getName();
    private static final int NEWS_LOADER_ID = 1;
    private static final String NEWS_REQUEST_URL = "http://content.guardianapis.com/search";
    private static final String API_KEY = "57eeaf06-0046-4d55-a9b8-2ba24bb41981";
    private static final String URI = "URI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        mNewsListView = (ListView) findViewById(R.id.list);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty);
        mNewsListView.setEmptyView(mEmptyStateTextView);
        mAdapter = new NewsAdapter(NewsActivity.this, new ArrayList<News>());
        mNewsListView.setAdapter(mAdapter);

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            if (savedInstanceState != null && !savedInstanceState.getString(URI).isEmpty()) {
                mUri = savedInstanceState.getString(URI);
                mLoaderManager = getLoaderManager();
                mLoaderManager.initLoader(NEWS_LOADER_ID, savedInstanceState, NewsActivity.this);
            } else {
                search();
            }
        } else {
            mEmptyStateTextView.setText(R.string.no_internet);
        }

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    search();
                } else {
                    mAdapter.clear();
                    mEmptyStateTextView.setText(R.string.no_internet);
                }
                mSwipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(R.color.swipe1, R.color.swipe2, R.color.swipe3, R.color.swipe4);

        mNewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()

        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });

    }

    private void search() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String maxNews = sharedPrefs.getString(
                getString(R.string.settings_max_news_key),
                getString(R.string.settings_max_news_default));
        String searchTerm = sharedPrefs.getString(
                getString(R.string.settings_search_term_key),
                getString(R.string.settings_search_term_default));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        if (!searchTerm.isEmpty()) {
            mEmptyStateTextView.setText("");
            Uri baseUri = Uri.parse(NEWS_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter("q", searchTerm);
            uriBuilder.appendQueryParameter("page-size", maxNews);
            uriBuilder.appendQueryParameter("order-by", orderBy);
            uriBuilder.appendQueryParameter("api-key", API_KEY);
            mUri = uriBuilder.toString();
            Bundle search = new Bundle();
            search.putString(URI, mUri);
            mLoaderManager = getLoaderManager();
            getLoaderManager().restartLoader(NEWS_LOADER_ID, search, this);
        } else {
            mAdapter.clear();
            mEmptyStateTextView.setText(R.string.no_search_term);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(URI, mUri);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        return new NewsLoader(this, bundle.getString(URI));
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        mAdapter.clear();
        if (news != null && !news.isEmpty()) {
            mAdapter = new NewsAdapter(NewsActivity.this, news);
            mNewsListView.setAdapter(mAdapter);
        } else {
            mEmptyStateTextView.setText(R.string.no_news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
