package com.example.shopapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopapp.R;
import com.example.shopapp.adapters.ProductAdapter;
import com.example.shopapp.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================
 * Activité 5 : AdminActivity.java  [Acteur : ADMIN]
 * =====================================================
 * Tableau de bord de l'administrateur.
 * Affiche tous les produits dans un RecyclerView.
 * Comprend un header personnalisé avec bouton de déconnexion.
 * =====================================================
 */
public class AdminActivity extends AppCompatActivity {

    private RecyclerView   recyclerView;
    private ProductAdapter productAdapter;
    private List<Product>  productList;
    private ProgressBar    progressBar;
    private FloatingActionButton fabAjouter;
    private MaterialButton btnLogout;

    private FirebaseFirestore db;
    private FirebaseAuth      mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Masquer l'ActionBar car nous utilisons un header personnalisé dans le layout
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialisation Firebase
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Liaison des vues
        recyclerView = findViewById(R.id.recycler_view);
        progressBar  = findViewById(R.id.progress_bar);
        fabAjouter   = findViewById(R.id.fab_ajouter);
        btnLogout    = findViewById(R.id.btn_logout_admin);

        productList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        productAdapter = new ProductAdapter(this, productList, product -> {
            // Clic simple -> ouvrir l'écran de modification
            ouvrirModification(product);
        });
        recyclerView.setAdapter(productAdapter);

        // Appui long -> confirmation de suppression
        recyclerView.addOnItemTouchListener(new RecyclerItemLongClickListener(this,
                recyclerView, (view, position) -> {
            if (position >= 0 && position < productList.size()) {
                demanderConfirmationSuppression(productList.get(position));
            }
        }));

        // Charger les données
        chargerProduits();

        // Action bouton ajouter
        fabAjouter.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditProductActivity.class));
        });

        // Action bouton déconnexion du layout
        btnLogout.setOnClickListener(v -> logout());
    }

    private void chargerProduits() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("produits")
                .orderBy("nom")
                .addSnapshotListener((valeur, erreur) -> {
                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        if (erreur != null) {
                            Toast.makeText(this, getString(R.string.error_format, erreur.getMessage()), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        productList.clear();
                        if (valeur != null) {
                            for (QueryDocumentSnapshot doc : valeur) {
                                Product product = doc.toObject(Product.class);
                                product.setId(doc.getId());
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void ouvrirModification(Product product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("product_id",          product.getId());
        intent.putExtra("product_nom",         product.getNom());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_prix",        product.getPrix());
        intent.putExtra("product_quantite",    product.getQuantite());
        intent.putExtra("product_categorie",   product.getCategorie());
        startActivity(intent);
    }

    private void demanderConfirmationSuppression(Product product) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message_format, product.getNom()))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> supprimerProduit(product))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void supprimerProduit(Product product) {
        db.collection("produits")
                .document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (!isFinishing()) {
                        Toast.makeText(this, getString(R.string.product_deleted_format, product.getNom()), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing()) {
                        Toast.makeText(this, getString(R.string.error_deleting_format, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
