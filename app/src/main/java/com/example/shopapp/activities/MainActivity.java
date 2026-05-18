package com.example.shopapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopapp.R;
import com.example.shopapp.adapters.ProductAdapter;
import com.example.shopapp.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================
 * Activité 3 : MainActivity.java  [Acteur : CLIENT]
 * =====================================================
 * Écran principal du client : affiche tous les produits
 * de la boutique dans un RecyclerView en grille (2 col).
 *
 * Fonctionnalités :
 *  - Chargement en temps réel depuis Firestore
 *    (SnapshotListener = mise à jour automatique)
 *  - Recherche par nom ou catégorie (filtre local)
 *  - Clic sur un produit → ProductDetailActivity
 *  - Menu de déconnexion
 * =====================================================
 */
public class MainActivity extends AppCompatActivity {

    // ── RecyclerView : liste des produits ─────────────
    private RecyclerView    recyclerView;
    private ProductAdapter  productAdapter;

    // ── Listes de données ─────────────────────────────
    // productList  : tous les produits chargés depuis Firebase
    // filteredList : les produits après application du filtre
    private List<Product> productList;
    private List<Product> filteredList;

    // ── Vues ──────────────────────────────────────────
    private EditText    etSearch;
    private ProgressBar progressBar;

    // ── Firebase ──────────────────────────────────────
    private FirebaseFirestore db;
    private FirebaseAuth      mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Titre de la barre d'action
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.main_title);
        }

        // Initialisation Firebase
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Liaison des vues
        recyclerView = findViewById(R.id.recycler_view);
        etSearch     = findViewById(R.id.et_search);
        progressBar  = findViewById(R.id.progress_bar);

        // Initialisation des listes
        productList  = new ArrayList<>();
        filteredList = new ArrayList<>();

        // ── Configuration du RecyclerView ────────────
        // GridLayoutManager(contexte, nb_colonnes)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true); // Optimisation performances

        // ── Création de l'adaptateur ──────────────────
        // On passe une lambda (OnProductClickListener) :
        // quand l'utilisateur clique → ouvrir le détail
        productAdapter = new ProductAdapter(this, filteredList, product -> {
            ouvrirDetailProduit(product);
        });
        recyclerView.setAdapter(productAdapter);

        // Charger les produits depuis Firestore
        chargerProduits();

        // ── Recherche en temps réel ───────────────────
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Rien à faire avant le changement
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrer la liste à chaque frappe de touche
                filtrerProduits(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Rien à faire après le changement
            }
        });
    }

    /**
     * Charge tous les produits depuis Firestore.
     *
     * addSnapshotListener() → écoute en temps réel :
     * si un admin ajoute/modifie/supprime un produit,
     * la liste du client se met à jour automatiquement.
     */
    private void chargerProduits() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("produits")
                // Tri par nom pour un affichage ordonné
                .orderBy("nom")
                .addSnapshotListener((valeur, erreur) -> {

                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);

                        // Gérer les erreurs de lecture
                        if (erreur != null) {
                            Toast.makeText(this,
                                    getString(R.string.error_loading_format, erreur.getMessage()),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Vider les listes avant de recharger
                        productList.clear();

                        if (valeur != null) {
                            // Convertir chaque document Firestore en objet Product
                            for (QueryDocumentSnapshot doc : valeur) {
                                Product product = doc.toObject(Product.class);
                                // L'ID Firestore n'est PAS dans le document — on l'ajoute manuellement
                                product.setId(doc.getId());
                                productList.add(product);
                            }
                        }

                        // Réappliquer le filtre de recherche actuel
                        // (pour ne pas effacer la saisie de l'utilisateur)
                        String recherche = etSearch.getText().toString().trim();
                        filtrerProduits(recherche);
                    }
                });
    }

    /**
     * Filtre la liste des produits selon la saisie de l'utilisateur.
     * La recherche est insensible à la casse.
     *
     * @param recherche Texte saisi dans la barre de recherche
     */
    private void filtrerProduits(String recherche) {
        filteredList.clear();

        if (recherche.isEmpty()) {
            // Pas de filtre → afficher tous les produits
            filteredList.addAll(productList);
        } else {
            // Convertir en minuscules pour ignorer la casse
            String rechLower = recherche.toLowerCase();

            for (Product p : productList) {
                // Filtre sur le nom OU la catégorie
                boolean matchNom       = p.getNom().toLowerCase().contains(rechLower);
                boolean matchCategorie = p.getCategorie().toLowerCase().contains(rechLower);

                if (matchNom || matchCategorie) {
                    filteredList.add(p);
                }
            }
        }

        // Notifier l'adaptateur que les données ont changé
        productAdapter.notifyDataSetChanged();
    }

    /**
     * Ouvre l'écran de détail pour le produit sélectionné.
     * On passe les données via Intent.putExtra (plus simple qu'un objet Parcelable).
     *
     * @param product Le produit cliqué
     */
    private void ouvrirDetailProduit(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        // Passer toutes les informations du produit à l'activité suivante
        intent.putExtra("product_id",          product.getId());
        intent.putExtra("product_nom",         product.getNom());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_prix",        product.getPrix());
        intent.putExtra("product_quantite",    product.getQuantite());
        intent.putExtra("product_categorie",   product.getCategorie());
        startActivity(intent);
    }

    // ── Menu de la barre d'action ─────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Charger le menu XML (res/menu/menu_main.xml)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Déconnecter l'utilisateur de Firebase
            mAuth.signOut();
            // Retourner à l'écran de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            // Vider toute la pile d'activités
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
