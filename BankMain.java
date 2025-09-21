import BankManagement.BankManagement;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BankMain {
    public static void main(String[] args) {
        try (BufferedReader sc = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("\nWelcome to My Bank ....\n ");
                System.out.println("1) Create Account");
                System.out.println("2) Login Account");
                System.out.println("9) Exit");
                System.out.print("Enter Input: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.readLine().trim());
                } catch (Exception e) {
                    System.out.println("Enter valid input!");
                    continue;
                }

                if (choice == 1) {
                    handleCreateAccount(sc);
                } else if (choice == 2) {
                    handleLoginAccount(sc);
                } else if (choice == 9) {
                    System.out.println("Exited Successfully!\nThank you!");
                    break;
                } else {
                    System.out.println("Invalid choice!\n");
                }
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred!");
        }
    }

    private static void handleCreateAccount(BufferedReader sc) {
        try {
            System.out.print("Enter Unique UserName: ");
            String name = sc.readLine();
            System.out.print("Enter New Password: ");
            int pass = Integer.parseInt(sc.readLine().trim());
            boolean ok = BankManagement.createAccount(name, pass);
            System.out.println(ok ? "Account created successfully!\n" : "Account creation failed!\n");
        } catch (Exception e) {
            System.out.println("Invalid input! Account creation failed!\n");
        }
    }

    private static void handleLoginAccount(BufferedReader sc) {
        try {
            System.out.print("Enter UserName: ");
            String name = sc.readLine();
            System.out.print("Enter Password: ");
            int pass = Integer.parseInt(sc.readLine().trim());
            boolean ok = BankManagement.loginAccount(name, pass, sc);
            System.out.println(ok ? "Logged out successfully!\n" : "Login failed!\n");
        } catch (Exception e) {
            System.out.println("Invalid input! Login failed!\n");
        }
    }

}
