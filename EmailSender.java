package View;

import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    
    private static final String MON_EMAIL = "an0247706@gmail.com"; 
    private static final String MON_PASSWORD = "iguvmxkswzqpamsx"; 

    // --- MÉTHODE 1 : NOTIFICATION D'ASSIGNATION (Ton code actuel) ---
    public static void envoyerNotification(String destinataire, String matiere, String date) {
        envoyerMail(destinataire, "📌 Nouveau cours assigné : " + matiere, 
            "Bonjour,\n\nUn nouveau cours vous a été assigné.\nMatière : " + matiere + "\nDate prévue : " + date + "\n\nCordialement,\nLa Direction ESITEC.");
    }

    // --- MÉTHODE 2 : NOTIFICATION D'ANNULATION ---
    public static void envoyerAnnulation(String destinataire, String matiere, String date) {
        envoyerMail(destinataire, "⚠️ ANNULATION : Séance de " + matiere, 
            "Bonjour,\n\nNous vous informons que la séance de " + matiere + " prévue le " + date + " est ANNULÉE.\n\nMerci de votre compréhension.\nLa Direction ESITEC.");
    }

    // --- MÉTHODE 3 : ENVOI GROUPÉ À TOUTE UNE CLASSE ---
    public static void notifierTouteLaClasse(int idClasse, String matiere, String date) {
        String sql = "SELECT email FROM etudiants WHERE id_classe = ?";
        
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idClasse);
            ResultSet rs = pst.executeQuery();

            int count = 0;
            while (rs.next()) {
                String email = rs.getString("email");
                if (email != null && !email.isEmpty()) {
                    envoyerAnnulation(email, matiere, date);
                    count++;
                }
            }
            System.out.println("✅ Notification envoyée à " + count + " étudiants.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTHODE PRIVÉE (Pour éviter de répéter la config Gmail partout) ---
    private static void envoyerMail(String destinataire, String sujet, String contenu) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); 

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MON_EMAIL, MON_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MON_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            Transport.send(message);
            System.out.println("✅ Email envoyé à : " + destinataire);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
}