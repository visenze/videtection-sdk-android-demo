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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductQuerySchema {
    private static final String CATEGORY = "Category";
    private static final String BRAND = "brand";
    private static final String PRICE = "Price";
    private static final String PRICE_UNIT = "PriceUnit";
    private static final String TITLE = "Title";
    private static final String IM_URL = "ImgUrl";
    private static final String PRODUCT_URL = "ProductUrl";

    private static final List<String> reqList = Arrays.asList(
            CATEGORY,
            BRAND,
            PRICE,
            PRICE_UNIT,
            TITLE,
            IM_URL,
            PRODUCT_URL
    );

    private static final Map<String, String> schemaMap = new HashMap<>();

    public static void loadSchemaConfig() {
        schemaMap.put(CATEGORY, "category");
        schemaMap.put(BRAND, "source");
        schemaMap.put(PRICE, "price");
        schemaMap.put(PRICE_UNIT, "price_unit");
        schemaMap.put(TITLE, "title");
        schemaMap.put(IM_URL, "im_url");
        schemaMap.put(PRODUCT_URL, "product_url");
    }

    public static List<String> getFieldList() {
        List<String> fl = new ArrayList<String>();

        for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
            fl.add(entry.getValue());
        }
        return fl;
    }

    public static Product fromMetaData(Map<String, String> metaData) {
        String imgUrl = metaData.get(schemaMap.get(IM_URL));
        String category = metaData.get(schemaMap.get(CATEGORY));
        String brand = metaData.get(schemaMap.get(BRAND));
        String price = metaData.get(schemaMap.get(PRICE));
        String priceUnit = metaData.get(schemaMap.get(PRICE_UNIT));
        String title = metaData.get(schemaMap.get(TITLE));
        String productUrl = metaData.get(schemaMap.get(PRODUCT_URL));

        return new Product(imgUrl, category, brand, price, priceUnit, title, productUrl);
    }
}
