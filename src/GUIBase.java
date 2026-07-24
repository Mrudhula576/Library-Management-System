import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GUIBase {
    public static void main(String[] args) {
        new GUIBase();
    }

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField usernameField, passwordField;
    private Regular membership;
    private int attempts;

    private MemberDAO memberDAO = new MemberDAO();
    private BookDAO bookDAO = new BookDAO();

    private DefaultListModel<Book> borrowListModel;
    private JList<Book> borrowJList;

    private DefaultListModel<BorrowedBook> returnListModel;
    private JList<BorrowedBook> returnJList;

    private JPanel profileContent;

    // ---- Shared style constants ----
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private static final Color BACKGROUND = new Color(245, 245, 250);
    private static final Color ACCENT = new Color(70, 130, 180);
    private static final Color ACCENT_DARK = new Color(50, 100, 140);
    private static final Color GRAY_BUTTON = new Color(120, 120, 120);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);

    public GUIBase() {
        frame = new JFrame("Erie Library System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(buildLoginPanel(), "login");
        mainPanel.add(buildSignupPanel(), "signup");
        mainPanel.add(buildDashboardPanel(), "dashboard");
        mainPanel.add(buildBorrowPanel(), "borrow");
        mainPanel.add(buildReturnPanel(), "returnBooks");
        mainPanel.add(buildProfilePanel(), "profile");

        frame.add(mainPanel);
        cardLayout.show(mainPanel, "login");
        frame.setVisible(true);
    }

    // ---- Helpers ----

    private void styleButton(JButton button, Color bg) {
        button.setFont(BUTTON_FONT);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
    }

    private JPanel buildHeader(String text) {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(ACCENT);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel(text);
        headerLabel.setFont(TITLE_FONT);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        return headerPanel;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_FONT);
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private void showCard(String name) {
        cardLayout.show(mainPanel, name);
    }

    private void showDbError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(frame,
            "A database error occurred: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- LOGIN PANEL ----

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(buildHeader("Erie Library System"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BACKGROUND);
        center.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel prompt = label("Enter Username and Password:");
        prompt.setFont(HEADER_FONT);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel loginRow = new JPanel();
        loginRow.setBackground(BACKGROUND);
        usernameField = new JTextField(15);
        usernameField.setFont(LABEL_FONT);
        passwordField = new JPasswordField(15);
        passwordField.setFont(LABEL_FONT);
        JButton loginButton = new JButton("Login");
        styleButton(loginButton, ACCENT);

        loginRow.add(label("Username:"));
        loginRow.add(usernameField);
        loginRow.add(label("Password:"));
        loginRow.add(passwordField);
        loginRow.add(loginButton);

        loginButton.addActionListener(e -> handleLogin());

        JLabel registerPrompt = new JLabel(
            "Don't have membership? Click register to create an account and become a member",
            SwingConstants.CENTER
        );
        registerPrompt.setFont(LABEL_FONT);
        registerPrompt.setForeground(TEXT_COLOR);
        registerPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = new JButton("Register");
        styleButton(registerButton, ACCENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> showCard("signup"));

        center.add(prompt);
        center.add(Box.createRigidArea(new Dimension(0, 15)));
        center.add(loginRow);
        center.add(Box.createRigidArea(new Dimension(0, 30)));
        center.add(registerPrompt);
        center.add(Box.createRigidArea(new Dimension(0, 10)));
        center.add(registerButton);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(((JPasswordField) passwordField).getPassword()).trim();

        try {
            Library result = memberDAO.login(username, password);
            if (result != null) {
                membership = (Regular) result;
                JOptionPane.showMessageDialog(frame, "Welcome, " + membership.memberName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                showCard("dashboard");
            } else {
                attempts++;
                if (attempts >= 3) {
                    JOptionPane.showMessageDialog(frame, "You have reached your limit for incorrect username or password. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password. Please try again", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ---- SIGNUP PANEL ----

    private JPanel buildSignupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(buildHeader("Create Your Membership"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BACKGROUND);
        center.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel registerLabel = label("Welcome! Please provide the information below.");
        registerLabel.setFont(HEADER_FONT);
        registerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel nameRow = new JPanel();
        nameRow.setBackground(BACKGROUND);
        JTextField fname = new JTextField(12);
        fname.setFont(LABEL_FONT);
        JTextField lname = new JTextField(12);
        lname.setFont(LABEL_FONT);
        nameRow.add(label("First Name:"));
        nameRow.add(fname);
        nameRow.add(label("Last Name:"));
        nameRow.add(lname);

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(BACKGROUND);
        JTextField memType = new JTextField(15);
        memType.setFont(LABEL_FONT);
        infoPanel.add(label("Membership Type:"));
        infoPanel.add(memType);

        JPanel signUpPanel = new JPanel();
        signUpPanel.setBackground(BACKGROUND);
        JTextField username = new JTextField(15);
        username.setFont(LABEL_FONT);
        JPasswordField password = new JPasswordField(15);
        password.setFont(LABEL_FONT);
        signUpPanel.add(label("Username:"));
        signUpPanel.add(username);
        signUpPanel.add(label("Password:"));
        signUpPanel.add(password);

        JPanel buttonRow = new JPanel();
        buttonRow.setBackground(BACKGROUND);
        JButton backButton = new JButton("Back");
        styleButton(backButton, GRAY_BUTTON);
        backButton.addActionListener(e -> showCard("login"));

        JButton submitButton = new JButton("Submit");
        styleButton(submitButton, ACCENT);
        submitButton.addActionListener(e -> createAccount(fname, lname, memType, username, password));

        buttonRow.add(backButton);
        buttonRow.add(submitButton);

        center.add(registerLabel);
        center.add(Box.createRigidArea(new Dimension(0, 20)));
        center.add(nameRow);
        center.add(Box.createRigidArea(new Dimension(0, 10)));
        center.add(infoPanel);
        center.add(Box.createRigidArea(new Dimension(0, 10)));
        center.add(signUpPanel);
        center.add(Box.createRigidArea(new Dimension(0, 20)));
        center.add(buttonRow);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void createAccount(JTextField fnameField, JTextField lnameField, JTextField memTypeField,
                                JTextField usernameF, JPasswordField passwordF) {
        String fname = fnameField.getText().trim();
        String lname = lnameField.getText().trim();
        String membershipType = memTypeField.getText().trim();
        String username = usernameF.getText().trim();
        String password = new String(passwordF.getPassword()).trim();

        if (fname.isEmpty() || membershipType.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in first name, membership type, username, and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (memberDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(frame, "Username already exists. Choose a different username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            memberDAO.signup(fname, lname, membershipType, username, password);
            JOptionPane.showMessageDialog(frame, "Membership account successfully created!", "Success", JOptionPane.INFORMATION_MESSAGE);
            fnameField.setText("");
            lnameField.setText("");
            memTypeField.setText("");
            usernameF.setText("");
            passwordF.setText("");
            showCard("login");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ---- DASHBOARD PANEL ----

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(buildHeader("Your Dashboard"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BACKGROUND);
        center.setBorder(new EmptyBorder(40, 50, 40, 50));

        JButton borrowBtn = new JButton("Borrow a Book");
        styleButton(borrowBtn, ACCENT);
        borrowBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        borrowBtn.addActionListener(e -> { refreshBorrowPanel(); showCard("borrow"); });

        JButton returnBtn = new JButton("Return a Book");
        styleButton(returnBtn, ACCENT);
        returnBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnBtn.addActionListener(e -> { refreshReturnPanel(); showCard("returnBooks"); });

        JButton profileBtn = new JButton("My Profile");
        styleButton(profileBtn, ACCENT_DARK);
        profileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileBtn.addActionListener(e -> { refreshProfilePanel(); showCard("profile"); });

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, GRAY_BUTTON);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            membership = null;
            usernameField.setText("");
            passwordField.setText("");
            attempts = 0;
            showCard("login");
        });

        center.add(borrowBtn);
        center.add(Box.createRigidArea(new Dimension(0, 15)));
        center.add(returnBtn);
        center.add(Box.createRigidArea(new Dimension(0, 15)));
        center.add(profileBtn);
        center.add(Box.createRigidArea(new Dimension(0, 30)));
        center.add(logoutBtn);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ---- BORROW PANEL ----

    private JPanel buildBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(buildHeader("Available Books"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND);
        center.setBorder(new EmptyBorder(20, 40, 20, 40));

        borrowListModel = new DefaultListModel<>();
        borrowJList = new JList<>(borrowListModel);
        borrowJList.setFont(LABEL_FONT);
        borrowJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(borrowJList);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        JPanel buttonRow = new JPanel();
        buttonRow.setBackground(BACKGROUND);

        JButton rentBtn = new JButton("Borrow (Rent)");
        styleButton(rentBtn, ACCENT);
        rentBtn.addActionListener(e -> {
            Book selected = borrowJList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Please select a book first.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String msg = membership.borrowBook(selected);
                JOptionPane.showMessageDialog(frame, msg);
                refreshBorrowPanel();
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });

        JButton buyBtn = new JButton("Buy");
        styleButton(buyBtn, ACCENT_DARK);
        buyBtn.addActionListener(e -> {
            Book selected = borrowJList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Please select a book first.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String msg = membership.buyBook(selected);
                JOptionPane.showMessageDialog(frame, msg);
                refreshBorrowPanel();
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });

        JButton backBtn = new JButton("Back");
        styleButton(backBtn, GRAY_BUTTON);
        backBtn.addActionListener(e -> showCard("dashboard"));

        buttonRow.add(backBtn);
        buttonRow.add(rentBtn);
        buttonRow.add(buyBtn);

        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void refreshBorrowPanel() {
        borrowListModel.clear();
        try {
            List<Book> available = bookDAO.getAvailableBooks();
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No books are currently available in the library.", "Unavailable", JOptionPane.INFORMATION_MESSAGE);
            }
            for (Book b : available) {
                borrowListModel.addElement(b);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ---- RETURN PANEL ----

    private JPanel buildReturnPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(buildHeader("Return a Book"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND);
        center.setBorder(new EmptyBorder(20, 40, 20, 40));

        returnListModel = new DefaultListModel<>();
        returnJList = new JList<>(returnListModel);
        returnJList.setFont(LABEL_FONT);
        returnJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(returnJList);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        JPanel buttonRow = new JPanel();
        buttonRow.setBackground(BACKGROUND);

        JButton returnBtn = new JButton("Return Selected");
        styleButton(returnBtn, ACCENT);
        returnBtn.addActionListener(e -> {
            BorrowedBook selected = returnJList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Please select a book to return.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                boolean success = membership.returnBook(selected.getTitle());
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Returned \"" + selected.getTitle() + "\". Thank you!");
                }
                refreshReturnPanel();
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });

        JButton backBtn = new JButton("Back");
        styleButton(backBtn, GRAY_BUTTON);
        backBtn.addActionListener(e -> showCard("dashboard"));

        buttonRow.add(backBtn);
        buttonRow.add(returnBtn);

        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void refreshReturnPanel() {
        returnListModel.clear();
        if (membership == null) return;
        try {
            List<BorrowedBook> active = membership.getBorrowedBooks();
            boolean any = false;
            for (BorrowedBook b : active) {
                if (!b.isPurchased()) {
                    returnListModel.addElement(b);
                    any = true;
                }
            }
            if (!any) {
                JOptionPane.showMessageDialog(frame, "You don't have any borrowed books to return.", "Nothing to return", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ---- PROFILE PANEL ----

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(buildHeader("My Profile"), BorderLayout.NORTH);

        profileContent = new JPanel();
        profileContent.setLayout(new BoxLayout(profileContent, BoxLayout.Y_AXIS));
        profileContent.setBackground(BACKGROUND);
        profileContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        JScrollPane scrollPane = new JScrollPane(profileContent);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel();
        bottomRow.setBackground(BACKGROUND);
        JButton backBtn = new JButton("Back");
        styleButton(backBtn, GRAY_BUTTON);
        backBtn.addActionListener(e -> showCard("dashboard"));
        bottomRow.add(backBtn);
        panel.add(bottomRow, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshProfilePanel() {
        profileContent.removeAll();
        if (membership == null) return;

        try {
            JLabel typeLabel = label("Membership Type: " + membership.membershipType());
            JLabel idLabel = label("Membership ID: " + membership.membershipID());
            JLabel nameLabel = label("Holder Name: " + membership.memberName());
            JLabel feeLabel = label("Monthly Fee: $" + membership.getMonthlyFee() + " (includes 5 free book rentals/month)");

            typeLabel.setFont(HEADER_FONT);
            idLabel.setFont(LABEL_FONT);
            nameLabel.setFont(LABEL_FONT);
            feeLabel.setFont(LABEL_FONT);

            JLabel booksHeader = label("Your Books:");
            booksHeader.setFont(HEADER_FONT);

            profileContent.add(typeLabel);
            profileContent.add(Box.createRigidArea(new Dimension(0, 5)));
            profileContent.add(idLabel);
            profileContent.add(nameLabel);
            profileContent.add(feeLabel);
            profileContent.add(Box.createRigidArea(new Dimension(0, 20)));
            profileContent.add(booksHeader);
            profileContent.add(Box.createRigidArea(new Dimension(0, 5)));

            List<BorrowedBook> books = membership.getBorrowedBooks();
            if (books.isEmpty()) {
                profileContent.add(label("You don't have any books right now."));
            } else {
                for (BorrowedBook b : books) {
                    profileContent.add(label("• " + b.toString()));
                }
            }

            profileContent.add(Box.createRigidArea(new Dimension(0, 25)));
            JLabel owedLabel = label(String.format("Amount Owed: $%.2f", membership.getAmountOwed()));
            JLabel paidLabel = label(String.format("Amount Paid: $%.2f", membership.getAmountPaid()));
            owedLabel.setFont(HEADER_FONT);
            paidLabel.setFont(LABEL_FONT);

            profileContent.add(owedLabel);
            profileContent.add(paidLabel);

            if (membership.getAmountOwed() > 0) {
                JButton payButton = new JButton("Pay Balance ($" + String.format("%.2f", membership.getAmountOwed()) + ")");
                styleButton(payButton, ACCENT);
                payButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                payButton.addActionListener(e -> {
                    try {
                        membership.payBalance(membership.getAmountOwed());
                        JOptionPane.showMessageDialog(frame, "Balance paid. Thank you!");
                        refreshProfilePanel();
                    } catch (SQLException ex) {
                        showDbError(ex);
                    }
                });
                profileContent.add(Box.createRigidArea(new Dimension(0, 10)));
                profileContent.add(payButton);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }

        profileContent.revalidate();
        profileContent.repaint();
    }
}