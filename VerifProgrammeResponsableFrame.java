package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class VerifProgrammeResponsableFrame extends JFrame {
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private DefaultTableModel model;

    public VerifProgrammeResponsableFrame(int idProf, String matiere) {
        setTitle("ESITEC - Vérification de Progression : " + matiere);
        setSize(700, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(236, 240, 241));

        // --- EN-TÊTE AVEC LA BARRE DE PROGRESSION ---
        JPanel header = new JPanel(new GridLayout(2, 1, 10, 10));
        header.setBackground(new Color(236, 240, 241));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        lblStatus = new JLabel("Analyse de la progression...", JLabel.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(0, 30));
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(25, 42, 86)); // Bleu ESITEC
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 13));

        header.add(lblStatus);
        header.add(progressBar);
        add(header, BorderLayout.NORTH);

        // --- TABLEAU DES SÉANCES ---
        model = new DefaultTableModel(new String[]{"Contenu validé", "Date de la séance"}, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Charger les données dès l'ouverture
        calculerProgression(idProf, matiere);
    }

    private void calculerProgression(int idProf, String matiere) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            
            // 1. Récupérer le total d'heures depuis la table 'matieres'
            int totalHeuresPrevues = 0;
            PreparedStatement pstM = con.prepareStatement("SELECT total_heures_prevues FROM matieres WHERE nom_matiere = ?");
            pstM.setString(1, matiere);
            ResultSet rsM = pstM.executeQuery();
            if (rsM.next()) {
                totalHeuresPrevues = rsM.getInt("total_heures_prevues");
            }

            // 2. Récupérer les séances validées pour ce prof et cette matière
            PreparedStatement pstS = con.prepareStatement(
                "SELECT contenu, date_seance FROM seances WHERE enseignant_id = ? AND nom_matiere = ? AND statut = 'VALIDÉ'"
            );
            pstS.setInt(1, idProf);
            pstS.setString(2, matiere);
            ResultSet rsS = pstS.executeQuery();

            int heuresFaites = 0;
            while (rsS.next()) {
                heuresFaites += 2; // On garde la base de 2h par séance
                model.addRow(new Object[]{rsS.getString("contenu"), rsS.getString("date_seance")});
            }

            // 3. Calcul du pourcentage final
            int pourcentage = (totalHeuresPrevues > 0) ? (heuresFaites * 100) / totalHeuresPrevues : 0;
            if (pourcentage > 100) pourcentage = 100;

            // 4. Mise à jour de l'affichage
            progressBar.setValue(pourcentage);
            lblStatus.setText("Progression du module : " + pourcentage + "% (" + heuresFaites + "h / " + totalHeuresPrevues + "h)");
            
            // Couleur visuelle pour le responsable
            if (pourcentage < 30) progressBar.setForeground(Color.RED);
            else if (pourcentage < 70) progressBar.setForeground(new Color(241, 196, 15)); // Jaune
            else progressBar.setForeground(new Color(46, 204, 113)); // Vert

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données.");
        }
    }
}