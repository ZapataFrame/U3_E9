package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.utils;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models.ImageMetadata;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase utilitaria para extraer metadatos de imágenes
 * Proporciona funciones para obtener información EXIF de archivos de imagen
 */
public class ImageMetadataExtractor {
    
    private static final String TAG = "ImageMetadataExtractor";
    
    /**
     * Extrae los metadatos EXIF de una imagen mediante su URI
     * @param context Contexto de la aplicación para acceder al ContentResolver
     * @param uri URI de la imagen de la cual extraer metadatos
     * @return Objeto ImageMetadata con la información extraída
     */
    public static ImageMetadata extractMetadata(Context context, Uri uri) {
        ImageMetadata metadata = null;
        
        try {
            // Usar ContentResolver para obtener InputStream de la imagen
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            
            if (inputStream != null) {
                // Crear ExifInterface desde el InputStream
                ExifInterface exif = new ExifInterface(inputStream);
                
                // Extraer metadatos específicos de la imagen
                String imageTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                String imageWidth = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                String imageHeight = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                
                // Crear objeto ImageMetadata con los datos extraídos
                metadata = new ImageMetadata(imageTime, imageWidth, imageHeight);
                
                Log.d(TAG, "Metadata extracted successfully for URI: " + uri.toString());
                
                // Cerrar el InputStream
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error extracting metadata from image", e);
            e.printStackTrace();
            
            // Retornar metadatos con valores por defecto en caso de error
            metadata = new ImageMetadata("Unknown", "Unknown", "Unknown");
        }
        
        return metadata;
    }
    
    /**
     * Valida si una URI corresponde a una imagen válida
     * @param context Contexto para acceder al ContentResolver
     * @param uri URI a validar
     * @return true si la URI es válida y accesible, false en caso contrario
     */
    public static boolean isValidImageUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Invalid image URI: " + uri.toString(), e);
        }
        return false;
    }
}
