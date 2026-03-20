package com.example.miprimeraapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "jeffphone_db";
    private static final int DATABASE_VERSION = 2;
    private static final String SQLdb = "CREATE TABLE products (idProduct INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, price REAL, description TEXT, specs TEXT, imageUri TEXT, imageUri2 TEXT, imageUri3 TEXT)";

    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQLdb);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS products");
        onCreate(sqLiteDatabase);
    }

    public String administrar_productos(String accion, String[] datos) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            
            if (accion.equals("eliminar")) {
                db.delete("products", "idProduct=?", new String[]{datos[0]});
                db.close();
                return "ok";
            }

            ContentValues values = new ContentValues();
            values.put("name", datos[1]);
            values.put("category", datos[2]);
            
            double priceValue = 0;
            try {
                if (datos[3] != null && !datos[3].isEmpty()) {
                    priceValue = Double.parseDouble(datos[3]);
                }
            } catch (Exception e) { 
                priceValue = 0; 
            }
            values.put("price", priceValue);

            values.put("description", datos[4]);
            values.put("specs", datos[5]);
            values.put("imageUri", datos[6]);
            values.put("imageUri2", datos.length > 7 ? datos[7] : "");
            values.put("imageUri3", datos.length > 8 ? datos[8] : "");

            if (accion.equals("nuevo")) {
                db.insert("products", null, values);
            } else if (accion.equals("modificar")) {
                db.update("products", values, "idProduct=?", new String[]{datos[0]});
            }

            db.close();
            return "ok";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Cursor lista_productos() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM products", null);
    }
}
