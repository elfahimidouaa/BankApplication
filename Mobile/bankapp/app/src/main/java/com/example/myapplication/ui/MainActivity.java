package com.example.myapplication.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.CompteAdapter;
import com.example.myapplication.api.config;
import com.example.myapplication.api.CompteApi;
import com.example.myapplication.model.Compte;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CompteAdapter adapter;
    private String selectedFormat = "application/json"; // Default format

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Spinner SpinnerId = findViewById(R.id.SpinnerId);
        SpinnerId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFormat = parent.getItemAtPosition(position).toString().equals("JSON")
                        ? "application/json" : "application/xml";
                fetchComptes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fetchComptes(); 
            }
        });

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new Compte object 
                showAddCompteDialog();
            }
        });
        fetchComptes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            fetchComptes(); // Refresh data after modification
        }
    }


    private void fetchComptes() {
        CompteApi api = config.getClient(selectedFormat).create(CompteApi.class);
        Call<List<Compte>> call = api.getComptes();

        call.enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new CompteAdapter(MainActivity.this, response.body());
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Erreur de récupération", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCompte(Compte compte) {
        CompteApi api = config.getClient(selectedFormat).create(CompteApi.class);
        Call<Compte> call = api.addCompte(compte);

        call.enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Compte ajouté", Toast.LENGTH_SHORT).show();
                    fetchComptes(); 
                } else {
                    Toast.makeText(MainActivity.this, "Erreur d'ajout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCompteDialog() {
        // Create a new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Ajouter un Compte");

        // Create a LinearLayout for the form
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create EditTexts for each field (id, solde)
        final EditText idEditText = new EditText(MainActivity.this);
        idEditText.setHint("ID");
        final EditText soldeEditText = new EditText(MainActivity.this);
        soldeEditText.setHint("Solde");

        // Create a Spinner for the type field
        final Spinner typeSpinner = new Spinner(MainActivity.this);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, new String[]{"EPARGNE", "COURANT"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Add the EditTexts and Spinner to the layout
        layout.addView(idEditText);
        layout.addView(soldeEditText);
        layout.addView(typeSpinner);

        // Set the layout to the dialog
        builder.setView(layout);

        // Set the "Add" button
        builder.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the user input
                String idStr = idEditText.getText().toString();
                String soldeStr = soldeEditText.getText().toString();
                String type = typeSpinner.getSelectedItem().toString(); // Get the selected type

                // Validate the input
                if (idStr.isEmpty() || soldeStr.isEmpty() || type.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    // Convert input to appropriate types
                    Long id = Long.parseLong(idStr);
                    double solde = Double.parseDouble(soldeStr);

                    // Create a new Compte object
                    Compte newCompte = new Compte(id, solde, type);

                    // Call the method to add the compte
                    addCompte(newCompte);
                }
            }
        });

        // Set the "Cancel" button
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.show();
    }


}
