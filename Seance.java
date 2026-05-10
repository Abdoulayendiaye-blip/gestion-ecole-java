 package Model;

public class Seance {
    private int id;
    private String date;      // Format "JJ/MM/AAAA"
    private String heure;     // Format "HH:mm"
    private int duree;        // En minutes ou heures
    private String contenu;   // Ce qui a été fait durant le cours
    private String observations;
    private String etat;      // "BROUILLON", "EN_ATTENTE", "VALIDE", "REJETE"
    private String commentaireAnnulation; // Pour les motifs de rejet

    // Constructeur pour créer une nouvelle séance
    public Seance(int id, String date, String heure, int duree, String contenu, String observations) {
        this.id = id;
        this.date = date;
        this.heure = heure;
        this.duree = duree;
        this.contenu = contenu;
        this.observations = observations;
        this.etat = "BROUILLON"; // Par défaut, une séance commence en brouillon
    }

    // Méthode pour changer l'état (utile pour le Responsable de classe)
    public void validerSeance() {
        this.etat = "VALIDE";
    }

    public void rejeterSeance(String commentaire) {
        this.etat = "REJETE";
        this.commentaireAnnulation = commentaire;
    }
}