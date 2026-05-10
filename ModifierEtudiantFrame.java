package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ModifierEtudiantFrame extends JFrame {
    public ModifierEtudiantFrame(int id, String nomActuel, String prenomActuel) {
        setTitle("Modifier Étudiant #" + id);
        setSize(350, 250);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtNom = new JTextField(nomActuel);
        JTextField txtPrenom = new JTextField(prenomActuel);
        JButton btnUpdate = new JButton("Enregistrer les modifications");
        btnUpdate.setBackground(new Color(46, 204, 113));
        btnUpdate.setForeground(Color.WHITE);

        panel.add(new JLabel("Nom :"));
        panel.add(txtNom);
        panel.add(new JLabel("Prénom :"));
        panel.add(txtPrenom);
        panel.add(new JLabel(""));
        panel.add(btnUpdate);

        btnUpdate.addActionListener(e -> {
            String nouveauNom = txtNom.getText().trim();
            String nouveauPrenom = txtPrenom.getText().trim();
            
            if(!nouveauNom.isEmpty() && !nouveauPrenom.isEmpty()) {
                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
                    String sql = "UPDATE etudiants SET nom = ?, prenom = ? WHERE id = ?";
                    PreparedStatement pst = con.prepareStatement(sql);
                    pst.setString(1, nouveauNom);
                    pst.setString(2, nouveauPrenom);
                    pst.setInt(3, id);
                    
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Informations mises à jour !");
                    this.dispose();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        add(panel);
    }
}