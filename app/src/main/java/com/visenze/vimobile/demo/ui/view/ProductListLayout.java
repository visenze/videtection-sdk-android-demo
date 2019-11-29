package com.visenze.vimobile.demo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.productsearch.model.Product;
import com.visenze.vimobile.demo.ui.adapter.ProductAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductListLayout extends FrameLayout {
    private RecyclerView productRecyclerView;
    private ProgressBar searchProgressBar;
    private TextView searchError;
    private ProductAdapter mProductAdapter;

    public ProductListLayout(@NonNull Context context) {
        this(context, null);
    }

    public ProductListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProductListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, 0, 0);
    }

    public ProductListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public View initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.product_list_layout, null);
        productRecyclerView = view.findViewById(R.id.product_recycler_view);
        searchProgressBar = view.findViewById(R.id.search_progress_bar);
        searchError = view.findViewById(R.id.product_recycler_error);
        productRecyclerView.setHasFixedSize(true);
        int columnSpace = getResources().getDimensionPixelSize(R.dimen.product_item_col_space);
        productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, columnSpace, false));
        productRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        mProductAdapter = new ProductAdapter(ImmutableList.of(), context);
        productRecyclerView.setAdapter(mProductAdapter);

        return view;
    }

    public void updateSearchResult(Context context, List<Product> productList) {
        searchError.setVisibility(View.INVISIBLE);
        updateProductList(context, productList);
    }

    public void updateErrorMessage(String errorMessage) {
        searchError.setText(errorMessage);
        searchError.setVisibility(View.VISIBLE);
    }

    public void showProgress() {
        searchError.setVisibility(View.INVISIBLE);
        searchProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        searchProgressBar.setVisibility(View.INVISIBLE);
    }

    public void updateProductList(Context context, List<Product> productList) {
        mProductAdapter = new ProductAdapter(productList, context);
        productRecyclerView.setAdapter(mProductAdapter);
    }

    public void clearProductList(Context context) {
        if(mProductAdapter != null) {
            mProductAdapter = new ProductAdapter(ImmutableList.of(), context);
            productRecyclerView.setAdapter(mProductAdapter);
        }
    }

}
