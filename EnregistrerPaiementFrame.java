package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EnregistrerPaiementFrame extends JFrame {
    public EnregistrerPaiementFrame() {
        setTitle("Enregistrer un Paiement");
        setSize(400, 300);
        setLayout(new GridLayout(4, 2, 10, 10));
        setLocationRelativeTo(null);

        JTextField txtIdEtu = new JTextField();
        JTextField txtMontant = new JTextField();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Scolarité", "Inscription", "Examen"});
        JButton btnValider = new JButton("Valider");

        add(new JLabel(" ID Étudiant :")); add(txtIdEtu);
        add(new JLabel(" Montant (FCFA) :")); add(txtMontant);
        add(new JLabel(" Type :")); add(cbType);
        add(btnValider);

        btnValider.addActionListener(e -> {
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
                String sql = "INSERT INTO paiements (id_etudiant, montant_paye, date_paiement, type_paiement) VALUES (?, ?, CURDATE(), ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtIdEtu.getText()));
                pst.setDouble(2, Double.parseDouble(txtMontant.getText()));
                pst.setString(3, cbType.getSelectedItem().toString());
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Paiement enregistré !");
                dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erreur : Vérifiez l'ID et le montant."); }
        });
    }
}