package view;

import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import interface_adapter.buddy_rating.BuddyRatingState.RateableBuddyRow;

/**
 * Read-only list of buddy and event pairs eligible for a rating.
 */
final class BuddyRatingTable extends JTable {

    private static final String[] HEADINGS = {"Buddy", "Shared event", "Starts", "Your rating"};
    private List<RateableBuddyRow> rows = Collections.emptyList();

    BuddyRatingTable() {
        super(new DefaultTableModel(HEADINGS, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);
        getAccessibleContext().setAccessibleName("Buddies from shared events");
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    RateableBuddyRow selectedBuddy() {
        RateableBuddyRow selected = null;
        final int index = getSelectedRow();
        if (index >= 0 && index < rows.size()) {
            selected = rows.get(index);
        }
        return selected;
    }

    boolean replaceRows(List<RateableBuddyRow> items) {
        final RateableBuddyRow previous = selectedBuddy();
        final DefaultTableModel model = (DefaultTableModel) getModel();
        clearSelection();
        model.setRowCount(0);
        rows = items;
        int selection = -1;
        for (final RateableBuddyRow row : rows) {
            if (previous != null && previous.getEventId().equals(row.getEventId())
                    && previous.getBuddyUserId().equals(row.getBuddyUserId())) {
                selection = model.getRowCount();
            }
            String rating = "Unrated";
            if (row.isAlreadyRated()) {
                rating = row.getExistingScore() + " / 5";
            }
            model.addRow(new Object[] {row.getBuddyDisplayName(), row.getEventTitle(), row.getEventTimeDisplay(),
                rating});
        }
        if (!rows.isEmpty()) {
            final int index = Math.max(0, selection);
            setRowSelectionInterval(index, index);
        }
        return selection >= 0;
    }
}
