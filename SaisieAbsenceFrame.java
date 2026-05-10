package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class SaisieAbsenceFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private int idProf;
    private int idClasse;

    // --- PALETTE ESITEC ---
    private final Color BLEU_ESITEC = new Color(25, 42, 86);
    private final Color JAUNE_ESITEC = new Color(241, 196, 15);
    private final Color FOND_GRIS = new Color(236, 240, 241);

    public SaisieAbsenceFrame(int idProf, int idClasse) {
        this.idProf = idProf;
        this.idClasse = idClasse;
        setTitle("ESITEC - Registre d'Appel");
        setSize(650, 550);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FOND_GRIS);
        setLayout(new BorderLayout(15, 15));

        // --- EN-TÊTE ---
        JLabel lblHeader = new JLabel("REGISTRE D'APPEL : COCHEZ LES ABSENTS", JLabel.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(BLEU_ESITEC);
        lblHeader.setForeground(JAUNE_ESITEC);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setPreferredSize(new Dimension(0, 50));
        add(lblHeader, BorderLayout.NORTH);

        // --- TABLEAU ---
        String[] columns = {"ID", "Nom & Prénom de l'Étudiant", "Absent ?"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 2) ? Boolean.class : super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // Seule la case à cocher est modifiable
            }
        };

        table = new JTable(model);
        styliserTableau(table);
        chargerEtudiants();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        scrollPane.setBackground(FOND_GRIS);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOUTON VALIDER ---
        JButton btnValider = new JButton("🚨 ENREGISTRER L'APPEL DÉFINITIVEMENT");
        btnValider.setBackground(BLEU_ESITEC);
        btnValider.setForeground(JAUNE_ESITEC);
        btnValider.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnValider.setPreferredSize(new Dimension(0, 60));
        btnValider.setFocusPainted(false);
        btnValider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnValider.addActionListener(e -> enregistrerAbsences());
        add(btnValider, BorderLayout.SOUTH);
    }

    private void styliserTableau(JTable table) {
        table.setRowHeight(40); // Lignes hautes pour cliquer facilement sur les cases
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(241, 196, 15, 80));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(BLEU_ESITEC);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BLEU_ESITEC));
    }

    private void chargerEtudiants() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("SELECT id, nom, prenom FROM etudiants WHERE id_classe = ? ORDER BY nom ASC");
            pst.setInt(1, idClasse);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("nom").toUpperCase() + " " + rs.getString("prenom"), 
                    false
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void enregistrerAbsences() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Confirmez-vous l'envoi de cette liste d'absence ?", 
            "Validation ESITEC", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
                String sql = "INSERT INTO absences (etudiant_id, enseignant_id, id_classe, date_absence) VALUES (?, ?, ?, CURDATE())";
                PreparedStatement pst = con.prepareStatement(sql);
                
                int count = 0;
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((Boolean) model.getValueAt(i, 2)) {
                        pst.setInt(1, (int) model.getValueAt(i, 0));
                        pst.setInt(2, idProf);
                        pst.setInt(3, idClasse);
                        pst.executeUpdate();
                        count++;
                    }
                }
                JOptionPane.showMessageDialog(this, count + " absence(s) enregistrée(s) avec succès.");
                this.dispose();
            } catch (SQLException ex) { 
                JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement.");
                ex.printStackTrace(); 
            }
        }
    }
}