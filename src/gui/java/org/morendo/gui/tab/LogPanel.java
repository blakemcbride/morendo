package org.morendo.gui.tab;

import org.morendo.gui.JamochaGui;
import org.morendo.gui.icons.IconLoader;
import org.morendo.messagerouter.InterestType;
import org.morendo.messagerouter.MessageEvent;
import org.morendo.messagerouter.StringChannel;
import org.morendo.rete.Function;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial") // Swing components are never serialized here
public final class LogPanel extends AbstractJamochaPanel
        implements ActionListener, ListSelectionListener {

    private JSplitPane pane;

    private JTextArea detailView;

    private JTable logTable;

    private JButton clearButton;

    private LogTableModel dataModel = new LogTableModel();

    private LogTableCellRenderer cellRenderer;

    private StringChannel logChannel;

    private boolean running = true;

    public LogPanel(JamochaGui gui) {
        super(gui);
        setLayout(new BorderLayout());
        logChannel = gui.getEngine().getMessageRouter().openChannel("gui_log", InterestType.ALL);
        detailView = new JTextArea();
        detailView.setEditable(false);
        detailView.setFont(new Font("Courier", Font.PLAIN, 12));
        cellRenderer = new LogTableCellRenderer();
        logTable =
                new JTable(dataModel) {

                    public TableCellRenderer getCellRenderer(int row, int column) {
                        return cellRenderer;
                    }
                };
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.getSelectionModel().addListSelectionListener(this);
        pane =
                new JSplitPane(
                        JSplitPane.VERTICAL_SPLIT,
                        new JScrollPane(logTable),
                        new JScrollPane(detailView));
        pane.setDividerLocation(gui.getPreferences().getInt("log.dividerlocation", 300));
        add(pane, BorderLayout.CENTER);

        Thread logThread =
                new Thread("morendo-gui-log") {
                    public void run() {
                        List<MessageEvent> msgEvents = new LinkedList<>();
                        while (running) {
                            logChannel.fillEventList(msgEvents);
                            if (!msgEvents.isEmpty()) {
                                dataModel.addEvents(msgEvents);
                                msgEvents.clear();
                            } else {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    // Can be ignored
                                }
                            }
                        }
                        LogPanel.this.gui.getEngine().getMessageRouter().closeChannel(logChannel);
                    }
                };
        logThread.setDaemon(true);
        logThread.start();
        clearButton = new JButton("Clear Log", IconLoader.getImageIcon("monitor"));
        clearButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    public void close() {
        running = false;
        gui.getPreferences().putInt("log.dividerlocation", pane.getDividerLocation());
    }

    public void settingsChanged() {}

    private final class LogMessageEvent extends MessageEvent {

        private Calendar datetime = Calendar.getInstance();

        private String typeFormatted;

        private int superType;

        // public static final int TYPE_EVENT = 1; Unused

        public static final int TYPE_WARNING = 2;

        public static final int TYPE_ERROR = 3;

        public LogMessageEvent(MessageEvent event) {
            this(event.getType(), event.getMessage(), event.getChannelId());
        }

        public LogMessageEvent(MessageEvent.Type type, Object message, String channelId) {
            super(type, message, channelId);
            switch (type) {
                case COMMAND -> {
                    typeFormatted = "EVENT: incoming Command";
                    superType = 1;
                }
                case RESULT -> {
                    typeFormatted = "EVENT: returned result";
                    superType = 1;
                }
                case ENGINE -> {
                    typeFormatted = "EVENT: Engine-Message";
                    superType = 1;
                }
                case PARSE_ERROR -> {
                    typeFormatted = "ERROR: Parse-Error";
                    superType = 3;
                }
                case ERROR -> {
                    typeFormatted = "ERROR: unspecified Error";
                    superType = 3;
                }
            }
        }

        public int getSuperType() {
            return superType;
        }

        public String getDatetimeFormatted() {
            StringBuilder res = new StringBuilder();
            res.append(datetime.get(Calendar.YEAR) + "/");
            res.append(
                    ((datetime.get(Calendar.MONTH) + 1 > 9) ? "" : "0")
                            + (datetime.get(Calendar.MONTH) + 1)
                            + "/");
            res.append(
                    ((datetime.get(Calendar.DAY_OF_MONTH) > 9) ? "" : "0")
                            + datetime.get(Calendar.DAY_OF_MONTH)
                            + " - ");
            res.append(
                    ((datetime.get(Calendar.HOUR_OF_DAY) > 9) ? "" : "0")
                            + datetime.get(Calendar.HOUR_OF_DAY)
                            + ":");
            res.append(
                    ((datetime.get(Calendar.MINUTE) > 9) ? "" : "0")
                            + datetime.get(Calendar.MINUTE)
                            + ":");
            res.append(
                    ((datetime.get(Calendar.SECOND) > 9) ? "" : "0")
                            + datetime.get(Calendar.SECOND));
            return res.toString();
        }

        public String getTypeFormatted() {
            return typeFormatted;
        }
    }

    private final class LogTableCellRenderer extends DefaultTableCellRenderer {

        private Color colorError = Color.RED;

        private Color colorWarning = Color.ORANGE;

        private Color colorEvent = Color.BLUE;

        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            JComponent returnComponent =
                    (JComponent)
                            super.getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);
            switch (((LogTableModel) table.getModel()).getRow(row).getSuperType()) {
                case LogMessageEvent.TYPE_ERROR:
                    setForeground(colorError);
                    break;
                case LogMessageEvent.TYPE_WARNING:
                    setForeground(colorWarning);
                    break;
                default:
                    setForeground(colorEvent);
                    break;
            }
            return returnComponent;
        }
    }

    private final class LogTableModel extends AbstractTableModel {

        private List<LogMessageEvent> events = new LinkedList<>();

        private int maxEventCount = 1000;

        private void addEvents(List<MessageEvent> events) {

            logTable.getColumnModel().getColumn(0).setPreferredWidth(180);
            logTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            logTable.getColumnModel().getColumn(2).setPreferredWidth(logTable.getWidth() - 280);
            for (MessageEvent event : events) {
                this.events.add(new LogMessageEvent(event));
            }
            while (this.events.size() > maxEventCount) {
                this.events.remove(0);
            }
            fireTableDataChanged();
        }

        private void clearEvents() {
            events.clear();
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Date - Time";
                case 1:
                    return "Channel";
                case 2:
                    return "Message-Type";
                default:
                    return null;
            }
        }

        public int getColumnCount() {
            return 3;
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Class<String> getColumnClass(int aColumn) {
            return java.lang.String.class;
        }

        public int getRowCount() {
            return events.size();
        }

        public LogMessageEvent getRow(int row) {
            return events.get(events.size() - (row + 1));
        }

        public Object getValueAt(int row, int column) {
            LogMessageEvent event = getRow(row);
            if (event != null) {
                switch (column) {
                    case 0:
                        return event.getDatetimeFormatted();
                    case 1:
                        return event.getChannelId();
                    case 2:
                        return event.getTypeFormatted();
                }
            }
            return null;
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == clearButton) {
            dataModel.clearEvents();
            detailView.setText("");
        }
    }

    public void valueChanged(ListSelectionEvent arg0) {
        if (arg0.getSource() == logTable.getSelectionModel()) {
            StringBuilder buffer = new StringBuilder();
            if (logTable.getSelectedRow() > -1) {
                LogMessageEvent event = dataModel.getRow(logTable.getSelectedRow());
                buffer.append(
                        "Date-Time:    "
                                + event.getDatetimeFormatted()
                                + "\nChannel:      "
                                + event.getChannelId()
                                + "\nMessage-Type: "
                                + event.getTypeFormatted()
                                + "\n\nMessage:\n========\n");
                Object message = event.getMessage();
                if (message instanceof Exception ex) {
                    StackTraceElement[] str = ex.getStackTrace();
                    buffer.append(ex.getClass().getName() + ": " + ex.getMessage());
                    for (StackTraceElement strelem : str) {
                        buffer.append("\n" + strelem);
                    }
                } else if (message instanceof Function function) {
                    buffer.append("(" + (function).getName() + ")");
                } else {
                    buffer.append(message.toString());
                }
            }
            detailView.setText(buffer.toString());
        }
    }
}
