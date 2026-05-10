package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AjoutProfFrame extends JFrame {
    private JTextField txtNom, txtPrenom, txtLogin, txtEmail; // Ajout de txtPrenom
    private JPasswordField txtPass;

    public AjoutProfFrame() {
        setTitle("Ajouter un nouvel Enseignant");
        setSize(350, 400); // Augmenté pour laisser de la place au nouveau champ
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Passé de 5 à 6 lignes dans le GridLayout pour accueillir Prénom
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtNom = new JTextField();
        txtPrenom = new JTextField();
        txtLogin = new JTextField();
        txtEmail = new JTextField();
        txtPass = new JPasswordField();

        p.add(new JLabel("Nom :")); p.add(txtNom);
        p.add(new JLabel("Prénom :")); p.add(txtPrenom);
        p.add(new JLabel("Login :")); p.add(txtLogin);
        p.add(new JLabel("Email :")); p.add(txtEmail);
        p.add(new JLabel("Mot de passe :")); p.add(txtPass);

        JButton btnSave = new JButton("ENREGISTRER");
        btnSave.addActionListener(e -> enregistrer());
        
        add(p, BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void enregistrer() {
        String nom = txtNom.getText();
        String prenom = txtPrenom.getText();
        String login = txtLogin.getText();
        String email = txtEmail.getText();
        String pass = new String(txtPass.getPassword());

        // Vérification que tous les champs sont remplis
        if(nom.isEmpty() || prenom.isEmpty() || login.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont requis !");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            // Requête incluant nom et prenom séparés
            String sql = "INSERT INTO utilisateurs (nom, prenom, login, password, role, email) VALUES (?, ?, ?, ?, 'enseignant', ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nom);
            pst.setString(2, prenom);
            pst.setString(3, login);
            pst.setString(4, pass);
            pst.setString(5, email);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Enseignant ajouté avec succès !");
            this.dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }
}