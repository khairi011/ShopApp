package com.example.shopapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shopapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================
 * Activité 6 : AddEditProductActivity.java  [ADMIN]
 * =====================================================
 * Formulaire pour AJOUTER ou MODIFIER un produit.
 *
 * Mode AJOUT   : aucun product_id reçu dans l'Intent
 *                → crée un nouveau document Firestore
 * Mode MODIF   : product_id reçu dans l'Intent
 *                → met à jour le document existant
 *
 * Les données sont stockées dans la collection
 * "produits" de Firebase Firestore.
 * =====================================================
 */
public class AddEditProductActivity extends AppCompatActivity {

    // ── Vues du formulaire ────────────────────────────
    private EditText    etNom, etDescription, etPrix, etQuantite, etCategorie;
    private Button      btnEnregistrer;
    private ProgressBar progressBar;

    // ── Firebase ──────────────────────────────────────
    private FirebaseFirestore db;

    // ── Mode de l'activité ────────────────────────────
    // true  = modification d'un produit existant
    // false = ajout d'un nouveau produit
    private boolean modeModification = false;

    // ID du produit à modifier (null si mode ajout)
    private String productId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        // Liaison des vues avec leurs identifiants XML
        etNom         = findViewById(R.id.et_nom);
        etDescription = findViewById(R.id.et_description);
        etPrix        = findViewById(R.id.et_prix);
        etQuantite    = findViewById(R.id.et_quantite);
        etCategorie   = findViewById(R.id.et_categorie);
        btnEnregistrer = findViewById(R.id.btn_enregistrer);
        progressBar   = findViewById(R.id.progress_bar);

        // ── Détecter le mode (ajout ou modification) ──
        // Si un product_id est passé dans l'Intent → mode modification
        productId = getIntent().getStringExtra("product_id");

        if (productId != null && !productId.isEmpty()) {
            // ── MODE MODIFICATION ─────────────────────
            modeModification = true;

            // Titre de la barre d'action
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_edit_product);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Pré-remplir les champs avec les valeurs existantes
            etNom.setText(getIntent().getStringExtra("product_nom"));
            etDescription.setText(getIntent().getStringExtra("product_description"));
            etPrix.setText(String.valueOf(getIntent().getDoubleExtra("product_prix", 0.0)));
            etQuantite.setText(String.valueOf(getIntent().getIntExtra("product_quantite", 0)));
            etCategorie.setText(getIntent().getStringExtra("product_categorie"));

            btnEnregistrer.setText(R.string.btn_update);

        } else {
            // ── MODE AJOUT ────────────────────────────
            modeModification = false;

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_add_product);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            btnEnregistrer.setText(R.string.btn_add_product);
        }

        // ── Bouton Enregistrer ────────────────────────
        btnEnregistrer.setOnClickListener(v -> {
            // Valider les champs puis sauvegarder
            if (validerChamps()) {
                enregistrerProduit();
            }
        });
    }

    /**
     * Valide tous les champs du formulaire.
     * Affiche un message d'erreur si un champ est invalide.
     *
     * @return true si tous les champs sont valides, false sinon
     */
    private boolean validerChamps() {
        String nom         = etNom.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String prixStr     = etPrix.getText().toString().trim();
        String quantiteStr = etQuantite.getText().toString().trim();
        String categorie   = etCategorie.getText().toString().trim();

        // Vérifier que le nom n'est pas vide
        if (nom.isEmpty()) {
            etNom.setError(getString(R.string.error_name_required));
            etNom.requestFocus();
            return false;
        }

        // Vérifier que la description n'est pas vide
        if (description.isEmpty()) {
            etDescription.setError(getString(R.string.error_description_required));
            etDescription.requestFocus();
            return false;
        }

        // Vérifier le prix
        if (prixStr.isEmpty()) {
            etPrix.setError(getString(R.string.error_price_required));
            etPrix.requestFocus();
            return false;
        }
        try {
            double prix = Double.parseDouble(prixStr);
            if (prix < 0) {
                etPrix.setError(getString(R.string.error_price_positive));
                etPrix.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrix.setError(getString(R.string.error_invalid_price));
            etPrix.requestFocus();
            return false;
        }

        // Vérifier la quantité
        if (quantiteStr.isEmpty()) {
            etQuantite.setError(getString(R.string.error_quantity_required));
            etQuantite.requestFocus();
            return false;
        }
        try {
            int quantite = Integer.parseInt(quantiteStr);
            if (quantite < 0) {
                etQuantite.setError(getString(R.string.error_quantity_positive));
                etQuantite.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etQuantite.setError(getString(R.string.error_invalid_quantity));
            etQuantite.requestFocus();
            return false;
        }

        // Vérifier la catégorie
        if (categorie.isEmpty()) {
            etCategorie.setError(getString(R.string.error_category_required));
            etCategorie.requestFocus();
            return false;
        }

        // Tous les champs sont valides
        return true;
    }

    /**
     * Enregistre ou met à jour le produit dans Firestore.
     * Appelle addProduit() ou modifierProduit() selon le mode.
     */
    private void enregistrerProduit() {
        // Lire les valeurs des champs
        String nom         = etNom.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        double prix        = Double.parseDouble(etPrix.getText().toString().trim());
        int    quantite    = Integer.parseInt(etQuantite.getText().toString().trim());
        String categorie   = etCategorie.getText().toString().trim();

        // Construire la Map des données à envoyer à Firestore
        // (Firestore stocke les documents comme des Maps clé-valeur)
        Map<String, Object> donneesProduit = new HashMap<>();
        donneesProduit.put("nom",         nom);
        donneesProduit.put("description", description);
        donneesProduit.put("prix",        prix);
        donneesProduit.put("quantite",    quantite);
        donneesProduit.put("categorie",   categorie);

        // Afficher le loader
        afficherChargement(true);

        if (modeModification) {
            // ── MISE À JOUR d'un document existant ────
            db.collection("produits")
                    .document(productId) // Cibler le document par son ID
                    .update(donneesProduit) // Mettre à jour uniquement les champs modifiés
                    .addOnSuccessListener(aVoid -> {
                        if (!isFinishing()) {
                            afficherChargement(false);
                            Toast.makeText(this, R.string.product_updated, Toast.LENGTH_SHORT).show();
                            finish(); // Fermer et retourner à AdminActivity
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isFinishing()) {
                            afficherChargement(false);
                            Toast.makeText(this, getString(R.string.error_format, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            // ── AJOUT d'un nouveau document ───────────
            // Firestore génère automatiquement un ID unique
            db.collection("produits")
                    .add(donneesProduit)
                    .addOnSuccessListener(documentReference -> {
                        if (!isFinishing()) {
                            afficherChargement(false);
                            Toast.makeText(this,
                                    getString(R.string.product_added_format, nom),
                                    Toast.LENGTH_SHORT).show();
                            finish(); // Fermer et retourner à AdminActivity
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isFinishing()) {
                            afficherChargement(false);
                            Toast.makeText(this, getString(R.string.error_format, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /**
     * Affiche ou cache l'indicateur de chargement.
     * @param chargement true = afficher, false = cacher
     */
    private void afficherChargement(boolean chargement) {
        progressBar.setVisibility(chargement ? View.VISIBLE : View.GONE);
        btnEnregistrer.setEnabled(!chargement);
    }

    /**
     * Gère le clic sur le bouton Retour de la barre d'action.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
