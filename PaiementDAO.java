package View;

import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class PaiementDAO {
    // Ajout de 'static' pour pouvoir l'appeler facilement
    public static void chargerListePaiements(DefaultTableModel model) {
        String sql = "SELECT p.id_paiement, e.nom, e.prenom, p.montant_paye, p.date_paiement, p.type_paiement " +
                     "FROM paiements p " +
                     "JOIN etudiants e ON p.id_etudiant = e.id";

        // Remplacement de ConnexionBD par la connexion directe que tu utilises ailleurs
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "");
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            model.setRowCount(0); 
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_paiement"));
                row.add(rs.getString("nom") + " " + rs.getString("prenom"));
                row.add(rs.getDouble("montant_paye") + " FCFA");
                row.add(rs.getDate("date_paiement"));
                row.add(rs.getString("type_paiement"));
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}