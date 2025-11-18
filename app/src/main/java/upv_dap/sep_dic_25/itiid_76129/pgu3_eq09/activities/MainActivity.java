package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.R;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.adapters.ImageGridAdapter;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.ImageMetadata;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.ImagePage;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.PageItem;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.utils.ImageMetadataExtractor;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.utils.PDFGenerator;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.utils.PermissionManager;

/**
 * Actividad principal de la aplicación HomeroImageArranger.
 * Gestiona selección, disposición y exportación de imágenes a PDF.
 */
public class MainActivity extends AppCompatActivity implements ImageGridAdapter.OnImageClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_SELECTION = 100;
    private static final String DEFAULT_METADATA_MESSAGE = "Toca una imagen para ver metadatos";

    private Button selectImagesButton;
    private Button generatePdfButton;
    private Button clearButton;
    private Button nextPageButton;
    private Button previousPageButton;
    private Button addPageButton;
    private Button layoutButton;
    private Button addPlaceholderButton;
    private Button deleteSelectedButton;
    private GridView imagesGridView;
    private TextView metadataTextView;
    private TextView pageInfoTextView;
    private Spinner pageSizeSpinner;

    private PermissionManager permissionManager;
    private ImageGridAdapter imageAdapter;
    private final List<ImagePage> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private PDFGenerator.PageSize currentPageSize = PDFGenerator.PageSize.LETTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initializeComponents();
        setupUI();
        checkAndRequestPermissions();
    }

    private void initializeComponents() {
        pages.clear();
        pages.add(new ImagePage());
        currentPageIndex = 0;
        permissionManager = new PermissionManager(this);

        selectImagesButton = findViewById(R.id.btn_select_images);
        generatePdfButton = findViewById(R.id.btn_generate_pdf);
        clearButton = findViewById(R.id.btn_clear);
        nextPageButton = findViewById(R.id.btn_next_page);
        previousPageButton = findViewById(R.id.btn_prev_page);
        addPageButton = findViewById(R.id.btn_add_page);
        layoutButton = findViewById(R.id.btn_layout);
        addPlaceholderButton = findViewById(R.id.btn_add_placeholder);
        deleteSelectedButton = findViewById(R.id.btn_delete_selected);
        imagesGridView = findViewById(R.id.grid_images);
        metadataTextView = findViewById(R.id.tv_metadata);
        pageInfoTextView = findViewById(R.id.tv_page_info);
        pageSizeSpinner = findViewById(R.id.spinner_page_size);

        deleteSelectedButton.setEnabled(false);

        imageAdapter = new ImageGridAdapter(this, getCurrentPage().getMutableItems());
        imageAdapter.setOnImageClickListener(this);
        imageAdapter.setSelectionListener(hasSelection -> {
            deleteSelectedButton.setEnabled(hasSelection);
            if (!hasSelection) {
                resetMetadataMessage();
            }
        });
    }

    private void setupUI() {
        imagesGridView.setAdapter(imageAdapter);
        imagesGridView.setNumColumns(getCurrentPage().getColumns());
        metadataTextView.setText(DEFAULT_METADATA_MESSAGE);
        updatePageInfo();
        updateLayoutButtonLabel();

        selectImagesButton.setOnClickListener(v -> openImageSelector());
        generatePdfButton.setOnClickListener(v -> generatePDF());
        clearButton.setOnClickListener(v -> clearWorkspace());
        addPageButton.setOnClickListener(v -> addNewPageAfterCurrent());
        nextPageButton.setOnClickListener(v -> moveToNextPage());
        previousPageButton.setOnClickListener(v -> moveToPreviousPage());
        addPlaceholderButton.setOnClickListener(v -> addPlaceholderToCurrentPage());
        deleteSelectedButton.setOnClickListener(v -> removeSelectedImages());
        layoutButton.setOnClickListener(v -> showLayoutSelectionDialog());

        ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.page_size_options,
            android.R.layout.simple_spinner_item
        );
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pageSizeSpinner.setAdapter(sizeAdapter);
        pageSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPageSize = position == 0 ? PDFGenerator.PageSize.LETTER : PDFGenerator.PageSize.LEGAL;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Mantener tamaño actual
            }
        });
    }
    
    /**
     * Verifica y solicita permisos necesarios como en la aplicación original
     */
    private void checkAndRequestPermissions() {
        if (permissionManager.checkPermission()) {
            Log.d(TAG, "onCreate: Permission already granted, create folder");
            createFolder();
        } else {
            Log.d(TAG, "onCreate: Permission was not granted, request");
            permissionManager.requestPermission();
        }
    }
    
    /**
     * Crea la carpeta para almacenar archivos como en la aplicación original
     */
    private void createFolder() {
        String directory = "Documents";
        String folderName = "HomeroImageArranger";
        File file = new File(Environment.getExternalStorageDirectory(), directory + "/" + folderName);
        
        boolean folderCreated = file.mkdirs();
        if (folderCreated || file.exists()) {
            showToast("Folder Created: " + file.getAbsolutePath());
        } else {
            showToast("Folder not created....");
        }
    }
    
    /**
     * Abre el selector de múltiples imágenes
     */
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        
        startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_IMAGE_SELECTION);
    }
    
    private void generatePDF() {
        if (!hasAnyImages()) {
            showToast("Selecciona al menos una imagen antes de generar el PDF");
            return;
        }

        PDFGenerator.generatePDF(this, pages, currentPageSize);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_SELECTION && resultCode == RESULT_OK && data != null) {
            processSelectedImages(data);
        }
    }
    
    /**
     * Procesa las imágenes seleccionadas por el usuario
     */
    private void processSelectedImages(Intent data) {
        ImagePage currentPage = getCurrentPage();
        int previousCount = currentPage.getImageCount();
        if (data.getClipData() != null) {
            // Múltiples imágenes seleccionadas
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                currentPage.addImage(imageUri);
            }
        } else if (data.getData() != null) {
            // Una sola imagen seleccionada
            currentPage.addImage(data.getData());
        }

        refreshGrid();
        Log.d(TAG, "Selected " + currentPage.getImageCount() + " images on current page");
        maybePromptLayoutSelection(previousCount, currentPage);
    }
    
    @Override
    public void onImageClick(PageItem item, int position) {
        if (item.isPlaceholder()) {
            metadataTextView.setText("Espacio en blanco reservado");
            return;
        }

        Uri imageUri = item.getUri();
        if (imageUri == null) {
            metadataTextView.setText("No es posible obtener metadatos");
            return;
        }

        ImageMetadata metadata = ImageMetadataExtractor.extractMetadata(this, imageUri);
        if (metadata != null) {
            metadataTextView.setText(metadata.toString());
            Log.d(TAG, "Metadata: " + metadata.toString());
        } else {
            metadataTextView.setText("No es posible obtener metadatos");
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        // Manejar resultado de permisos para versiones anteriores a Android 11
        if (permissionManager.handlePermissionResult(requestCode, permissions, grantResults)) {
            createFolder();
        }
    }
    
    /**
     * Muestra un mensaje toast corto
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private ImagePage getCurrentPage() {
        return pages.get(currentPageIndex);
    }

    private void refreshGrid() {
        imageAdapter.updateItems(getCurrentPage().getMutableItems(), getCurrentPage().getColumns());
        imagesGridView.setNumColumns(getCurrentPage().getColumns());
        updatePageInfo();
        updateLayoutButtonLabel();
    }

    private void updatePageInfo() {
        String info = String.format(Locale.getDefault(),
            "Página %d de %d | Imágenes: %d",
            currentPageIndex + 1,
            pages.size(),
            getCurrentPage().getImageCount());
        pageInfoTextView.setText(info);
    }

    private void updateLayoutButtonLabel() {
        ImagePage page = getCurrentPage();
        layoutButton.setText(String.format(Locale.getDefault(), "Rejilla %dx%d",
            page.getEffectiveRows(), page.getColumns()));
    }

    private void clearWorkspace() {
        pages.clear();
        pages.add(new ImagePage());
        currentPageIndex = 0;
        refreshGrid();
        resetMetadataMessage();
    }

    private void addNewPageAfterCurrent() {
        pages.add(currentPageIndex + 1, new ImagePage());
        currentPageIndex++;
        refreshGrid();
        resetMetadataMessage();
    }

    private void moveToNextPage() {
        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            refreshGrid();
            resetMetadataMessage();
        } else {
            showToast("Ya estás en la última página");
        }
    }

    private void moveToPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            refreshGrid();
            resetMetadataMessage();
        } else {
            showToast("Ya estás en la primera página");
        }
    }

    private boolean hasAnyImages() {
        for (ImagePage page : pages) {
            if (page.getImageCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private void resetMetadataMessage() {
        metadataTextView.setText(DEFAULT_METADATA_MESSAGE);
    }

    private void addPlaceholderToCurrentPage() {
        getCurrentPage().addPlaceholder();
        refreshGrid();
        resetMetadataMessage();
    }

    private void removeSelectedImages() {
        List<Integer> positions = imageAdapter.getSelectedPositions();
        if (positions.isEmpty()) {
            showToast("Selecciona elementos para eliminar");
            return;
        }

        Collections.sort(positions);
        getCurrentPage().removePositions(positions);
        refreshGrid();
        resetMetadataMessage();
        showToast("Elementos eliminados");
    }

    private void showLayoutSelectionDialog() {
        ImagePage page = getCurrentPage();
        List<GridOption> options = buildGridOptions(Math.max(1, page.getTotalSlots()));
        CharSequence[] labels = new CharSequence[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = options.get(i).label;
        }

        new AlertDialog.Builder(this)
            .setTitle("Selecciona la rejilla")
            .setItems(labels, (dialog, which) -> {
                GridOption option = options.get(which);
                page.setLayout(option.rows, option.columns);
                refreshGrid();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private List<GridOption> buildGridOptions(int slotCount) {
        List<GridOption> options = new ArrayList<>();
        addOption(options, 1, slotCount);
        if (slotCount > 1) {
            addOption(options, slotCount, 1);
        }

        int maxDimension = Math.min(slotCount, 6);
        for (int rows = 2; rows <= maxDimension; rows++) {
            int columns = (int) Math.ceil(slotCount / (double) rows);
            addOption(options, rows, columns);
            addOption(options, columns, rows);
        }

        options.sort((a, b) -> {
            int areaCompare = Integer.compare(a.rows * a.columns, b.rows * b.columns);
            if (areaCompare != 0) {
                return areaCompare;
            }
            return Integer.compare(a.rows, b.rows);
        });

        return options;
    }

    private void addOption(List<GridOption> options, int rows, int columns) {
        rows = Math.max(1, rows);
        columns = Math.max(1, columns);
        for (GridOption option : options) {
            if (option.rows == rows && option.columns == columns) {
                return;
            }
        }
        options.add(new GridOption(rows, columns));
    }

    private void maybePromptLayoutSelection(int previousCount, ImagePage page) {
        if (previousCount <= 3 && page.getImageCount() > 3 && !page.isLayoutCustomized()) {
            showLayoutSelectionDialog();
        }
    }

    private static class GridOption {
        final int rows;
        final int columns;
        final String label;

        GridOption(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            this.label = String.format(Locale.getDefault(), "%dx%d", rows, columns);
        }
    }
}
