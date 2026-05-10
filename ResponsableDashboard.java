package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.io.FileOutputStream;
import java.io.File;

// --- Imports iText ---
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image; // Import pour le logo

public class ResponsableDashboard extends JFrame {
    private int idResp;
    private JTable table;
    private DefaultTableModel model;

    // --- PALETTE ESITEC ---
    private final Color BLEU_ESITEC = new Color(25, 42, 86);
    private final Color JAUNE_ESITEC = new Color(241, 196, 15);
    private final Color FOND_GRIS = new Color(236, 240, 241);

    public ResponsableDashboard(int id) {
        this.idResp = id;
        setTitle("ESITEC - Espace Responsable Scolarité");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(FOND_GRIS);

        // --- BARRE SUPÉRIEURE ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BLEU_ESITEC);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("VALIDATION ET SUIVI DU PROGRAMME");
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        lblTitle.setForeground(JAUNE_ESITEC);

        JButton btnLogout = new JButton("🚪 DÉCONNEXION");
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> { new LoginFrame().setVisible(true); this.dispose(); });

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        // --- TABLEAU ---
        String[] columns = {"ID Séance", "Enseignant", "Matière", "Date", "Statut", "Commentaire"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styliserTableau(table);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(FOND_GRIS);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- PANNEAU DE BOUTONS ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        southPanel.setBackground(FOND_GRIS);

        JButton btnRefresh = creerBoutonEcole("🔄 ACTUALISER", false);
        JButton btnVerifProg = creerBoutonEcole("📊 VÉRIFIER PROGRAMME", true);
        JButton btnFichePDF = creerBoutonModern("📄 FICHE PDF", new Color(44, 62, 80));
        JButton btnValider = creerBoutonModern("✅ VALIDER", new Color(46, 204, 113));
        JButton btnRefuser = creerBoutonModern("❌ REFUSER", new Color(231, 76, 60));

        // --- ACTIONS ---
        btnRefresh.addActionListener(e -> chargerDonnees());
        btnValider.addActionListener(e -> modifierStatut("VALIDÉ", null));
        btnRefuser.addActionListener(e -> {
            String motif = JOptionPane.showInputDialog(this, "Motif du refus :");
            if (motif != null && !motif.trim().isEmpty()) {
                modifierStatut("REFUSÉ", motif);
            }
        });
        
        btnFichePDF.addActionListener(e -> genererFichePDF());

        btnVerifProg.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String nomProf = (String) model.getValueAt(row, 1);
                String matiere = (String) model.getValueAt(row, 2);
                int idProf = obtenirIdProfDepuisNom(nomProf);
                if (idProf != 0) {
                    new VerifProgrammeResponsableFrame(idProf, matiere).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez une séance.");
            }
        });

        southPanel.add(btnRefresh);
        southPanel.add(btnVerifProg);
        southPanel.add(btnFichePDF);
        southPanel.add(btnRefuser);
        southPanel.add(btnValider);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        chargerDonnees();
    }

    private void genererFichePDF() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une séance.");
            return;
        }

        String id = model.getValueAt(row, 0).toString();
        String prof = model.getValueAt(row, 1).toString();
        String matiere = model.getValueAt(row, 2).toString();
        String date = model.getValueAt(row, 3).toString();

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Fiche_Suivi_Seance_" + id + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(chooser.getSelectedFile()));
                document.open();

                // --- AJOUT DU LOGO ---
                try {
                    // "logo.jpg" car il est à la racine de ton projet
                    Image logo = Image.getInstance("logo.jpg");
                    logo.scaleToFit(90, 90);
                    logo.setAlignment(Element.ALIGN_LEFT);
                    document.add(logo);
                } catch (Exception e) {
                    System.out.println("Logo non trouvé : " + e.getMessage());
                }

                // --- POLICES ET TITRE ---
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new BaseColor(25, 42, 86));
                com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);

                Paragraph title = new Paragraph("ESITEC / SUP DE CO - FICHE DE SUIVI\n\n", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                // --- CONTENU ---
                document.add(new Paragraph("Référence séance : #" + id, boldFont));
                document.add(new Paragraph("Enseignant : " + prof));
                document.add(new Paragraph("Matière : " + matiere));
                document.add(new Paragraph("Date : " + date));
                document.add(new Paragraph("\n--------------------------------------------------------------------------\n\n"));
                
                document.add(new Paragraph("Observations et Cachet de l'administration :", boldFont));
                document.add(new Paragraph("\n\n\n\n"));
                
                Paragraph signature = new Paragraph("Signature : ______________________");
                signature.setAlignment(Element.ALIGN_RIGHT);
                document.add(signature);

                document.close();
                JOptionPane.showMessageDialog(this, "Fiche PDF générée !");
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur PDF : " + ex.getMessage());
            }
        }
    }

    private void styliserTableau(JTable table) {
        table.setRowHeight(38);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setSelectionBackground(new Color(241, 196, 15, 100));
        JTableHeader header = table.getTableHeader();
        header.setBackground(BLEU_ESITEC);
        header.setForeground(JAUNE_ESITEC);
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));
    }

    private JButton creerBoutonEcole(String texte, boolean estBleu) {
        JButton b = new JButton(texte);
        b.setPreferredSize(new Dimension(190, 45));
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        b.setBackground(estBleu ? BLEU_ESITEC : JAUNE_ESITEC);
        b.setForeground(estBleu ? JAUNE_ESITEC : BLEU_ESITEC);
        return b;
    }

    private JButton creerBoutonModern(String texte, Color bg) {
        JButton b = new JButton(texte);
        b.setPreferredSize(new Dimension(160, 45));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        return b;
    }

    private void chargerDonnees() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            String sql = "SELECT s.id, u.nom, s.nom_matiere, s.date_seance, s.statut, s.commentaire " +
                         "FROM seances s JOIN utilisateurs u ON s.enseignant_id = u.id " +
                         "ORDER BY FIELD(s.statut, 'Terminé', 'REFUSÉ', 'VALIDÉ') ASC, s.date_seance DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void modifierStatut(String nouveauStatut, String motif) {
        int row = table.getSelectedRow();
        if (row != -1) {
            int idSeance = (int) model.getValueAt(row, 0);
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
                String sql = "UPDATE seances SET statut = ?, commentaire = ? WHERE id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, nouveauStatut);
                pst.setString(2, (nouveauStatut.equals("VALIDÉ")) ? "" : motif);
                pst.setInt(3, idSeance);
                pst.executeUpdate();
                chargerDonnees();
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private int obtenirIdProfDepuisNom(String nom) {
        int id = 0;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("SELECT id FROM utilisateurs WHERE nom = ?");
            pst.setString(1, nom);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) id = rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return id;
    }
}