/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2019 ViSenze Pte. Ltd.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.visenze.vimobile.demo.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.visenze.vimobile.demo.DetailActivity;
import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.productsearch.model.Product;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

/**
 * Presents the list of product items from cloud product search.
 */
public class ProductAdapter extends Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        static ProductViewHolder create(ViewGroup parent, Context context) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
            return new ProductViewHolder(view, context);
        }

        private final ImageView imageView;
        private final TextView priceView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final Context context;

        private ProductViewHolder(View view, Context context) {
            super(view);
            this.context = context;
            imageView = view.findViewById(R.id.product_image);

            priceView = view.findViewById(R.id.product_price);
            titleView = view.findViewById(R.id.product_title);
            subtitleView = view.findViewById(R.id.product_subtitle);
        }

        private void bindProduct(Product product) {
            priceView.setText(product.getPrice());
            titleView.setText(product.getTitle());
            subtitleView.setText(product.getBrand());

            this.itemView.setOnClickListener(view1 -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("PRODUCT", product);
                context.startActivity(intent);
            });

            Picasso.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.empty_image)
                    .error(R.drawable.empty_image)
                    .tag(context)
                    .into(imageView);
        }
    }

    private final List<Product> productList;
    private final Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    @NonNull
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProductViewHolder.create(parent, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bindProduct(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
