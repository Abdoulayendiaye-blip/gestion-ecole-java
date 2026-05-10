package View;

import Model.Classe;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EnseignantDashboard extends JFrame {
    private int idProf;
    private JComboBox<Classe> comboClasses = new JComboBox<>();

    // --- PALETTE ESITEC ---
    private final Color BLEU_ESITEC = new Color(25, 42, 86);
    private final Color JAUNE_ESITEC = new Color(241, 196, 15);
    private final Color FOND_GRIS = new Color(236, 240, 241);

    public EnseignantDashboard(int id) {
        this.idProf = id;
        setTitle("ESITEC - Espace Enseignant");
        setSize(550, 500); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(FOND_GRIS);

        // --- BARRE SUPÉRIEURE (BLEU MARINE) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BLEU_ESITEC);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblWelcome = new JLabel("BIENVENUE, PROFESSEUR");
        lblWelcome.setForeground(JAUNE_ESITEC);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnLogout = new JButton("🚪 DÉCONNEXION");
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        topPanel.add(lblWelcome, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);

        // --- ZONE DE SÉLECTION (BANDEAU JAUNE) ---
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        selectionPanel.setBackground(JAUNE_ESITEC); // Contraste fort pour la sélection
        selectionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BLEU_ESITEC));
        
        JLabel lblClasse = new JLabel("Classe actuelle :");
        lblClasse.setForeground(BLEU_ESITEC);
        lblClasse.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        comboClasses.setPreferredSize(new Dimension(220, 30));
        comboClasses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        selectionPanel.add(lblClasse);
        selectionPanel.add(comboClasses);

        // Charger les classes
        chargerMesClasses();

        // --- BOUTONS D'ACTION (MODERNES) ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 25, 25));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        centerPanel.setBackground(FOND_GRIS);

        JButton btnCahier = creerGrandBouton("📝 REMPLIR CAHIER DE TEXTE", BLEU_ESITEC, JAUNE_ESITEC);
        JButton btnAppel = creerGrandBouton("👥 FAIRE L'APPEL", BLEU_ESITEC, JAUNE_ESITEC);

        btnCahier.addActionListener(e -> {
            Classe selectedClasse = (Classe) comboClasses.getSelectedItem();
            if (selectedClasse != null) {
                new SaisieSeanceFrame(idProf, selectedClasse.getId()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Aucune classe sélectionnée.", "ESITEC", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnAppel.addActionListener(e -> {
            Classe selectedClasse = (Classe) comboClasses.getSelectedItem();
            if (selectedClasse != null) {
                new SaisieAbsenceFrame(idProf, selectedClasse.getId()).setVisible(true);
            }
        });

        centerPanel.add(btnCahier);
        centerPanel.add(btnAppel);

        // Assemblage
        JPanel northGroup = new JPanel(new BorderLayout());
        northGroup.add(topPanel, BorderLayout.NORTH);
        northGroup.add(selectionPanel, BorderLayout.CENTER);

        mainPanel.add(northGroup, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }

    // Méthode utilitaire pour les boutons du dashboard
    private JButton creerGrandBouton(String texte, Color bg, Color fg) {
        JButton b = new JButton(texte);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));
        return b;
    }

    private void chargerMesClasses() {
        String sql = "SELECT c.id_classe, c.nom_classe FROM classes c " +
                     "JOIN affectations a ON c.id_classe = a.id_classe " +
                     "WHERE a.enseignant_id = ?";
        
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idProf);
            ResultSet rs = pst.executeQuery();

            comboClasses.removeAllItems();

            while (rs.next()) {
                comboClasses.addItem(new Classe(
                    rs.getInt("id_classe"), 
                    rs.getString("nom_classe")
                ));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}