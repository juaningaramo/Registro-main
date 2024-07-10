package es.travelworld.registro;

import static android.app.ProgressDialog.show;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private ImageButton imageButton;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextInputLayout usernameInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private ActivityResultLauncher<Intent> activityResultLauncherCamara;

    private TextView conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.tiet_nombre);
        passwordEditText = findViewById(R.id.tiet_apellido);
        usernameInputLayout = findViewById(R.id.tv_nombre);
        passwordInputLayout = findViewById(R.id.tv_apellido);
        loginButton = findViewById(R.id.b_avanzar);
        conditions = findViewById(R.id.tv_linkcondiciones);

        // Deshabilitamos el botón de login al inicio
        loginButton.setEnabled(false);

        // Creamos un TextWatcher para monitorear los cambios en los campos de texto
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields();
                checkFieldsForEmptyValues();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Añadimos el TextWatcher a los campos de texto
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        // Configuraciones adicionales (como el TextInputEditText para edad y el ImageButton para cámara)
        setupAdditionalUI();

        activityResultLauncherCamara = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        imageButton.setImageBitmap(imageBitmap);
                    }
                });

        imageButton = findViewById(R.id.ib_camara);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://developers.google.com/ml-kit/terms"));
                startActivity(browserIntent);
            }
        });
    }

    // Método para verificar si los campos están vacíos
    private void checkFieldsForEmptyValues() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        loginButton.setEnabled(!username.isEmpty() && !password.isEmpty());
    }

    private void validateFields() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String regex = "^[a-zA-Z]+$";

        if (!username.matches(regex)) {
            usernameInputLayout.setError("Solo se permiten letras");
        } else {
            usernameInputLayout.setError(null);
        }

        if (!password.matches(regex)) {
            passwordInputLayout.setError("Solo se permiten letras");
        } else {
            passwordInputLayout.setError(null);
        }
    }

    private void setupAdditionalUI() {
        final TextInputEditText textInputEditText = findViewById(R.id.tiet_edad);
        final String[] opciones = {"0-5", "6-11", "12-17", "18-99"};

        textInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("¿Cuántos años tienes?");
                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < 3) {
                            Toast.makeText(MainActivity.this, "Esta aplicación no es para ti", Toast.LENGTH_SHORT).show();
                        } else {
                            String selectedOption = opciones[which];
                            textInputEditText.setText(selectedOption);
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("depurando", "No tengo permiso para la cámara");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Log.d("depurando", "Tengo permiso para la cámara");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activityResultLauncherCamara.launch(intent);
        }
    }

    // Manejar la solicitud de permiso de la cámara
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

}