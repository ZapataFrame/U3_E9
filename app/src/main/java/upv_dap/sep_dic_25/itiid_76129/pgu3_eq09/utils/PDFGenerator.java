package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.ImagePage;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.PageItem;

/**
 * Clase utilitaria para generar documentos esto solo es para acompletar el PDF con collages de imágenes
 * Convierte una lista de imágenes en un PDF organizado en cuadrícula
 */
public class PDFGenerator {
    
    private static final String TAG = "PDFGenerator";
    private static final int PDF_SCALE = 2;
    /**
     * Tipos de página soportados para la exportación del PDF.
     */
    public enum PageSize {
        LETTER(612 * PDF_SCALE, 792 * PDF_SCALE, "carta"),
        LEGAL(612 * PDF_SCALE, 1008 * PDF_SCALE, "oficio");

        private final int width;
        private final int height;
        private final String suffix;

        PageSize(int width, int height, String suffix) {
            this.width = width;
            this.height = height;
            this.suffix = suffix;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    /**
     * Genera un PDF con las páginas proporcionadas organizadas como se muestran en pantalla.
     * @param context Contexto de la aplicación
     * @param pages Conjunto de páginas con sus imágenes
     * @param pageSize Tamaño objetivo del PDF
     */
    public static void generatePDF(Context context, List<ImagePage> pages, PageSize pageSize) {
        if (pages == null || pages.isEmpty()) {
            Toast.makeText(context, "No hay páginas para generar", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        boolean containsImages = false;

        for (int index = 0; index < pages.size(); index++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                pageSize.getWidth(),
                pageSize.getHeight(),
                index + 1
            ).create();

            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            ImagePage currentPage = pages.get(index);
            if (currentPage.getImageCount() > 0) {
                containsImages = true;
            }
            drawPage(canvas, context, paint, currentPage, pageSize);

            pdfDocument.finishPage(page);
        }

        if (!containsImages) {
            pdfDocument.close();
            Toast.makeText(context, "Agrega al menos una imagen antes de generar el PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        savePDF(context, pdfDocument, pageSize);
        pdfDocument.close();
    }

    private static void drawPage(Canvas canvas, Context context, Paint paint, ImagePage page, PageSize pageSize) {
        List<PageItem> items = page.getItems();
        if (items.isEmpty()) {
            return;
        }

        int columns = Math.max(1, page.getColumns());
        int rows = page.getEffectiveRows();
        int requiredRows = (int) Math.ceil(items.size() / (double) columns);
        rows = Math.max(rows, requiredRows);

        float cellWidth = pageSize.getWidth() / (float) columns;
        float cellHeight = pageSize.getHeight() / (float) rows;

        for (int i = 0; i < items.size(); i++) {
            PageItem item = items.get(i);
            if (item.isPlaceholder() || item.getUri() == null) {
                continue;
            }

            int column = i % columns;
            int row = i / columns;

            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(context.getContentResolver(), item.getUri());
                if (original == null) {
                    continue;
                }

                float scale = Math.min(
                    cellWidth / original.getWidth(),
                    cellHeight / original.getHeight()
                );

                int targetWidth = Math.max(1, Math.round(original.getWidth() * scale));
                int targetHeight = Math.max(1, Math.round(original.getHeight() * scale));

                Bitmap scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true);

                float drawX = column * cellWidth + (cellWidth - targetWidth) / 2f;
                float drawY = row * cellHeight + (cellHeight - targetHeight) / 2f;

                canvas.drawBitmap(scaled, drawX, drawY, paint);
                Log.d(TAG, "Image pasted at X: " + drawX + " Y: " + drawY);

                if (scaled != original) {
                    scaled.recycle();
                }
                original.recycle();

            } catch (Exception e) {
                Log.e(TAG, "Error processing image for PDF", e);
            }
        }
    }

    /**
     * Guarda el documento PDF en el sistema de archivos
     * @param context Contexto de la aplicación
     * @param pdfDocument Documento PDF a guardar
     * @param pageSize Tamaño de página utilizado
     */
    private static void savePDF(Context context, PdfDocument pdfDocument, PageSize pageSize) {
        String directory = "Documents";
        String folderName = "HomeroImageArranger";
        String fileName = "collage_" + pageSize.getSuffix() + ".pdf";
        File file = new File(Environment.getExternalStorageDirectory(),
            directory + "/" + folderName + "/" + fileName);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            pdfDocument.writeTo(fileOutputStream);
            fileOutputStream.close();

            Toast.makeText(context, "PDF generado en: " + file.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
            Log.d(TAG, "PDF saved successfully at: " + file.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Failed to save PDF", e);
            Toast.makeText(context, "No fue posible generar el PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
