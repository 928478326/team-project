package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Layout and text styles shared by the profile editor and saved card. */
final class ProfileLayout {
    static final int GAP = 8;
    static final int PAD = 16;
    static final int HEADING = 18;
    static final int TITLE = 26;
    static final Color CANVAS = new Color(0xF2F5F4);
    static final Color INK = new Color(0x162E31);
    static final Color MUTED = new Color(0x455F62);
    static final Color TEAL = new Color(0x155E55);
    static final Color LINE = new Color(0xCCD9D7);

    private ProfileLayout() {
    }

    static JLabel heading(String text, int size) {
        final JLabel label = new JLabel(text);
        label.putClientProperty("html.disable", true);
        label.setFont(label.getFont().deriveFont(Font.BOLD, size));
        label.setForeground(INK);
        return label;
    }

    static JPanel column() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    static void append(JPanel column, Component component) {
        final GridBagConstraints cell = new GridBagConstraints();
        cell.gridx = 0;
        cell.weightx = 1;
        cell.fill = GridBagConstraints.HORIZONTAL;
        cell.insets = new Insets(0, 0, GAP, 0);
        column.add(component, cell);
    }

    static JPanel card(String title, JComponent content) {
        final JPanel panel = new JPanel(new BorderLayout(0, GAP));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(PAD, PAD, GAP, PAD)));
        panel.add(heading(title, HEADING), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    static JTextArea wrappingText() {
        final JTextArea area = new JTextArea();
        area.setFont(new JLabel().getFont().deriveFont(Font.PLAIN));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(INK);
        return area;
    }

    static JPanel field(String caption, JComponent input) {
        final JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);
        final JLabel label = new JLabel(caption);
        label.setLabelFor(input);
        label.setForeground(MUTED);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }
}
