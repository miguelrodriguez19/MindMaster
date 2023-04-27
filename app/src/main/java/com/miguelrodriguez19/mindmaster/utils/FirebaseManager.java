package com.miguelrodriguez19.mindmaster.utils;

import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    private static FirebaseManager INSTANCE;
   // private FirebaseFirestore db;
    private FirebaseAuth auth;

    private FirebaseManager() {
        //db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FirebaseManager();
        }
        return INSTANCE;
    }

    /*public FirebaseFirestore getFirestore() {
        return db;
    }*/

    public FirebaseAuth getAuth() {
        return auth;
    }

    // Agrega aquí los métodos que necesites para interactuar con Firestore y Auth
}
