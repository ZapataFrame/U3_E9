package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models;

import android.net.Uri;
import androidx.annotation.Nullable;

/**
 * Represents a single slot in a page, either holding an image URI or a blank placeholder.
 */
public class PageItem {

    private final Uri uri;
    private final boolean placeholder;

    private PageItem(@Nullable Uri uri, boolean placeholder) {
        this.uri = uri;
        this.placeholder = placeholder;
    }

    /**
     * Creates a new PageItem containing a real image.
     * @param uri image location to render inside the page slot
     * @return populated PageItem
     */
    public static PageItem fromUri(Uri uri) {
        return new PageItem(uri, false);
    }

    /**
     * Creates a blank placeholder slot that reserves space inside the grid.
     * @return placeholder PageItem
     */
    public static PageItem placeholder() {
        return new PageItem(null, true);
    }

    /**
     * Indicates whether this slot references a blank placeholder.
     * @return true when slot is intentionally empty
     */
    public boolean isPlaceholder() {
        return placeholder;
    }

    /**
     * Provides the URI associated with the slot when it is not a placeholder.
     * @return image URI or null when placeholder
     */
    @Nullable
    public Uri getUri() {
        return uri;
    }
}
