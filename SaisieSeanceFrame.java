package View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import Model.AssistantIA; // Import de l'IA

public class SaisieSeanceFrame extends JFrame {
    private int idProf, idClasse;
    private JComboBox<String> comboMatieres;
    private JTextArea txtContenu;
    private DefaultTableModel modelRefus, modelProgression;
    private JProgressBar progressBar;
    private JLabel lblPourcentage;

    private final Color BLEU_ESITEC = new Color(25, 42, 86);
    private final Color JAUNE_ESITEC = new Color(241, 196, 15);
    private final Color FOND_GRIS = new Color(236, 240, 241);

    public SaisieSeanceFrame(int id, int idClasse) {
        this.idProf = id;
        this.idClasse = idClasse;
        setTitle("ESITEC - Gestion des Enseignements");
        setSize(900, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FOND_GRIS);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("📝 NOUVELLE SÉANCE", creerPanelSaisie());
        tabs.addTab("📈 PROGRESSION DU PROGRAMME", creerPanelProgression());

        add(tabs);
        chargerMatieres();
        chargerHistorique();
    }

    private JPanel creerPanelSaisie() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(FOND_GRIS);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setBackground(FOND_GRIS);

        comboMatieres = new JComboBox<>();
        comboMatieres.setFont(new Font("Segoe UI", Font.BOLD, 14));
        comboMatieres.setBorder(creerTitledBorder("Matière concernée"));
        comboMatieres.addActionListener(e -> chargerProgression());

        txtContenu = new JTextArea(10, 20);
        txtContenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtContenu.setLineWrap(true);
        txtContenu.setWrapStyleWord(true);
        JScrollPane scrollTxt = new JScrollPane(txtContenu);
        scrollTxt.setBorder(creerTitledBorder("Contenu de la séance réalisée"));

        // --- ZONE ACTIONS (IA + ENREGISTRER) ---
        JPanel panelActions = new JPanel(new GridLayout(1, 2, 15, 0));
        panelActions.setBackground(FOND_GRIS);
        panelActions.setPreferredSize(new Dimension(0, 50));

        JButton btnIA = new JButton("✨ OPTIMISER AVEC L'IA");
        btnIA.setBackground(new Color(41, 128, 185));
        btnIA.setForeground(Color.WHITE);
        btnIA.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIA.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnIA.addActionListener(e -> {
            String texte = txtContenu.getText().trim();
            if(!texte.isEmpty()){
                btnIA.setText("⌛ ANALYSE EN COURS...");
                btnIA.setEnabled(false);
                new SwingWorker<String, Void>() {
                    @Override protected String doInBackground() { return AssistantIA.optimiserContenu(texte); }
                    @Override protected void done() {
                        try { txtContenu.setText(get()); } catch (Exception ex) { ex.printStackTrace(); }
                        btnIA.setText("✨ OPTIMISER AVEC L'IA");
                        btnIA.setEnabled(true);
                    }
                }.execute();
            }
        });

        JButton btnSave = new JButton("💾 ENREGISTRER LA SÉANCE");
        btnSave.setBackground(BLEU_ESITEC);
        btnSave.setForeground(JAUNE_ESITEC);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(e -> enregistrer());

        panelActions.add(btnIA);
        panelActions.add(btnSave);

        form.add(comboMatieres, BorderLayout.NORTH);
        form.add(scrollTxt, BorderLayout.CENTER);
        form.add(panelActions, BorderLayout.SOUTH);

        // --- HISTORIQUE ---
        modelRefus = new DefaultTableModel(new String[]{"Date", "Matière", "Statut", "Commentaire"}, 0);
        JTable tableHist = new JTable(modelRefus);
        styliserTableau(tableHist);
        JScrollPane scrollTable = new JScrollPane(tableHist);
        scrollTable.setBorder(creerTitledBorder("Historique récent des séances"));
        scrollTable.setPreferredSize(new Dimension(0, 250));

