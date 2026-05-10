package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ConsultationCahiersFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public ConsultationCahiersFrame() {
        setTitle("ESITEC - Consultation Globale des Cahiers");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ajout de la colonne "État" dans le titre
        model = new DefaultTableModel(new String[]{"Date", "Enseignant", "Matière", "Durée", "Contenu", "État"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tableau en lecture seule
            }
        };
        
        table = new JTable(model);
        
        // Personnalisation de l'affichage
        table.getColumnModel().getColumn(4).setPreferredWidth(250); // Plus de place pour le contenu
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        chargerDonnees();
    }

    private void chargerDonnees() {
        model.setRowCount(0);
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "");
            
            // On récupère aussi la colonne valide_par_responsable
            String sql = "SELECT s.date_seance, u.nom, s.nom_matiere, s.duree, s.contenu, s.valide_par_responsable " +
                         "FROM seances s " +
                         "JOIN utilisateurs u ON s.enseignant_id = u.id " +
                         "ORDER BY s.date_seance DESC";
            
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                // On transforme le 0 ou 1 en texte clair
                int status = rs.getInt("valide_par_responsable");
                String etatTexte = (status == 1) ? "✅ Validé" : "⏳ En attente";

                model.addRow(new Object[]{
                    rs.getDate("date_seance"),
                    rs.getString("nom"),
                    rs.getString("matiere"),
                    rs.getInt("duree") + "h",
                    rs.getString("contenu"),
                    etatTexte // Affichage du statut
                });
            }
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement : " + ex.getMessage());
        }
    }
}