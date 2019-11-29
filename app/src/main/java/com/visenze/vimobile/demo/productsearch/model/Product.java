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

package com.visenze.vimobile.demo.productsearch.model;

import java.io.Serializable;

/**
 * Information about a product.
 */
public class Product implements Serializable {
    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public String getTitle() {
        return title;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return im_url;
    }

    private String im_url;
    private String category;
    private String brand;
    private String price;
    private String priceUnit;
    private String title;
    private String productUrl;

    public Product(String im_url, String category, String brand, String price, String priceUnit, String title, String productUrl) {
        this.im_url = im_url == null ? "" : im_url.trim();
        this.category = category == null ? "" : category.trim();
        this.brand = brand == null ? "" : brand.trim();
        this.price = price == null ? "" : price.trim();
        this.priceUnit = priceUnit == null ? "" : priceUnit.trim();
        this.title = title == null ? "" : title.trim();
        this.productUrl = productUrl == null ? "" : productUrl.trim();
    }

}
