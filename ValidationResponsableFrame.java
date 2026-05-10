package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ValidationResponsableFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public ValidationResponsableFrame() {
        Color bgDark = new Color(44, 62, 80);
        setTitle("ESITEC - Validation");
        setSize(700, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgDark);

        model = new DefaultTableModel(new String[]{"ID", "Date", "Matière", "Contenu"}, 0);
        table = new JTable(model);
        
        // STYLE DU TABLEAU : Fond blanc, Écriture noire
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(25);

        table.getColumnModel().getColumn(0).setMaxWidth(0); 
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btn = new JButton("✅ VALIDER LA SÉLECTION");
        btn.setBackground(new Color(39, 174, 96));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(200, 50));
        btn.addActionListener(e -> valider());

        JPanel p = new JPanel();
        p.setBackground(bgDark);
        p.add(btn);
        add(p, BorderLayout.SOUTH);

        charger();
    }

    private void charger() {
        model.setRowCount(0);
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "");
            ResultSet rs = con.createStatement().executeQuery("SELECT id, date_seance, nom_matiere, contenu FROM seances WHERE valide_par_responsable=0");
            while(rs.next()) model.addRow(new Object[]{rs.getInt("id"), rs.getString("date_seance"), rs.getString("nom_matiere"), rs.getString("contenu")});
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void valider() {
        int r = table.getSelectedRow();
        if(r == -1) return;
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "");
            PreparedStatement pst = con.prepareStatement("UPDATE seances SET valide_par_responsable=1 WHERE id=?");
            pst.setInt(1, (int)model.getValueAt(r, 0));
            pst.executeUpdate();
            charger();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}