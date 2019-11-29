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

package com.visenze.vimobile.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.productsearch.model.Product;

import javax.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    //inject ui
    ImageView detailImageView;
    TextView titleView;
    TextView sourceView;
    TextView priceView;

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        Product product = (Product) getIntent().getSerializableExtra("PRODUCT");
        setContentView(R.layout.activity_detail);

        detailImageView = findViewById(R.id.detail_image_view);
        titleView = findViewById(R.id.detail_title_view);
        sourceView = findViewById(R.id.detail_source_view);
        priceView = findViewById(R.id.detail_price_view);

        updateUI(product);
    }

    private void updateUI(Product product) {
        Picasso.with(getApplication())
                .load(product.getImageUrl())
                .placeholder(R.drawable.empty_image)
                .error(R.drawable.empty_image)
                .tag(getApplication())
                .into(detailImageView);

        String title = product.getTitle();
        String brand = product.getBrand();
        String price = product.getPriceUnit() + " " + product.getPrice();

        titleView.setText(title);
        sourceView.setText(brand);
        priceView.setText("Buy for " + price);

        priceView.setOnClickListener(view1 -> {
            String productUrl = product.getProductUrl();
            if (productUrl == null || productUrl.length() == 0) {
                return;
            }
            if (!productUrl.startsWith("http://") && !productUrl.startsWith("https://")) {
                productUrl = "http://" + productUrl;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(productUrl));
            startActivity(browserIntent);
        });
    }
}