        p.add(form, BorderLayout.CENTER);
        p.add(scrollTable, BorderLayout.SOUTH);
        return p;
    }

    private JPanel creerPanelProgression() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(FOND_GRIS);
        p.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerProg = new JPanel(new GridLayout(2, 1, 5, 5));
        headerProg.setBackground(FOND_GRIS);
        lblPourcentage = new JLabel("Progression de la matière", SwingConstants.CENTER);
        lblPourcentage.setFont(new Font("Segoe UI", Font.BOLD, 16));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(JAUNE_ESITEC);
        
        headerProg.add(lblPourcentage);
        headerProg.add(progressBar);

        modelProgression = new DefaultTableModel(new String[]{"Contenu de la séance", "Date"}, 0);
        JTable tableProg = new JTable(modelProgression);
        styliserTableau(tableProg);
        
        p.add(headerProg, BorderLayout.NORTH);
        p.add(new JScrollPane(tableProg), BorderLayout.CENTER);
        return p;
    }

    private TitledBorder creerTitledBorder(String titre) {
        return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BLEU_ESITEC), titre, 
               TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), BLEU_ESITEC);
    }

    private void styliserTableau(JTable table) {
        table.setRowHeight(30);
        JTableHeader h = table.getTableHeader();
        h.setBackground(BLEU_ESITEC); h.setForeground(JAUNE_ESITEC);
    }

    private void chargerMatieres() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("SELECT nom_matiere FROM affectations WHERE enseignant_id = ? AND id_classe = ?");
            pst.setInt(1, idProf); pst.setInt(2, idClasse);
            ResultSet rs = pst.executeQuery();
            comboMatieres.removeAllItems();
            while (rs.next()) comboMatieres.addItem(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerHistorique() {
        modelRefus.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("SELECT date_seance, nom_matiere, statut, commentaire FROM seances WHERE enseignant_id = ? AND id_classe = ? ORDER BY date_seance DESC");
            pst.setInt(1, idProf); pst.setInt(2, idClasse);
            ResultSet rs = pst.executeQuery();
            while(rs.next()) modelRefus.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerProgression() {
        String mat = (String) comboMatieres.getSelectedItem();
        if (mat == null) return;
        modelProgression.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pstM = con.prepareStatement("SELECT total_heures_prevues FROM matieres WHERE nom_matiere = ?");
            pstM.setString(1, mat);
            ResultSet rsM = pstM.executeQuery();
            int total = rsM.next() ? rsM.getInt(1) : 0;

            PreparedStatement pstS = con.prepareStatement("SELECT contenu, date_seance FROM seances WHERE nom_matiere = ? AND id_classe = ? AND (statut = 'Terminé' OR statut = 'VALIDÉ')");
            pstS.setString(1, mat); pstS.setInt(2, idClasse);
            ResultSet rsS = pstS.executeQuery();
            int faites = 0;
            while (rsS.next()) {
                faites += 2;
                modelProgression.addRow(new Object[]{rsS.getString(1), rsS.getString(2)});
            }
            int p = (total > 0) ? (faites * 100) / total : 0;
            progressBar.setValue(p);
            lblPourcentage.setText("Progression " + mat + " : " + p + "% (" + faites + "h/" + total + "h)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void enregistrer() {
        String mat = (String) comboMatieres.getSelectedItem();
        String cont = txtContenu.getText().trim();
        if(cont.isEmpty()) return;

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO seances (enseignant_id, id_classe, nom_matiere, contenu, date_seance, statut) VALUES (?, ?, ?, ?, ?, 'Terminé')");
            pst.setInt(1, idProf); pst.setInt(2, idClasse); pst.setString(3, mat); pst.setString(4, cont);
            pst.setString(5, LocalDate.now().toString());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Séance enregistrée !");
            txtContenu.setText(""); chargerHistorique(); chargerProgression();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}