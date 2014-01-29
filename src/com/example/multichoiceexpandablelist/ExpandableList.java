/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.multichoiceexpandablelist;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
public class ExpandableList extends ExpandableListActivity implements OnGroupClickListener {

    private ActionMode mActionMode;
    ExpandableListAdapter mAdapter;
    int expandableListSelectionType = ExpandableListView.PACKED_POSITION_TYPE_NULL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up our adapter
        mAdapter = new MyExpandableListAdapter();
        setListAdapter(mAdapter);
        final ExpandableListView lv = getExpandableListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

          @Override
          public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                long id, boolean checked) {
            int count = lv.getCheckedItemCount();
            if (count == 1) {
              expandableListSelectionType = ExpandableListView.getPackedPositionType(
                  lv.getExpandableListPosition(position));
            }
            mode.setTitle(String.valueOf(count));
            configureMenu(mode.getMenu(), count);
          }

          @Override
          public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //After orientation change,
            //setting expandableListSelectionType, as tried in setExpandableListSelectionType
            //does not work, because getExpandableListPosition does not return the correct value
            //probably because the adapter has not yet been set up correctly
            //thus we default to PACKED_POSITION_TYPE_GROUP
            //this workaround works because orientation change collapses the groups
            //so we never restore the CAB for PACKED_POSITION_TYPE_CHILD
            expandableListSelectionType = ExpandableListView.PACKED_POSITION_TYPE_GROUP;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context,menu);
            mode.setTitle(String.valueOf(lv.getCheckedItemCount()));
            mActionMode = mode;
            return true;
          }

          @Override
          public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            configureMenu(menu, lv.getCheckedItemCount());
            return false;
          }

          @Override
          public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            SparseBooleanArray checkedItemPositions = lv.getCheckedItemPositions();
            int checkedItemCount = checkedItemPositions.size();
            String msgAction="", msgObject="";
            if (checkedItemPositions != null) {
              for (int i=0; i<checkedItemCount; i++) {
                if (checkedItemPositions.valueAt(i)) {
                  int position = checkedItemPositions.keyAt(i);
                  ContextMenu.ContextMenuInfo info;
                  long pos = lv.getExpandableListPosition(position);
                  int groupPos = ExpandableListView.getPackedPositionGroup(pos);
                  if (ExpandableListView.getPackedPositionType(pos) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    msgObject = ": Group " + groupPos;
                  } else {
                    int childPos = ExpandableListView.getPackedPositionChild(pos);
                    msgObject = ": Child " + childPos + " in group " + groupPos;
                  }
                }
              }
            }
            switch(itemId) {
            case R.id.BulkChildCommand:
              msgAction = "Received bulk command on ";
              break;
            case R.id.BulkGroupCommand:
              msgAction = "Received bulk command on ";
              break;
            case R.id.SingleChildCommand:
              msgAction = "Received single command on ";
              break;
            case R.id.SingleGroupCommand:
              msgAction = "Received single command on ";
              break;
            }
            Toast.makeText(getBaseContext(), msgAction + msgObject, Toast.LENGTH_LONG).show();
            mode.finish();
            return true;
          }

          @Override
          public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
          }
        });
        lv.setOnGroupClickListener(this);
    }

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. 
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     *
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        private String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
        private String[][] children = {
                { "Arnold", "Barry", "Chuck", "David" },
                { "Ace", "Bandit", "Cha-Cha", "Deuce" },
                { "Fluffy", "Snuggles" },
                { "Goldy", "Bubbles" }
        };
        
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = (TextView)
                (convertView != null ?
                    convertView :
                    getLayoutInflater().inflate(R.layout.simple_expandable_list_item_1, null));  
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = (TextView)
                (convertView != null ?
                    convertView :
                    getLayoutInflater().inflate(R.layout.simple_expandable_list_item_1, null));
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
    }

    protected void configureMenu(Menu menu, int count) {
        boolean inGroup = expandableListSelectionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP;
        menu.setGroupVisible(R.id.MenuBulkGroup, inGroup);
        menu.setGroupVisible(R.id.MenuSingleGroup, inGroup && count==1);
        menu.setGroupVisible(R.id.MenuBulkChild, !inGroup);
        menu.setGroupVisible(R.id.MenuSingleChild, !inGroup && count==1);
      }
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
      if (mActionMode != null)  {
        if (expandableListSelectionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
          int flatPosition = parent.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
          parent.setItemChecked(
              flatPosition,
              !parent.isItemChecked(flatPosition));
          return true;
        }
      }
      return false;
    }
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
        int groupPosition, int childPosition, long id) {
      if (mActionMode != null)  {
        if (expandableListSelectionType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
          int flatPosition = parent.getFlatListPosition(
              ExpandableListView.getPackedPositionForChild(groupPosition,childPosition));
          parent.setItemChecked(
              flatPosition,
              !parent.isItemChecked(flatPosition));
        }
        return true;
      }
      return false;
    }
}
