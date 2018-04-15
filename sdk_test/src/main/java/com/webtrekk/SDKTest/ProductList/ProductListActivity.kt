package com.webtrekk.SDKTest.ProductList

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.webtrekk.SDKTest.R
import com.webtrekk.webtrekksdk.ProductParameterBuilder
import com.webtrekk.webtrekksdk.Webtrekk
import kotlinx.android.synthetic.main.product_list_layout.*

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

class ProductListActivity : AppCompatActivity(){

    private lateinit var adapter: ProductListAdapter
    private lateinit var model: ProductListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_list_layout)

        model = ViewModelProviders.of(this).get(ProductListModel::class.java)
        model.getList().observe(this, Observer { list ->
            if (list != null) {adapter.setItems(list)}
        })

        productListRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = ProductListAdapter(mutableListOf(), View.OnClickListener { view ->
            val intent = Intent(this@ProductListActivity, ProductDetailActivity::class.java)
            intent.putExtra(PRODUCT_ITEM, (view.tag as ProductItem).saveToBundle())
            startActivity(intent)

        })
        productListRecyclerView.adapter = adapter
        productListRecyclerView.addItemDecoration(DividerItemDecoration(this, (productListRecyclerView.layoutManager as LinearLayoutManager).orientation))

        if (savedInstanceState == null) {
            testBasket.value = mutableListOf<ProductItem>()
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterTracking();
    }

    override fun onStart() {
        super.onStart()
        val webtrekk = Webtrekk.getInstance()

        webtrekk.productListTracker.registerView(productListRecyclerView, 4000){ position ->
            val item = model.getList().value!!.get(position)
            val builder = ProductParameterBuilder(item.id, ProductParameterBuilder.ActionType.list)

            builder.setPosition(position).setCost(item.cost).
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
                    setIsProductSoldOut(item.productSoldOut).result!!
        }
    }

    fun unregisterTracking(){
        val webtrekk = Webtrekk.getInstance()

        if (webtrekk.isInitialized) {
            webtrekk.productListTracker.unregisterView(productListRecyclerView)
        }
    }

    fun getRecyclerView() : RecyclerView {
        return productListRecyclerView
    }
}
