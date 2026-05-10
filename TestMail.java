package View;

public class TestMail {
    public static void main(String[] args) {
        System.out.println("--- DÉBUT DU TEST D'ENVOI GROUPÉ ---");
        
        // On teste pour la classe ID 1 (L1 Info par exemple)
        // Remplace 1 par un ID qui existe dans ta table classes
        int idClasseATester = 1; 
        String matiere = "TEST SYSTÈME";
        String date = "Lundi 16 Mars";

        // Appel de ta méthode
        EmailSender.notifierTouteLaClasse(idClasseATester, matiere, date);
        
        System.out.println("--- FIN DU TEST ---");
    }
}