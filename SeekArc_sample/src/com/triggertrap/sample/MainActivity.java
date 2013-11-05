/*******************************************************************************
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
package com.triggertrap.sample;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * MainActivity.java
 * @author Neil Davies
 * 
 */
public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String [] items = getResources().getStringArray(R.array.items);
		setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));
		
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
			Intent intent;
			switch (position) {
			case 0:
				//Start Activity for holo view
				intent = new Intent(this, SimpleActivity.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(this, CustomActivity.class);
				startActivity(intent);
				break;
			}
	}

}
