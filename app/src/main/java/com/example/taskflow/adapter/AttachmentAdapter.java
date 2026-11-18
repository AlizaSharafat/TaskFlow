package com.example.taskflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.R;
import com.example.taskflow.model.AttachmentCatalog;
import com.example.taskflow.model.AttachmentLink;
import com.example.taskflow.model.AttachmentType;
import com.example.taskflow.model.CatalogItem;

/**
 * Feeds the attachment RecyclerView in the Files tab.
 *
 * Image rows show a real thumbnail of the linked drawable, text rows show a
 * document icon. Preview and remove stay separate click targets on the row, so
 * a swipe is not the only way to remove something.
 *
 * Like the project list this is a ListAdapter, so updates run through DiffUtil
 * and notifyDataSetChanged is never called.
 */
public class AttachmentAdapter
        extends ListAdapter<AttachmentLink, AttachmentAdapter.AttachmentViewHolder> {

    public interface OnPreview {
        void onPreview(AttachmentLink link);
    }

    public interface OnRemove {
        void onRemove(AttachmentLink link);
    }

    /**
     * A link is identified by its own id, not by the catalog id: the same
     * catalog item could in principle be linked twice. Nothing inside a link
     * is editable, so two links with the same id always look the same.
     */
    private static final DiffUtil.ItemCallback<AttachmentLink> DIFF =
            new DiffUtil.ItemCallback<AttachmentLink>() {

                @Override
                public boolean areItemsTheSame(@NonNull AttachmentLink oldItem,
                                               @NonNull AttachmentLink newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull AttachmentLink oldItem,
                                                  @NonNull AttachmentLink newItem) {
                    return oldItem.getCatalogId().equals(newItem.getCatalogId());
                }
            };

    private final OnPreview onPreview;
    private final OnRemove onRemove;

    public AttachmentAdapter(OnPreview onPreview, OnRemove onRemove) {
        super(DIFF);
        this.onPreview = onPreview;
        this.onRemove = onRemove;
    }

    /** Null rather than an exception, because a swipe can outlive a row. */
    public AttachmentLink linkAt(int position) {
        if (position < 0 || position >= getItemCount()) {
            return null;
        }
        return getItem(position);
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        holder.bind(getItem(position), onPreview, onRemove);
    }

    static class AttachmentViewHolder extends RecyclerView.ViewHolder {

        private final ImageView thumb;
        private final TextView name;
        private final TextView meta;
        private final TextView preview;
        private final ImageView remove;

        AttachmentViewHolder(@NonNull View view) {
            super(view);
            thumb = view.findViewById(R.id.ivAttachmentThumb);
            name = view.findViewById(R.id.tvAttachmentName);
            meta = view.findViewById(R.id.tvAttachmentMeta);
            preview = view.findViewById(R.id.btnPreview);
            remove = view.findViewById(R.id.btnRemoveAttachment);
        }

        void bind(final AttachmentLink link, final OnPreview onPreview, final OnRemove onRemove) {
            CatalogItem item = AttachmentCatalog.find(link.getCatalogId());

            if (item == null) {
                name.setText(R.string.missing_attachment);
                meta.setText("");
                thumb.setImageResource(R.drawable.ic_doc);
                preview.setOnClickListener(null);
                remove.setOnClickListener(v -> onRemove.onRemove(link));
                return;
            }

            name.setText(item.getDisplayName());
            meta.setText(itemView.getContext().getString(R.string.attachment_meta,
                    item.getType().getLabel(), item.getSizeLabel()));

            if (item.getType() == AttachmentType.IMAGE) {
                thumb.setImageResource(item.getImageRes());
                thumb.setBackgroundResource(R.drawable.bg_thumb_image);
            } else {
                thumb.setImageResource(R.drawable.ic_doc);
                thumb.setBackgroundResource(R.drawable.bg_thumb_text);
            }

            preview.setOnClickListener(v -> onPreview.onPreview(link));
            remove.setOnClickListener(v -> onRemove.onRemove(link));
        }
    }
}
