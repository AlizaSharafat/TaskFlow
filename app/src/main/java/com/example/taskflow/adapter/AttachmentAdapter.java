package com.example.taskflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskflow.R;
import com.example.taskflow.model.AttachmentCatalog;
import com.example.taskflow.model.AttachmentLink;
import com.example.taskflow.model.AttachmentType;
import com.example.taskflow.model.CatalogItem;

import java.util.ArrayList;
import java.util.List;

public class AttachmentAdapter extends BaseAdapter {

    public interface OnPreview {
        void onPreview(AttachmentLink link);
    }

    public interface OnRemove {
        void onRemove(AttachmentLink link);
    }

    private final Context context;
    private final OnPreview onPreview;
    private final OnRemove onRemove;
    private List<AttachmentLink> rows;

    public AttachmentAdapter(Context context, List<AttachmentLink> rows,
                             OnPreview onPreview, OnRemove onRemove) {
        this.context = context;
        this.rows = new ArrayList<>(rows);
        this.onPreview = onPreview;
        this.onRemove = onRemove;
    }

    public void submit(List<AttachmentLink> newRows) {
        this.rows = new ArrayList<>(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public AttachmentLink getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_attachment, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        final AttachmentLink link = rows.get(position);
        CatalogItem item = AttachmentCatalog.find(link.getCatalogId());

        if (item == null) {
            holder.name.setText(R.string.missing_attachment);
            holder.meta.setText("");
            holder.thumb.setImageResource(R.drawable.ic_doc);
            return view;
        }

        holder.name.setText(item.getDisplayName());
        holder.meta.setText(context.getString(R.string.attachment_meta,
                item.getType().getLabel(), item.getSizeLabel()));

        if (item.getType() == AttachmentType.IMAGE) {
            holder.thumb.setImageResource(item.getImageRes());
            holder.thumb.setBackgroundResource(R.drawable.bg_thumb_image);
        } else {
            holder.thumb.setImageResource(R.drawable.ic_doc);
            holder.thumb.setBackgroundResource(R.drawable.bg_thumb_text);
        }

        holder.preview.setOnClickListener(v -> onPreview.onPreview(link));
        holder.remove.setOnClickListener(v -> onRemove.onRemove(link));

        return view;
    }

    private static class ViewHolder {

        final ImageView thumb;
        final TextView name;
        final TextView meta;
        final TextView preview;
        final ImageView remove;

        ViewHolder(View view) {
            thumb = view.findViewById(R.id.ivAttachmentThumb);
            name = view.findViewById(R.id.tvAttachmentName);
            meta = view.findViewById(R.id.tvAttachmentMeta);
            preview = view.findViewById(R.id.btnPreview);
            remove = view.findViewById(R.id.btnRemoveAttachment);
        }
    }
}
