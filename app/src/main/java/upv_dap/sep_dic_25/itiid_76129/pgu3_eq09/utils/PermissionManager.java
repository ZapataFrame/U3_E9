package upv_dap.sep_dic_25.itiid_76129.pgu3_eq09.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Clase utilitaria para manejo de permisos de almacenamiento externo
 * Gestiona los permisos necesarios para leer y escribir archivos
 */
public class PermissionManager {
    
    private static final String TAG = "PERMISSION_TAG";
    private static final int STORAGE_PERMISSION_CODE = 100;
    
    private Activity activity;
    
    /**
     * Constructor que recibe la actividad para manejo de permisos
     * @param activity Actividad desde donde se solicitan los permisos
     */
    public PermissionManager(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Verifica si los permisos de almacenamiento están concedidos
     * @return true si los permisos están concedidos, false en caso contrario
     */
    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            // Para versiones anteriores a Android 11
            int readPermission = ContextCompat.checkSelfPermission(activity, 
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(activity, 
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED && 
                   writePermission == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Solicita los permisos de almacenamiento al usuario
     */
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "requestPermission: try");
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Log.e(TAG, "requestPermission: ", e);
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            // Para versiones anteriores a Android 11
            String[] permissions = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_CODE);
        }
    }
    
    /**
     * Maneja el resultado de la solicitud de permisos
     * @param requestCode Código de la solicitud
     * @param permissions Array de permisos solicitados
     * @param grantResults Array con los resultados de cada permiso
     * @return true si se concedieron los permisos, false en caso contrario
     */
    public boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                // Verificar si los permisos fueron concedidos
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                
                if (write && read) {
                    // Permisos de almacenamiento externo concedidos
                    Log.d(TAG, "handlePermissionResult: External Storage Permission granted");
                    showToast("External Storage Permission granted");
                    return true;
                } else {
                    // Permisos de almacenamiento externo denegados
                    Log.d(TAG, "handlePermissionResult: External Storage Permission denied...");
                    showToast("External Storage Permission denied...");
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * Muestra un mensaje toast corto
     * @param message Mensaje a mostrar
     */
    private void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Obtiene el código de solicitud de permisos de almacenamiento
     * @return Código de solicitud de permisos
     */
    public static int getStoragePermissionCode() {
        return STORAGE_PERMISSION_CODE;
    }
}
