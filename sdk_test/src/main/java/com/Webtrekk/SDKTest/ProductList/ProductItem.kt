package com.Webtrekk.SDKTest.ProductList

import android.os.Bundle

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Webtrekk GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by vartbaronov on 13.11.17.
 */
const val PRODUCT_POSITION: String = "PRODUCT_POSITION"
const val PRODUCT_ID: String = "PRODUCT_ID"
const val PRODUCT_CATEGORIES: String = "PRODUCT_CATEGORIES"
const val PRODUCT_COST: String = "PRODUCT_COST"
const val PRODUCT_ECOM_PARAMETERS: String = "PRODUCT_ECOM_PARAMETERS"
const val PRODUCT_PAYMENT_METHOD: String = "PRODUCT_PAYMENT_METHOD"
const val PRODUCT_SHIPPING_SERVICE: String = "PRODUCT_SHIPPING_SERVICE"
const val PRODUCT_SHIPPING_SPEED: String = "PRODUCT_SHIPPING_SPEED"
const val PRODUCT_SHIPPING_COST: String = "PRODUCT_SHIPPING_COST"
const val PRODUCT_GROSS_MARGIN: String = "PRODUCT_GROSS_MARGIN"
const val PRODUCT_PRODUCT_VARIANT: String = "PRODUCT_PRODUCT_VARIANT"
const val PRODUCT_PRODUCT_SOLD_OUT: String = "PRODUCT_PRODUCT_SOLD_OUT"

public data class ProductItem(val ind: Int, val id: String, val categories: Array<String>, val cost: Float,
                       val ecomParameters: Array<String>, val paymentMethod: String,
                       val shippingService: String, val shippingSpeed: String,
                       val shippingCost: Float, val grossMargin: Float,
                       val productVariant: String, val productSoldOut: Boolean){
    companion object Handler{
        fun getProductItemByIndex(ind: Int): ProductItem{
            return ProductItem(ind,
                    "productId" + ind,
                    arrayOf("Cat" + ind + "1", "Cat" + ind + "2"),
                    13.5f + ind,
                    arrayOf("Ecom" + ind + "1", "Ecom" + ind + "2", "Ecom" + ind + "3"),
                    "PayMethod" + ind,
                    "ShippingService" + ind,
                    "ShippingSpeed" + ind,
                    23.5f + ind,
                    33.5f + ind,
                    "ProductVariant" + ind,
                    ind % 2 == 0)
        }

        fun getFromBundle(bundle: Bundle): ProductItem {
            val indNew = bundle.getInt(PRODUCT_POSITION)
            val idNew = bundle.getString(PRODUCT_ID)
            val categoriesNew  = bundle.getStringArray(PRODUCT_CATEGORIES)
            val costNew = bundle.getFloat(PRODUCT_COST)
            val ecomParametersNew = bundle.getStringArray(PRODUCT_ECOM_PARAMETERS)
            val paymentMethodNew = bundle.getString(PRODUCT_PAYMENT_METHOD)
            val shippingServiceNew = bundle.getString(PRODUCT_SHIPPING_SERVICE)
            val shippingSpeedNew = bundle.getString(PRODUCT_SHIPPING_SPEED)
            val shippingCostsNew = bundle.getFloat(PRODUCT_SHIPPING_COST)
            val grossMarginNew = bundle.getFloat(PRODUCT_GROSS_MARGIN)
            val productVariantNew = bundle.getString(PRODUCT_PRODUCT_VARIANT)
            val productSoldOutNew = bundle.getBoolean(PRODUCT_PRODUCT_SOLD_OUT)

            return ProductItem(indNew, idNew, categoriesNew, costNew, ecomParametersNew,
                    paymentMethodNew, shippingServiceNew, shippingSpeedNew, shippingCostsNew,
                    grossMarginNew, productVariantNew, productSoldOutNew)
        }
    }

    fun saveToBundle():Bundle{
        val bundle = Bundle()
        bundle.putInt(PRODUCT_POSITION, ind)
        bundle.putString(PRODUCT_ID, id)
        bundle.putStringArray(PRODUCT_CATEGORIES, categories)
        bundle.putFloat(PRODUCT_COST, cost)
        bundle.putStringArray(PRODUCT_ECOM_PARAMETERS, ecomParameters)
        bundle.putString(PRODUCT_PAYMENT_METHOD, paymentMethod)
        bundle.putString(PRODUCT_SHIPPING_SERVICE, shippingService)
        bundle.putString(PRODUCT_SHIPPING_SPEED, shippingSpeed)
        bundle.putFloat(PRODUCT_SHIPPING_COST, shippingCost)
        bundle.putFloat(PRODUCT_GROSS_MARGIN, grossMargin)
        bundle.putString(PRODUCT_PRODUCT_VARIANT, productVariant)
        bundle.putBoolean(PRODUCT_PRODUCT_SOLD_OUT, productSoldOut)
        return bundle
    }
}
