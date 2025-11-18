package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models;

import android.net.Uri;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the content and layout metadata for a logical page inside the PDF composer.
 */
public class ImagePage {

    private static final int DEFAULT_COLUMNS = 3;

    private final List<PageItem> items = new ArrayList<>();
    private int rows = 1;
    private int columns = DEFAULT_COLUMNS;
    private boolean layoutCustomized = false;

    /**
     * Registers an image inside the page, replacing the first placeholder when available.
     * @param uri image URI chosen by the user
     */
    public void addImage(Uri uri) {
        if (uri == null || containsUri(uri)) {
            return;
        }

        int placeholderIndex = findFirstPlaceholderIndex();
        PageItem newItem = PageItem.fromUri(uri);

        if (placeholderIndex >= 0) {
            items.set(placeholderIndex, newItem);
        } else {
            items.add(newItem);
        }
    }

    /**
     * Inserts an explicit placeholder so the user can reserve empty space inside the grid.
     */
    public void addPlaceholder() {
        items.add(PageItem.placeholder());
    }

    /**
     * Removes multiple positions from the page. Positions must be zero based.
     * @param positions list of adapter positions to remove
     */
    public void removePositions(List<Integer> positions) {
        if (positions == null || positions.isEmpty()) {
            return;
        }

        positions.sort(Comparator.reverseOrder());
        for (int position : positions) {
            if (position >= 0 && position < items.size()) {
                items.remove(position);
            }
        }
    }

    /**
     * Eliminates all items contained on the page.
     */
    public void clear() {
        items.clear();
        rows = 1;
        columns = DEFAULT_COLUMNS;
        layoutCustomized = false;
    }

    /**
     * Returns an immutable view of the page slots for read-only operations.
     * @return unmodifiable list of page items
     */
    public List<PageItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Provides the mutable list used by adapters for direct data binding.
     * @return mutable list of page items
     */
    public List<PageItem> getMutableItems() {
        return items;
    }

    /**
     * Counts only the real images assigned to the page (ignoring placeholders).
     * @return number of actual images
     */
    public int getImageCount() {
        int count = 0;
        for (PageItem item : items) {
            if (!item.isPlaceholder()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Total slots including placeholders.
     * @return number of items currently tracked
     */
    public int getTotalSlots() {
        return items.size();
    }

    /**
     * Sets the grid layout preferred for rendering this page.
     * @param rows desired amount of rows (min 1)
     * @param columns desired amount of columns (min 1)
     */
    public void setLayout(int rows, int columns) {
        this.rows = Math.max(1, rows);
        this.columns = Math.max(1, columns);
        this.layoutCustomized = true;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean isLayoutCustomized() {
        return layoutCustomized;
    }

    /**
     * Calculates the minimum amount of rows required to host all items with the current column count.
     * @return effective row count when rendering
     */
    public int getEffectiveRows() {
        int required = (int) Math.ceil(items.size() / (double) Math.max(1, columns));
        return Math.max(rows, Math.max(1, required));
    }

    private int findFirstPlaceholderIndex() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isPlaceholder()) {
                return i;
            }
        }
        return -1;
    }

    private boolean containsUri(Uri uri) {
        for (PageItem item : items) {
            if (!item.isPlaceholder() && uri.equals(item.getUri())) {
                return true;
            }
        }
        return false;
    }
}
