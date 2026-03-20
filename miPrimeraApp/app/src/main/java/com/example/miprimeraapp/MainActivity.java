package com.example.miprimeraapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean isGuest = false;
    private String userEmail = "";
    private TabHost tbh;
    private static final int PICK_PRODUCT_IMAGE = 101;
    private static final int TAKE_PRODUCT_PHOTO = 102;
    private static final int PICK_PROFILE_IMAGE = 103;
    private static final int TAKE_PROFILE_PHOTO = 104;
    private static final int PICK_PRODUCT_IMAGE2 = 105;
    private static final int TAKE_PRODUCT_PHOTO2 = 106;
    private static final int PICK_PRODUCT_IMAGE3 = 107;
    private static final int TAKE_PRODUCT_PHOTO3 = 108;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // Modos de administración
    private enum AdminMode { NORMAL, MODIFY, DELETE }
    private AdminMode currentMode = AdminMode.NORMAL;

    private EditText searchEdit;

    // Auxiliares para imágenes
    private ImageView tempDialogImageView;
    private ImageView tempDialogImageView2;
    private ImageView tempDialogImageView3;
    private ImageView profileImageView;
    private Uri selectedProductImageUri;
    private Uri selectedProductImageUri2;
    private Uri selectedProductImageUri3;
    private Uri selectedProfileImageUri;
    private Uri cameraPhotoUri;

    // Lista dinámica de productos (Catálogo)
    private List<Product> productCatalog = new ArrayList<>();
    private DB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DB(this);

        isGuest = getIntent().getBooleanExtra("isGuest", false);
        userEmail = getIntent().getStringExtra("userEmail");

        searchEdit = findViewById(R.id.search);
        profileImageView = findViewById(R.id.profile_image);
        
        setupTabs();
        loadProducts(); 
        setupProfile();

        findViewById(R.id.cart).setOnClickListener(v -> showCartDialog());
        findViewById(R.id.profile_btn).setOnClickListener(v -> tbh.setCurrentTabByTag("Profile"));

        // Configurar botones de administración
        findViewById(R.id.btnAddProduct).setOnClickListener(v -> {
            currentMode = AdminMode.NORMAL;
            showAddProductDialog(null);
        });

        findViewById(R.id.btnModeModify).setOnClickListener(v -> {
            if (currentMode == AdminMode.MODIFY) currentMode = AdminMode.NORMAL;
            else currentMode = AdminMode.MODIFY;
            applyFilters();
            Toast.makeText(this, currentMode == AdminMode.MODIFY ? "Modo Edición Activado" : "Modo Normal", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnModeDelete).setOnClickListener(v -> {
            if (currentMode == AdminMode.DELETE) currentMode = AdminMode.NORMAL;
            else currentMode = AdminMode.DELETE;
            applyFilters();
            Toast.makeText(this, currentMode == AdminMode.DELETE ? "Modo Eliminación Activado" : "Modo Normal", Toast.LENGTH_SHORT).show();
        });

        // Buscador en tiempo real
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        refreshAllContainers(); 
    }

    private void loadProducts() {
        productCatalog.clear();
        Cursor cursor = dbHelper.lista_productos();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product p = new Product();
                p.setId(cursor.getInt(0));
                p.setName(cursor.getString(1));
                p.setCategory(cursor.getString(2));
                p.setPrice(cursor.getDouble(3));
                p.setDescription(cursor.getString(4));
                p.setSpecs(cursor.getString(5));
                p.setImageUri(cursor.getString(6));
                if (cursor.getColumnCount() > 7) p.setImageUri2(cursor.getString(7));
                if (cursor.getColumnCount() > 8) p.setImageUri3(cursor.getString(8));
                productCatalog.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (productCatalog.isEmpty()) {
            initCatalog();
            // Guardar iniciales en la nueva DB
            for (Product p : productCatalog) {
                String[] datos = {
                    "0", p.getName(), p.getCategory(), String.valueOf(p.getPrice()), 
                    p.getDescription(), p.getSpecs(), 
                    p.getImageUri() != null ? p.getImageUri() : "",
                    p.getImageUri2() != null ? p.getImageUri2() : "",
                    p.getImageUri3() != null ? p.getImageUri3() : ""
                };
                dbHelper.administrar_productos("nuevo", datos);
            }
            loadProducts(); // Recargar para tener IDs reales
        }
    }

    private void applyFilters() {
        String query = searchEdit.getText().toString().toLowerCase();
        List<Product> filtered = new ArrayList<>();

        for (Product p : productCatalog) {
            boolean matchesName = p.getName().toLowerCase().contains(query);
            boolean matchesPrice = String.valueOf(p.getPrice()).contains(query);
            
            if (matchesName || matchesPrice) {
                filtered.add(p);
            }
        }
        updateUIWithList(filtered);
    }

    private void updateUIWithList(List<Product> list) {
        int[] ids = {R.id.inicio_container, R.id.gamaAlta_container, R.id.gamaMedia_container, R.id.gamaBaja_container, R.id.gaming_container};
        for (int id : ids) {
            LinearLayout container = findViewById(id);
            if (container != null) container.removeAllViews();
        }

        for (Product p : list) {
            addProductToUI(p);
        }
    }

    private void setupTabs() {
        tbh = findViewById(R.id.tabHost);
        tbh.setup();
        addTab("Inicio", R.id.tab_inicio, "🏠Home");
        addTab("Alta", R.id.tab_gamaAlta, "Gama Alta");
        addTab("Media", R.id.tab_gamaMedia, "Gama Media");
        addTab("Baja", R.id.tab_gamaBaja, "Gama Baja");
        addTab("Gaming", R.id.tab_gaming, "Gaming");
        addTab("Profile", R.id.tab_profile, "👤");
    }

    private void addTab(String tag, int contentId, String label) {
        TabHost.TabSpec spec = tbh.newTabSpec(tag);
        spec.setContent(contentId);
        spec.setIndicator(label);
        tbh.addTab(spec);
    }

    private void initCatalog() {
        productCatalog.add(new Product("iPhone 15 Pro Max", "Alta", 1399.0, 
            "El tope de gama de Apple con acabado en titanio de grado aeroespacial.", 
            "• Chip A17 Pro (3nm)\n• Pantalla Super Retina XDR OLED 6.7\"\n• Sistema de Cámara Pro 48MP (Teleobjetivo x5)\n• Puerto USB-C 3.0\n• Botón de Acción personalizable.", ""));

        productCatalog.add(new Product("Samsung S24 Ultra", "Alta", 1299.0, 
            "El smartphone definitivo impulsado por Inteligencia Artificial Avanzada.", 
            "• Snapdragon 8 Gen 3 for Galaxy\n• Pantalla 6.8\" Dynamic AMOLED 2X QHD+\n• S-Pen de baja latencia integrado\n• Cámara principal de 200MP\n• Funciones Galaxy AI integradas.", ""));

        productCatalog.add(new Product("Galaxy A54 5G", "Media", 450.0, 
            "Equilibrio perfecto entre diseño premium y rendimiento excepcional.", 
            "• Procesador Exynos 1380\n• Pantalla Super AMOLED 120Hz 6.4\"\n• 8GB RAM + 128GB Almacenamiento\n• Cámara 50MP con OIS\n• Certificación IP67 agua/polvo.", ""));

        productCatalog.add(new Product("Redmi Note 13", "Baja", 199.0, 
            "La mejor opción económica con pantalla de calidad y carga ultra rápida.", 
            "• Pantalla AMOLED 120Hz Full HD+\n• Cámara triple de 108MP\n• Carga rápida de 33W (cargador incluido)\n• Batería de 5000mAh\n• Sensor de huellas bajo pantalla.", ""));

        productCatalog.add(new Product("Asus ROG Phone 8", "Gaming", 1099.0, 
            "Potencia extrema y diseño optimizado para los jugadores más exigentes.", 
            "• Snapdragon 8 Gen 3\n• Pantalla LTPO 165Hz ultra fluida\n• Sistema de enfriamiento AeroActive\n• Gatillos AirTrigger sensibles a la presión\n• Iluminación Aura RGB personalizable.", ""));
    }

    private void refreshAllContainers() {
        applyFilters();
    }

    private void addProductToUI(Product p) {
        String query = searchEdit.getText().toString().trim();
        List<LinearLayout> containers = new ArrayList<>();
        
        // REGLA PARA EL HOME (INICIO):
        if (!query.isEmpty()) {
            // Si hay búsqueda, todos al inicio sin importar la gama
            containers.add(findViewById(R.id.inicio_container));
        } else if (p.getCategory().equals("Alta") || p.getCategory().equals("Gaming")) {
            // Si no hay búsqueda, solo Alta y Gaming en el Home
            containers.add(findViewById(R.id.inicio_container));
        }

        // REGLA PARA LAS PESTAÑAS ESPECÍFICAS (NO se eliminan las gamas):
        switch (p.getCategory()) {
            case "Alta": containers.add(findViewById(R.id.gamaAlta_container)); break;
            case "Media": containers.add(findViewById(R.id.gamaMedia_container)); break;
            case "Baja": containers.add(findViewById(R.id.gamaBaja_container)); break;
            case "Gaming": containers.add(findViewById(R.id.gaming_container)); break;
            default: 
                // Evitar duplicados en el Home si no tiene categoría reconocida
                LinearLayout inicio = findViewById(R.id.inicio_container);
                if (!containers.contains(inicio)) containers.add(inicio);
                break;
        }

        for (LinearLayout container : containers) {
            if (container == null) continue;
            
            View v = LayoutInflater.from(this).inflate(R.layout.item_product, container, false);
            ImageView imgView = v.findViewById(R.id.imgProduct);
            if (p.getImageUri() != null && !p.getImageUri().isEmpty()) {
                imgView.setImageURI(Uri.parse(p.getImageUri()));
            } else {
                imgView.setImageResource(R.mipmap.ic_launcher);
            }

            ((TextView) v.findViewById(R.id.txtName)).setText(p.getName());
            ((TextView) v.findViewById(R.id.txtDescription)).setText(p.getDescription());
            ((TextView) v.findViewById(R.id.txtPrice)).setText(String.format(Locale.US, "$%.2f", p.getPrice()));

            View btnBuy = v.findViewById(R.id.btnBuyNow);
            View btnCart = v.findViewById(R.id.btnAddToCart);
            View btnEdit = v.findViewById(R.id.btnModify);
            View btnDel = v.findViewById(R.id.btnDelete);

            if (currentMode == AdminMode.MODIFY) {
                btnBuy.setVisibility(View.GONE); btnCart.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE); btnDel.setVisibility(View.GONE);
            } else if (currentMode == AdminMode.DELETE) {
                btnBuy.setVisibility(View.GONE); btnCart.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE); btnDel.setVisibility(View.VISIBLE);
            } else {
                btnBuy.setVisibility(View.VISIBLE); btnCart.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE); btnDel.setVisibility(View.GONE);
            }

            btnBuy.setOnClickListener(view -> confirmPurchase(p));
            btnCart.setOnClickListener(view -> {
                CartManager.getInstance().addToCart(p);
                updateProfileLists();
                Toast.makeText(this, "Añadido", Toast.LENGTH_SHORT).show();
            });

            btnEdit.setOnClickListener(view -> showAddProductDialog(p));
            btnDel.setOnClickListener(view -> {
                new AlertDialog.Builder(this).setTitle("Eliminar").setMessage("¿Eliminar " + p.getName() + "?")
                    .setPositiveButton("Sí", (d, w) -> {
                        dbHelper.administrar_productos("eliminar", new String[]{String.valueOf(p.getId())});
                        loadProducts();
                        applyFilters();
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                    }).setNegativeButton("No", null).show();
            });

            v.setOnClickListener(view -> showSpecs(p));
            container.addView(v);
        }
    }

    private void showAddProductDialog(@Nullable Product existingProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingProduct == null ? "Nuevo Producto" : "Modificar Producto");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        LinearLayout imagesLayout = new LinearLayout(this);
        imagesLayout.setOrientation(LinearLayout.HORIZONTAL);
        imagesLayout.setGravity(Gravity.CENTER);

        tempDialogImageView = createDialogImageView();
        tempDialogImageView.setOnClickListener(v -> showImageSelectionDialog(1));
        imagesLayout.addView(tempDialogImageView);

        tempDialogImageView2 = createDialogImageView();
        tempDialogImageView2.setOnClickListener(v -> showImageSelectionDialog(2));
        imagesLayout.addView(tempDialogImageView2);

        tempDialogImageView3 = createDialogImageView();
        tempDialogImageView3.setOnClickListener(v -> showImageSelectionDialog(3));
        imagesLayout.addView(tempDialogImageView3);

        layout.addView(imagesLayout);
        
        TextView tvLabel = new TextView(this); tvLabel.setText("Pulsa arriba para cambiar fotos (Principal, Extra 1, Extra 2)");
        tvLabel.setGravity(Gravity.CENTER_HORIZONTAL); layout.addView(tvLabel);

        EditText etName = new EditText(this); etName.setHint("Nombre"); layout.addView(etName);
        EditText etPrice = new EditText(this); etPrice.setHint("Precio"); etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); layout.addView(etPrice);
        EditText etDesc = new EditText(this); etDesc.setHint("Descripción Corta"); layout.addView(etDesc);
        EditText etSpecs = new EditText(this); etSpecs.setHint("Especificaciones"); layout.addView(etSpecs);

        Spinner spnCat = new Spinner(this);
        String[] cats = {"Alta", "Media", "Baja", "Gaming", "Inicio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        spnCat.setAdapter(adapter);
        layout.addView(spnCat);

        selectedProductImageUri = null;
        selectedProductImageUri2 = null;
        selectedProductImageUri3 = null;
        if (existingProduct != null) {
            etName.setText(existingProduct.getName());
            etPrice.setText(String.valueOf(existingProduct.getPrice()));
            etDesc.setText(existingProduct.getDescription());
            etSpecs.setText(existingProduct.getSpecs());
            if (existingProduct.getImageUri() != null && !existingProduct.getImageUri().isEmpty()) {
                selectedProductImageUri = Uri.parse(existingProduct.getImageUri());
                tempDialogImageView.setImageURI(selectedProductImageUri);
            }
            if (existingProduct.getImageUri2() != null && !existingProduct.getImageUri2().isEmpty()) {
                selectedProductImageUri2 = Uri.parse(existingProduct.getImageUri2());
                tempDialogImageView2.setImageURI(selectedProductImageUri2);
            }
            if (existingProduct.getImageUri3() != null && !existingProduct.getImageUri3().isEmpty()) {
                selectedProductImageUri3 = Uri.parse(existingProduct.getImageUri3());
                tempDialogImageView3.setImageURI(selectedProductImageUri3);
            }
            int pos = adapter.getPosition(existingProduct.getCategory());
            if (pos >= 0) spnCat.setSelection(pos);
        }

        builder.setView(layout);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            try {
                String name = etName.getText().toString();
                String price = etPrice.getText().toString();
                String desc = etDesc.getText().toString();
                String specs = etSpecs.getText().toString();
                String cat = spnCat.getSelectedItem().toString();
                String uri = selectedProductImageUri != null ? selectedProductImageUri.toString() : 
                            (existingProduct != null ? existingProduct.getImageUri() : "");
                String uri2 = selectedProductImageUri2 != null ? selectedProductImageUri2.toString() : 
                            (existingProduct != null ? existingProduct.getImageUri2() : "");
                String uri3 = selectedProductImageUri3 != null ? selectedProductImageUri3.toString() : 
                            (existingProduct != null ? existingProduct.getImageUri3() : "");

                String result;
                if (existingProduct != null) {
                    String[] datos = {String.valueOf(existingProduct.getId()), name, cat, price, desc, specs, uri, uri2, uri3};
                    result = dbHelper.administrar_productos("modificar", datos);
                } else {
                    String[] datos = {"0", name, cat, price, desc, specs, uri, uri2, uri3};
                    result = dbHelper.administrar_productos("nuevo", datos);
                }
                
                if (result.equals("ok")) {
                    loadProducts();
                    applyFilters();
                    Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error: " + result, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error en los datos", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private ImageView createDialogImageView() {
        ShapeableImageView siv = new ShapeableImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
        params.setMargins(10, 10, 10, 10);
        siv.setLayoutParams(params);
        siv.setImageResource(R.mipmap.ic_launcher);
        siv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        float radius = getResources().getDisplayMetrics().density * 12;
        siv.setShapeAppearanceModel(siv.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build());
        return siv;
    }

    private void showImageSelectionDialog(int type) {
        // type: 0 = Profile, 1 = Product 1, 2 = Product 2, 3 = Product 3
        String[] options = {"Elegir de Galería", "Tomar Foto"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Imagen")
                .setItems(options, (dialog, which) -> {
                    int pickCode, takeCode;
                    switch(type) {
                        case 1: pickCode = PICK_PRODUCT_IMAGE; takeCode = TAKE_PRODUCT_PHOTO; break;
                        case 2: pickCode = PICK_PRODUCT_IMAGE2; takeCode = TAKE_PRODUCT_PHOTO2; break;
                        case 3: pickCode = PICK_PRODUCT_IMAGE3; takeCode = TAKE_PRODUCT_PHOTO3; break;
                        default: pickCode = PICK_PROFILE_IMAGE; takeCode = TAKE_PROFILE_PHOTO; break;
                    }
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(intent, pickCode);
                    } else {
                        checkCameraPermissionAndTakePhoto(takeCode);
                    }
                }).show();
    }

    private void checkCameraPermissionAndTakePhoto(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestCode);
        } else {
            takePhoto(requestCode);
        }
    }

    private void takePhoto(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                cameraPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto(requestCode);
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PRODUCT_IMAGE && data != null && data.getData() != null) {
                selectedProductImageUri = data.getData();
                tempDialogImageView.setImageURI(selectedProductImageUri);
                persistUri(selectedProductImageUri);
            } else if (requestCode == TAKE_PRODUCT_PHOTO) {
                selectedProductImageUri = cameraPhotoUri;
                tempDialogImageView.setImageURI(selectedProductImageUri);
            } else if (requestCode == PICK_PRODUCT_IMAGE2 && data != null && data.getData() != null) {
                selectedProductImageUri2 = data.getData();
                tempDialogImageView2.setImageURI(selectedProductImageUri2);
                persistUri(selectedProductImageUri2);
            } else if (requestCode == TAKE_PRODUCT_PHOTO2) {
                selectedProductImageUri2 = cameraPhotoUri;
                tempDialogImageView2.setImageURI(selectedProductImageUri2);
            } else if (requestCode == PICK_PRODUCT_IMAGE3 && data != null && data.getData() != null) {
                selectedProductImageUri3 = data.getData();
                tempDialogImageView3.setImageURI(selectedProductImageUri3);
                persistUri(selectedProductImageUri3);
            } else if (requestCode == TAKE_PRODUCT_PHOTO3) {
                selectedProductImageUri3 = cameraPhotoUri;
                tempDialogImageView3.setImageURI(selectedProductImageUri3);
            } else if (requestCode == PICK_PROFILE_IMAGE && data != null && data.getData() != null) {
                selectedProfileImageUri = data.getData();
                if (profileImageView != null) profileImageView.setImageURI(selectedProfileImageUri);
                persistUri(selectedProfileImageUri);
            } else if (requestCode == TAKE_PROFILE_PHOTO) {
                selectedProfileImageUri = cameraPhotoUri;
                if (profileImageView != null) profileImageView.setImageURI(selectedProfileImageUri);
            }
        }
    }

    private void persistUri(Uri uri) {
        try {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setupProfile() {
        updateProfileLists();
    }

    private void confirmPurchase(Product p) {
        new AlertDialog.Builder(this).setTitle("Compra").setMessage("¿Comprar " + p.getName() + "?")
                .setPositiveButton("Sí", (d, w) -> {
                    CartManager.getInstance().addToPurchased(p);
                    updateProfileLists();
                }).show();
    }

    private void showCartDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Carrito de Compras");

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        // Función para llenar el layout (se puede llamar repetidamente)
        fillCartLayout(mainLayout);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(mainLayout);
        
        b.setView(scroll);
        b.setPositiveButton("Cerrar", null);
        b.show();
    }

    private void fillCartLayout(LinearLayout container) {
        container.removeAllViews();
        List<Product> items = CartManager.getInstance().getCartItems();
        
        if (items.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("El carrito está vacío");
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 50, 0, 50);
            container.addView(tvEmpty);
            return;
        }

        for (Product p : items) {
            LinearLayout itemRow = new LinearLayout(this);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setGravity(Gravity.CENTER_VERTICAL);
            itemRow.setPadding(0, 10, 0, 10);

            TextView tvInfo = new TextView(this);
            tvInfo.setText("🛒 " + p.getName() + " - $" + p.getPrice());
            tvInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            itemRow.addView(tvInfo);

            Button btnRemove = new Button(this);
            btnRemove.setText("❌");
            btnRemove.setLayoutParams(new LinearLayout.LayoutParams(120, 100));
            btnRemove.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(p);
                updateProfileLists();
                fillCartLayout(container); // RECARGA EL CONTENIDO SIN CERRAR EL DIÁLOGO
                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
            });
            itemRow.addView(btnRemove);

            container.addView(itemRow);
        }
    }

    private void updateProfileLists() {
        TextView tvEmail = findViewById(R.id.user_email_display);
        if (tvEmail != null) tvEmail.setText(isGuest ? "Modo Explorador" : userEmail);

        View btnChangePhoto = findViewById(R.id.btn_change_photo);
        if (btnChangePhoto != null) btnChangePhoto.setOnClickListener(v -> showImageSelectionDialog(0));

        if (selectedProfileImageUri != null && profileImageView != null) {
            profileImageView.setImageURI(selectedProfileImageUri);
        }

        LinearLayout cartC = findViewById(R.id.cart_items_container);
        LinearLayout purchC = findViewById(R.id.purchased_items_container);
        if (cartC == null || purchC == null) return;
        cartC.removeAllViews();
        for (Product p : CartManager.getInstance().getCartItems()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView tv = new TextView(this);
            tv.setText("🛒 " + p.getName());
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            row.addView(tv);

            TextView btnDel = new TextView(this);
            btnDel.setText(" [X] ");
            btnDel.setPadding(20, 10, 20, 10);
            btnDel.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(p);
                updateProfileLists();
            });
            row.addView(btnDel);

            cartC.addView(row);
        }
        purchC.removeAllViews();
        for (Product p : CartManager.getInstance().getPurchasedItems()) {
            TextView tv = new TextView(this); tv.setText("✅ " + p.getName()); purchC.addView(tv);
        }
    }

    private void showSpecs(Product p) {
        String detail = "Descripción:\n" + p.getDescription() + 
                       "\n\nEspecificaciones Técnicas:\n" + p.getSpecs() +
                       "\n\nPrecio: $" + p.getPrice();
                       
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(p.getName());
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        
        float density = getResources().getDisplayMetrics().density;

        // Imagen principal con bordes redondeados
        if (p.getImageUri() != null && !p.getImageUri().isEmpty()) {
            ShapeableImageView iv = new ShapeableImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(250 * density));
            params.setMargins(0, 0, 0, (int)(15 * density));
            iv.setLayoutParams(params);
            iv.setImageURI(Uri.parse(p.getImageUri()));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setShapeAppearanceModel(iv.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 25 * density).build());
            layout.addView(iv);
        }
        
        // Imágenes extra en horizontal (Más grandes y redondeadas)
        LinearLayout extraImages = new LinearLayout(this);
        extraImages.setOrientation(LinearLayout.HORIZONTAL);
        extraImages.setGravity(Gravity.CENTER);
        
        if (p.getImageUri2() != null && !p.getImageUri2().isEmpty()) {
            ShapeableImageView iv2 = new ShapeableImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int)(180 * density), 1f);
            params.setMargins(0, 0, (int)(5 * density), 0);
            iv2.setLayoutParams(params);
            iv2.setImageURI(Uri.parse(p.getImageUri2()));
            iv2.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv2.setShapeAppearanceModel(iv2.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 20 * density).build());
            extraImages.addView(iv2);
        }
        
        if (p.getImageUri3() != null && !p.getImageUri3().isEmpty()) {
            ShapeableImageView iv3 = new ShapeableImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int)(180 * density), 1f);
            params.setMargins((int)(5 * density), 0, 0, 0);
            iv3.setLayoutParams(params);
            iv3.setImageURI(Uri.parse(p.getImageUri3()));
            iv3.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv3.setShapeAppearanceModel(iv3.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 20 * density).build());
            extraImages.addView(iv3);
        }
        
        if (extraImages.getChildCount() > 0) {
            layout.addView(extraImages);
        }

        TextView tv = new TextView(this);
        tv.setText(detail);
        tv.setPadding(0, (int)(20 * density), 0, 0);
        tv.setTextSize(16);
        layout.addView(tv);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        builder.setView(scrollView);
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }
}
