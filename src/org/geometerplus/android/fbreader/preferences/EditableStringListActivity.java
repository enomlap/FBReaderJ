/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.preferences;

import java.util.*;

import android.content.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import android.os.Bundle;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;

import org.geometerplus.fbreader.Paths;

public class EditableStringListActivity extends ListActivity {

	public static final String OPTION_NAME = "optionName";
	public static final String TITLE = "title";

	private static String[] rootpaths;

	static {
		rootpaths = new String[1];
		rootpaths[0] = Paths.cardDirectory() + "/";
	}

	private ZLStringListOption myOption;
	private ImageButton addButton;
	private Button okButton;

	private void enableButtons() {
		if (addButton != null) addButton.setEnabled(!getListAdapter().hasEmpty());
		if (okButton != null) okButton.setEnabled(!getListAdapter().hasEmpty());
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.editable_stringlist);
		setTitle(getIntent().getStringExtra(TITLE));

		myOption = Paths.DirectoryOption(getIntent().getStringExtra(OPTION_NAME));


		final View bottomView = getLayoutInflater().inflate(R.layout.editable_stringlist_lastitem, null);
		getListView().addFooterView(bottomView);

		setListAdapter(new ItemAdapter());

		for (String s : myOption.getValue()) {
			DirectoryItem i = new DirectoryItem();
			i.setPath(s);
			getListAdapter().addDirectoryItem(i);
		}

		addButton = (ImageButton)bottomView.findViewById(R.id.editable_stringlist_addbutton);
		addButton.setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					EditableStringListActivity.this.getListAdapter().addDirectoryItem(new DirectoryItem());
				}
			}
		);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final View buttonView = bottomView.findViewById(R.id.editable_stringlist_buttons);
		okButton = (Button)buttonView.findViewById(R.id.ok_button);
		final Button cancelButton = (Button)buttonView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		okButton.setText(buttonResource.getResource("ok").getValue());

		okButton.setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					ArrayList<String> paths = new ArrayList<String>();
					for (int i = 0; i < EditableStringListActivity.this.getListAdapter().getCount(); i++) {
						paths.add(EditableStringListActivity.this.getListAdapter().getItem(i).getPath());
					}
					EditableStringListActivity.this.myOption.setValue(paths);

					EditableStringListActivity.this.finish();
				}
			}
		);
		enableButtons();

		cancelButton.setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					EditableStringListActivity.this.finish();
				}
			}
		);

	}

	@Override
	public ItemAdapter getListAdapter() {
		return (ItemAdapter)super.getListAdapter();
	}

	private class ItemAdapter extends BaseAdapter {
		private int nextId = 0;

		private final ArrayList<DirectoryItem> myItems = new ArrayList<DirectoryItem>();

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public DirectoryItem getItem(int position) {
			return myItems.get(position);
		}

		synchronized void addDirectoryItem(DirectoryItem i) {
			i.setId(nextId);
			nextId = nextId + 1;
			myItems.add(i);
			notifyDataSetChanged();
			EditableStringListActivity.this.enableButtons();
		}

		@Override
		public synchronized int getCount() {
			return myItems.size();
		}

		public boolean hasEmpty() {
			for (DirectoryItem i : myItems) {
				if ("".equals(i.getPath())) return true;
			}
			return false;
		}

		synchronized void removeDirectoryItem(int id) {
			for (int i = 0; i < myItems.size(); i++) {
				if (myItems.get(i).getId() == id) {
					myItems.remove(i);
					notifyDataSetChanged();
					EditableStringListActivity.this.enableButtons();
					return;
				}
			}
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final DirectoryItem item = getItem(position);
			final View view = LayoutInflater.from(EditableStringListActivity.this).inflate(R.layout.editable_stringlist_item, parent, false);

			final AutoCompleteTextView text = (AutoCompleteTextView)view.findViewById(R.id.editable_stringlist_text);
			text.setText(item.getPath());
			text.addTextChangedListener(new TextWatcher(){
				public void afterTextChanged(Editable s) {}
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					item.setPath(s.toString());
					EditableStringListActivity.this.enableButtons();
				}
			});
			text.setAdapter(new ArrayAdapter<String>(EditableStringListActivity.this, android.R.layout.simple_dropdown_item_1line, rootpaths));
			final ImageButton button = (ImageButton)view.findViewById(R.id.editable_stringlist_deletebutton);
			button.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						ItemAdapter.this.removeDirectoryItem(item.getId());
						EditableStringListActivity.this.enableButtons();
					}
				}
			);
			button.setEnabled(ItemAdapter.this.getCount() > 1);
			return view;
		}
	}

	private static class DirectoryItem {
		private String myPath = "";
		private int myId;

		public String getPath() {
			return myPath;
		}
		public void setPath(String path) {
			myPath = path;
		}

		public int getId() {
			return myId;
		}
		public void setId(int id) {
			myId = id;
		}
	}
}
