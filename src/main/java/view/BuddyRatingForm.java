package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import interface_adapter.buddy_rating.BuddyRatingState.RateableBuddyRow;
import interface_adapter.buddy_rating.RateBuddyController;

/**
 * Inputs for the current buddy and event. Drafts stay in the form until a save succeeds.
 */
final class BuddyRatingForm extends JPanel {

    private static final int MAX_STARS = 5;
    private static final int COMMENT_ROWS = 3;

    private final JComboBox<Integer> stars = new JComboBox<>();
    private final JCheckBox meetAgain = new JCheckBox("Would meet again");
    private final JTextArea comment = new JTextArea(COMMENT_ROWS, 0);

    BuddyRatingForm(Runnable edited) {
        super(new BorderLayout());
        for (int value = 1; value <= MAX_STARS; value++) {
            stars.addItem(value);
        }
        final JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JLabel scoreLabel = new JLabel("Rating (1-5)");
        scoreLabel.setLabelFor(stars);
        options.add(scoreLabel);
        options.add(stars);
        options.add(meetAgain);
        add(options, BorderLayout.NORTH);

        final JPanel commentPanel = new JPanel(new BorderLayout());
        final JLabel commentLabel = new JLabel("Comment (optional, up to 300 characters)");
        commentLabel.setLabelFor(comment);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        commentPanel.add(commentLabel, BorderLayout.NORTH);
        commentPanel.add(new JScrollPane(comment), BorderLayout.CENTER);
        add(commentPanel, BorderLayout.CENTER);

        stars.addActionListener(event -> edited.run());
        meetAgain.addActionListener(event -> edited.run());
        comment.getDocument().addUndoableEditListener(event -> edited.run());
    }

    void showRating(RateableBuddyRow row) {
        if (row != null && row.isAlreadyRated()) {
            stars.setSelectedItem(row.getExistingScore());
            meetAgain.setSelected(row.isExistingWouldMeetAgain());
            comment.setText(row.getExistingComment());
        }
        else {
            stars.setSelectedIndex(-1);
            meetAgain.setSelected(false);
            comment.setText("");
        }
        comment.setCaretPosition(0);
    }

    void enableInputs(boolean enabled) {
        stars.setEnabled(enabled);
        meetAgain.setEnabled(enabled);
        comment.setEnabled(enabled);
    }

    boolean hasScore() {
        return stars.getSelectedIndex() >= 0;
    }

    Runnable submission(RateBuddyController controller, String userId, RateableBuddyRow row) {
        final int score = (Integer) stars.getSelectedItem();
        final boolean again = meetAgain.isSelected();
        final String draft = comment.getText();
        return () -> controller.execute(userId, row.getBuddyUserId(), row.getEventId(), score, again, draft);
    }
}
