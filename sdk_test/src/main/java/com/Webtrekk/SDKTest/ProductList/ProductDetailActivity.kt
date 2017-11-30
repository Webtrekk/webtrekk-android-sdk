package com.Webtrekk.SDKTest.ProductList

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.Webtrekk.SDKTest.R
import com.webtrekk.webtrekksdk.ProductParameterBuilder
import com.webtrekk.webtrekksdk.TrackingParameter
import com.webtrekk.webtrekksdk.Webtrekk
import kotlinx.android.synthetic.main.product_detail.*
import java.util.*

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
 * Created by vartbaronov on 20.11.17.
 */

const val PRODUCT_ITEM: String = "PRODUCT_ITEM"

class ProductDetailActivity : AppCompatActivity(){
    private lateinit var model: ProductDetailModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_detail)

        model = ViewModelProviders.of(this).get(ProductDetailModel::class.java)

        if(savedInstanceState == null){
            val bundle = intent.getBundleExtra(PRODUCT_ITEM)

            model.getItem().observe(this, Observer { item ->
                var text = "Product position: "+ item?.ind + "\n"
                text += "Product id: "+ item?.id + "\n"
                text += "Product categories: "+ Arrays.toString(item?.categories) + "\n"
                text += "Product cost: "+ item?.cost + "\n"
                text += "Product ecom par: "+ Arrays.toString(item?.ecomParameters) + "\n"
                text += "Product paymentMethod: "+ item?.paymentMethod + "\n"
                text += "Product shippingService: "+ item?.shippingService + "\n"
                text += "Product shippingSpeed: "+ item?.shippingSpeed + "\n"
                text += "Product shippingCosts: "+ item?.shippingCost + "\n"
                text += "Product grossMargin: "+ item?.grossMargin + "\n"
                text += "Product productVariant: "+ item?.productVariant + "\n"
                text += "Product productSoldOut: "+ item?.productSoldOut + "\n"
                ProductDetails.text = text
            })

            model.getItem().value = ProductItem.getFromBundle(bundle)

            testBasket.observe(this, Observer { items ->
                var textProductList = ""

                if (items == null){
                    return@Observer
                }

                for (item in items.iterator()){
                    textProductList += item.id + "; "
                }

                Basket.text = textProductList
            })
        }

    }

    public fun onAdd(view: View){
        trackProduct(ProductParameterBuilder.ActionType.add)

        val item = model.getItem().value!!
        if (!testBasket.value!!.contains(item)) {
            val newList = mutableListOf<ProductItem>()
            newList.addAll(testBasket.value!!)
            newList.add(item)
            testBasket.value = newList
        }
    }

    public fun onView(view: View){
        trackProduct(ProductParameterBuilder.ActionType.view)
    }

    public fun onConf(view: View){
        val items = testBasket.value
        if (items != null){

            val productTracker = Webtrekk.getInstance().productListTracker
            for (item in items){
                val parameter = ProductParameterBuilder(item.id, ProductParameterBuilder.ActionType.conf).
                        setCost(item.cost).
                        setEcommerce(1, item.ecomParameters[0]).
                        setEcommerce(2, item.ecomParameters[1]).
                        setProductCategory(1, item.categories[0]).
                        setProductCategory(2, item.categories[1]).
                        setPaymentMethod(item.paymentMethod).
                        setShippingService(item.shippingService).
                        setShippingSpeed(item.shippingSpeed).
                        setShippingCost(item.shippingCost).
                        setGrossMargin(item.grossMargin).
                        setProductVariant(item.productVariant).
                        setIsProductSoldOut(item.productSoldOut).
                        setCouponValue("CouponValue").
                        setOrderStatus("orderStatus").
                        setProductQuantity(2).
                        result

                if (parameter != null) {
                    productTracker.trackProduct(parameter)
                }

            }
            productTracker.send()

            onBasketClear(view)
        }
    }

    public fun onBasketClear(view: View){
        testBasket.value = mutableListOf<ProductItem>()
    }

    private fun trackProduct(status: ProductParameterBuilder.ActionType){
        val item = model.getItem().value

        if (item != null){
            val productTracker = Webtrekk.getInstance().productListTracker
            val parameter = ProductParameterBuilder(item.id, status).setCost(item.cost).
                    setEcommerce(1, item.ecomParameters[0]).
                    setEcommerce(2, item.ecomParameters[1]).
                    setProductCategory(1, item.categories[0]).
                    setProductCategory(2, item.categories[1]).
                    setPaymentMethod(item.paymentMethod).
                    setShippingService(item.shippingService).
                    setShippingSpeed(item.shippingSpeed).
                    setShippingCost(item.shippingCost).
                    setGrossMargin(item.grossMargin).
                    setProductVariant(item.productVariant).
                    setIsProductSoldOut(item.productSoldOut).result

            if (parameter != null) {
                productTracker.trackProduct(parameter, true)
            }
        }
    }
}