package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import interface_adapter.recommendation.RecommendationState;

/** A saved match-card preview and the empty recommendations area. */
final class ProfilePreviewPanel extends JPanel {
    private final JLabel name = ProfileLayout.heading("", ProfileLayout.TITLE);
    private final JTextArea bio = ProfileLayout.wrappingText();
    private final JTextArea weekend = ProfileLayout.wrappingText();
    private final JTextArea interests = ProfileLayout.wrappingText();
    private final JTextArea contact = ProfileLayout.wrappingText();

    ProfilePreviewPanel() {
        super(new BorderLayout());
        setOpaque(false);
        final JPanel stack = ProfileLayout.column();
        ProfileLayout.append(stack, profileCard());
        final JTextArea empty = ProfileLayout.wrappingText();
        empty.setText("Buddy matches will appear here when recommendations are available.");
        ProfileLayout.append(stack, ProfileLayout.card("Buddy recommendations", empty));
        add(stack, BorderLayout.NORTH);
    }

    private JPanel profileCard() {
        final JPanel top = new JPanel(new BorderLayout(0, ProfileLayout.GAP));
        top.setBackground(ProfileLayout.TEAL);
        top.setBorder(BorderFactory.createEmptyBorder(ProfileLayout.PAD, ProfileLayout.PAD,
                ProfileLayout.PAD, ProfileLayout.PAD));
        final JLabel heading = new JLabel("Match card preview");
        heading.setForeground(Color.WHITE);
        name.setForeground(Color.WHITE);
        name.setPreferredSize(new Dimension(0, name.getFontMetrics(name.getFont()).getHeight()));
        top.add(heading, BorderLayout.NORTH);
        top.add(name, BorderLayout.CENTER);

        final JPanel details = ProfileLayout.column();
        details.setBorder(BorderFactory.createEmptyBorder(ProfileLayout.PAD, ProfileLayout.PAD,
                ProfileLayout.GAP, ProfileLayout.PAD));
        ProfileLayout.append(details, bio);
        ProfileLayout.append(details, ProfileLayout.field("Weekend style", weekend));
        ProfileLayout.append(details, ProfileLayout.field("Communities", interests));
        ProfileLayout.append(details, ProfileLayout.field("Contact", contact));
        final JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(ProfileLayout.LINE));
        card.setBackground(Color.WHITE);
        card.add(top, BorderLayout.NORTH);
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    void populate(RecommendationState state) {
        name.setText(state.getDisplayName());
        bio.setText(state.getBio());
        bio.setVisible(!state.getBio().isBlank());
        weekend.setText(ProfileEditorPanel.styleLabel(state.getWeekendStyleCode()));
        interests.setText(String.join(", ", new TreeSet<>(state.getInterestedCommunityIds())));
        String visibleContact = "Hidden on match cards";
        if (state.isShareContactInfo()) {
            visibleContact = state.getContactInfo();
            if (visibleContact.isBlank()) {
                visibleContact = "No contact added";
            }
        }
        contact.setText(visibleContact);
    }

    void clear() {
        name.setText("");
        bio.setText("");
        weekend.setText("");
        interests.setText("");
        contact.setText("");
    }
}
