package com.example.miprimeraapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // Modos de administración
    private enum AdminMode { NORMAL, MODIFY, DELETE }
    private AdminMode currentMode = AdminMode.NORMAL;

    // Filtros
    private String currentPriceFilter = "Todos";
    private EditText searchEdit;

    // Auxiliares para imágenes
    private ImageView tempDialogImageView;
    private ImageView profileImageView;
    private Uri selectedProductImageUri;
    private Uri cameraPhotoUri;

    // Lista dinámica de productos (Catálogo)
    private List<Product> productCatalog = new ArrayList<>();
    private ProductDao productDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productDao = AppDatabase.getInstance(this).productDao();

        isGuest = getIntent().getBooleanExtra("isGuest", false);
        userEmail = getIntent().getStringExtra("userEmail");

        searchEdit = findViewById(R.id.search);
        profileImageView = findViewById(R.id.profile_image);
        
        setupTabs();
        loadProducts(); 
        setupProfile();
        setupFilters();

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
        productCatalog = productDao.getAll();

        if (productCatalog == null || productCatalog.isEmpty()) {
            productCatalog = new ArrayList<>();
            initCatalog();
            for (Product p : productCatalog) {
                productDao.insert(p);
            }
            productCatalog = productDao.getAll();
        }
    }

    private void setupFilters() {
        View.OnClickListener filterClick = v -> {
            findViewById(R.id.btnFilterAll).setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            findViewById(R.id.btnFilterCheap).setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            findViewById(R.id.btnFilterMid).setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            findViewById(R.id.btnFilterPremium).setBackgroundTintList(getColorStateList(android.R.color.darker_gray));

            v.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));

            if (v.getId() == R.id.btnFilterAll) currentPriceFilter = "Todos";
            else if (v.getId() == R.id.btnFilterCheap) currentPriceFilter = "Económicos";
            else if (v.getId() == R.id.btnFilterMid) currentPriceFilter = "Media";
            else if (v.getId() == R.id.btnFilterPremium) currentPriceFilter = "Premium";

            applyFilters();
        };

        findViewById(R.id.btnFilterAll).setOnClickListener(filterClick);
        findViewById(R.id.btnFilterCheap).setOnClickListener(filterClick);
        findViewById(R.id.btnFilterMid).setOnClickListener(filterClick);
        findViewById(R.id.btnFilterPremium).setOnClickListener(filterClick);
        
        findViewById(R.id.btnFilterAll).setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
    }

    private void applyFilters() {
        String query = searchEdit.getText().toString().toLowerCase();
        List<Product> filtered = new ArrayList<>();

        for (Product p : productCatalog) {
            boolean matchesText = p.getName().toLowerCase().contains(query) || 
                                 p.getDescription().toLowerCase().contains(query);
            
            boolean matchesPrice = true;
            if (currentPriceFilter.equals("Económicos")) matchesPrice = p.getPrice() < 300;
            else if (currentPriceFilter.equals("Media")) matchesPrice = p.getPrice() >= 300 && p.getPrice() <= 800;
            else if (currentPriceFilter.equals("Premium")) matchesPrice = p.getPrice() > 800;

            if (matchesText && matchesPrice) {
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
        int icon = R.mipmap.ic_launcher;
        productCatalog.add(new Product("iPhone 15 Pro Max", "Alta", 1399.0, 
            "El tope de gama de Apple con acabado en titanio de grado aeroespacial.", 
            "• Chip A17 Pro (3nm)\n• Pantalla Super Retina XDR OLED 6.7\"\n• Sistema de Cámara Pro 48MP (Teleobjetivo x5)\n• Puerto USB-C 3.0\n• Botón de Acción personalizable.", icon));

        productCatalog.add(new Product("Samsung S24 Ultra", "Alta", 1299.0, 
            "El smartphone definitivo impulsado por Inteligencia Artificial Avanzada.", 
            "• Snapdragon 8 Gen 3 for Galaxy\n• Pantalla 6.8\" Dynamic AMOLED 2X QHD+\n• S-Pen de baja latencia integrado\n• Cámara principal de 200MP\n• Funciones Galaxy AI integradas.", icon));

        productCatalog.add(new Product("Galaxy A54 5G", "Media", 450.0, 
            "Equilibrio perfecto entre diseño premium y rendimiento excepcional.", 
            "• Procesador Exynos 1380\n• Pantalla Super AMOLED 120Hz 6.4\"\n• 8GB RAM + 128GB Almacenamiento\n• Cámara 50MP con OIS\n• Certificación IP67 agua/polvo.", icon));

        productCatalog.add(new Product("Redmi Note 13", "Baja", 199.0, 
            "La mejor opción económica con pantalla de calidad y carga ultra rápida.", 
            "• Pantalla AMOLED 120Hz Full HD+\n• Cámara triple de 108MP\n• Carga rápida de 33W (cargador incluido)\n• Batería de 5000mAh\n• Sensor de huellas bajo pantalla.", icon));

        productCatalog.add(new Product("Asus ROG Phone 8", "Gaming", 1099.0, 
            "Potencia extrema y diseño optimizado para los jugadores más exigentes.", 
            "• Snapdragon 8 Gen 3\n• Pantalla LTPO 165Hz ultra fluida\n• Sistema de enfriamiento AeroActive\n• Gatillos AirTrigger sensibles a la presión\n• Iluminación Aura RGB personalizable.", icon));
    }

    private void refreshAllContainers() {
        applyFilters();
    }

    private void addProductToUI(Product p) {
        int containerId;
        switch (p.getCategory()) {
            case "Alta": containerId = R.id.gamaAlta_container; break;
            case "Media": containerId = R.id.gamaMedia_container; break;
            case "Baja": containerId = R.id.gamaBaja_container; break;
            case "Gaming": containerId = R.id.gaming_container; break;
            default: containerId = R.id.inicio_container;
        }

        LinearLayout container = findViewById(containerId);
        if (container == null) return;

        View v = LayoutInflater.from(this).inflate(R.layout.item_product, container, false);
        ImageView imgView = v.findViewById(R.id.imgProduct);
        if (p.getImageUri() != null) {
            imgView.setImageURI(Uri.parse(p.getImageUri()));
        } else {
            imgView.setImageResource(p.getImageResource());
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
                    productDao.delete(p);
                    productCatalog = productDao.getAll();
                    applyFilters();
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("No", null).show();
        });

        v.setOnClickListener(view -> showSpecs(p));
        container.addView(v);
    }

    private void showAddProductDialog(@Nullable Product existingProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingProduct == null ? "Nuevo Producto" : "Modificar Producto");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        tempDialogImageView = new ImageView(this);
        tempDialogImageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        tempDialogImageView.setImageResource(R.mipmap.ic_launcher);
        tempDialogImageView.setOnClickListener(v -> showImageSelectionDialog(true));
        layout.addView(tempDialogImageView);
        
        TextView tvLabel = new TextView(this); tvLabel.setText("Pulsa arriba para cambiar foto");
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
        if (existingProduct != null) {
            etName.setText(existingProduct.getName());
            etPrice.setText(String.valueOf(existingProduct.getPrice()));
            etDesc.setText(existingProduct.getDescription());
            etSpecs.setText(existingProduct.getSpecs());
            if (existingProduct.getImageUri() != null) {
                selectedProductImageUri = Uri.parse(existingProduct.getImageUri());
                tempDialogImageView.setImageURI(selectedProductImageUri);
            } else {
                tempDialogImageView.setImageResource(existingProduct.getImageResource());
            }
            int pos = adapter.getPosition(existingProduct.getCategory());
            if (pos >= 0) spnCat.setSelection(pos);
        }

        builder.setView(layout);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            try {
                String name = etName.getText().toString();
                double price = Double.parseDouble(etPrice.getText().toString());
                String desc = etDesc.getText().toString();
                String specs = etSpecs.getText().toString();
                String cat = spnCat.getSelectedItem().toString();

                Product p;
                if (existingProduct != null) {
                    p = existingProduct;
                    p.setName(name);
                    p.setPrice(price);
                    p.setDescription(desc);
                    p.setSpecs(specs);
                    p.setCategory(cat);
                    if (selectedProductImageUri != null) {
                        p.setImageUri(selectedProductImageUri.toString());
                        p.setImageResource(0);
                    }
                    productDao.update(p);
                } else {
                    if (selectedProductImageUri != null) {
                        p = new Product(name, cat, price, desc, specs, selectedProductImageUri.toString());
                    } else {
                        p = new Product(name, cat, price, desc, specs, R.mipmap.ic_launcher);
                    }
                    productDao.insert(p);
                }
                
                productCatalog = productDao.getAll();
                applyFilters();
                Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error en los datos", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showImageSelectionDialog(boolean isProduct) {
        String[] options = {"Elegir de Galería", "Tomar Foto"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Imagen")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(intent, isProduct ? PICK_PRODUCT_IMAGE : PICK_PROFILE_IMAGE);
                    } else {
                        checkCameraPermissionAndTakePhoto(isProduct);
                    }
                }).show();
    }

    private void checkCameraPermissionAndTakePhoto(boolean isProduct) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, isProduct ? TAKE_PRODUCT_PHOTO : TAKE_PROFILE_PHOTO);
        } else {
            takePhoto(isProduct);
        }
    }

    private void takePhoto(boolean isProduct) {
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
                startActivityForResult(takePictureIntent, isProduct ? TAKE_PRODUCT_PHOTO : TAKE_PROFILE_PHOTO);
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
            if (requestCode == TAKE_PRODUCT_PHOTO) takePhoto(true);
            else if (requestCode == TAKE_PROFILE_PHOTO) takePhoto(false);
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
            } else if (requestCode == PICK_PROFILE_IMAGE && data != null && data.getData() != null) {
                Uri profileUri = data.getData();
                profileImageView.setImageURI(profileUri);
                persistUri(profileUri);
            } else if (requestCode == TAKE_PROFILE_PHOTO) {
                profileImageView.setImageURI(cameraPhotoUri);
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
        TextView tvEmail = findViewById(R.id.user_email_display);
        if (isGuest) tvEmail.setText("Modo Explorador");
        else tvEmail.setText(userEmail);
        
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> showImageSelectionDialog(false));

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
        List<Product> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) { Toast.makeText(this, "Vacio", Toast.LENGTH_SHORT).show(); return; }
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Carrito");
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(40,40,40,40);
        for (Product p : items) {
            TextView t = new TextView(this); t.setText("🛒 " + p.getName() + " - $" + p.getPrice());
            l.addView(t);
        }
        b.setView(l); b.setPositiveButton("Cerrar", null); b.show();
    }

    private void updateProfileLists() {
        LinearLayout cartC = findViewById(R.id.cart_items_container);
        LinearLayout purchC = findViewById(R.id.purchased_items_container);
        if (cartC == null || purchC == null) return;
        cartC.removeAllViews();
        for (Product p : CartManager.getInstance().getCartItems()) {
            TextView tv = new TextView(this); tv.setText("🛒 " + p.getName()); cartC.addView(tv);
        }
        purchC.removeAllViews();
        for (Product p : CartManager.getInstance().getPurchasedItems()) {
            TextView tv = new TextView(this); tv.setText("✅ " + p.getName()); purchC.addView(tv);
        }
    }

    private void showSpecs(Product p) {
        String detail = "Descripción:\n" + p.getDescription() + 
                       "\n\nEspecificaciones Técnicas:\n" + p.getSpecs();
                       
        new AlertDialog.Builder(this)
            .setTitle(p.getName())
            .setMessage(detail)
            .setPositiveButton("Cerrar", null)
            .show();
    }
}
