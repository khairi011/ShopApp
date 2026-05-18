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

/**
 * =====================================================
 * Activité 2 : RegisterActivity.java
 * =====================================================
 * Écran d'inscription réservé aux nouveaux clients.
 * L'admin n'a pas besoin de s'inscrire (son compte
 * est créé manuellement dans la console Firebase).
 *
 * Après une inscription réussie, l'utilisateur est
 * automatiquement connecté et redirigé vers MainActivity.
 * =====================================================
 */
public class RegisterActivity extends AppCompatActivity {

    // ── Vues de l'interface ───────────────────────────
    private EditText    etNom, etEmail, etPassword, etConfirmPassword;
    private Button      btnRegister;
    private TextView    tvLogin;
    private ProgressBar progressBar;

    // ── Instance Firebase Auth ────────────────────────
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation de Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Liaison des vues avec leurs identifiants XML
        etNom             = findViewById(R.id.et_nom);
        etEmail           = findViewById(R.id.et_email);
        etPassword        = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister       = findViewById(R.id.btn_register);
        tvLogin           = findViewById(R.id.tv_login);
        progressBar       = findViewById(R.id.progress_bar);

        // ── Bouton S'inscrire ─────────────────────────
        btnRegister.setOnClickListener(v -> {
            // Récupérer les valeurs saisies
            String nom             = etNom.getText().toString().trim();
            String email           = etEmail.getText().toString().trim();
            String password        = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // ── Validation des champs ─────────────────
            if (nom.isEmpty()) {
                etNom.setError(getString(R.string.error_name_required));
                etNom.requestFocus();
                return;
            }
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
            if (!password.equals(confirmPassword)) {
                // Les mots de passe ne correspondent pas
                etConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
                etConfirmPassword.requestFocus();
                return;
            }

            // Afficher le loader
            afficherChargement(true);

            // Créer le compte avec Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (!isFinishing()) {
                            afficherChargement(false);

                            if (task.isSuccessful()) {
                                // Inscription réussie → aller à MainActivity
                                Toast.makeText(this,
                                        getString(R.string.registration_success_format, nom),
                                        Toast.LENGTH_SHORT).show();

                                // Rediriger vers la liste des produits
                                Intent intent = new Intent(this, MainActivity.class);
                                // Passer le nom de l'utilisateur à MainActivity
                                intent.putExtra("user_nom", nom);
                                // Vider la pile d'activités (ne pas revenir en arrière)
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {
                                // Échec — afficher l'erreur Firebase
                                String erreur = task.getException() != null
                                        ? task.getException().getMessage()
                                        : getString(R.string.error_registration_failed);
                                Toast.makeText(this, erreur, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // ── Lien de retour vers la connexion ──────────
        tvLogin.setOnClickListener(v -> {
            // Retourner à LoginActivity (la pile remonte)
            finish();
        });
    }

    /**
     * Affiche ou cache l'indicateur de chargement.
     * @param chargement true = afficher, false = cacher
     */
    private void afficherChargement(boolean chargement) {
        progressBar.setVisibility(chargement ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!chargement);
    }
}
