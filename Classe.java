package Model;

public class Classe {
    private int id;
    private String nom;

    public Classe(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }

    // Très important : c'est ce qui sera affiché dans le JComboBox
    @Override
    public String toString() {
        return nom;
    }
}