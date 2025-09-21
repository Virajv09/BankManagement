package BankManagement;

import BankManagement.Connection.DBConnection;
import java.io.BufferedReader;
import java.sql.*;

public class BankManagement {

    public static boolean createAccount(String name, int passCode) {
        if (name == null || name.isBlank() || passCode <= 0) {
            System.out.println("All fields are required!");
            return false;
        }
        String sql = "INSERT INTO customer(cname, balance, pass_code) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setInt(2, 1000);
            ps.setInt(3, passCode);
            int n = ps.executeUpdate();
            if (n == 1) {
                System.out.println(name + ", now you can login!");
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username not available!");
        } catch (Exception e) {
            System.out.println("Account creation failed!");
        }
        return false;
    }

    public static boolean loginAccount(String name, int passCode, BufferedReader sc) {
        if (name == null || name.isBlank() || passCode <= 0) {
            System.out.println("All fields are required!");
            return false;
        }
        String sql = "SELECT ac_no, cname FROM customer WHERE cname = ? AND pass_code = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setInt(2, passCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Login failed! Incorrect username or password.");
                    return false;
                }
                int acNo = rs.getInt("ac_no");
                String cname = rs.getString("cname");
                handleUserSession(acNo, cname, sc); // pass sc
                return true;
            }
        } catch (Exception e) {
            System.out.println("Login failed!");
            return false;
        }
    }


    // In BankManagement.java
    private static boolean handleUserSession(int acNo, String cname, BufferedReader sc) {
        try {
            while (true) {
                System.out.printf("Hello, %s (A/c %d)%n", cname, acNo);
                System.out.println("1) Transfer Money");
                System.out.println("2) View Balance");
                System.out.println("5) LogOut");
                System.out.print("Enter choice: ");

                int ch;
                try {
                    String line = sc.readLine();
                    if (line == null || line.isBlank()) {
                        System.out.println("Enter valid number!");
                        continue;
                    }
                    ch = Integer.parseInt(line.trim());
                } catch (Exception e) {
                    System.out.println("Enter valid number!");
                    continue;
                }

                if (ch == 1) {
                    try {
                        System.out.print("Enter Receiver A/c No: ");
                        int recv = Integer.parseInt(sc.readLine().trim());
                        System.out.print("Enter Amount: ");
                        int amt = Integer.parseInt(sc.readLine().trim());
                        boolean sent = transferMoney(acNo, recv, amt);
                        System.out.println(sent ? "Money Sent Successfully!\n" : "Transfer failed!\n");
                    } catch (Exception e) {
                        System.out.println("Invalid data entered! Transfer failed!\n");
                    }
                } else if (ch == 2) {
                    getBalance(acNo);
                } else if (ch == 5) {
                    System.out.println("Logged out!\n");
                    return true;
                } else {
                    System.out.println("Invalid input!\n");
                }
            }
        } catch (Exception e) {
            System.out.println("Session ended due to error.");
            return true;
        }
    }


    public static void getBalance(int acNo) {
        String sql = "SELECT ac_no, cname, balance FROM customer WHERE ac_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, acNo);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("---------------------------------------------------");
                System.out.printf("%12s %12s %12s%n", "Account No", "Name", "Balance");
                if (rs.next()) {
                    System.out.printf("%12d %12s %12d.00%n",
                            rs.getInt("ac_no"),
                            rs.getString("cname"),
                            rs.getInt("balance"));
                } else {
                    System.out.println("No account found!");
                }
                System.out.println("---------------------------------------------------\n");
            }
        } catch (Exception e) {
            System.out.println("Fetch balance failed!");
        }
    }

    public static boolean transferMoney(int senderAc, int receiverAc, int amount) {
        if (receiverAc <= 0 || amount <= 0) {
            System.out.println("All fields are required and amount must be positive!");
            return false;
        }
        if (senderAc == receiverAc) {
            System.out.println("Cannot transfer to the same account!");
            return false;
        }
        String lockSql = "SELECT balance FROM customer WHERE ac_no = ? FOR UPDATE";
        String debitSql = "UPDATE customer SET balance = balance - ? WHERE ac_no = ?";
        String creditSql = "UPDATE customer SET balance = balance + ? WHERE ac_no = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            int senderBal;
            try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                ps.setInt(1, senderAc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Sender account not found!");
                        con.rollback();
                        return false;
                    }
                    senderBal = rs.getInt("balance");
                }
            }
            try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                ps.setInt(1, receiverAc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Receiver account not found!");
                        con.rollback();
                        return false;
                    }
                }
            }
            if (senderBal < amount) {
                System.out.println("Insufficient balance!");
                con.rollback();
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(debitSql)) {
                ps.setInt(1, amount);
                ps.setInt(2, senderAc);
                if (ps.executeUpdate() != 1) {
                    System.out.println("Debit failed!");
                    con.rollback();
                    return false;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(creditSql)) {
                ps.setInt(1, amount);
                ps.setInt(2, receiverAc);
                if (ps.executeUpdate() != 1) {
                    System.out.println("Credit failed!");
                    con.rollback();
                    return false;
                }
            }
            con.commit();
            return true;
        } catch (Exception e) {
            System.out.println("Transfer failed!");
            return false;
        }
    }
}
