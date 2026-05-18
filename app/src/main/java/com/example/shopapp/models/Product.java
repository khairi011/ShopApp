package com.example.shopapp.models;

/**
 * =====================================================
 * Modèle : Product.java
 * =====================================================
 * Représente un produit dans la boutique.
 * Ce POJO (Plain Old Java Object) est utilisé par
 * Firebase Firestore pour sérialiser / désérialiser
 * les documents de la collection "produits".
 *
 * Règle Firebase : le constructeur VIDE est OBLIGATOIRE.
 * =====================================================
 */
public class Product {

    // ── Champs correspondant aux propriétés Firestore ──
    private String id;          // ID du document (généré par Firestore)
    private String nom;         // Nom du produit
    private String description; // Description détaillée
    private double prix;        // Prix en DT
    private int    quantite;    // Quantité en stock
    private String categorie;   // Catégorie (ex: "Électronique", "Vêtements"…)

    // ── Constructeur VIDE requis par Firebase ──────────
    public Product() { }

    // ── Constructeur complet (utilisé lors de la création) ──
    public Product(String id, String nom, String description,
                   double prix, int quantite, String categorie) {
        this.id          = id;
        this.nom         = nom;
        this.description = description;
        this.prix        = prix;
        this.quantite    = quantite;
        this.categorie   = categorie;
    }

    // ═══════════════════════════════════════════════════
    //  GETTERS & SETTERS
    //  Nécessaires pour que Firestore lise / écrive
    //  automatiquement les champs de l'objet.
    // ═══════════════════════════════════════════════════

    public String getId()               { return id; }
    public void   setId(String id)      { this.id = id; }

    public String getNom()              { return nom; }
    public void   setNom(String nom)    { this.nom = nom; }

    public String getDescription()                     { return description; }
    public void   setDescription(String description)   { this.description = description; }

    public double getPrix()             { return prix; }
    public void   setPrix(double prix)  { this.prix = prix; }

    public int  getQuantite()              { return quantite; }
    public void setQuantite(int quantite)  { this.quantite = quantite; }

    public String getCategorie()                   { return categorie; }
    public void   setCategorie(String categorie)   { this.categorie = categorie; }

    /**
     * Retourne une représentation lisible du produit.
     * Utile pour les logs et le débogage.
     */
    @Override
    public String toString() {
        return "Product{id='" + id + "', nom='" + nom +
                "', prix=" + prix + " DT, quantite=" + quantite + "}";
    }
}