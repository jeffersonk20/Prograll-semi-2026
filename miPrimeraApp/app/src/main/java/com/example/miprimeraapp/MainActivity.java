package com.example.miprimeraapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean isGuest = false;
    private String userEmail = "";
    private TabHost tbh;
    private ImageView profileImageView;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isGuest = getIntent().getBooleanExtra("isGuest", false);
        userEmail = getIntent().getStringExtra("userEmail");

        setupTabs();
        loadProducts();
        setupProfile();

        findViewById(R.id.cart).setOnClickListener(v -> showCartDialog());
        findViewById(R.id.profile_btn).setOnClickListener(v -> tbh.setCurrentTabByTag("Profile"));
    }

    private void setupTabs() {
        tbh = findViewById(R.id.tabHost);
        tbh.setup();

        addTab("Inicio", R.id.tab_inicio, "🏠");
        addTab("Alta", R.id.tab_gamaAlta, "💎");
        addTab("Media", R.id.tab_gamaMedia, "📱");
        addTab("Gaming", R.id.tab_gaming, "🎮");
        addTab("Profile", R.id.tab_profile, "👤");
    }

    private void addTab(String tag, int contentId, String label) {
        TabHost.TabSpec spec = tbh.newTabSpec(tag);
        spec.setContent(contentId);
        spec.setIndicator(label);
        tbh.addTab(spec);
    }

    private void setupProfile() {
        profileImageView = findViewById(R.id.profile_image);
        TextView tvEmail = findViewById(R.id.user_email_display);
        Button btnChangePhoto = findViewById(R.id.btn_change_photo);

        if (isGuest) {
            tvEmail.setText("Modo Explorador");
            btnChangePhoto.setVisibility(View.GONE);
        } else {
            tvEmail.setText(userEmail);
            btnChangePhoto.setOnClickListener(v -> {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            });
            updateProfileLists();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void loadProducts() {
        int icon = R.mipmap.ic_launcher;
        
        Product p1 = new Product("iPhone 15 Pro", "Alta", 1199.0, 
                "Titanio aeroespacial.", 
                "• Pantalla: 6.1\" OLED\n• Chip: A17 Pro\n• Cámara: 48MP Principal", icon);
        
        Product p2 = new Product("S24 Ultra", "Alta", 1299.0, 
                "Galaxy AI Integrada.", 
                "• Pantalla: 6.8\" Dynamic AMOLED\n• Cámara: 200MP\n• S-Pen incluido", icon);

        Product p3 = new Product("Galaxy A54", "Media", 450.0, "Pantalla Super AMOLED.", "• Pantalla: 6.4\"\n• Batería: 5000 mAh", icon);

        addProduct(p1, R.id.gamaAlta_container);
        addProduct(p2, R.id.gamaAlta_container);
        addProduct(p3, R.id.gamaMedia_container);
        addProduct(p1, R.id.inicio_container);
    }

    private void addProduct(Product p, int containerId) {
        LinearLayout container = findViewById(containerId);
        if (container == null) return;

        View v = LayoutInflater.from(this).inflate(R.layout.item_product, container, false);
        ((TextView) v.findViewById(R.id.txtName)).setText(p.getName());
        ((TextView) v.findViewById(R.id.txtDescription)).setText(p.getDescription());
        ((TextView) v.findViewById(R.id.txtPrice)).setText(String.format(Locale.US, "$%.2f", p.getPrice()));

        View actionContainer = v.findViewById(R.id.action_buttons_container);
        if (isGuest) {
            actionContainer.setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.btnBuyNow).setOnClickListener(view -> showPaymentMethodDialog(p));
            v.findViewById(R.id.btnAddToCart).setOnClickListener(view -> {
                CartManager.getInstance().addToCart(p);
                updateProfileLists();
                Toast.makeText(this, p.getName() + " añadido al carrito", Toast.LENGTH_SHORT).show();
            });
        }

        v.setOnClickListener(view -> showSpecs(p));
        container.addView(v);
    }

    private void showPaymentMethodDialog(Product p) {
        String[] options = {"Efectivo", "Tarjeta de Crédito/Débito"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Método de Pago")
                .setItems(options, (dialog, which) -> {
                    String method = options[which];
                    confirmPurchase(p, method);
                })
                .show();
    }

    private void confirmPurchase(Product p, String method) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Compra")
                .setMessage("¿Deseas comprar " + p.getName() + " por $" + p.getPrice() + " usando " + method + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    CartManager.getInstance().addToPurchased(p);
                    updateProfileLists();
                    Toast.makeText(this, "¡Compra exitosa!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showCartDialog() {
        if (isGuest) {
            Toast.makeText(this, "Modo Explorador: Inicia sesión para usar el carrito", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tu Carrito JEFFPHONE");

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);

        LinearLayout itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);

        TextView tvTotal = new TextView(this);
        tvTotal.setTextSize(18);
        tvTotal.setTextColor(getResources().getColor(android.R.color.black));
        tvTotal.setGravity(Gravity.END);
        tvTotal.setPadding(0, 20, 0, 0);

        builder.setView(mainLayout);
        builder.setPositiveButton("Pagar Todo", (d, w) -> {
            for (Product p : new ArrayList<>(CartManager.getInstance().getCartItems())) {
                CartManager.getInstance().addToPurchased(p);
            }
            CartManager.getInstance().clearCart();
            updateProfileLists();
            Toast.makeText(this, "¡Gracias por tu compra!", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cerrar", null);

        AlertDialog dialog = builder.create();
        updateCartDialogUI(itemsContainer, tvTotal, dialog);
        mainLayout.addView(itemsContainer);
        mainLayout.addView(tvTotal);
        dialog.show();
    }

    private void updateCartDialogUI(LinearLayout container, TextView totalTv, AlertDialog dialog) {
        container.removeAllViews();
        List<Product> cartItems = CartManager.getInstance().getCartItems();

        if (cartItems.isEmpty()) {
            dialog.dismiss();
            return;
        }

        double total = 0;
        for (Product p : cartItems) {
            LinearLayout itemRow = new LinearLayout(this);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setGravity(Gravity.CENTER_VERTICAL);
            itemRow.setPadding(0, 10, 0, 10);

            TextView tv = new TextView(this);
            tv.setText("🛒 " + p.getName() + " - $" + p.getPrice());
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            tv.setTextColor(getResources().getColor(android.R.color.black));

            Button btnRemove = new Button(this);
            btnRemove.setText("X");
            btnRemove.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            btnRemove.setTextColor(getResources().getColor(android.R.color.white));
            btnRemove.setLayoutParams(new LinearLayout.LayoutParams(120, 100));

            btnRemove.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(p);
                updateProfileLists();
                updateCartDialogUI(container, totalTv, dialog);
                Toast.makeText(this, "Eliminado del carrito", Toast.LENGTH_SHORT).show();
            });

            itemRow.addView(tv);
            itemRow.addView(btnRemove);
            container.addView(itemRow);
            total += p.getPrice();
        }
        totalTv.setText("Total a pagar: $" + String.format(Locale.US, "%.2f", total));
    }

    private void updateProfileLists() {
        LinearLayout cartContainer = findViewById(R.id.cart_items_container);
        LinearLayout purchasedContainer = findViewById(R.id.purchased_items_container);
        
        if (cartContainer == null || purchasedContainer == null) return;

        cartContainer.removeAllViews();
        for (Product p : CartManager.getInstance().getCartItems()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView tv = new TextView(this);
            tv.setText("🛒 " + p.getName() + " - $" + p.getPrice());
            tv.setTextColor(getResources().getColor(android.R.color.black));
            tv.setPadding(0, 8, 0, 8);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button btnDel = new Button(this);
            btnDel.setText("X");
            btnDel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            btnDel.setTextColor(getResources().getColor(android.R.color.white));
            btnDel.setLayoutParams(new LinearLayout.LayoutParams(100, 80));
            btnDel.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(p);
                updateProfileLists();
            });

            row.addView(tv);
            row.addView(btnDel);
            cartContainer.addView(row);
        }

        purchasedContainer.removeAllViews();
        for (Product p : CartManager.getInstance().getPurchasedItems()) {
            TextView tv = new TextView(this);
            tv.setText("✅ " + p.getName() + " - $" + p.getPrice());
            tv.setTextColor(getResources().getColor(android.R.color.black));
            tv.setPadding(0, 8, 0, 8);
            purchasedContainer.addView(tv);
        }
    }

    private void showSpecs(Product p) {
        new AlertDialog.Builder(this)
                .setTitle(p.getName())
                .setMessage(p.getSpecs() + "\n\nPrecio: $" + p.getPrice())
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
