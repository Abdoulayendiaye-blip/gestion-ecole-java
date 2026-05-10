package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VisualisationProgrammeFrame extends JFrame {

    private String matiere;
    private int idProf;

    public VisualisationProgrammeFrame(int idProf, String matiere) {

        this.idProf = idProf;
        this.matiere = matiere;

        setTitle("Progression du programme : " + matiere);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---- CALCUL DE LA PROGRESSION ----
        int totalChapitres = obtenirTotalChapitres();
        int chapitresFaits = obtenirChapitresRealises();
        int pourcentage = (totalChapitres > 0) ? (chapitresFaits * 100 / totalChapitres) : 0;

        // ---- BARRE DE PROGRESSION ----
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JLabel lblStatut = new JLabel("Progression globale : " + pourcentage + "%");
        lblStatut.setFont(new Font("Arial", Font.BOLD, 14));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(pourcentage);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(46, 204, 113));

        topPanel.add(lblStatut);
        topPanel.add(progressBar);

        // ---- LISTE DES SEANCES REALISEES ----
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);

        chargerDetails(listModel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(list), BorderLayout.CENTER);

        add(mainPanel);
    }

    // Nombre total de chapitres prévus
    private int obtenirTotalChapitres() {

        // Tu peux aussi récupérer ce nombre dans la base de données
        return 10;

    }

    // Nombre de chapitres déjà réalisés
    private int obtenirChapitresRealises() {

        int count = 0;

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gestion_cahier_texte",
                "root",
                "")) {

            String sql = "SELECT COUNT(*) FROM seances WHERE enseignant_id = ? AND nom_matiere = ? AND statut = 'VALID'";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setInt(1, idProf);
            pst.setString(2, matiere);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    // Charger les détails des séances réalisées
    private void chargerDetails(DefaultListModel<String> model) {

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gestion_cahier_texte",
                "root",
                "")) {

            String sql = "SELECT date_seance, contenu FROM seances WHERE enseignant_id = ? AND nom_matiere = ? AND statut = 'VALID' ORDER BY date_seance ASC";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setInt(1, idProf);
            pst.setString(2, matiere);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                String ligne = rs.getString("date_seance") + " : " + rs.getString("contenu");

                model.addElement(ligne);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}