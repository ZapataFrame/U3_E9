package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.R;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.PageItem;

/**
 * Adaptador personalizado para mostrar imágenes y espacios en blanco dentro de
 * una cuadrícula
 * con soporte de selección múltiple.
 */
public class ImageGridAdapter extends BaseAdapter {

    private static final int CELL_SIZE = 320;

    private final Context context;
    private List<PageItem> pageItems;
    private final LayoutInflater inflater;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private OnImageClickListener onImageClickListener;
    private SelectionListener selectionListener;
    private int columnCount = 3;

    /**
     * Interface para manejar clicks en las imágenes de la cuadrícula.
     */
    public interface OnImageClickListener {
        void onImageClick(PageItem item, int position);
    }

    /**
     * Listener notificado cuando cambia el estado de selección múltiple.
     */
    public interface SelectionListener {
        void onSelectionChanged(boolean hasSelection);
    }

    public ImageGridAdapter(Context context, List<PageItem> pageItems) {
        this.context = context;
        this.pageItems = pageItems;
        this.inflater = LayoutInflater.from(context);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public void updateItems(List<PageItem> newItems, int newColumnCount) {
        this.pageItems = newItems;
        this.columnCount = Math.max(1, newColumnCount);
        if (!selectedPositions.isEmpty()) {
            selectedPositions.clear();
            notifySelectionListener();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pageItems != null ? pageItems.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return pageItems != null ? pageItems.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_image_cell, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final PageItem item = pageItems.get(position);

        int parentWidth = parent.getWidth();
        int cellSize = parentWidth > 0 ? Math.max(120, parentWidth / columnCount) : CELL_SIZE;
        convertView.setLayoutParams(new AbsListView.LayoutParams(cellSize, cellSize));

        if (item.isPlaceholder()) {
            holder.placeholderLabel.setVisibility(View.VISIBLE);
            holder.imageView.setImageDrawable(null);
        } else if (item.getUri() != null) {
            holder.placeholderLabel.setVisibility(View.GONE);
            Picasso.get()
                    .load(item.getUri())
                    .resize(cellSize, cellSize)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imageView);
        } else {
            holder.placeholderLabel.setVisibility(View.GONE);
            holder.imageView.setImageDrawable(null);
        }

        holder.selectionOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);
        holder.iconCheck.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedPositions.isEmpty()) {
                    toggleSelection(position, v);
                } else if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(item, position);
                }
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleSelection(position, v);
                return true;
            }
        });

        return convertView;
    }

    public void toggleSelection(int position, View view) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifySelectionListener();

        // Update visual state directly to avoid full refresh delay
        if (view != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null) {
                boolean isSelected = isSelected(position);
                holder.selectionOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                holder.iconCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void clearSelection() {
        if (!selectedPositions.isEmpty()) {
            selectedPositions.clear();
            notifySelectionListener();
        }
    }

    public boolean hasSelection() {
        return !selectedPositions.isEmpty();
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedPositions);
    }

    private boolean isSelected(int position) {
        return selectedPositions.contains(position);
    }

    private void notifySelectionListener() {
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(hasSelection());
        }
    }

    private static class ViewHolder {
        final ImageView imageView;
        final TextView placeholderLabel;
        final View selectionOverlay;
        final ImageView iconCheck;

        ViewHolder(View root) {
            imageView = root.findViewById(R.id.image_content);
            placeholderLabel = root.findViewById(R.id.placeholder_label);
            selectionOverlay = root.findViewById(R.id.selection_overlay);
            iconCheck = root.findViewById(R.id.icon_check);
        }
    }
}
