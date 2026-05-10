package Model;

public class Utilisateur {
    // Les attributs (caractéristiques) de chaque utilisateur
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role; // "CHEF", "ENSEIGNANT" ou "RESPONSABLE"

    // Le Constructeur : pour créer un utilisateur facilement
    public Utilisateur(int id, String nom, String prenom, String email, String motDePasse, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters : pour permettre aux autres classes de lire les informations
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getRole() {
        return role;
    }
    
    // On ajoutera d'autres méthodes plus tard si besoin
}
