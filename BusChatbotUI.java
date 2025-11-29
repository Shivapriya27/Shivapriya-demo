import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BusChatbotUI extends JFrame {
    private JPanel chatPanel;
    private JTextField inputField;
    private RoundedButton sendButton;
    private JScrollPane scrollPane;
    private List<Message> messages;
    private String stage = "initial";
    private String userLocation = "";
    private String destination = "";
    private Timer typingTimer;
    private JPanel typingIndicator;

    // ⭐ FIX: Added missing mainPanel
    private JPanel mainPanel;

    // Gradient colors
    private final Color GRADIENT_START = new Color(99, 102, 241);
    private final Color GRADIENT_END = new Color(139, 92, 246);
    private final Color USER_BUBBLE_START = new Color(59, 130, 246);
    private final Color USER_BUBBLE_END = new Color(99, 102, 241);
    private final Color BOT_BUBBLE = new Color(255, 255, 255);
    private final Color BACKGROUND = new Color(249, 250, 251);
    private final Color ACCENT = new Color(236, 72, 153);

    public BusChatbotUI() {
        setTitle("Bus Route Assistant");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 1000, 750, 30, 30));

        messages = new ArrayList<>();
        initComponents();
        addWelcomeMessage();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ⭐ FIX: Initialize mainPanel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Header Panel
        JPanel headerPanel = createGradientHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Chat Panel
        chatPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, new Color(249, 250, 251), 0, h, new Color(243, 244, 246));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);

                g2d.setColor(new Color(219, 234, 254, 50));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(w - 150, h - 150, 200, 200);
            }
        };
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Quick Actions Panel
        JPanel quickActionsPanel = createQuickActionsPanel();
        mainPanel.add(quickActionsPanel, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = createModernInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createGradientHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, getWidth(), 0, GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("🚌");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(Color.WHITE);
        startIconAnimation(iconLabel);
        leftPanel.add(iconLabel);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Bus Route Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("✨ Find routes, timings & more instantly");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(224, 231, 255));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);

        leftPanel.add(textPanel);
        headerPanel.add(leftPanel, BorderLayout.WEST);

        RoundedButton closeBtn = new RoundedButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        closeBtn.setBackground(new Color(255, 255, 255, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(45, 45));
        closeBtn.addActionListener(e -> System.exit(0));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(closeBtn);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createQuickActionsPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        String[][] actions = {
                {"📍", "Current Location"},
                {"🔥", "Popular Routes"},
                {"🕐", "Schedule"}
        };

        for (String[] action : actions) {
            RoundedButton btn = new RoundedButton(action[0] + " " + action[1]);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setBackground(new Color(219, 234, 254));
            btn.setForeground(new Color(30, 64, 175));
            btn.addActionListener(e -> {
                inputField.setText(action[1]);
                handleSend();
            });
            actionsPanel.add(btn);
        }

        container.add(actionsPanel, BorderLayout.NORTH);

        // ❌ Removed invalid duplicate scrollPane.add

        return container;
    }

    private JPanel createModernInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(15, 0));

        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                new EmptyBorder(25, 30, 25, 30)
        ));

        inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 2, true),
                new EmptyBorder(14, 20, 14, 20)
        ));
        inputField.setBackground(new Color(249, 250, 251));
        inputField.setOpaque(false);
        updatePlaceholder();

        sendButton = new RoundedButton("Send ➤");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sendButton.setBackground(GRADIENT_START);
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(new Dimension(120, 55));

        sendButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(GRADIENT_END);
            }

            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(GRADIENT_START);
            }
        });

        sendButton.addActionListener(e -> handleSend());
        inputField.addActionListener(e -> handleSend());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        return inputPanel;
    }

    private void addWelcomeMessage() {
        addBotMessage("Hello! 👋 I'm your Bus Route Assistant.\n\nI can help you find:\n• Bus routes & timings\n• Fare information\n• Real-time updates\n\nWhere would you like to go today?");
    }

    private void handleSend() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) return;

        addUserMessage(userInput);
        inputField.setText("");

        showTypingIndicator();

        Timer timer = new Timer(1200, e -> {
            hideTypingIndicator();
            processUserInput(userInput);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showTypingIndicator() {
        typingIndicator = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typingIndicator.setOpaque(false);

        JPanel bubble = new RoundedPanel(20);
        bubble.setBackground(BOT_BUBBLE);
        bubble.setBorder(new EmptyBorder(15, 20, 15, 20));
        bubble.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        for (int i = 0; i < 3; i++) {
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("Arial", Font.PLAIN, 20));
            dot.setForeground(new Color(156, 163, 175));
            bubble.add(dot);
            animateDot(dot, i * 200);
        }

        typingIndicator.add(bubble);
        chatPanel.add(typingIndicator);
        chatPanel.revalidate();
        scrollToBottom();
    }

    private void hideTypingIndicator() {
        if (typingIndicator != null) {
            chatPanel.remove(typingIndicator);
            chatPanel.revalidate();
            chatPanel.repaint();
        }
    }

    private void animateDot(JLabel dot, int delay) {
        Timer timer = new Timer(50, new ActionListener() {
            int alpha = 100;
            boolean increasing = true;

            public void actionPerformed(ActionEvent e) {
                if (increasing) {
                    alpha += 10;
                    if (alpha >= 255) {
                        alpha = 255;
                        increasing = false;
                    }
                } else {
                    alpha -= 10;
                    if (alpha <= 100) {
                        alpha = 100;
                        increasing = true;
                    }
                }
                dot.setForeground(new Color(156, 163, 175, alpha));
            }
        });
        Timer delayTimer = new Timer(delay, e -> timer.start());
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private void processUserInput(String input) {
        if (stage.equals("initial")) {
            userLocation = input;
            stage = "destination";
            addBotMessage("Perfect! 🎯 You're starting from " + input + ".\n\nNow, where would you like to go?");
            updatePlaceholder();
        } else if (stage.equals("destination")) {
            destination = input;
            stage = "results";
            showRouteResults();
            updatePlaceholder();
        } else {
            String lowerInput = input.toLowerCase();
            if (lowerInput.contains("search") || lowerInput.contains("new") || lowerInput.contains("another")) {
                resetConversation();
            } else if (lowerInput.contains("detail") || lowerInput.matches(".*\\d+[a-zA-Z]?.*")) {
                addBotMessage("📋 Complete Route Details:\n\n🕐 Operating: 6:00 AM -PM\n📍 Major Stops:\n  • City Center\n  • Market Square\n  • Railway Station\n  • Bus Terminal\n⏱️ Wait Time: 10-15 mins\n\nAnything else you'd like to know?");
            } else {
                addBotMessage("I'm here to help! 🤝\n\n• Type 'new search' for another route\n• Mention a bus number for details\n• Ask about timings or stops\n\nWhat can I help you with?");
            }
        }
    }

    private void showRouteResults() {
        StringBuilder response = new StringBuilder();
        response.append("🚌 Found 3 routes from ").append(userLocation)
                .append(" to ").append(destination).append("!\n\n");

        String[][] routes = {
                {"42A", "35 mins", "₹25", "10 mins", "Every 15 mins", "🟢"},
                {"156", "42 mins", "₹30", "25 mins", "Every 20 mins", "🟡"},
                {"X9", "28 mins", "₹40", "5 mins", "Every 30 mins", "🟢"}
        };

        for (String[] route : routes) {
            response.append("━━━━━━━━━━━━━━━━━\n");
            response.append(route[5]).append(" Bus ").append(route[0]).append("\n");
            response.append("⏱️ Duration: ").append(route[1]).append("\n");
            response.append("💰 Fare: ").append(route[2]).append("\n");
            response.append("🚏 Next bus: ").append(route[3]).append("\n");
            response.append("🔄 Runs ").append(route[4]).append("\n\n");
        }

        response.append("💡 Need details? Just type the bus number!\nOr search for another route.");
        addBotMessage(response.toString());
    }

    private void resetConversation() {
        stage = "initial";
        userLocation = "";
        destination = "";
        addBotMessage("Let's start fresh! 🔄\n\nWhere are you starting from?");
        updatePlaceholder();
    }

    private void updatePlaceholder() {
        if (stage.equals("initial")) {
            inputField.setToolTipText("Type your starting location...");
        } else if (stage.equals("destination")) {
            inputField.setToolTipText("Type your destination...");
        } else {
            inputField.setToolTipText("Ask me anything...");
        }
    }

    private void addUserMessage(String text) {
        messages.add(new Message(text, true));
        addMessageBubble(text, true);
    }

    private void addBotMessage(String text) {
        messages.add(new Message(text, false));
        addMessageBubble(text, false);
    }

    private void addMessageBubble(String text, boolean isUser) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
        messagePanel.setOpaque(false);
        messagePanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        if (isUser) {
            messagePanel.add(Box.createHorizontalGlue());
        }

        JPanel bubble = createStylishBubble(text, isUser);
        messagePanel.add(bubble);

        if (!isUser) {
            messagePanel.add(Box.createHorizontalGlue());
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        scrollToBottom();

        bubble.setOpaque(false);
        animateFadeIn(bubble);
    }

    private JPanel createStylishBubble(String text, boolean isUser) {
        RoundedPanel bubble = new RoundedPanel(20);
        bubble.setLayout(new BorderLayout());

        if (isUser) {
            bubble.setGradient(USER_BUBBLE_START, USER_BUBBLE_END);
        } else {
            bubble.setBackground(BOT_BUBBLE);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                    new EmptyBorder(16, 20, 16, 20)
            ));
        }

        if (isUser) {
            bubble.setBorder(new EmptyBorder(16, 20, 16, 20));
        }

        JTextArea textArea = new JTextArea(text);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textArea.setForeground(isUser ? Color.WHITE : new Color(31, 41, 55));

        bubble.add(textArea, BorderLayout.CENTER);

        JLabel timeLabel = new JLabel(getCurrentTime());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(isUser ? new Color(224, 231, 255) : new Color(156, 163, 175));
        timeLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        bubble.add(timeLabel, BorderLayout.SOUTH);

        return bubble;
    }

    private void animateFadeIn(JPanel component) {
        Timer timer = new Timer(20, new ActionListener() {
            float alpha = 0.0f;

            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                component.repaint();
            }
        });
        timer.start();
    }

    private void startIconAnimation(JLabel icon) {
        Timer timer = new Timer(2000, new ActionListener() {
            boolean enlarged = false;

            public void actionPerformed(ActionEvent e) {
                if (!enlarged) {
                    icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
                    enlarged = true;
                } else {
                    icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
                    enlarged = false;
                }
            }
        });
        timer.start();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    static class Message {
        String text;
        boolean isUser;
        LocalTime timestamp;

        Message(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = LocalTime.now();
        }
    }

    static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }

    static class RoundedPanel extends JPanel {
        private int radius;
        private Color gradientStart, gradientEnd;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public void setGradient(Color start, Color end) {
            this.gradientStart = start;
            this.gradientEnd = end;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (gradientStart != null && gradientEnd != null) {
                GradientPaint gp = new GradientPaint(0, 0, gradientStart, getWidth(), 0, gradientEnd);
                g2.setPaint(gp);
            } else {
                g2.setColor(getBackground());
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            BusChatbotUI chatbot = new BusChatbotUI();
            chatbot.setVisible(true);
        });
    }
}
