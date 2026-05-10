package View;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;

public class ChefDashboard extends JFrame {
    private int idChef;
    private DefaultTableModel modelAbs, modelCahier, modelEtu, modelProf;
    private JTable tableEtu, tableProf, tableAbs, tableCahier;

    // --- PALETTE DE COULEURS ESITEC ---
    private final Color BLEU_ESITEC = new Color(25, 42, 86);
    private final Color JAUNE_ESITEC = new Color(241, 196, 15);
    private final Color FOND_GRIS = new Color(236, 240, 241);

    public ChefDashboard(int id) {
        this.idChef = id;
        setTitle("ESITEC - Direction Générale (Chef de Département)");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(FOND_GRIS);

        // --- BARRE SUPÉRIEURE ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BLEU_ESITEC);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("TABLEAU DE BORD DIRECTION");
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        lblTitle.setForeground(JAUNE_ESITEC);

        JButton btnLogout = new JButton("🚪 DÉCONNEXION");
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { new LoginFrame().setVisible(true); this.dispose(); });
        
        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        // --- NAVIGATION PAR ONGLETS ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        
        tabs.addTab("🚨 ABSENCES", creerPanelAbsences());
        tabs.addTab("📚 CAHIER DE TEXTE", creerPanelCahier());
        tabs.addTab("👨‍🏫 GESTION PROFS", creerPanelGestionProfs());
        tabs.addTab("🎓 GESTION ÉTUDIANTS", creerPanelEtudiants());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);
        add(mainPanel);
    }

    // --- LOGIQUE DE GÉNÉRATION PDF (COPIÉE DU RESPONSABLE) ---
    private void genererFicheSuiviPDF() {
        int row = tableCahier.getSelectedRow();
        String filename = (row != -1) ? "Fiche_Suivi_Seance_" + tableCahier.getValueAt(row, 3) + ".pdf" : "Rapport_Global_ESITEC.pdf";

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(filename));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Document document = new Document(PageSize.A4);
            try {
                PdfWriter.getInstance(document, new FileOutputStream(chooser.getSelectedFile()));
                document.open();

                // 1. AJOUT DU LOGO
                try {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("logo.jpg");
                    logo.scaleToFit(90, 90);
                    logo.setAlignment(Element.ALIGN_LEFT);
                    document.add(logo);
                } catch (Exception e) { System.out.println("Logo introuvable."); }

                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new BaseColor(25, 42, 86));
                com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);

                Paragraph title = new Paragraph("ESITEC / SUP DE CO - FICHE DE SUIVI\n\n", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                if (row != -1) {
                    document.add(new Paragraph("Enseignant : " + tableCahier.getValueAt(row, 0), boldFont));
                    document.add(new Paragraph("Matière : " + tableCahier.getValueAt(row, 1)));
                    document.add(new Paragraph("Date : " + tableCahier.getValueAt(row, 3)));
                    document.add(new Paragraph("Statut : " + tableCahier.getValueAt(row, 4)));
                    document.add(new Paragraph("\nContenu de la séance :\n" + tableCahier.getValueAt(row, 2)));
                } else {
                    document.add(new Paragraph("RAPPORT GLOBAL DES SÉANCES\n\n", boldFont));
                    PdfPTable pdfTable = new PdfPTable(tableCahier.getColumnCount());
                    pdfTable.setWidthPercentage(100);
                    for (int i = 0; i < tableCahier.getColumnCount(); i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(tableCahier.getColumnName(i)));
                        cell.setBackgroundColor(new BaseColor(25, 42, 86));
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }
                    for (int r = 0; r < tableCahier.getRowCount(); r++) {
                        for (int c = 0; c < tableCahier.getColumnCount(); c++) {
                            pdfTable.addCell(tableCahier.getValueAt(r, c).toString());
                        }
                    }
                    document.add(pdfTable);
                }

                document.add(new Paragraph("\n--------------------------------------------------------------------------\n\n"));
                document.add(new Paragraph("Observations et Cachet de la Direction Générale :", boldFont));
                document.add(new Paragraph("\n\n\n\n"));
                Paragraph signature = new Paragraph("Signature du Chef : ______________________");
                signature.setAlignment(Element.ALIGN_RIGHT);
                document.add(signature);

                document.close();
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erreur PDF : " + ex.getMessage()); }
        }
    }

    // --- MÉTHODES UTILITAIRES ---
    private void styliserTableau(JTable table) {
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setSelectionBackground(new Color(241, 196, 15, 100));
        JTableHeader header = table.getTableHeader();
        header.setBackground(BLEU_ESITEC);
        header.setForeground(JAUNE_ESITEC);
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        header.setPreferredSize(new Dimension(0, 45));
    }

    private JButton creerBoutonEcole(String texte, boolean estBleu) {
        JButton b = new JButton(texte);
        b.setPreferredSize(new Dimension(180, 45));
        b.setFocusPainted(false);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (estBleu) { b.setBackground(BLEU_ESITEC); b.setForeground(JAUNE_ESITEC); }
        else { b.setBackground(JAUNE_ESITEC); b.setForeground(BLEU_ESITEC); }
        return b;
    }

    // --- ONGLETS ---
    private JPanel creerPanelCahier() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20)); p.setBackground(FOND_GRIS);
        modelCahier = new DefaultTableModel(new String[]{"Enseignant", "Matière", "Contenu", "Date", "Statut"}, 0);
        tableCahier = new JTable(modelCahier); styliserTableau(tableCahier);
        
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); south.setBackground(FOND_GRIS);
        JButton btnRefresh = creerBoutonEcole("🔄 ACTUALISER", false);
        JButton btnPDF = creerBoutonEcole("📋 GÉNÉRER PDF", true);
        
        btnRefresh.addActionListener(e -> chargerCahier());
        btnPDF.addActionListener(e -> genererFicheSuiviPDF());
        
        south.add(btnRefresh); south.add(btnPDF);
        p.add(new JScrollPane(tableCahier), BorderLayout.CENTER); p.add(south, BorderLayout.SOUTH);
        chargerCahier(); return p;
    }

    private JPanel creerPanelEtudiants() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20)); p.setBackground(FOND_GRIS);
        modelEtu = new DefaultTableModel(new String[]{"ID", "Nom", "Prénom"}, 0);
        tableEtu = new JTable(modelEtu); styliserTableau(tableEtu);
        
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); south.setBackground(FOND_GRIS);
        JButton btnRefresh = creerBoutonEcole("🔄 ACTUALISER", false);
        JButton btnAdd = creerBoutonEcole("🎓 + NOUVEAU", true);
        JButton btnDelete = creerBoutonEcole("🗑️ SUPPRIMER", false);
        btnDelete.setBackground(new Color(231, 76, 60)); btnDelete.setForeground(Color.WHITE);

        btnRefresh.addActionListener(e -> chargerEtudiants());
        btnAdd.addActionListener(e -> new AjoutEtudiantFrame().setVisible(true));
        btnDelete.addActionListener(e -> {
            int row = tableEtu.getSelectedRow();
            if(row != -1) {
                int id = (int) modelEtu.getValueAt(row, 0);
                if(JOptionPane.showConfirmDialog(this, "Supprimer l'étudiant ?", "Confirmation", 0) == 0) {
                    supprimerEntite("etudiants", id); chargerEtudiants();
                }
            }
        });

        south.add(btnRefresh); south.add(btnAdd); south.add(btnDelete);
        p.add(new JScrollPane(tableEtu), BorderLayout.CENTER); p.add(south, BorderLayout.SOUTH);
        chargerEtudiants(); return p;
    }

    private JPanel creerPanelGestionProfs() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20)); p.setBackground(FOND_GRIS);
        modelProf = new DefaultTableModel(new String[]{"ID", "Nom", "Login"}, 0);
        tableProf = new JTable(modelProf); styliserTableau(tableProf);
        
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); south.setBackground(FOND_GRIS);
        JButton btnRefresh = creerBoutonEcole("🔄 ACTUALISER", false);
        JButton btnAdd = creerBoutonEcole("👤 + NOUVEAU PROF", true);
        JButton btnAssign = creerBoutonEcole("📚 ASSIGNER", false);
        JButton btnDelete = creerBoutonEcole("🗑️ SUPPRIMER", true);
        btnDelete.setBackground(new Color(231, 76, 60));

        btnRefresh.addActionListener(e -> chargerProfs());
        btnAdd.addActionListener(e -> new AjoutProfFrame().setVisible(true));
        btnAssign.addActionListener(e -> new AssignerCoursFrame().setVisible(true));
        btnDelete.addActionListener(e -> {
            int row = tableProf.getSelectedRow();
            if(row != -1) {
                int id = (int) modelProf.getValueAt(row, 0);
                if(JOptionPane.showConfirmDialog(this, "Supprimer le professeur ?", "Confirmation", 0) == 0) {
                    supprimerEntite("utilisateurs", id); chargerProfs();
                }
            }
        });

        south.add(btnRefresh); south.add(btnAdd); south.add(btnAssign); south.add(btnDelete);
        p.add(new JScrollPane(tableProf), BorderLayout.CENTER); p.add(south, BorderLayout.SOUTH);
        chargerProfs(); return p;
    }

    private JPanel creerPanelAbsences() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20)); p.setBackground(FOND_GRIS);
        modelAbs = new DefaultTableModel(new String[]{"Étudiant", "Date", "Saisi par"}, 0);
        tableAbs = new JTable(modelAbs); styliserTableau(tableAbs);
        
        JButton btnRefresh = creerBoutonEcole("🔄 ACTUALISER", false);
        btnRefresh.addActionListener(e -> chargerAbsences());
        
        p.add(new JScrollPane(tableAbs), BorderLayout.CENTER); 
        p.add(btnRefresh, BorderLayout.SOUTH);
        chargerAbsences(); return p;
    }

    // --- LOGIQUE SQL ---
    private void chargerAbsences() { modelAbs.setRowCount(0); remplirModele(modelAbs, "SELECT e.nom, a.date_absence, u.nom FROM absences a JOIN etudiants e ON a.etudiant_id = e.id JOIN utilisateurs u ON a.enseignant_id = u.id"); }
    private void chargerCahier() { modelCahier.setRowCount(0); remplirModele(modelCahier, "SELECT u.nom, s.nom_matiere, s.contenu, s.date_seance, s.statut FROM seances s JOIN utilisateurs u ON s.enseignant_id = u.id"); }
    private void chargerProfs() { modelProf.setRowCount(0); remplirModele(modelProf, "SELECT id, nom, login FROM utilisateurs WHERE role='enseignant'"); }
    private void chargerEtudiants() { modelEtu.setRowCount(0); remplirModele(modelEtu, "SELECT id, nom, prenom FROM etudiants"); }

    private void remplirModele(DefaultTableModel model, String sql) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            ResultSet rs = con.createStatement().executeQuery(sql);
            int cols = rs.getMetaData().getColumnCount();
            while(rs.next()) {
                Object[] row = new Object[cols];
                for(int i=0; i<cols; i++) row[i] = rs.getObject(i+1);
                model.addRow(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void supprimerEntite(String table, int id) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM " + table + " WHERE id = ?");
            pst.setInt(1, id); pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Suppression réussie !");
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Erreur : Élément utilisé ailleurs."); }
    }
}