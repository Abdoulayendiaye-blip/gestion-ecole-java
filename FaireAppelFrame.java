package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FaireAppelFrame extends JFrame {
    private DefaultTableModel model;
    private int idProf;

    public FaireAppelFrame(int idProf) {
        this.idProf = idProf;
        setTitle("Appel");
        setSize(400, 500);
        setLocationRelativeTo(null);
        
        model = new DefaultTableModel(new String[]{"ID", "Nom", "Absent"}, 0) {
            public Class<?> getColumnClass(int c) { return c == 2 ? Boolean.class : Object.class; }
        };
        
        JTable table = new JTable(model);
        chargerEtudiants();
        
        JButton btn = new JButton("Valider l'appel");
        btn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Appel enregistré !");
            this.dispose();
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btn, BorderLayout.SOUTH);
    }

    private void chargerEtudiants() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            ResultSet rs = con.createStatement().executeQuery("SELECT id, nom FROM etudiants");
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), false});
        } catch (Exception e) { e.printStackTrace(); }
    }
}