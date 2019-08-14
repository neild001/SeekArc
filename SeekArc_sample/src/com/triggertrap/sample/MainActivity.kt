/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.triggertrap.sample

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

/**
 * MainActivity.java
 *
 * @author Neil Davies
 */
class MainActivity : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = resources.getStringArray(R.array.items)
        listAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, items
        )
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val intent: Intent
        when (position) {
            0 -> {
                intent = Intent(this, SimpleActivity::class.java)
                startActivity(intent)
            }
            1 -> {
                intent = Intent(this, CustomActivity::class.java)
                startActivity(intent)
            }
            2 -> {
                intent = Intent(this, ScrollViewActivity::class.java)
                startActivity(intent)
            }
            3 -> {
                intent = Intent(this, DisabledActivity::class.java)
                startActivity(intent)
            }
        }
    }

}
