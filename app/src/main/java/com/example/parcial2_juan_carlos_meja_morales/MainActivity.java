package com.example.parcial2_juan_carlos_meja_morales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parcial2_juan_carlos_meja_morales.ConexionSQLite.DBSQLite;
import com.example.parcial2_juan_carlos_meja_morales.ConexionSQLite.Productos;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    DBSQLite miBD;
    Cursor misProductos;
    Productos producto;
    ArrayList<Productos> stringArrayList = new ArrayList<Productos>();
    ArrayList<Productos> copyStringArrayList = new ArrayList<Productos>();
    ListView listProduc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton btnAgregar = (FloatingActionButton)findViewById(R.id.GuardarProductos);
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarAmigo("nuevo", new String[]{});
            }
        });
        ObtenerProductos();
        BuscarProductos();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_productos, menu);

        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
        misProductos.moveToPosition(adapterContextMenuInfo.position);
        menu.setHeaderTitle(misProductos.getString(1));
    }
    void BuscarProductos(){
        final TextView tempVal = (TextView)findViewById(R.id.Buscar);
        tempVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    stringArrayList.clear();
                    if (tempVal.getText().toString().trim().length() < 1) {
                        stringArrayList.addAll(copyStringArrayList);
                    } else {
                        for (Productos am : copyStringArrayList) {
                            String codigo = am.getCodigo();
                            if (codigo.toLowerCase().contains(tempVal.getText().toString().trim().toLowerCase())) {
                                stringArrayList.add(am);
                            }
                        }
                    }
                    AdaptadorImagenes adaptadorImg = new AdaptadorImagenes(getApplicationContext(), stringArrayList);
                    listProduc.setAdapter(adaptadorImg);
                }catch (Exception ex){
                    Toast.makeText(getApplicationContext(), "Error: "+ ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnxAgregar:
                agregarAmigo("nuevo", new String[]{});
                return true;

            case R.id.mnxModificar:
                String[] dataProducto = {
                        misProductos.getString(0),//idProducto
                        misProductos.getString(1),//codigo
                        misProductos.getString(2),//descripcion
                        misProductos.getString(3),//marca
                        misProductos.getString(4), //presentacion
                        misProductos.getString(5),  //precio
                        misProductos.getString(6)  //urlImg
                };
                agregarAmigo("modificar",dataProducto);
                return true;

            case R.id.mnxEliminar:
                AlertDialog eliminarFriend =  eliminarAmigo();
                eliminarFriend.show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
    AlertDialog eliminarAmigo(){
        AlertDialog.Builder confirmacion = new AlertDialog.Builder(MainActivity.this);
        confirmacion.setTitle(misProductos.getString(1));
        confirmacion.setMessage("Esta seguro de eliminar el producto?");
        confirmacion.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                miBD.MantenimientoProductos("eliminar",new String[]{misProductos.getString(0)});
                ObtenerProductos();
                Toast.makeText(getApplicationContext(), "Producto eliminado con exito.",Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        confirmacion.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Eliminacion cancelada por el usuario.",Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        return confirmacion.create();
    }
    void ObtenerProductos(){
        miBD = new DBSQLite(getApplicationContext(), "", null, 1);
        misProductos = miBD.MantenimientoProductos("consultar", null);
        if( misProductos.moveToFirst() ){ //hay registro en la BD que mostrar
            mostrarDatosProductos();


        } else{ //No tengo registro que mostrar.
            Toast.makeText(getApplicationContext(), "CONEXIÓN ESTABLECIDA",Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "No hay registros de amigos que mostrar",Toast.LENGTH_LONG).show();

            agregarAmigo("nuevo", new String[]{});
        }
    }
    void agregarAmigo(String accion, String[] dataAmigo){
        Bundle enviarParametros = new Bundle();
        enviarParametros.putString("accion",accion);
        enviarParametros.putStringArray("dataAmigo",dataAmigo);
        Intent agregarAmigos = new Intent(MainActivity.this, ProductosDetalle.class);
        agregarAmigos.putExtras(enviarParametros);
        startActivity(agregarAmigos);
    }
    void mostrarDatosProductos(){
        stringArrayList.clear();
        listProduc = (ListView)findViewById(R.id.Productos);
        do {
            producto = new Productos(misProductos.getString(0),misProductos.getString(1), misProductos.getString(2), misProductos.getString(3), misProductos.getString(4), misProductos.getString(5), misProductos.getString(6));
            stringArrayList.add(producto);
        }while(misProductos.moveToNext());
        AdaptadorImagenes adaptadorImg = new AdaptadorImagenes(getApplicationContext(), stringArrayList);
        listProduc.setAdapter(adaptadorImg);

        copyStringArrayList.clear();
        copyStringArrayList.addAll(stringArrayList);
        registerForContextMenu(listProduc);


    }

}