package be.howest.sooa.o5.gui;

import be.howest.sooa.o5.data.BookingRepository;
import be.howest.sooa.o5.data.RoomOptionRepository;
import be.howest.sooa.o5.data.RoomTypeRepository;
import be.howest.sooa.o5.domain.Booking;
import be.howest.sooa.o5.domain.BookingValidation;
import be.howest.sooa.o5.domain.RoomOption;
import be.howest.sooa.o5.domain.RoomType;
import be.howest.sooa.o5.ex.DBException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Hayk
 */
public class MainFrame extends javax.swing.JFrame {

    private transient BookingRepository bookingRepo;
    private transient RoomOptionRepository roomOptionRepo;
    private transient RoomTypeRepository roomTypeRepo;

    private transient Booking booking;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }

    public void confirmAuthentication() {
        bookingRepo = new BookingRepository();
        roomOptionRepo = new RoomOptionRepository();
        roomTypeRepo = new RoomTypeRepository();
        init();
        addListeners();
    }

    private void init() {
        booking = new Booking();
        fillRoomTypes();
        fillRoomOptions();
        updatePrice();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Listeners">
    private void addListeners() {
        roomTypesList.addItemListener(new RoomTypeItemListener(this));
        addBookingButtonActionListener();
        addCustomerFieldActionListener();
        addOptionListsListeners();
        exitButton.addActionListener((ActionEvent e) -> {
            close();
        });
    }
    
    private void addBookingButtonActionListener() {
        addBookingButton.addActionListener((ActionEvent e) -> {
            booking.setName(customerNameField.getText().trim());
            BookingValidation validation = new BookingValidation(booking);
            if (validation.isValid()) {
                save(booking);
            } else {
                showWarning(validation.getMessage());
            }
        });
    }
    
    private void addCustomerFieldActionListener() {
        customerNameField.addActionListener((ActionEvent e) -> {
            addBookingButton.doClick();
        });
    }
    
    private void addOptionListsListeners() {
        availableOptionsList.addListSelectionListener(
                new RoomOptionSelectionListener(this, addOptionsButton,
                        availableOptionsList, selectedOptionsList));
        selectedOptionsList.addListSelectionListener(
                new RoomOptionSelectionListener(this, removeOptionsButton,
                        selectedOptionsList, availableOptionsList));
    }

    public void addDialogKeyListener(JDialog dialog) {
        KeyStroke escapeStroke
                = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        String dispatchWindowClosingActionMapKey
                = "com.spodding.tackline.dispatch:WINDOW_CLOSING";
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                escapeStroke, dispatchWindowClosingActionMapKey);
        root.getActionMap().put(dispatchWindowClosingActionMapKey,
                new DialogClosingOnEscapeAction(dialog));
    }
    // </editor-fold>
    //
    // <editor-fold defaultstate="collapsed" desc="Fill and Reset">
    private void fillRoomTypes() {
        DefaultComboBoxModel<RoomType> model = new DefaultComboBoxModel<>();
        model.addElement(null);
        roomTypeRepo.findAll().forEach((roomType) -> {
            model.addElement(roomType);
        });
        roomTypesList.setModel(model);
    }

    private void fillRoomOptions() {
        RoomOptionListModel model = new RoomOptionListModel();
        roomOptionRepo.findAll().forEach((roomOption) -> {
            model.addElement(roomOption);
        });
        availableOptionsList.setModel(model);
    }
    
    private void reset() {
        selectedOptionsList.setModel(new DefaultListModel());
        customerNameField.setText("");
        init();
    }
    // </editor-fold>
    //
    // <editor-fold defaultstate="collapsed" desc="Data Manipulation">
    private void save(Booking booking) {
        try {
            long lastInsertId = bookingRepo.save(booking);
            Booking _booking = bookingRepo.read(lastInsertId);
            String message = String.format("Booking #%d is registered with"
                    + " %d option(s) for a total price of %s for %s",
                    _booking.getId(), _booking.getOptionCount(),
                    _booking.getFormattedPrice(), _booking.getName());
            JOptionPane.showMessageDialog(this, message, "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            reset();
        } catch (DBException ex) {
            showWarning(ex.getMessage());
        }
    }
    // </editor-fold>
    //
    // <editor-fold defaultstate="collapsed" desc="Custom Functions">
    private void centerScreen(Window window) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - window.getWidth()) / 2;
        final int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    private void centerScreen() {
        centerScreen(this);
    }

    private void connectToDatabase() {
        DatabaseConnectionDialog dialog = new DatabaseConnectionDialog(this);
        centerScreen(dialog);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (!isConnected()) {
                    close();
                }
            }
        });
        dialog.setVisible(true);
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConnected() {
        return bookingRepo != null && roomOptionRepo != null
                && roomTypeRepo != null;
    }

    public Set<RoomOption> buildRoomOptions() {
        Set<RoomOption> roomOptions = new TreeSet<>();
        ListModel<RoomOption> model = selectedOptionsList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            roomOptions.add(model.getElementAt(i));
        }
        return roomOptions;
    }

    private void updatePrice() {
        RoomOptionListModel model
                = new RoomOptionListModel(selectedOptionsList.getModel());
        booking.setRoomOptions(model.getElements());
        basePrice.setText(booking.getFormattedRoomPrice());
        totalPrice.setText(booking.getFormattedPrice());

    }

    // </editor-fold>
    //
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        roomTypeLabel = new javax.swing.JLabel();
        roomTypesList = new javax.swing.JComboBox<>();
        basePriceLabel = new javax.swing.JLabel();
        basePrice = new javax.swing.JLabel();
        availableOptionsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        availableOptionsList = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedOptionsList = new javax.swing.JList<>();
        selectedOptionsLabel = new javax.swing.JLabel();
        addOptionsButton = new javax.swing.JButton();
        removeOptionsButton = new javax.swing.JButton();
        totalPriceLabel = new javax.swing.JLabel();
        totalPrice = new javax.swing.JLabel();
        customerNameLabel = new javax.swing.JLabel();
        customerNameField = new javax.swing.JTextField();
        addBookingButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        titleLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        titleLabel.setText("Howest Hotel Boeking Systeem");

        roomTypeLabel.setText("Room Type:");

        basePriceLabel.setText("Base price:");
        basePriceLabel.setToolTipText("");

        basePrice.setText("0");
        basePrice.setToolTipText("");

        availableOptionsLabel.setText("Available Options:");

        jScrollPane1.setViewportView(availableOptionsList);

        jScrollPane2.setViewportView(selectedOptionsList);

        selectedOptionsLabel.setText("Selected Options");

        addOptionsButton.setText("Add >");
        addOptionsButton.setEnabled(false);

        removeOptionsButton.setText("< Remove");
        removeOptionsButton.setActionCommand("> Remove");
        removeOptionsButton.setEnabled(false);

        totalPriceLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalPriceLabel.setText("Total price, including selected options:");
        totalPriceLabel.setToolTipText("");

        totalPrice.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalPrice.setText("0");
        totalPrice.setToolTipText("");

        customerNameLabel.setText("Customer Name:");

        addBookingButton.setText("Add booking");

        exitButton.setText("Exit");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customerNameField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(availableOptionsLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(addOptionsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(removeOptionsButton))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(selectedOptionsLabel)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(totalPriceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(totalPrice))
                            .addComponent(customerNameLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(roomTypesList, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(basePriceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(basePrice))
                            .addComponent(titleLabel)
                            .addComponent(roomTypeLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addBookingButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exitButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addGap(38, 38, 38)
                .addComponent(roomTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roomTypesList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(basePriceLabel)
                    .addComponent(basePrice))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(availableOptionsLabel)
                            .addComponent(selectedOptionsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addOptionsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeOptionsButton)))))
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPriceLabel)
                    .addComponent(totalPrice))
                .addGap(18, 18, 18)
                .addComponent(customerNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customerNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addBookingButton)
                    .addComponent(exitButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            System.out.println(ex.getMessage());
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame newMainFrame = new MainFrame();
            newMainFrame.centerScreen();
            newMainFrame.setVisible(true);
            newMainFrame.connectToDatabase();
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBookingButton;
    private javax.swing.JButton addOptionsButton;
    private javax.swing.JLabel availableOptionsLabel;
    private javax.swing.JList<RoomOption> availableOptionsList;
    private javax.swing.JLabel basePrice;
    private javax.swing.JLabel basePriceLabel;
    private javax.swing.JTextField customerNameField;
    private javax.swing.JLabel customerNameLabel;
    private javax.swing.JButton exitButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton removeOptionsButton;
    private javax.swing.JLabel roomTypeLabel;
    private javax.swing.JComboBox<RoomType> roomTypesList;
    private javax.swing.JLabel selectedOptionsLabel;
    private javax.swing.JList<RoomOption> selectedOptionsList;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel totalPrice;
    private javax.swing.JLabel totalPriceLabel;
    // End of variables declaration//GEN-END:variables

    // <editor-fold defaultstate="collapsed" desc="Custom Listeners">
    private static class RoomOptionListModel extends AbstractListModel {

        Set<RoomOption> elements = new TreeSet<>();

        RoomOptionListModel() {

        }

        RoomOptionListModel(ListModel model) {
            for (int i = 0; i < model.getSize(); i++) {
                this.elements.add((RoomOption) model.getElementAt(i));
            }
        }

        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public RoomOption getElementAt(int index) {
            return elements.toArray(new RoomOption[getSize()])[index];
        }

        public Set<RoomOption> getElements() {
            return Collections.unmodifiableSet(elements);
        }

        void addElement(RoomOption roomOption) {
            elements.add(roomOption);
        }

        void addAllElements(List<RoomOption> roomOptions) {
            elements.addAll(roomOptions);
        }

        void removeElement(RoomOption roomOption) {
            elements.remove(roomOption);
        }

        void removeAllElements(List<RoomOption> roomOptions) {
            elements.removeAll(roomOptions);
        }
    }

    private static class RoomOptionSelectionListener implements ListSelectionListener {

        final MainFrame frame;
        final JButton button;
        final JList list;
        final JList siblingList;

        RoomOptionSelectionListener(MainFrame frame, JButton button,
                JList list, JList siblingList) {
            this.frame = frame;
            this.button = button;
            this.list = list;
            this.siblingList = siblingList;
            addListeners();
        }

        final void addListeners() {
            button.addActionListener((ActionEvent e) -> {
                List<RoomOption> selected = list.getSelectedValuesList();
                RoomOptionListModel model = new RoomOptionListModel(list.getModel());
                RoomOptionListModel siblingModel = new RoomOptionListModel(siblingList.getModel());
                model.removeAllElements(selected);
                siblingModel.addAllElements(selected);
                list.setModel(model);
                siblingList.setModel(siblingModel);
                frame.updatePrice();
                button.setEnabled(false);
            });
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                button.setEnabled(list.getSelectedValuesList().size() > 0);
            }
        }
    }

    private static class DialogClosingOnEscapeAction extends AbstractAction {

        final JDialog dialog;

        DialogClosingOnEscapeAction(JDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.dispatchEvent(new WindowEvent(
                    dialog, WindowEvent.WINDOW_CLOSING));
        }
    }

    private static class RoomTypeItemListener implements ItemListener {

        final MainFrame frame;

        RoomTypeItemListener(MainFrame frame) {
            this.frame = frame;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            RoomType roomType = (RoomType) frame.roomTypesList.getSelectedItem();
            frame.booking.setRoomType(roomType);
            frame.updatePrice();
        }
    }
    // </editor-fold>
}
