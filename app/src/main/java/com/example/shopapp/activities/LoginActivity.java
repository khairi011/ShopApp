package com.example.shopapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shopapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * =====================================================
 * Activité 1 : LoginActivity.java
 * =====================================================
 * Écran de connexion partagé entre les deux acteurs :
 *   - Admin  → redirigé vers AdminActivity
 *   - Client → redirigé vers MainActivity
 *
 * La distinction Admin / Client se fait par l'email :
 * si l'email est ADMIN_EMAIL, c'est l'administrateur.
 *
 * Firebase Auth gère l'authentification sécurisée.
 * =====================================================
 */
public class LoginActivity extends AppCompatActivity {

    // ── Email hardcodé de l'administrateur ───────────
    // (dans un vrai projet, utiliser des "Custom Claims" Firebase)
    private static final String ADMIN_EMAIL = "admin@boutique.com";

    // ── Vues de l'interface ───────────────────────────
    private EditText    etEmail, etPassword;
    private Button      btnLogin;
    private TextView    tvRegister;
    private ProgressBar progressBar;

    // ── Instance Firebase Auth ────────────────────────
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation de Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Liaison des vues avec leurs identifiants XML
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        tvRegister  = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);

        // ── Vérifier si une session est déjà active ───
        // (l'utilisateur a déjà ouvert l'app et n'a pas déconnecté)
        FirebaseUser utilisateurActuel = mAuth.getCurrentUser();
        if (utilisateurActuel != null) {
            // Session existante → rediriger directement
            redirigerUtilisateur(utilisateurActuel.getEmail());
            return; // Arrêter l'exécution du reste de onCreate
        }

        // ── Bouton Se connecter ───────────────────────
        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation des champs obligatoires
            if (email.isEmpty()) {
                etEmail.setError(getString(R.string.error_email_required));
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.error_password_required));
                etPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPassword.setError(getString(R.string.error_password_length));
                etPassword.requestFocus();
                return;
            }

            // Afficher le loader et désactiver le bouton
            // pour éviter les doubles clics
            afficherChargement(true);

            // Appel à Firebase Auth pour la connexion
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (!isFinishing()) {
                            afficherChargement(false); // Cacher le loader

                            if (task.isSuccessful()) {
                                // Connexion réussie
                                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                                redirigerUtilisateur(email);
                            } else {
                                // Échec de connexion — afficher le message d'erreur Firebase
                                String erreur = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Erreur inconnue";
                                Toast.makeText(this, getString(R.string.login_failed_format, erreur), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // ── Lien vers l'écran d'inscription ───────────
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    /**
     * Redirige l'utilisateur selon son rôle.
     * Appelé après une connexion réussie OU si une session existe déjà.
     *
     * @param email Email de l'utilisateur connecté
     */
    private void redirigerUtilisateur(String email) {
        Intent intent;
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            // C'est l'admin → aller à AdminActivity
            intent = new Intent(this, AdminActivity.class);
            Toast.makeText(this, R.string.welcome_admin, Toast.LENGTH_SHORT).show();
        } else {
            // C'est un client → aller à MainActivity
            intent = new Intent(this, MainActivity.class);
            Toast.makeText(this, R.string.welcome_user, Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);
        // finish() empêche de revenir à cet écran avec le bouton Retour
        finish();
    }

    /**
     * Affiche ou cache l'indicateur de chargement.
     * Active/désactive aussi le bouton pour éviter les doubles appels.
     *
     * @param chargement true = afficher, false = cacher
     */
    private void afficherChargement(boolean chargement) {
        progressBar.setVisibility(chargement ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!chargement);
    }
}
