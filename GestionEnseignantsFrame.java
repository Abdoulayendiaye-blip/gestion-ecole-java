package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestionEnseignantsFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtNom, txtEmail, txtPass;

    public GestionEnseignantsFrame() {
        Color bgDark = new Color(44, 62, 80);
        setTitle("ESITEC - Gestion des Enseignants");
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgDark);
        setLayout(new BorderLayout(15, 15));

        // --- FORMULAIRE ---
        JPanel pForm = new JPanel(new GridLayout(2, 3, 15, 10));
        pForm.setBackground(bgDark);
        pForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Ajouter un Enseignant", 0, 0, null, Color.WHITE));

        // Initialisation des champs
        txtNom = new JTextField(); styleChamp(txtNom);
        txtEmail = new JTextField(); styleChamp(txtEmail);
        txtPass = new JTextField(); styleChamp(txtPass);

        pForm.add(creerLabel("Nom Complet :"));
        pForm.add(creerLabel("Email :"));
        pForm.add(creerLabel("Mot de Passe :"));
        pForm.add(txtNom);
        pForm.add(txtEmail);
        pForm.add(txtPass);

        // --- TABLEAU ---
        model = new DefaultTableModel(new String[]{"ID", "Nom", "Email", "Rôle"}, 0);
        table = new JTable(model);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setRowHeight(30);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(bgDark);

        // --- BOUTONS ---
        JPanel pButtons = new JPanel();
        pButtons.setBackground(bgDark);

        JButton btnAdd = creerBouton("AJOUTER", new Color(39, 174, 96));
        JButton btnDel = creerBouton("SUPPRIMER", new Color(192, 57, 43));
        JButton btnRefresh = creerBouton("ACTUALISER", new Color(52, 152, 219));

        pButtons.add(btnAdd);
        pButtons.add(btnRefresh);
        pButtons.add(btnDel);

        add(pForm, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pButtons, BorderLayout.SOUTH);

        // --- ACTIONS ---
        btnAdd.addActionListener(e -> ajouterEnseignant());
        btnDel.addActionListener(e -> supprimerEnseignant());
        btnRefresh.addActionListener(e -> chargerEnseignants());

        chargerEnseignants();
    }

    private void ajouterEnseignant() {
        String valNom = txtNom.getText().trim();
        String valEmail = txtEmail.getText().trim();
        String valPass = txtPass.getText().trim();

        if (valNom.isEmpty() || valEmail.isEmpty() || valPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Remplissez tous les champs !");
            return;
        }

        // On remplit 'nom' et 'prenom' avec la même valeur pour éviter l'erreur SQL
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, password, role) VALUES (?, ?, ?, ?, 'ENSEIGNANT')";

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, valNom);
            pst.setString(2, valNom); 
            pst.setString(3, valEmail);
            pst.setString(4, valPass);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Enseignant ajouté !");
            txtNom.setText(""); txtEmail.setText(""); txtPass.setText("");
            chargerEnseignants();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }
    }

    private void chargerEnseignants() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            ResultSet rs = con.createStatement().executeQuery("SELECT id, nom, email, role FROM utilisateurs WHERE role='ENSEIGNANT'");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("nom"), rs.getString("email"), rs.getString("role")});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void supprimerEnseignant() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM utilisateurs WHERE id=?");
            pst.setInt(1, id);
            pst.executeUpdate(); 
            chargerEnseignants();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // --- METHODES UTILITAIRES (Bien à l'intérieur de la classe) ---
    private void styleChamp(JTextField f) {
        f.setBackground(Color.WHITE);
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
    }

    private JLabel creerLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        return l;
    }

    private JButton creerBouton(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        return b;
    }
}