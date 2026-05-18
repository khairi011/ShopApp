package com.example.shopapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.shopapp.R;
import java.util.Locale;

/**
 * =====================================================
 * Activité 4 : ProductDetailActivity.java  [CLIENT]
 * =====================================================
 * Affiche toutes les informations d'un produit
 * sélectionné depuis MainActivity.
 *
 * Les données sont reçues via Intent.getExtra()
 * (passées depuis MainActivity.ouvrirDetailProduit()).
 *
 * Fonctionnalités :
 *  - Affichage complet du produit
 *  - Bouton "Ajouter au panier" (avec contrôle de stock)
 *  - Gestion des ruptures de stock
 * =====================================================
 */
public class ProductDetailActivity extends AppCompatActivity {

    // ── Vues pour les informations du produit ─────────
    private TextView tvNom;
    private TextView tvCategorie;
    private TextView tvPrix;
    private TextView tvDescription;
    private TextView tvStock;
    private Button   btnAjouterPanier;

    // ── Données du produit reçues via Intent ──────────
    private String productId;
    private String productNom;
    private String productDescription;
    private double productPrix;
    private int    productQuantite;
    private String productCategorie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // ── Récupérer les données de l'Intent ─────────
        // Ces valeurs ont été envoyées par MainActivity
        productId          = getIntent().getStringExtra("product_id");
        productNom         = getIntent().getStringExtra("product_nom");
        productDescription = getIntent().getStringExtra("product_description");
        productPrix        = getIntent().getDoubleExtra("product_prix", 0.0);
        productQuantite    = getIntent().getIntExtra("product_quantite", 0);
        productCategorie   = getIntent().getStringExtra("product_categorie");

        // Titre de la barre d'action = nom du produit
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(productNom);
            // Bouton Retour dans la barre d'action
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Liaison des vues
        tvNom            = findViewById(R.id.tv_nom);
        tvCategorie      = findViewById(R.id.tv_categorie);
        tvPrix           = findViewById(R.id.tv_prix);
        tvDescription    = findViewById(R.id.tv_description);
        tvStock          = findViewById(R.id.tv_stock);
        btnAjouterPanier = findViewById(R.id.btn_ajouter_panier);

        // Remplir les TextViews avec les données reçues
        afficherDetailsProduit();

        // ── Bouton Ajouter au panier ───────────────────
        btnAjouterPanier.setOnClickListener(v -> {
            if (productQuantite > 0) {
                // Produit disponible → simuler l'ajout au panier
                // (dans un vrai projet : mettre à jour une collection "panier" dans Firestore)
                Toast.makeText(this,
                        getString(R.string.added_to_cart_format, productNom),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Rupture de stock
                Toast.makeText(this,
                        R.string.error_out_of_stock,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Remplit les TextViews avec les informations du produit.
     * Adapte l'affichage selon la disponibilité du stock.
     */
    private void afficherDetailsProduit() {
        tvNom.setText(productNom);
        tvCategorie.setText(getString(R.string.category_format, productCategorie));
        tvPrix.setText(String.format(Locale.getDefault(), "Prix : %.2f DT", productPrix));
        tvDescription.setText(productDescription);

        if (productQuantite > 0) {
            // Produit en stock → bouton actif + message vert
            tvStock.setText(getString(R.string.in_stock_format, productQuantite));
            tvStock.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            btnAjouterPanier.setEnabled(true);
        } else {
            // Rupture de stock → bouton désactivé + message rouge
            tvStock.setText(R.string.out_of_stock);
            tvStock.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            btnAjouterPanier.setEnabled(false);
            btnAjouterPanier.setText(R.string.unavailable);
        }
    }

    /**
     * Gère le clic sur le bouton Retour (flèche dans la barre d'action).
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Revenir à l'activité précédente (MainActivity)
        finish();
        return true;
    }
}
