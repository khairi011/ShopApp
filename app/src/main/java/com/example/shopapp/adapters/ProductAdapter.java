package com.example.shopapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import java.util.List;
import java.util.Locale;

/**
 * =====================================================
 * Adaptateur : ProductAdapter.java
 * =====================================================
 * Fait le lien entre la liste de produits (données)
 * et le RecyclerView (interface graphique).
 *
 * Utilisé par :
 *   - MainActivity     (vue client  — grille 2 colonnes)
 *   - AdminActivity    (vue admin   — liste verticale)
 *
 * Pattern utilisé : ViewHolder (optimise les performances
 * en évitant des appels répétés à findViewById).
 *
 * Interface callback : permet à l'activité parente de
 * réagir aux clics sans couplage fort.
 * =====================================================
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // ── Contexte de l'activité appelante ──────────────
    private final Context context;

    // ── Liste des produits à afficher ─────────────────
    private final List<Product> productList;

    // ── Interface de callback pour les clics ──────────
    private final OnProductClickListener listener;

    // ── Interface fonctionnelle (callback) ────────────
    // L'activité parente implémente cette interface pour
    // recevoir le produit sur lequel l'utilisateur clique.
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    // ── Constructeur ──────────────────────────────────
    public ProductAdapter(Context context,
                          List<Product> productList,
                          OnProductClickListener listener) {
        this.context     = context;
        this.productList = productList;
        this.listener    = listener;
    }

    // ═══════════════════════════════════════════════════
    //  MÉTHODES OBLIGATOIRES du RecyclerView.Adapter
    // ═══════════════════════════════════════════════════

    /**
     * Crée une nouvelle vue (appelé quand le RecyclerView
     * a besoin d'un nouveau ViewHolder).
     * On "gonfle" (inflate) le layout XML de l'item.
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Charger le layout XML item_product.xml
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    /**
     * Remplit les données dans le ViewHolder à la position donnée.
     * Appelé par le RecyclerView pour chaque item visible.
     * @param holder   Le ViewHolder à remplir
     * @param position L'index dans la liste
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Récupérer le produit à cette position
        Product product = productList.get(position);

        // Remplir les TextViews avec les données du produit
        holder.tvNom.setText(product.getNom());
        holder.tvPrix.setText(String.format(Locale.getDefault(), "%.2f DT", product.getPrix()));
        holder.tvCategorie.setText(product.getCategorie());

        // Afficher le statut de stock avec couleur indicative
        if (product.getQuantite() > 0) {
            String stockText = context.getString(R.string.in_stock_format, product.getQuantite());
            holder.tvStock.setText(stockText);
            holder.tvStock.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.tvStock.setText(R.string.out_of_stock);
            holder.tvStock.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }

        // Gérer le clic sur la carte — notifier l'activité parente
        holder.cardView.setOnClickListener(v -> listener.onProductClick(product));
    }

    /**
     * Retourne le nombre total d'éléments dans la liste.
     * Indispensable pour que le RecyclerView sache combien
     * d'items afficher.
     */
    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ═══════════════════════════════════════════════════
    //  ViewHolder — mémorise les références aux vues
    //  pour éviter des appels coûteux à findViewById()
    //  à chaque défilement.
    // ═══════════════════════════════════════════════════
    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        // La carte contenante
        CardView cardView;

        // Les vues à l'intérieur de la carte
        TextView tvNom;
        TextView tvPrix;
        TextView tvCategorie;
        TextView tvStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Lier les vues à leurs IDs XML
            cardView    = itemView.findViewById(R.id.card_view);
            tvNom       = itemView.findViewById(R.id.tv_nom);
            tvPrix      = itemView.findViewById(R.id.tv_prix);
            tvCategorie = itemView.findViewById(R.id.tv_categorie);
            tvStock     = itemView.findViewById(R.id.tv_stock);
        }
    }
}
