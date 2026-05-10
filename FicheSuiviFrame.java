package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FicheSuiviFrame extends JFrame {
    private JComboBox<String> comboProfs;
    private DefaultTableModel model;

    public FicheSuiviFrame() {
        setTitle("Fiche de Suivi P�dagogique");
        setSize(800, 500);
        setLocationRelativeTo(null);
        
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- HAUT : S�lection du prof ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboProfs = new JComboBox<>();
        chargerProfsDansCombo();
        
        JButton btnGenerer = new JButton("Afficher le suivi");
        top.add(new JLabel("S�lectionner Enseignant : "));
        top.add(comboProfs);
        top.add(btnGenerer);

        // --- MILIEU : Tableau de suivi ---
        model = new DefaultTableModel(new String[]{"Date", "Mati�re", "Contenu r�alis�", "Statut"}, 0);
        JTable table = new JTable(model);
        
        btnGenerer.addActionListener(e -> chargerSuiviProf());

        main.add(top, BorderLayout.NORTH);
        main.add(new JScrollPane(table), BorderLayout.CENTER);
        add(main);
    }

    private void chargerProfsDansCombo() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            ResultSet rs = con.createStatement().executeQuery("SELECT nom FROM utilisateurs WHERE role='enseignant'");
            while(rs.next()) comboProfs.addItem(rs.getString("nom"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerSuiviProf() {
        model.setRowCount(0);
        String nomProf = (String) comboProfs.getSelectedItem();
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            String sql = "SELECT s.date_seance, s.nom_matiere, s.contenu, s.statut " +
                         "FROM seances s JOIN utilisateurs u ON s.enseignant_id = u.id " +
                         "WHERE u.nom = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nomProf);
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}