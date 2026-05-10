package View;

import Model.Classe; // N'oublie pas de créer cette classe comme vu précédemment
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class AjoutEtudiantFrame extends JFrame {
    
    // On déclare les composants en dehors pour y accéder partout
    private JTextField txtNom = new JTextField();
    private JTextField txtPrenom = new JTextField();
    private JTextField txtEmail = new JTextField();
    private JComboBox<Classe> comboClasses = new JComboBox<>();
    private JButton btnValider = new JButton("Enregistrer");

    public AjoutEtudiantFrame() {
        setTitle("Ajouter un Étudiant");
        setSize(400, 350);
        setLocationRelativeTo(null);
        
        // Layout plus spacieux (5 lignes, 2 colonnes)
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Ajout des composants
        panel.add(new JLabel("Nom :"));
        panel.add(txtNom);
        
        panel.add(new JLabel("Prénom :"));
        panel.add(txtPrenom);
        
        panel.add(new JLabel("Email :"));
        panel.add(txtEmail);
        
        panel.add(new JLabel("Classe :"));
        panel.add(comboClasses);
        
        panel.add(new JLabel(""));
        panel.add(btnValider);

        // 1. Charger les classes dès l'ouverture de la fenêtre
        chargerClasses();

        // 2. Action du bouton valider
        btnValider.addActionListener(e -> enregistrerEtudiant());

        add(panel);
    }

    private void chargerClasses() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            String query = "SELECT id_classe, nom_classe FROM classes";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                // On ajoute des objets Classe dans le JComboBox
                comboClasses.addItem(new Classe(rs.getInt("id_classe"), rs.getString("nom_classe")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void enregistrerEtudiant() {
        String nom = txtNom.getText();
        String prenom = txtPrenom.getText();
        String email = txtEmail.getText();
        
        // On récupère l'objet Classe sélectionné
        Classe classeSelectionnee = (Classe) comboClasses.getSelectedItem();

        if (!nom.isEmpty() && !prenom.isEmpty() && !email.isEmpty() && classeSelectionnee != null) {
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
                
                // On ajoute id_classe et email dans la requête
                String sql = "INSERT INTO etudiants (nom, prenom, email, id_classe) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, nom);
                pst.setString(2, prenom);
                pst.setString(3, email);
                pst.setInt(4, classeSelectionnee.getId()); // On utilise l'ID de la classe
                
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Étudiant ajouté en " + classeSelectionnee.getNom() + " !");
                this.dispose();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.");
        }
    }
}