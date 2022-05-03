package com.example.parcial2_juan_carlos_meja_morales;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parcial2_juan_carlos_meja_morales.ConexionSQLite.DBSQLite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductosDetalle extends AppCompatActivity {
    DBSQLite miDB;
    String accion = "nuevo";
    String idProducto = "0";
    ImageView imgProducto;
    String urlCompletaImg;
    Button btnProductos;
    Intent takePictureIntent;
    private ImageView mUploadPicture;
    private ImageView mImageView;
    private Bitmap mCaptureOrUploadBitmap = null;
    private String mProductImagePath = null;
    String productImagePath;


    private static final int UPLOAD_PICTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos_detalle);

        mImageView = (ImageView) findViewById(R.id.imgProducto);
        imgProducto = findViewById(R.id.imgProducto);

        btnProductos = findViewById(R.id.btnMostrar);
        btnProductos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MostrarListaProductos();
            }
        });

        GuardarDatosPrductos();
        MostrarDatosProductos();
        TomarFotoProducto();


        mUploadPicture = (ImageView) findViewById(R.id.suburImage);
        mUploadPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                if (Build.VERSION.SDK_INT > 19) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }

                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), UPLOAD_PICTURE);
            }
        });

    }


    void TomarFotoProducto() {
        imgProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (Exception ex) {
                    }
                    if (photoFile != null) {
                        try {
                            Uri photoURI = FileProvider.getUriForFile(ProductosDetalle.this, "com.example.lab1tiendasqlite.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, 1);
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Error Toma Foto: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(urlCompletaImg);
                imgProducto.setImageBitmap(imageBitmap);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (resultCode == RESULT_OK && requestCode == UPLOAD_PICTURE) {

            Uri targetURI = data.getData();
            Bitmap bitmap = null;

            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetURI));
                mImageView.setImageBitmap(bitmap);
                mCaptureOrUploadBitmap = bitmap;

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            }

        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "imagen_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir.exists() == false) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        urlCompletaImg = image.getAbsolutePath();
        return image;
    }

    void GuardarDatosPrductos() {
        btnProductos = findViewById(R.id.btnProducto);
        btnProductos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mImageView.getDrawable() != null) {
                    createProductImageFile();
                    productImagePath = mProductImagePath;
                } else {
                    productImagePath = urlCompletaImg;
                }


                TextView tempVal = findViewById(R.id.edtCodigo);
                String codigo = tempVal.getText().toString();

                tempVal = findViewById(R.id.edtDescrip);
                String descrip = tempVal.getText().toString();

                tempVal = findViewById(R.id.edtMarca);
                String marca = tempVal.getText().toString();

                tempVal = findViewById(R.id.edtPresen);
                String presen = tempVal.getText().toString();

                tempVal = findViewById(R.id.edtPrecio);
                String precio = tempVal.getText().toString();

                String[] data = {idProducto, codigo, descrip, marca, presen, precio, productImagePath};

                miDB = new DBSQLite(getApplicationContext(), "", null, 1);
                miDB.MantenimientoProductos(accion, data);

                Toast.makeText(getApplicationContext(), "Registro de productos insertado con exito", Toast.LENGTH_LONG).show();
                MostrarListaProductos();
            }
        });
    }

    void MostrarListaProductos() {
        Intent mostrarProductos = new Intent(ProductosDetalle.this, MainActivity.class);
        startActivity(mostrarProductos);
    }

    void MostrarDatosProductos() {
        try {
            Bundle recibirParametros = getIntent().getExtras();
            accion = recibirParametros.getString("accion");
            if (accion.equals("modificar")) {
                String[] dataAmigo = recibirParametros.getStringArray("dataAmigo");

                idProducto = dataAmigo[0];

                TextView tempVal = (TextView) findViewById(R.id.edtCodigo);
                tempVal.setText(dataAmigo[1]);

                tempVal = (TextView) findViewById(R.id.edtDescrip);
                tempVal.setText(dataAmigo[2]);

                tempVal = (TextView) findViewById(R.id.edtMarca);
                tempVal.setText(dataAmigo[3]);

                tempVal = (TextView) findViewById(R.id.edtPresen);
                tempVal.setText(dataAmigo[4]);

                tempVal = (TextView) findViewById(R.id.edtPrecio);
                tempVal.setText(dataAmigo[5]);

                productImagePath = dataAmigo[6];
                Bitmap imageBitmap = BitmapFactory.decodeFile(productImagePath);
                imgProducto.setImageBitmap(imageBitmap);
            }
        } catch (Exception ex) {
            ///
        }
    }

    private void createProductImageFile() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir("Pictures", Context.MODE_PRIVATE);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File myPath = new File(directory, imageFileName + ".jpg");

        mProductImagePath = myPath.toString();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            mCaptureOrUploadBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
