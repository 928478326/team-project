package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import interface_adapter.recommendation.EditProfileController;
import interface_adapter.recommendation.PrivateSettingsInput;
import interface_adapter.recommendation.RecommendationState;

/** Owns the editable public fields and settings visible only to the signed-in student. */
final class ProfileEditorPanel extends JPanel {
    private static final int FIELD_COLUMNS = 14;
    private static final List<String> STYLES = List.of("Indoor Chill", "Study Focus", "Outdoor Active",
            "Food Explore", "Social Events");
    private final JTextField displayName = new JTextField(FIELD_COLUMNS);
    private final JComboBox<String> weekend = new JComboBox<>(STYLES.toArray(new String[0]));
    private final JTextArea bio = new JTextArea(2, 0);
    private final JTextField contact = new JTextField(FIELD_COLUMNS);
    private final Map<String, JCheckBox> communities = new LinkedHashMap<>();
    private final Set<String> otherCommunities = new HashSet<>();
    private final JTextField utorid = new JTextField(FIELD_COLUMNS);
    private final JLabel identity = new JLabel();
    private final JCheckBox share = new JCheckBox("Share my contact info on match cards");

    ProfileEditorPanel() {
        super(new BorderLayout());
        setOpaque(false);
        weekend.setEditable(true);
        weekend.setPrototypeDisplayValue("Outdoor Active");
        weekend.setToolTipText("Choose a preset or type your own weekend plans.");
        bio.setFont(displayName.getFont());
        bio.setLineWrap(true);
        bio.setWrapStyleWord(true);
        final JPanel sections = ProfileLayout.column();
        ProfileLayout.append(sections, publicSection());
        ProfileLayout.append(sections, privateSection());
        add(sections, BorderLayout.NORTH);
    }

    private JPanel publicSection() {
        final JPanel rows = ProfileLayout.column();
        final JPanel pair = new JPanel(new GridLayout(1, 2, ProfileLayout.GAP, 0));
        pair.setOpaque(false);
        final JPanel nameField = new JPanel(new BorderLayout());
        nameField.setOpaque(false);
        nameField.add(ProfileLayout.field("Display Name", displayName), BorderLayout.NORTH);
        pair.add(nameField);
        final JPanel styleField = ProfileLayout.field("Weekend Style", weekend);
        final JLabel hint = new JLabel("Choose a preset or type your own.");
        hint.setForeground(ProfileLayout.MUTED);
        styleField.add(hint, BorderLayout.SOUTH);
        pair.add(styleField);
        ProfileLayout.append(rows, pair);
        final JScrollPane bioScroll = new JScrollPane(bio);
        bio.getAccessibleContext().setAccessibleName("Bio");
        ProfileLayout.append(rows, ProfileLayout.field("Bio", bioScroll));
        ProfileLayout.append(rows, ProfileLayout.field("Contact Info", contact));
        final JPanel options = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        options.setOpaque(false);
        for (String id : List.of("study", "sports", "gaming", "food", "chill")) {
            final JCheckBox option = new JCheckBox(id);
            option.setOpaque(false);
            communities.put(id, option);
            options.add(option);
        }
        ProfileLayout.append(rows, ProfileLayout.field("Interest communities", options));
        return ProfileLayout.card("Public Profile", rows);
    }

    private JPanel privateSection() {
        final JPanel rows = ProfileLayout.column();
        final JPanel idRow = new JPanel(new GridLayout(1, 2, ProfileLayout.GAP, 0));
        idRow.setOpaque(false);
        idRow.add(ProfileLayout.field("UTORid", utorid));
        idRow.add(ProfileLayout.field("Saved identity status", identity));
        ProfileLayout.append(rows, idRow);
        share.setOpaque(false);
        share.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ProfileLayout.append(rows, share);
        return ProfileLayout.card("Private Settings", rows);
    }

    static String styleLabel(String stored) {
        String label = stored;
        for (String preset : STYLES) {
            if (preset.toUpperCase(Locale.ROOT).replace(' ', '_').equals(stored)) {
                label = preset;
                break;
            }
        }
        return label;
    }

    void populate(RecommendationState state) {
        displayName.setText(state.getDisplayName());
        bio.setText(state.getBio());
        bio.setCaretPosition(0);
        contact.setText(state.getContactInfo());
        weekend.setSelectedItem(styleLabel(state.getWeekendStyleCode()));
        otherCommunities.clear();
        otherCommunities.addAll(state.getInterestedCommunityIds());
        otherCommunities.removeAll(communities.keySet());
        communities.forEach((id, box) -> box.setSelected(state.getInterestedCommunityIds().contains(id)));
        utorid.setText(state.getUtorid());
        share.setSelected(state.isShareContactInfo());
        String status = "No UTORid recorded";
        if (state.isUoftVerified()) {
            status = "UofT verified";
        }
        else if (state.hasUtorid()) {
            status = "Recorded - pending verification";
        }
        identity.setText(status);
    }

    void clear() {
        displayName.setText("");
        bio.setText("");
        contact.setText("");
        weekend.setSelectedItem("");
        communities.values().forEach(box -> box.setSelected(false));
        otherCommunities.clear();
        utorid.setText("");
        identity.setText("");
        share.setSelected(false);
    }

    void allowEditing(boolean enabled) {
        displayName.setEnabled(enabled);
        weekend.setEnabled(enabled);
        bio.setEnabled(enabled);
        contact.setEnabled(enabled);
        communities.values().forEach(box -> box.setEnabled(enabled));
        utorid.setEnabled(enabled);
        share.setEnabled(enabled);
    }

    // Read every Swing control on the EDT before creating the background operation.
    Runnable captureSave(EditProfileController controller, String userId) {
        final String name = displayName.getText();
        final String about = bio.getText();
        final String handle = contact.getText();
        final String style = Objects.toString(weekend.getEditor().getItem(), "");
        final Set<String> selected = new HashSet<>(otherCommunities);
        communities.forEach((id, box) -> {
            if (box.isSelected()) {
                selected.add(id);
            }
        });
        final PrivateSettingsInput settings = new PrivateSettingsInput(utorid.getText(), share.isSelected());
        return () -> controller.saveProfile(userId, name, about, handle, style, selected, settings);
    }
}
