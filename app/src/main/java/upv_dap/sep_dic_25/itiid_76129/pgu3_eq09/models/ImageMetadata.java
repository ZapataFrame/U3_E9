package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.models;

/**
 * Clase que representa los metadatos de una imagen
 * Almacena información sobre fecha, ancho y largo de imágenes seleccionadas
 */
public class ImageMetadata {
    private String dateTime;
    private String width;
    private String height;

    /**
     * Constructor para crear objeto de metadatos de imagen
     * @param dateTime Fecha y hora de captura de la imagen
     * @param width Ancho de la imagen en píxeles
     * @param height Alto de la imagen en píxeles
     */
    public ImageMetadata(String dateTime, String width, String height) {
        this.dateTime = dateTime;
        this.width = width;
        this.height = height;
    }

    // Getters y setters para los metadatos de la imagen
    
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * Convierte los metadatos a string legible
     * @return String con información formateada de los metadatos
     */
    @Override
    public String toString() {
        return "DateTime: " + dateTime + 
               "\nWidth: " + width + 
               "\nHeight: " + height;
    }
}
