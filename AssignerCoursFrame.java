package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AssignerCoursFrame extends JFrame {
    private JComboBox<String> comboProfs;
    private JComboBox<String> comboClasses;
    private JTextField txtMatiere;

    public AssignerCoursFrame() {
        setTitle("Assignation des cours");
        setSize(400, 350);
        setLocationRelativeTo(null);
        // On garde ton style de layout
        setLayout(new GridLayout(6, 1, 10, 10));

        comboProfs = new JComboBox<>();
        comboClasses = new JComboBox<>();
        txtMatiere = new JTextField();
        txtMatiere.setBorder(BorderFactory.createTitledBorder("Nom de la matière"));

        JButton btnValider = new JButton("Assigner la matière");
        
        chargerProfs();
        chargerClasses();

        btnValider.addActionListener(e -> enregistrer());

        add(new JLabel("  Sélectionnez l'enseignant :"));
        add(comboProfs);
        add(new JLabel("  Sélectionnez la classe :"));
        add(comboClasses);
        add(txtMatiere);
        add(btnValider);
    }

    private void chargerProfs() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            ResultSet rs = con.createStatement().executeQuery("SELECT nom FROM utilisateurs WHERE role='enseignant'");
            while(rs.next()) {
                comboProfs.addItem(rs.getString("nom"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerClasses() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            ResultSet rs = con.createStatement().executeQuery("SELECT nom_classe FROM classes");
            while(rs.next()) {
                comboClasses.addItem(rs.getString("nom_classe"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void enregistrer() {
        String nomProf = (String) comboProfs.getSelectedItem();
        String nomClasse = (String) comboClasses.getSelectedItem();
        String matiere = txtMatiere.getText().trim();

        if(matiere.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une matière.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            // Ici, j'utilise c.id_classe au lieu de c.id pour éviter ton erreur SQL
            String sqlIds = "SELECT u.id AS prof_id, u.email, c.id_classe AS classe_id " +
                            "FROM utilisateurs u, classes c " +
                            "WHERE u.nom = ? AND c.nom_classe = ?";
            
            PreparedStatement psId = con.prepareStatement(sqlIds);
            psId.setString(1, nomProf);
            psId.setString(2, nomClasse);
            ResultSet rs = psId.executeQuery();
            
            if(rs.next()) {
                int idProf = rs.getInt("prof_id");
                int idClasse = rs.getInt("classe_id");
                String emailProf = rs.getString("email");

                // 1. Enregistrement en base de données avec id_classe
                PreparedStatement psIns = con.prepareStatement("INSERT INTO affectations (enseignant_id, nom_matiere, id_classe) VALUES (?, ?, ?)");
                psIns.setInt(1, idProf);
                psIns.setString(2, matiere);
                psIns.setInt(3, idClasse);
                psIns.executeUpdate();

                // 2. ENVOI DE L'EMAIL (Ton code d'origine)
                if (emailProf != null && !emailProf.isEmpty()) {
                    String dateJour = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
                    EmailSender.envoyerNotification(emailProf, matiere, dateJour);
                    JOptionPane.showMessageDialog(this, "Cours assigné et email envoyé à " + emailProf);
                } else {
                    JOptionPane.showMessageDialog(this, "Cours assigné, mais le prof n'a pas d'email.");
                }

                this.dispose();
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Erreur lors de l'assignation : " + e.getMessage());
        }
    }
}