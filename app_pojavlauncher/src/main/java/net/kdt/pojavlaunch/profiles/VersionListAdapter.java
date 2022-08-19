package net.kdt.pojavlaunch.profiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.utils.FilteredSubList;

import java.util.List;

public class VersionListAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {
    
    private final LayoutInflater mLayoutInflater;
    
    private final String[] mGroups = new String[]{"Release", "Snapshot","Old Beta", "Old Alpha"};
    private final List<JMinecraftVersionList.Version> mReleaseList, mSnapshotList, mBetaList, mAlphaList;
    private final List<JMinecraftVersionList.Version>[] mData;

    public VersionListAdapter(JMinecraftVersionList versionList, Context ctx){
        mLayoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mReleaseList = new FilteredSubList<>(versionList.versions, item -> item.type.equals("release"));
        mSnapshotList = new FilteredSubList<>(versionList.versions, item -> item.type.equals("snapshot"));
        mBetaList = new FilteredSubList<>(versionList.versions, item -> item.type.equals("old_beta"));
        mAlphaList = new FilteredSubList<>(versionList.versions, item -> item.type.equals("old_alpha"));

        mData = new List[]{mReleaseList, mSnapshotList, mBetaList, mAlphaList};
    }

    @Override
    public int getGroupCount() {
        return mGroups.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData[groupPosition].size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mData[groupPosition].get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = mLayoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);

        ((TextView) convertView).setText(mGroups[groupPosition]);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = mLayoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);

        ((TextView) convertView).setText(mData[groupPosition].get(childPosition).id);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
