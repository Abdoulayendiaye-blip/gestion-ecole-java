package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.formdev.flatlaf.FlatLightLaf; // Passage en Light pour mieux voir le Bleu/Jaune

public class LoginFrame extends JFrame {
    private JTextField txtLogin;
    private JPasswordField txtPassword;

    // --- PALETTE ESITEC ---
    private final Color BLEU_ESITEC = new Color(25, 42, 86);
    private final Color JAUNE_ESITEC = new Color(241, 196, 15);

    public LoginFrame() {
        setTitle("ESITEC - Authentification");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panneau principal avec fond blanc
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // --- BANDEAU SUPÉRIEUR ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(BLEU_ESITEC);
        headerPanel.setPreferredSize(new Dimension(450, 80));
        JLabel lblTitle = new JLabel("CONNEXION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(JAUNE_ESITEC); // Titre en Jaune
        headerPanel.add(lblTitle);
        // Petit ajustement pour centrer verticalement le texte dans le header
        headerPanel.setLayout(new GridBagLayout()); 

        // --- FORMULAIRE ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Login
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblLogin = new JLabel("Utilisateur :");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(lblLogin, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        txtLogin = new JTextField();
        txtLogin.setPreferredSize(new Dimension(280, 40));
        txtLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(txtLogin, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblPass = new JLabel("Mot de passe :");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(lblPass, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(280, 40));
        formPanel.add(txtPassword, gbc);

        // Bouton Se Connecter
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.insets = new Insets(25, 5, 10, 5);
        JButton btnLogin = new JButton("ACCÉDER AU TABLEAU DE BORD");
        btnLogin.setBackground(BLEU_ESITEC);
        btnLogin.setForeground(JAUNE_ESITEC); // Texte Jaune sur bouton Bleu
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(280, 45));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFocusPainted(false);
        formPanel.add(btnLogin, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Action au clic
        btnLogin.addActionListener(e -> connecter());
        
        // Permettre de valider avec la touche "Entrée"
        getRootPane().setDefaultButton(btnLogin);
    }

    private void connecter() {
        String login = txtLogin.getText();
        String password = new String(txtPassword.getPassword());

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_cahier_texte", "root", "")) {
            String sql = "SELECT id, role FROM utilisateurs WHERE login=? AND password=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, login);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");

                if (role.equalsIgnoreCase("chef")) {
                    new ChefDashboard(id).setVisible(true);
                } else if (role.equalsIgnoreCase("responsable")) {
                    new ResponsableDashboard(id).setVisible(true);
                } else if (role.equalsIgnoreCase("enseignant")) {
                    new EnseignantDashboard(id).setVisible(true);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Identifiants ESITEC incorrects.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Connexion serveur impossible : " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Utilisation de FlatLightLaf pour un look "propre" sur fond blanc
            FlatLightLaf.setup();
            
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            
        } catch (Exception ex) {
            System.err.println("Erreur de design.");
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}