/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.file.Files;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ScheduledExecutorService;
import java.text.SimpleDateFormat;

/**
 * Enhanced Todo List Application with comprehensive features
 * @author Apon
 */
public class TodoListApp extends javax.swing.JFrame {
    
    // Task priority levels
    public enum TaskPriority {
        LOW("Low", new Color(46, 125, 50)),      // Green
        MEDIUM("Medium", new Color(255, 152, 0)), // Orange
        HIGH("High", new Color(244, 67, 54)),     // Red
        URGENT("Urgent", new Color(156, 39, 176)); // Purple
        
        private final String name;
        private final Color color;
        
        TaskPriority(String name, Color color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() { return name; }
        public Color getColor() { return color; }
    }
    
    // Enhanced Task class with all new features
    public static class Task {
        private String text;
        private boolean completed;
        private TaskPriority priority;
        private java.util.Date dueDate;
        private String category;
        private List<String> tags;
        private long timeSpent; // in milliseconds
        private java.util.Date createdDate;
        private boolean recurring;
        private String recurrencePattern;
        
        public Task(String text) {
            this.text = text;
            this.completed = false;
            this.priority = TaskPriority.MEDIUM;
            this.dueDate = null;
            this.category = "General";
            this.tags = new ArrayList<>();
            this.timeSpent = 0;
            this.createdDate = new java.util.Date();
            this.recurring = false;
            this.recurrencePattern = "";
        }
        
        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }
        public java.util.Date getDueDate() { return dueDate; }
        public void setDueDate(java.util.Date dueDate) { this.dueDate = dueDate; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public long getTimeSpent() { return timeSpent; }
        public void setTimeSpent(long timeSpent) { this.timeSpent = timeSpent; }
        public java.util.Date getCreatedDate() { return createdDate; }
        public void setCreatedDate(java.util.Date createdDate) { this.createdDate = createdDate; }
        public boolean isRecurring() { return recurring; }
        public void setRecurring(boolean recurring) { this.recurring = recurring; }
        public String getRecurrencePattern() { return recurrencePattern; }
        public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }
        
        public boolean isOverdue() {
            if (dueDate == null || completed) return false;
            return new java.util.Date().after(dueDate);
        }
        
        public boolean isDueSoon() {
            if (dueDate == null || completed) return false;
            long dayInMillis = 24 * 60 * 60 * 1000;
            return dueDate.getTime() - System.currentTimeMillis() <= dayInMillis;
        }
    }
    
    // Undo/Redo system
    private static class UndoRedoAction {
        String type;
        Object data;
        
        UndoRedoAction(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }
    
    // Data structures to hold enhanced tasks
    private final Map<String, List<Task>> eventTasks;
    private final Map<String, List<Task>> eventCompletedTasks;
    private final Map<String, String> eventDates;
    private final Map<String, String> eventCategories;
    private DefaultListModel<String> eventListModel;
    private java.util.Date selectedDate;
    
    // Data storage directory
    private final File dataDirectory;
    
    // New UI enhancement fields
    private boolean isDarkMode = false;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private JProgressBar overallProgressBar;
    private JLabel statsLabel;
    private Timer autoSaveTimer;
    private Stack<UndoRedoAction> undoStack;
    private Stack<UndoRedoAction> redoStack;
    private ScheduledExecutorService notificationScheduler;
    
    // Quick action components
    private JButton newEventButton;
    private JButton newTaskButton;
    private JButton undoButton;
    private JButton redoButton;
    private JButton darkModeToggle;
    private ImageIcon darkModeIcon;
    private ImageIcon lightModeIcon;
    private JButton statsButton;
    private JButton exportButton;
    private JButton importButton;
    private JToolBar toolbar;
    private JPanel searchPanel;
    private JPanel progressPanel;
    private JPanel bottomPanel;
    private JLabel searchLabel;
    private JLabel filterLabel;
    private JLabel progressLabel;

    /**
     * Creates new form TodoListApp with enhanced features
     */
    public TodoListApp() {
        eventTasks = new HashMap<>();
        eventCompletedTasks = new HashMap<>();
        eventDates = new HashMap<>();
        eventCategories = new HashMap<>();
        selectedDate = new java.util.Date(); // Initialize with current date
        
        // Initialize data directory
        dataDirectory = initializeDataDirectory();
        
        // Initialize new UI enhancement components
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        
        initComponents();
        setupCustomComponents();
        setupEventListeners();
        loadEventsFromFiles();
    }
    
    /**
     * Initialize and create the data directory for storing todo list files
     * @return File object representing the data directory
     */
    private File initializeDataDirectory() {
        // Get user's Documents folder
        String userHome = System.getProperty("user.home");
        File documentsDir = new File(userHome, "Documents");
        File todoDataDir = new File(documentsDir, "To Do List");
        
        // Create directory if it doesn't exist
        if (!todoDataDir.exists()) {
            boolean created = todoDataDir.mkdirs();
            if (created) {
                System.out.println("Created data directory: " + todoDataDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create data directory: " + todoDataDir.getAbsolutePath());
                // Fallback to user home directory
                todoDataDir = new File(userHome, "To Do List");
                todoDataDir.mkdirs();
            }
        }
        
        return todoDataDir;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        eventPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        eventList = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        eventNameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        eventDateSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerDateModel());
        eventDateButton = new javax.swing.JButton();
        addEventButton = new javax.swing.JButton();
        taskPanel = new javax.swing.JPanel();
        eventInfoPanel = new javax.swing.JPanel();
        selectedEventTitle = new javax.swing.JLabel();
        selectedEventDate = new javax.swing.JLabel();
        jSplitPane2 = new javax.swing.JSplitPane();
        todoScrollPane = new javax.swing.JScrollPane();
        todoPanel = new javax.swing.JPanel();
        completedScrollPane = new javax.swing.JScrollPane();
        completedPanel = new javax.swing.JPanel();
        taskControlsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        taskField = new javax.swing.JTextField();
        addTaskButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Todo List Manager");

        eventPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Events"));

        eventList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(eventList);

        jLabel1.setText("Event Name:");

        jLabel2.setText("Event Date:");

        addEventButton.setText("Add Event");
        addEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEventButtonActionPerformed(evt);
            }
        });

        // Configure the date button
        eventDateButton.setText("Select Date");
        eventDateButton.addActionListener(e -> openCalendarDialog());
        
        // Hide the spinner since we're using the button
        eventDateSpinner.setVisible(false);

        javax.swing.GroupLayout eventPanelLayout = new javax.swing.GroupLayout(eventPanel);
        eventPanel.setLayout(eventPanelLayout);
        eventPanelLayout.setHorizontalGroup(
            eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
            .addGroup(eventPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventNameField)
                    .addComponent(eventDateButton)
                    .addComponent(addEventButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(eventPanelLayout.createSequentialGroup()
                        .addGroup(eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        eventPanelLayout.setVerticalGroup(
            eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventDateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addEventButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(eventPanel);

        selectedEventTitle.setText("Event: No event selected");

        selectedEventDate.setText("Date:");

        javax.swing.GroupLayout eventInfoPanelLayout = new javax.swing.GroupLayout(eventInfoPanel);
        eventInfoPanel.setLayout(eventInfoPanelLayout);
        eventInfoPanelLayout.setHorizontalGroup(
            eventInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedEventTitle)
                .addGap(18, 18, 18)
                .addComponent(selectedEventDate)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        eventInfoPanelLayout.setVerticalGroup(
            eventInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(eventInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectedEventTitle)
                    .addComponent(selectedEventDate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);

        todoScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Todo List"));

        todoPanel.setLayout(new javax.swing.BoxLayout(todoPanel, javax.swing.BoxLayout.Y_AXIS));
        todoScrollPane.setViewportView(todoPanel);

        jSplitPane2.setTopComponent(todoScrollPane);

        completedScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Completed List"));

        completedPanel.setLayout(new javax.swing.BoxLayout(completedPanel, javax.swing.BoxLayout.Y_AXIS));
        completedScrollPane.setViewportView(completedPanel);

        jSplitPane2.setBottomComponent(completedScrollPane);

        jLabel3.setText("New Task:");

        addTaskButton.setText("Add Task");
        addTaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTaskButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout taskControlsPanelLayout = new javax.swing.GroupLayout(taskControlsPanel);
        taskControlsPanel.setLayout(taskControlsPanelLayout);
        taskControlsPanelLayout.setHorizontalGroup(
            taskControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addTaskButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        taskControlsPanelLayout.setVerticalGroup(
            taskControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(taskControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(taskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addTaskButton)
                    .addComponent(saveButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout taskPanelLayout = new javax.swing.GroupLayout(taskPanel);
        taskPanel.setLayout(taskPanelLayout);
        taskPanelLayout.setHorizontalGroup(
            taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eventInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane2)
            .addComponent(taskControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        taskPanelLayout.setVerticalGroup(
            taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskPanelLayout.createSequentialGroup()
                .addComponent(eventInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(taskPanel);

        // Use BorderLayout to accommodate toolbar
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jSplitPane1, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void addEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventButtonActionPerformed
        addEvent();
    }//GEN-LAST:event_addEventButtonActionPerformed

    private void deleteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEventButtonActionPerformed
        deleteEvent();
    }//GEN-LAST:event_deleteEventButtonActionPerformed

    private void addTaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTaskButtonActionPerformed
        addTask();
    }//GEN-LAST:event_addTaskButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveCurrentEvent();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openCalendarDialog() {
        // Create a custom calendar dialog
        JDialog calendarDialog = new JDialog(this, "Select Date", true);
        calendarDialog.setSize(350, 400);
        calendarDialog.setLocationRelativeTo(this);
        
        // Create calendar components
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(selectedDate);
        
        // Month and Year selection
        JPanel topPanel = new JPanel(new FlowLayout());
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        
        SpinnerNumberModel yearModel = new SpinnerNumberModel(calendar.get(java.util.Calendar.YEAR), 1900, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        
        // Remove comma from year display
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(yearEditor);
        
        monthCombo.setSelectedIndex(calendar.get(java.util.Calendar.MONTH));
        
        JLabel monthLabel = new JLabel("Month:");
        JLabel yearLabel = new JLabel("Year:");
        
        topPanel.add(monthLabel);
        topPanel.add(monthCombo);
        topPanel.add(yearLabel);
        topPanel.add(yearSpinner);
        
        // Calendar grid
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Day headers
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayHeaders) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            calendarPanel.add(label);
        }
        
        // Function to update calendar
        Runnable updateCalendar = () -> {
            // Remove existing day components (keep headers)
            Component[] components = calendarPanel.getComponents();
            for (int i = 7; i < components.length; i++) {
                calendarPanel.remove(components[i]);
            }
            
            int month = monthCombo.getSelectedIndex();
            int year = (Integer) yearSpinner.getValue();
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            
            int startDay = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            
            // Add empty cells for days before month starts
            for (int i = 0; i < startDay; i++) {
                calendarPanel.add(new JLabel(""));
            }
            
            // Add day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setPreferredSize(new Dimension(40, 30));
                
                // Highlight selected date
                java.util.Calendar selectedCal = java.util.Calendar.getInstance();
                selectedCal.setTime(selectedDate);
                if (selectedCal.get(java.util.Calendar.YEAR) == year &&
                    selectedCal.get(java.util.Calendar.MONTH) == month &&
                    selectedCal.get(java.util.Calendar.DAY_OF_MONTH) == day) {
                    dayButton.setBackground(Color.BLUE);
                    dayButton.setForeground(Color.WHITE);
                    dayButton.setOpaque(true);
                }
                
                final int selectedDay = day;
                dayButton.addActionListener(e -> {
                    java.util.Calendar newDate = java.util.Calendar.getInstance();
                    newDate.set(year, month, selectedDay);
                    selectedDate = newDate.getTime();
                    
                    // Update button text to show selected date in DD/MM/YYYY format
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    eventDateButton.setText(sdf.format(selectedDate));
                    
                    calendarDialog.dispose();
                });
                
                calendarPanel.add(dayButton);
            }
            
            calendarPanel.revalidate();
            calendarPanel.repaint();
        };
        
        // Add listeners for month/year changes
        monthCombo.addActionListener(e -> updateCalendar.run());
        yearSpinner.addChangeListener(e -> updateCalendar.run());
        
        // Initial calendar setup
        updateCalendar.run();
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(e -> {
            selectedDate = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            eventDateButton.setText(sdf.format(selectedDate));
            calendarDialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> calendarDialog.dispose());
        
        bottomPanel.add(todayButton);
        bottomPanel.add(cancelButton);
        
        // Layout dialog
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.add(topPanel, BorderLayout.NORTH);
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.add(bottomPanel, BorderLayout.SOUTH);
        
        calendarDialog.setVisible(true);
    }

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TodoListApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TodoListApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TodoListApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TodoListApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TodoListApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEventButton;
    private javax.swing.JButton addTaskButton;
    private javax.swing.JPanel completedPanel;
    private javax.swing.JScrollPane completedScrollPane;
    private javax.swing.JButton eventDateButton;
    private javax.swing.JSpinner eventDateSpinner;
    private javax.swing.JPanel eventInfoPanel;
    private javax.swing.JList<String> eventList;
    private javax.swing.JTextField eventNameField;
    private javax.swing.JPanel eventPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel selectedEventDate;
    private javax.swing.JLabel selectedEventTitle;
    private javax.swing.JPanel taskControlsPanel;
    private javax.swing.JTextField taskField;
    private javax.swing.JPanel taskPanel;
    private javax.swing.JPanel todoPanel;
    private javax.swing.JScrollPane todoScrollPane;
    // End of variables declaration//GEN-END:variables

    private void setupCustomComponents() {
        // Initialize the list model and set it to the event list
        eventListModel = new DefaultListModel<>();
        eventList.setModel(eventListModel);
        
        // Set custom cell renderer for events with delete buttons
        eventList.setCellRenderer(new EventListCellRenderer());
        
        // Set larger row height for better appearance
        eventList.setFixedCellHeight(45);
        
        // Apply basic font improvements (NetBeans compatible)
        applyBasicFonts();
        
        // Add Enter key listeners for text fields
        setupEnterKeyListeners();
        
        // Configure scroll panes for better behavior
        todoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        todoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        completedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        completedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Set white background for the panels and scroll panes
        todoPanel.setBackground(Color.WHITE);
        completedPanel.setBackground(Color.WHITE);
        todoScrollPane.setBackground(Color.WHITE);
        completedScrollPane.setBackground(Color.WHITE);
        todoScrollPane.getViewport().setBackground(Color.WHITE);
        completedScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Set scroll unit increments for smoother scrolling
        todoScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        completedScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Add enhanced UI components
        setupEnhancedUI();
    }
    
    private void setupEnhancedUI() {
        // Add toolbar with quick actions
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // New Event button
        newEventButton = new JButton("📅 New Event");
        newEventButton.setToolTipText("Create new event (Ctrl+N)");
        newEventButton.addActionListener(e -> {
            eventNameField.requestFocus();
        });
        
        // New Task button
        newTaskButton = new JButton("📝 New Task");
        newTaskButton.setToolTipText("Create new task (Ctrl+T)");
        newTaskButton.addActionListener(e -> {
            taskField.requestFocus();
        });
        
        // Undo button
        undoButton = new JButton("↶ Undo");
        undoButton.setToolTipText("Undo last action (Ctrl+Z)");
        undoButton.addActionListener(e -> performUndo());
        
        // Redo button
        redoButton = new JButton("↷ Redo");
        redoButton.setToolTipText("Redo last action (Ctrl+Y)");
        redoButton.addActionListener(e -> performRedo());
        
        // Dark mode toggle with images
        try {
            // Load and scale images to proper size
            ImageIcon originalDarkIcon = new ImageIcon(getClass().getClassLoader().getResource("images/switch-dark-mode.png"));
            ImageIcon originalLightIcon = new ImageIcon(getClass().getClassLoader().getResource("images/switch-light-mode.png"));
            
            // Scale icons to 24x24 for better fit
            Image darkImg = originalDarkIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            Image lightImg = originalLightIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            
            darkModeIcon = new ImageIcon(darkImg);
            lightModeIcon = new ImageIcon(lightImg);
            
            darkModeToggle = new JButton(isDarkMode ? lightModeIcon : darkModeIcon);
            darkModeToggle.setToolTipText("Toggle dark/light theme");
            darkModeToggle.setPreferredSize(new Dimension(32, 32));
            darkModeToggle.setBorderPainted(false);
            darkModeToggle.setContentAreaFilled(false);
            darkModeToggle.setFocusPainted(false);
            darkModeToggle.setHorizontalAlignment(SwingConstants.CENTER);
            darkModeToggle.setVerticalAlignment(SwingConstants.CENTER);
        } catch (Exception e) {
            // Fallback to text if images not found
            darkModeToggle = new JButton("🌙 Dark Mode");
            darkModeToggle.setToolTipText("Toggle dark/light theme");
        }
        darkModeToggle.addActionListener(e -> toggleDarkMode());
        
        // Stats button
        statsButton = new JButton("📊 Stats");
        statsButton.setToolTipText("Show statistics");
        statsButton.addActionListener(e -> showStatistics());
        
        // Export button
        exportButton = new JButton("💾 Export");
        exportButton.setToolTipText("Export data");
        exportButton.addActionListener(e -> exportData());
        
        // Import button
        importButton = new JButton("📂 Import");
        importButton.setToolTipText("Import data");
        importButton.addActionListener(e -> importData());
        
        toolbar.add(newEventButton);
        toolbar.add(newTaskButton);
        toolbar.addSeparator();
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.addSeparator();
        toolbar.add(statsButton);
        toolbar.add(exportButton);
        toolbar.add(importButton);
        toolbar.addSeparator();
        toolbar.add(darkModeToggle);
        
        // Add search functionality
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchLabel = new JLabel("Search:");
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(15);
        searchField.setToolTipText("Search tasks and events");
        searchField.addActionListener(e -> performSearch());
        
        filterComboBox = new JComboBox<>(new String[]{"All", "High Priority", "Overdue", "Due Soon", "Completed"});
        filterComboBox.setToolTipText("Filter tasks");
        filterComboBox.addActionListener(e -> applyFilter());
        
        searchPanel.add(searchField);
        filterLabel = new JLabel("Filter:");
        searchPanel.add(filterLabel);
        searchPanel.add(filterComboBox);
        
        // Add progress bar
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setString("No tasks");
        
        // Add stats label
        statsLabel = new JLabel("Ready");
        
        // Add components to the main frame
        Container contentPane = getContentPane();
        contentPane.add(toolbar, BorderLayout.NORTH);
        
        // Add search and progress to the bottom
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(searchPanel, BorderLayout.WEST);
        
        progressPanel = new JPanel(new BorderLayout());
        progressLabel = new JLabel("Progress: ");
        progressPanel.add(progressLabel, BorderLayout.WEST);
        progressPanel.add(overallProgressBar, BorderLayout.CENTER);
        progressPanel.add(statsLabel, BorderLayout.EAST);
        
        bottomPanel.add(progressPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        
        // Update layout
        pack();
    }
    
    private void applyBasicFonts() {
        // Simple font application that won't conflict with NetBeans
        try {
            Font baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
            Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 15);
            
            // Only set fonts if not already customized
            eventNameField.setFont(baseFont);
            taskField.setFont(baseFont);
            eventList.setFont(baseFont);
            selectedEventTitle.setFont(titleFont);
            
            // Set button fonts
            eventDateButton.setFont(baseFont);
            addEventButton.setFont(baseFont);
            addTaskButton.setFont(baseFont);
            saveButton.setFont(baseFont);
            
        } catch (Exception e) {
            // If font setting fails, continue without custom fonts
            System.out.println("Font setting failed, using defaults");
        }
    }
    
    private void setupEnterKeyListeners() {
        // Add Enter key listener for event name field
        eventNameField.addActionListener(e -> {
            // Trigger calendar popup when Enter is pressed
            openCalendarDialog();
        });
        
        // Add Enter key listener for task field
        taskField.addActionListener(e -> {
            // Trigger add task when Enter is pressed
            addTask();
        });
    }
    
    private void setupEventListeners() {
        eventList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedEvent = eventList.getSelectedValue();
                if (selectedEvent != null) {
                    loadTasksForEvent(selectedEvent);
                }
            }
        });
        
        // Add mouse listener to handle delete button clicks
        eventList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = eventList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle cellBounds = eventList.getCellBounds(index, index);
                    if (cellBounds != null) {
                        // Check if click is in the delete button area (right side of cell)
                        int deleteButtonX = cellBounds.x + cellBounds.width - 35; // 35px for button + padding
                        if (e.getX() >= deleteButtonX && e.getX() <= cellBounds.x + cellBounds.width) {
                            // Click was on delete button
                            String eventName = eventListModel.getElementAt(index);
                            deleteSpecificEvent(eventName);
                        }
                    }
                }
            }
        });
    }

    private void addEvent() {
        String eventName = eventNameField.getText().trim();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String eventDate = dateFormat.format(selectedDate);

        if (!eventName.isEmpty()) {
            if (!eventListModel.contains(eventName)) {
                eventListModel.addElement(eventName);
                eventTasks.put(eventName, new ArrayList<>());
                eventCompletedTasks.put(eventName, new ArrayList<>());
                eventDates.put(eventName, eventDate);

                // Clear the event name field
                eventNameField.setText("");
                // Reset date button text to default
                eventDateButton.setText("Select Date");
                selectedDate = new java.util.Date(); // Reset to current date
                
                // Automatically select the newly created event
                eventList.setSelectedValue(eventName, true);
                
                // Load tasks for the newly selected event (this will update the UI)
                loadTasksForEvent(eventName);
                
                // Focus on the task field for immediate task entry
                SwingUtilities.invokeLater(() -> {
                    taskField.requestFocusInWindow();
                });
            } else {
                JOptionPane.showMessageDialog(this, "Event already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteEvent() {
        String selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(this, "No event selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete event '" + selectedEvent + "' and all its tasks?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Remove from UI
            eventListModel.removeElement(selectedEvent);
            
            // Remove from data structures
            eventTasks.remove(selectedEvent);
            eventCompletedTasks.remove(selectedEvent);
            eventDates.remove(selectedEvent);
            
            // Delete files
            try {
                File taskFile = new File(dataDirectory, selectedEvent + ".txt");
                File completedFile = new File(dataDirectory, "COMPLETED_" + selectedEvent + ".txt");
                
                if (taskFile.exists()) {
                    taskFile.delete();
                }
                if (completedFile.exists()) {
                    completedFile.delete();
                }
            } catch (Exception e) {
                System.err.println("Error deleting files for " + selectedEvent + ": " + e.getMessage());
            }
            
            // Clear task panels
            todoPanel.removeAll();
            completedPanel.removeAll();
            selectedEventTitle.setText("Event: No event selected");
            selectedEventDate.setText("Date:");
            
            todoPanel.revalidate();
            todoPanel.repaint();
            completedPanel.revalidate();
            completedPanel.repaint();
        }
    }

    private void addTask() {
        String selectedEvent = eventList.getSelectedValue();
        String taskText = taskField.getText().trim();

        if (selectedEvent != null && !taskText.isEmpty()) {
            // Create new enhanced task with default values
            Task newTask = new Task(taskText);
            
            // Add task immediately with default settings
            eventTasks.get(selectedEvent).add(newTask);
            taskField.setText("");
            
            // Auto-save after adding task
            autoSaveCurrentEvent(selectedEvent);
            
            // Add to undo stack
            addToUndoStack("ADD_TASK", new Object[]{selectedEvent, newTask});
            
            // Refresh display
            loadTasksForEvent(selectedEvent);
            
            // Show task details dialog for setting priority, due date, etc. (optional)
            // This allows editing but doesn't prevent the task from being added
            SwingUtilities.invokeLater(() -> {
                if (showTaskDetailsDialog(newTask)) {
                    // If user made changes, save again and refresh
                    autoSaveCurrentEvent(selectedEvent);
                    loadTasksForEvent(selectedEvent);
                }
            });
        }
    }

    private void loadTasksForEvent(String eventName) {
        selectedEventTitle.setText("Event: " + eventName);
        selectedEventDate.setText("Date: " + eventDates.getOrDefault(eventName, ""));

        // Load tasks from files
        loadTasksFromFile(eventName);

        // Clear panels
        todoPanel.removeAll();
        completedPanel.removeAll();

        // Add pending tasks with zebra striping
        List<Task> tasks = eventTasks.get(eventName);
        if (tasks != null) {
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                JPanel taskPanel = createTaskPanel(eventName, task, false, i);
                todoPanel.add(taskPanel);
            }
        }

        // Add completed tasks with zebra striping
        List<Task> completedTasks = eventCompletedTasks.get(eventName);
        if (completedTasks != null) {
            for (int i = 0; i < completedTasks.size(); i++) {
                Task task = completedTasks.get(i);
                JPanel taskPanel = createTaskPanel(eventName, task, true, i);
                completedPanel.add(taskPanel);
            }
        }

        todoPanel.revalidate();
        todoPanel.repaint();
        completedPanel.revalidate();
        completedPanel.repaint();
        
        // Update progress bar
        updateProgressBar();
    }

    private JPanel createTaskPanel(String eventName, Task task, boolean isCompleted, int rowIndex) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        // Set fixed height for consistent sizing
        panel.setPreferredSize(new java.awt.Dimension(0, 40));
        panel.setMinimumSize(new java.awt.Dimension(0, 40));
        panel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));

        // Apply zebra striping with dark mode support
        Color lightColor, darkColor;
        if (isDarkMode) {
            lightColor = new Color(60, 60, 60);      // Dark grey
            darkColor = new Color(45, 45, 45);       // Darker grey
        } else {
            lightColor = Color.WHITE;                 // White
            darkColor = new Color(245, 245, 245);    // Light grey
        }
        
        if (rowIndex % 2 == 0) {
            panel.setBackground(lightColor);
        } else {
            panel.setBackground(darkColor);
        }
        panel.setOpaque(true);

        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        // Create task text with priority indicator
        String taskText = task.getText();
        if (task.getPriority() != TaskPriority.MEDIUM) {
            taskText = "[" + task.getPriority().getName() + "] " + taskText;
        }
        
        // Add due date indicator if present
        if (task.getDueDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
            taskText += " (Due: " + sdf.format(task.getDueDate()) + ")";
        }

        JCheckBox checkBox = new JCheckBox(taskText);
        checkBox.setSelected(isCompleted);
        
        // Apply dark mode styling to checkbox
        if (isDarkMode) {
            checkBox.setForeground(Color.WHITE);
            checkBox.setBackground(panel.getBackground());
        } else {
            checkBox.setForeground(Color.BLACK);
            checkBox.setBackground(panel.getBackground());
        }
        
        // Color coding for priorities and due dates (override dark mode for important states)
        if (task.isOverdue()) {
            checkBox.setForeground(isDarkMode ? new Color(255, 120, 120) : Color.RED);
            checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));
        } else if (task.isDueSoon()) {
            checkBox.setForeground(isDarkMode ? new Color(255, 180, 100) : new Color(255, 140, 0)); // Orange
        } else {
            // Use priority colors but adjust for dark mode if needed
            Color priorityColor = task.getPriority().getColor();
            if (isDarkMode) {
                // Lighten priority colors for dark mode visibility
                if (priorityColor.equals(TaskPriority.LOW.getColor())) {
                    checkBox.setForeground(new Color(100, 200, 100)); // Lighter green
                } else if (priorityColor.equals(TaskPriority.HIGH.getColor())) {
                    checkBox.setForeground(new Color(255, 120, 120)); // Lighter red
                } else if (priorityColor.equals(TaskPriority.URGENT.getColor())) {
                    checkBox.setForeground(new Color(200, 120, 255)); // Lighter purple
                } else {
                    checkBox.setForeground(new Color(255, 180, 100)); // Lighter orange for medium
                }
            } else {
                checkBox.setForeground(priorityColor);
            }
        }
        
        // Make checkbox background transparent to show panel background
        checkBox.setOpaque(false);
        
        // Apply font safely to avoid NetBeans conflicts
        try {
            checkBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        } catch (Exception e) {
            // If font setting fails, continue without custom font
        }
        
        checkBox.addItemListener(new TaskItemListener(eventName, task, isCompleted));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);
        
        // Edit button
        JButton editButton = createEditButton();
        editButton.addActionListener(evt -> editTask(eventName, task));
        
        JButton deleteButton = createDeleteButton();
        deleteButton.addActionListener(evt -> deleteTask(eventName, task, isCompleted));

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        contentPanel.add(checkBox, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JButton createEditButton() {
        JButton editButton = new JButton();
        
        // Try to create icon-based edit button
        try {
            ImageIcon icon = createEditIcon();
            if (icon != null) {
                editButton.setIcon(icon);
            } else {
                // Fallback to Unicode edit icon
                editButton.setText("✏");
                editButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14));
            }
        } catch (Exception e) {
            // Fallback to Unicode edit icon
            editButton.setText("✏");
            editButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14));
        }
        
        // Set fixed size for consistency
        editButton.setPreferredSize(new java.awt.Dimension(26, 26));
        editButton.setMinimumSize(new java.awt.Dimension(26, 26));
        editButton.setMaximumSize(new java.awt.Dimension(26, 26));
        editButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        editButton.setToolTipText("Edit task");
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        
        // Add hover effect
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                editButton.setContentAreaFilled(true);
                editButton.setBackground(new java.awt.Color(200, 230, 255));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                editButton.setContentAreaFilled(false);
            }
        });
        
        return editButton;
    }

    private JButton createDeleteButton() {
        JButton deleteButton = new JButton();
        
        // Try to create PNG-based icon
        try {
            ImageIcon icon = createSVGIcon();
            if (icon != null) {
                deleteButton.setIcon(icon);
            } else {
                // Fallback to Unicode trash icon with bigger font
                deleteButton.setText("🗑");
                deleteButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14));
            }
        } catch (Exception e) {
            // Fallback to Unicode trash icon with bigger font
            deleteButton.setText("🗑");
            deleteButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14));
        }
        
        // Set fixed size for consistency
        deleteButton.setPreferredSize(new java.awt.Dimension(26, 26));
        deleteButton.setMinimumSize(new java.awt.Dimension(26, 26));
        deleteButton.setMaximumSize(new java.awt.Dimension(26, 26));
        deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteButton.setToolTipText("Delete task");
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setFocusPainted(false);
        
        // Add hover effect
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deleteButton.setContentAreaFilled(true);
                deleteButton.setBackground(new java.awt.Color(255, 200, 200));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                deleteButton.setContentAreaFilled(false);
            }
        });
        
        return deleteButton;
    }

    private ImageIcon createEditIcon() {
        try {
            // Try multiple paths to load the edit PNG icon
            String[] possiblePaths = {
                "c:\\Users\\Apon\\Desktop\\JavaApplication1\\src\\images\\edit.png",
                "src\\images\\edit.png",
                "images\\edit.png",
                "..\\images\\edit.png"
            };
            
            for (String path : possiblePaths) {
                java.io.File iconFile = new java.io.File(path);
                if (iconFile.exists()) {
                    ImageIcon originalIcon = new ImageIcon(iconFile.getAbsolutePath());
                    // Check if the image loaded successfully
                    if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                        java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            }
            
            // Try loading from classpath
            try {
                java.net.URL iconURL = getClass().getClassLoader().getResource("images/edit.png");
                if (iconURL != null) {
                    ImageIcon originalIcon = new ImageIcon(iconURL);
                    if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                        java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            } catch (Exception e) {
                System.err.println("Classpath loading failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error loading edit.png: " + e.getMessage());
        }
        
        // Fallback: Use Unicode edit emoji
        return null; // This will trigger the fallback text in createEditButton
    }

    private ImageIcon createSVGIcon() {
        try {
            // Try multiple paths to load the delete PNG icon
            String[] possiblePaths = {
                "c:\\Users\\Apon\\Desktop\\JavaApplication1\\src\\images\\delete.png",
                "src\\images\\delete.png",
                "images\\delete.png",
                "..\\images\\delete.png"
            };
            
            for (String path : possiblePaths) {
                java.io.File iconFile = new java.io.File(path);
                if (iconFile.exists()) {
                    ImageIcon originalIcon = new ImageIcon(iconFile.getAbsolutePath());
                    // Check if the image loaded successfully
                    if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                        java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            }
            
            // Try loading from classpath
            try {
                java.net.URL iconURL = getClass().getClassLoader().getResource("images/delete.png");
                if (iconURL != null) {
                    ImageIcon originalIcon = new ImageIcon(iconURL);
                    if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                        java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            } catch (Exception e) {
                System.err.println("Classpath loading failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error loading delete.png: " + e.getMessage());
        }
        
        // Fallback: Use Unicode trash emoji
        return null; // This will trigger the fallback text in createDeleteButton
    }

    private void deleteTask(String eventName, Task task, boolean isCompleted) {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this task?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            if (isCompleted) {
                eventCompletedTasks.get(eventName).remove(task);
            } else {
                eventTasks.get(eventName).remove(task);
            }
            
            // Auto-save after deleting task
            autoSaveCurrentEvent(eventName);
            
            // Add to undo stack
            addToUndoStack("DELETE_TASK", new Object[]{eventName, task, isCompleted});
            
            // Refresh the display
            loadTasksForEvent(eventName);
        }
    }

    private class TaskItemListener implements ItemListener {
        private final String eventName;
        private final Task task;
        private final boolean wasCompleted;

        public TaskItemListener(String eventName, Task task, boolean wasCompleted) {
            this.eventName = eventName;
            this.task = task;
            this.wasCompleted = wasCompleted;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // Task completed - move from todo to completed
                if (!wasCompleted) {
                    eventTasks.get(eventName).remove(task);
                    task.setCompleted(true);
                    eventCompletedTasks.get(eventName).add(task);
                }
            } else {
                // Task unchecked - move from completed to todo
                if (wasCompleted) {
                    eventCompletedTasks.get(eventName).remove(task);
                    task.setCompleted(false);
                    eventTasks.get(eventName).add(task);
                }
            }

            // Auto-save after task state change
            autoSaveCurrentEvent(eventName);
            
            // Refresh the display
            SwingUtilities.invokeLater(() -> loadTasksForEvent(eventName));
        }
    }
    
    // Custom cell renderer for event list with delete buttons
    private class EventListCellRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel eventLabel;
        private JButton deleteButton;
        
        public EventListCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            eventLabel = new JLabel();
            eventLabel.setOpaque(false);
            
            deleteButton = createDeleteButton();
            deleteButton.setToolTipText("Delete event");
            
            add(eventLabel, BorderLayout.CENTER);
            add(deleteButton, BorderLayout.EAST);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            eventLabel.setText(value);
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                eventLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                eventLabel.setForeground(list.getForeground());
            }
            
            setOpaque(isSelected);
            return this;
        }
    }
    
    private void deleteSpecificEvent(String eventName) {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete event '" + eventName + "' and all its tasks?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Remove from UI
            eventListModel.removeElement(eventName);
            
            // Remove from data structures
            eventTasks.remove(eventName);
            eventCompletedTasks.remove(eventName);
            eventDates.remove(eventName);
            
            // Delete files
            try {
                File taskFile = new File(dataDirectory, eventName + ".txt");
                File completedFile = new File(dataDirectory, "COMPLETED_" + eventName + ".txt");
                
                if (taskFile.exists()) {
                    taskFile.delete();
                }
                if (completedFile.exists()) {
                    completedFile.delete();
                }
            } catch (Exception e) {
                System.err.println("Error deleting files for " + eventName + ": " + e.getMessage());
            }
            
            // Clear task panels if this was the selected event
            String selectedEvent = eventList.getSelectedValue();
            if (selectedEvent == null || selectedEvent.equals(eventName)) {
                todoPanel.removeAll();
                completedPanel.removeAll();
                selectedEventTitle.setText("Event: No event selected");
                selectedEventDate.setText("Date:");
                
                todoPanel.revalidate();
                todoPanel.repaint();
                completedPanel.revalidate();
                completedPanel.repaint();
            }
        }
    }

    private void loadEventsFromFiles() {
        try {
            // Use the data directory instead of current working directory
            try (var pathStream = Files.list(dataDirectory.toPath())) {
                pathStream.filter(path -> path.toString().endsWith(".txt"))
                    .filter(path -> !path.getFileName().toString().startsWith("COMPLETED_"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String eventName = fileName.substring(0, fileName.length() - 4); // Remove .txt

                        if (!eventListModel.contains(eventName)) {
                            eventListModel.addElement(eventName);
                            eventTasks.put(eventName, new ArrayList<>());
                            eventCompletedTasks.put(eventName, new ArrayList<>());
                            eventDates.put(eventName, ""); // Date will be loaded if available
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("Error loading events from files: " + e.getMessage());
        }
    }

    private void loadTasksFromFile(String eventName) {
        // Load pending tasks
        try {
            File taskFile = new File(dataDirectory, eventName + ".txt");
            if (taskFile.exists()) {
                List<String> taskLines = Files.readAllLines(taskFile.toPath());
                List<Task> tasks = new ArrayList<>();
                for (String line : taskLines) {
                    if (!line.trim().isEmpty()) {
                        Task task = parseTaskFromString(line);
                        tasks.add(task);
                    }
                }
                eventTasks.put(eventName, tasks);
            }
        } catch (IOException e) {
            System.err.println("Error loading tasks for " + eventName + ": " + e.getMessage());
        }

        // Load completed tasks
        try {
            File completedFile = new File(dataDirectory, "COMPLETED_" + eventName + ".txt");
            if (completedFile.exists()) {
                List<String> taskLines = Files.readAllLines(completedFile.toPath());
                List<Task> completedTasks = new ArrayList<>();
                for (String line : taskLines) {
                    if (!line.trim().isEmpty()) {
                        Task task = parseTaskFromString(line);
                        task.setCompleted(true);
                        completedTasks.add(task);
                    }
                }
                eventCompletedTasks.put(eventName, completedTasks);
            }
        } catch (IOException e) {
            System.err.println("Error loading completed tasks for " + eventName + ": " + e.getMessage());
        }
    }
    
    private Task parseTaskFromString(String taskString) {
        // Simple parsing - in a real application, you might use JSON or XML
        // For now, just create a task with the text
        return new Task(taskString);
    }
    
    private String taskToString(Task task) {
        // Simple serialization - in a real application, you might use JSON
        return task.getText();
    }

    private void saveCurrentEvent() {
        String selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(this, "No event selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Save pending tasks
            File taskFile = new File(dataDirectory, selectedEvent + ".txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(taskFile))) {
                List<Task> tasks = eventTasks.get(selectedEvent);
                if (tasks != null) {
                    for (Task task : tasks) {
                        writer.println(taskToString(task));
                    }
                }
            }

            // Save completed tasks
            File completedFile = new File(dataDirectory, "COMPLETED_" + selectedEvent + ".txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(completedFile))) {
                List<Task> completedTasks = eventCompletedTasks.get(selectedEvent);
                if (completedTasks != null) {
                    for (Task task : completedTasks) {
                        writer.println(taskToString(task));
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Auto-save event data without showing success message
     * @param eventName the event to save
     */
    private void autoSaveCurrentEvent(String eventName) {
        try {
            // Save pending tasks
            File taskFile = new File(dataDirectory, eventName + ".txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(taskFile))) {
                List<Task> tasks = eventTasks.get(eventName);
                if (tasks != null) {
                    for (Task task : tasks) {
                        writer.println(taskToString(task));
                    }
                }
            }

            // Save completed tasks
            File completedFile = new File(dataDirectory, "COMPLETED_" + eventName + ".txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(completedFile))) {
                List<Task> completedTasks = eventCompletedTasks.get(eventName);
                if (completedTasks != null) {
                    for (Task task : completedTasks) {
                        writer.println(taskToString(task));
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Auto-save error for " + eventName + ": " + e.getMessage());
        }
    }
    
    // ============= NEW ENHANCED METHODS =============
    
    private boolean showTaskDetailsDialog(Task task) {
        JDialog dialog = new JDialog(this, "Task Details", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Priority selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Priority:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<TaskPriority> priorityCombo = new JComboBox<>(TaskPriority.values());
        priorityCombo.setSelectedItem(task.getPriority());
        panel.add(priorityCombo, gbc);
        
        // Due date selection
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Due Date:"), gbc);
        
        gbc.gridx = 1;
        JButton dueDateButton = new JButton("Select Date");
        if (task.getDueDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dueDateButton.setText(sdf.format(task.getDueDate()));
        }
        
        dueDateButton.addActionListener(e -> {
            java.util.Date selectedDueDate = showCalendarDialog(task.getDueDate());
            if (selectedDueDate != null) {
                task.setDueDate(selectedDueDate);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dueDateButton.setText(sdf.format(selectedDueDate));
            }
        });
        panel.add(dueDateButton, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        JTextField categoryField = new JTextField(task.getCategory(), 15);
        panel.add(categoryField, gbc);
        
        // Tags
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Tags:"), gbc);
        
        gbc.gridx = 1;
        JTextField tagsField = new JTextField(String.join(", ", task.getTags()), 15);
        panel.add(tagsField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        final boolean[] result = {false};
        
        okButton.addActionListener(e -> {
            task.setPriority((TaskPriority) priorityCombo.getSelectedItem());
            task.setCategory(categoryField.getText().trim());
            
            // Parse tags
            String tagsText = tagsField.getText().trim();
            if (!tagsText.isEmpty()) {
                List<String> tags = new ArrayList<>();
                for (String tag : tagsText.split(",")) {
                    tags.add(tag.trim());
                }
                task.setTags(tags);
            }
            
            result[0] = true;
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
        
        return result[0];
    }
    
    private java.util.Date showDatePicker(java.util.Date initialDate) {
        JDialog dialog = new JDialog(this, "Select Due Date", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        if (initialDate != null) {
            dateModel.setValue(initialDate);
        }
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy");
        dateSpinner.setEditor(editor);
        
        panel.add(new JLabel("Select due date:", JLabel.CENTER), BorderLayout.NORTH);
        panel.add(dateSpinner, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton clearButton = new JButton("Clear");
        
        final java.util.Date[] result = {null};
        
        okButton.addActionListener(e -> {
            result[0] = (java.util.Date) dateSpinner.getValue();
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        clearButton.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
        
        return result[0];
    }
    
    private java.util.Date showCalendarDialog(java.util.Date initialDate) {
        // Create a custom calendar dialog similar to the event date calendar
        JDialog calendarDialog = new JDialog(this, "Select Due Date", true);
        calendarDialog.setSize(350, 400);
        calendarDialog.setLocationRelativeTo(this);
        
        // Create calendar components
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (initialDate != null) {
            calendar.setTime(initialDate);
        }
        
        // Month and Year selection
        JPanel topPanel = new JPanel(new FlowLayout());
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        
        SpinnerNumberModel yearModel = new SpinnerNumberModel(calendar.get(java.util.Calendar.YEAR), 1900, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        
        // Remove comma from year display
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(yearEditor);
        
        monthCombo.setSelectedIndex(calendar.get(java.util.Calendar.MONTH));
        
        JLabel monthLabel = new JLabel("Month:");
        JLabel yearLabel = new JLabel("Year:");
        
        topPanel.add(monthLabel);
        topPanel.add(monthCombo);
        topPanel.add(yearLabel);
        topPanel.add(yearSpinner);
        
        // Calendar grid
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Day headers
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayHeaders) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            calendarPanel.add(label);
        }
        
        // Store the selected date
        final java.util.Date[] selectedDueDate = {initialDate};
        
        // Function to update calendar
        Runnable updateCalendar = () -> {
            // Remove existing day components (keep headers)
            Component[] components = calendarPanel.getComponents();
            for (int i = 7; i < components.length; i++) {
                calendarPanel.remove(components[i]);
            }
            
            int month = monthCombo.getSelectedIndex();
            int year = (Integer) yearSpinner.getValue();
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            
            int startDay = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            
            // Add empty cells for days before month starts
            for (int i = 0; i < startDay; i++) {
                calendarPanel.add(new JLabel(""));
            }
            
            // Add day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setPreferredSize(new Dimension(40, 30));
                
                // Highlight selected date
                if (selectedDueDate[0] != null) {
                    java.util.Calendar selectedCal = java.util.Calendar.getInstance();
                    selectedCal.setTime(selectedDueDate[0]);
                    if (selectedCal.get(java.util.Calendar.YEAR) == year &&
                        selectedCal.get(java.util.Calendar.MONTH) == month &&
                        selectedCal.get(java.util.Calendar.DAY_OF_MONTH) == day) {
                        dayButton.setBackground(Color.BLUE);
                        dayButton.setForeground(Color.WHITE);
                        dayButton.setOpaque(true);
                    }
                }
                
                final int selectedDay = day;
                dayButton.addActionListener(e -> {
                    java.util.Calendar newDate = java.util.Calendar.getInstance();
                    newDate.set(year, month, selectedDay);
                    selectedDueDate[0] = newDate.getTime();
                    calendarDialog.dispose();
                });
                
                calendarPanel.add(dayButton);
            }
            
            calendarPanel.revalidate();
            calendarPanel.repaint();
        };
        
        // Add listeners for month/year changes
        monthCombo.addActionListener(e -> updateCalendar.run());
        yearSpinner.addChangeListener(e -> updateCalendar.run());
        
        // Initial calendar setup
        updateCalendar.run();
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(e -> {
            selectedDueDate[0] = new java.util.Date();
            calendarDialog.dispose();
        });
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            selectedDueDate[0] = null;
            calendarDialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> calendarDialog.dispose());
        
        bottomPanel.add(todayButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(cancelButton);
        
        // Layout dialog
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.add(topPanel, BorderLayout.NORTH);
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.add(bottomPanel, BorderLayout.SOUTH);
        
        calendarDialog.setVisible(true);
        
        return selectedDueDate[0];
    }
    
    private void editTask(String eventName, Task task) {
        if (showTaskDetailsDialog(task)) {
            loadTasksForEvent(eventName);
            addToUndoStack("EDIT_TASK", new Object[]{eventName, task});
        }
    }
    
    private void updateProgressBar() {
        String selectedEvent = eventList.getSelectedValue();
        if (selectedEvent != null && overallProgressBar != null) {
            List<Task> allTasks = eventTasks.get(selectedEvent);
            List<Task> completedTasks = eventCompletedTasks.get(selectedEvent);
            
            int total = (allTasks != null ? allTasks.size() : 0) + 
                       (completedTasks != null ? completedTasks.size() : 0);
            int completed = completedTasks != null ? completedTasks.size() : 0;
            
            if (total > 0) {
                int progress = (int) ((completed * 100.0) / total);
                overallProgressBar.setValue(progress);
                overallProgressBar.setString(completed + "/" + total + " tasks completed (" + progress + "%)");
            } else {
                overallProgressBar.setValue(0);
                overallProgressBar.setString("No tasks");
            }
        }
    }
    
    private void addToUndoStack(String actionType, Object data) {
        if (undoStack != null) {
            undoStack.push(new UndoRedoAction(actionType, data));
            // Limit undo stack size
            if (undoStack.size() > 50) {
                undoStack.remove(0);
            }
            redoStack.clear(); // Clear redo stack when new action is performed
        }
    }
    
    // ============= ADDITIONAL ENHANCED METHODS =============
    
    private void performUndo() {
        if (!undoStack.isEmpty()) {
            UndoRedoAction action = undoStack.pop();
            redoStack.push(action);
            
            // Perform undo based on action type
            switch (action.type) {
                case "ADD_TASK":
                    Object[] addData = (Object[]) action.data;
                    String eventName = (String) addData[0];
                    Task task = (Task) addData[1];
                    eventTasks.get(eventName).remove(task);
                    loadTasksForEvent(eventName);
                    break;
                case "DELETE_TASK":
                    Object[] deleteData = (Object[]) action.data;
                    String delEventName = (String) deleteData[0];
                    Task delTask = (Task) deleteData[1];
                    boolean wasCompleted = (Boolean) deleteData[2];
                    if (wasCompleted) {
                        eventCompletedTasks.get(delEventName).add(delTask);
                    } else {
                        eventTasks.get(delEventName).add(delTask);
                    }
                    loadTasksForEvent(delEventName);
                    break;
            }
            
            statsLabel.setText("Undo performed");
        }
    }
    
    private void performRedo() {
        if (!redoStack.isEmpty()) {
            UndoRedoAction action = redoStack.pop();
            undoStack.push(action);
            
            // Perform redo (opposite of undo)
            switch (action.type) {
                case "ADD_TASK":
                    Object[] addData = (Object[]) action.data;
                    String eventName = (String) addData[0];
                    Task task = (Task) addData[1];
                    eventTasks.get(eventName).add(task);
                    loadTasksForEvent(eventName);
                    break;
                case "DELETE_TASK":
                    Object[] deleteData = (Object[]) action.data;
                    String delEventName = (String) deleteData[0];
                    Task delTask = (Task) deleteData[1];
                    boolean wasCompleted = (Boolean) deleteData[2];
                    if (wasCompleted) {
                        eventCompletedTasks.get(delEventName).remove(delTask);
                    } else {
                        eventTasks.get(delEventName).remove(delTask);
                    }
                    loadTasksForEvent(delEventName);
                    break;
            }
            
            statsLabel.setText("Redo performed");
        }
    }
    
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
        
        // Update button icon
        if (darkModeIcon != null && lightModeIcon != null) {
            darkModeToggle.setIcon(isDarkMode ? lightModeIcon : darkModeIcon);
        } else {
            // Fallback to text if images not available
            darkModeToggle.setText(isDarkMode ? "☀ Light Mode" : "🌙 Dark Mode");
        }
        
        // Refresh the display
        String selectedEvent = eventList.getSelectedValue();
        if (selectedEvent != null) {
            loadTasksForEvent(selectedEvent);
        }
        
        repaint();
        statsLabel.setText(isDarkMode ? "Dark mode enabled" : "Light mode enabled");
    }
    
    private void applyTheme() {
        // Define color schemes
        Color bgColor = isDarkMode ? new Color(43, 43, 43) : Color.WHITE;
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;
        Color panelBgColor = isDarkMode ? new Color(60, 60, 60) : new Color(245, 245, 245);
        Color fieldBgColor = isDarkMode ? new Color(55, 55, 55) : Color.WHITE;
        Color buttonBgColor = isDarkMode ? new Color(70, 70, 70) : new Color(240, 240, 240);
        Color borderColor = isDarkMode ? new Color(100, 100, 100) : new Color(200, 200, 200);
        Color selectedColor = isDarkMode ? new Color(80, 120, 160) : new Color(184, 207, 229);
        
        // Main panels
        getContentPane().setBackground(bgColor);
        eventPanel.setBackground(bgColor);
        taskPanel.setBackground(bgColor);
        eventInfoPanel.setBackground(bgColor);
        taskControlsPanel.setBackground(bgColor);
        
        // Task display panels
        todoPanel.setBackground(bgColor);
        completedPanel.setBackground(bgColor);
        todoScrollPane.setBackground(bgColor);
        completedScrollPane.setBackground(bgColor);
        todoScrollPane.getViewport().setBackground(bgColor);
        completedScrollPane.getViewport().setBackground(bgColor);
        
        // Event list
        eventList.setBackground(fieldBgColor);
        eventList.setForeground(fgColor);
        eventList.setSelectionBackground(selectedColor);
        eventList.setSelectionForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        jScrollPane1.setBackground(bgColor);
        jScrollPane1.getViewport().setBackground(fieldBgColor);
        
        // Input fields
        eventNameField.setBackground(fieldBgColor);
        eventNameField.setForeground(fgColor);
        eventNameField.setCaretColor(fgColor);
        taskField.setBackground(fieldBgColor);
        taskField.setForeground(fgColor);
        taskField.setCaretColor(fgColor);
        
        // Search field
        if (searchField != null) {
            searchField.setBackground(fieldBgColor);
            searchField.setForeground(fgColor);
            searchField.setCaretColor(fgColor);
        }
        
        // Combo box
        if (filterComboBox != null) {
            filterComboBox.setBackground(fieldBgColor);
            filterComboBox.setForeground(fgColor);
        }
        
        // Progress bar
        if (overallProgressBar != null) {
            overallProgressBar.setBackground(panelBgColor);
            overallProgressBar.setForeground(isDarkMode ? new Color(100, 200, 100) : new Color(0, 150, 0));
        }
        
        // Buttons
        addEventButton.setBackground(buttonBgColor);
        addEventButton.setForeground(fgColor);
        addTaskButton.setBackground(buttonBgColor);
        addTaskButton.setForeground(fgColor);
        saveButton.setBackground(buttonBgColor);
        saveButton.setForeground(fgColor);
        eventDateButton.setBackground(buttonBgColor);
        eventDateButton.setForeground(fgColor);
        
        // Toolbar buttons
        if (newEventButton != null) {
            newEventButton.setBackground(buttonBgColor);
            newEventButton.setForeground(fgColor);
        }
        if (newTaskButton != null) {
            newTaskButton.setBackground(buttonBgColor);
            newTaskButton.setForeground(fgColor);
        }
        if (undoButton != null) {
            undoButton.setBackground(buttonBgColor);
            undoButton.setForeground(fgColor);
        }
        if (redoButton != null) {
            redoButton.setBackground(buttonBgColor);
            redoButton.setForeground(fgColor);
        }
        if (statsButton != null) {
            statsButton.setBackground(buttonBgColor);
            statsButton.setForeground(fgColor);
        }
        if (exportButton != null) {
            exportButton.setBackground(buttonBgColor);
            exportButton.setForeground(fgColor);
        }
        if (importButton != null) {
            importButton.setBackground(buttonBgColor);
            importButton.setForeground(fgColor);
        }
        if (darkModeToggle != null) {
            darkModeToggle.setBackground(buttonBgColor);
            darkModeToggle.setForeground(fgColor);
        }
        
        // Labels
        selectedEventTitle.setForeground(fgColor);
        selectedEventDate.setForeground(fgColor);
        
        // Form labels
        if (jLabel1 != null) {
            jLabel1.setForeground(fgColor);
        }
        if (jLabel2 != null) {
            jLabel2.setForeground(fgColor);
        }
        if (jLabel3 != null) {
            jLabel3.setForeground(fgColor);
        }
        
        // Bottom panel components
        if (bottomPanel != null) {
            bottomPanel.setBackground(panelBgColor);
        }
        if (searchPanel != null) {
            searchPanel.setBackground(panelBgColor);
        }
        if (progressPanel != null) {
            progressPanel.setBackground(panelBgColor);
        }
        if (searchLabel != null) {
            searchLabel.setForeground(fgColor);
        }
        if (filterLabel != null) {
            filterLabel.setForeground(fgColor);
        }
        if (progressLabel != null) {
            progressLabel.setForeground(fgColor);
        }
        
        // Toolbar
        if (toolbar != null) {
            toolbar.setBackground(panelBgColor);
        }
        
        if (statsLabel != null) {
            statsLabel.setForeground(fgColor);
        }
        
        // Split panes
        jSplitPane1.setBackground(bgColor);
        jSplitPane2.setBackground(bgColor);
        
        // Event panel border
        eventPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(borderColor), 
            "Events", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            null, 
            fgColor));
        
        // Scroll pane borders
        todoScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(borderColor), 
            "Todo List", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            null, 
            fgColor));
        completedScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(borderColor), 
            "Completed List", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            null, 
            fgColor));
    }
    
    private void showStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== TODO LIST STATISTICS ===\n\n");
        
        int totalEvents = eventListModel.getSize();
        int totalTasks = 0;
        int totalCompleted = 0;
        int highPriorityTasks = 0;
        int overdueTasks = 0;
        
        for (int i = 0; i < eventListModel.getSize(); i++) {
            String eventName = eventListModel.getElementAt(i);
            List<Task> tasks = eventTasks.get(eventName);
            List<Task> completed = eventCompletedTasks.get(eventName);
            
            if (tasks != null) {
                totalTasks += tasks.size();
                for (Task task : tasks) {
                    if (task.getPriority() == TaskPriority.HIGH || task.getPriority() == TaskPriority.URGENT) {
                        highPriorityTasks++;
                    }
                    if (task.isOverdue()) {
                        overdueTasks++;
                    }
                }
            }
            
            if (completed != null) {
                totalCompleted += completed.size();
            }
        }
        
        stats.append("Total Events: ").append(totalEvents).append("\n");
        stats.append("Total Tasks: ").append(totalTasks + totalCompleted).append("\n");
        stats.append("Pending Tasks: ").append(totalTasks).append("\n");
        stats.append("Completed Tasks: ").append(totalCompleted).append("\n");
        stats.append("High Priority Tasks: ").append(highPriorityTasks).append("\n");
        stats.append("Overdue Tasks: ").append(overdueTasks).append("\n\n");
        
        if (totalTasks + totalCompleted > 0) {
            double completionRate = (totalCompleted * 100.0) / (totalTasks + totalCompleted);
            stats.append("Completion Rate: ").append(String.format("%.1f%%", completionRate)).append("\n");
        }
        
        JTextArea textArea = new JTextArea(stats.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 250));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Todo Data");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Event,Task,Priority,Status,Due Date,Category");
                
                for (int i = 0; i < eventListModel.getSize(); i++) {
                    String eventName = eventListModel.getElementAt(i);
                    
                    // Export pending tasks
                    List<Task> tasks = eventTasks.get(eventName);
                    if (tasks != null) {
                        for (Task task : tasks) {
                            writer.printf("\"%s\",\"%s\",\"%s\",\"Pending\",\"%s\",\"%s\"\n",
                                eventName,
                                task.getText().replace("\"", "\"\""),
                                task.getPriority().getName(),
                                task.getDueDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()) : "",
                                task.getCategory());
                        }
                    }
                    
                    // Export completed tasks
                    List<Task> completed = eventCompletedTasks.get(eventName);
                    if (completed != null) {
                        for (Task task : completed) {
                            writer.printf("\"%s\",\"%s\",\"%s\",\"Completed\",\"%s\",\"%s\"\n",
                                eventName,
                                task.getText().replace("\"", "\"\""),
                                task.getPriority().getName(),
                                task.getDueDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()) : "",
                                task.getCategory());
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Data exported successfully to " + file.getName());
                statsLabel.setText("Data exported");
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void importData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Todo Data");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine(); // Skip header
                int importedCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 4) {
                        String eventName = parts[0].replace("\"", "");
                        String taskText = parts[1].replace("\"", "").replace("\"\"", "\"");
                        String priority = parts[2].replace("\"", "");
                        String status = parts[3].replace("\"", "");
                        
                        // Create event if it doesn't exist
                        if (!eventListModel.contains(eventName)) {
                            eventListModel.addElement(eventName);
                            eventTasks.put(eventName, new ArrayList<>());
                            eventCompletedTasks.put(eventName, new ArrayList<>());
                            eventDates.put(eventName, new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
                        }
                        
                        // Create task
                        Task task = new Task(taskText);
                        try {
                            task.setPriority(TaskPriority.valueOf(priority.toUpperCase()));
                        } catch (Exception e) {
                            task.setPriority(TaskPriority.MEDIUM);
                        }
                        
                        if (parts.length > 5) {
                            task.setCategory(parts[5].replace("\"", ""));
                        }
                        
                        // Add to appropriate list
                        if ("Completed".equals(status)) {
                            task.setCompleted(true);
                            eventCompletedTasks.get(eventName).add(task);
                        } else {
                            eventTasks.get(eventName).add(task);
                        }
                        
                        importedCount++;
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Successfully imported " + importedCount + " tasks");
                statsLabel.setText("Data imported: " + importedCount + " tasks");
                
                // Refresh display
                if (!eventListModel.isEmpty()) {
                    eventList.setSelectedIndex(0);
                }
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error importing data: " + e.getMessage(), 
                    "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            // If search is empty, show all tasks
            String selectedEvent = eventList.getSelectedValue();
            if (selectedEvent != null) {
                loadTasksForEvent(selectedEvent);
            }
            return;
        }
        
        // Search in current event's tasks
        String selectedEvent = eventList.getSelectedValue();
        if (selectedEvent != null) {
            // Clear panels
            todoPanel.removeAll();
            completedPanel.removeAll();
            
            // Search in pending tasks
            List<Task> tasks = eventTasks.get(selectedEvent);
            if (tasks != null) {
                int rowIndex = 0;
                for (Task task : tasks) {
                    if (task.getText().toLowerCase().contains(searchText) ||
                        task.getCategory().toLowerCase().contains(searchText) ||
                        task.getPriority().getName().toLowerCase().contains(searchText)) {
                        JPanel taskPanel = createTaskPanel(selectedEvent, task, false, rowIndex++);
                        todoPanel.add(taskPanel);
                    }
                }
            }
            
            // Search in completed tasks
            List<Task> completed = eventCompletedTasks.get(selectedEvent);
            if (completed != null) {
                int rowIndex = 0;
                for (Task task : completed) {
                    if (task.getText().toLowerCase().contains(searchText) ||
                        task.getCategory().toLowerCase().contains(searchText) ||
                        task.getPriority().getName().toLowerCase().contains(searchText)) {
                        JPanel taskPanel = createTaskPanel(selectedEvent, task, true, rowIndex++);
                        completedPanel.add(taskPanel);
                    }
                }
            }
            
            todoPanel.revalidate();
            todoPanel.repaint();
            completedPanel.revalidate();
            completedPanel.repaint();
            
            statsLabel.setText("Search: '" + searchText + "'");
        }
    }
    
    private void applyFilter() {
        String filter = (String) filterComboBox.getSelectedItem();
        String selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) return;
        
        // Clear panels
        todoPanel.removeAll();
        completedPanel.removeAll();
        
        // Apply filter to pending tasks
        List<Task> tasks = eventTasks.get(selectedEvent);
        if (tasks != null) {
            int rowIndex = 0;
            for (Task task : tasks) {
                boolean showTask = false;
                
                switch (filter) {
                    case "All":
                        showTask = true;
                        break;
                    case "High Priority":
                        showTask = task.getPriority() == TaskPriority.HIGH || task.getPriority() == TaskPriority.URGENT;
                        break;
                    case "Overdue":
                        showTask = task.isOverdue();
                        break;
                    case "Due Soon":
                        showTask = task.isDueSoon();
                        break;
                    case "Completed":
                        showTask = false; // Don't show pending tasks when filtering for completed
                        break;
                }
                
                if (showTask) {
                    JPanel taskPanel = createTaskPanel(selectedEvent, task, false, rowIndex++);
                    todoPanel.add(taskPanel);
                }
            }
        }
        
        // Apply filter to completed tasks
        if ("All".equals(filter) || "Completed".equals(filter)) {
            List<Task> completed = eventCompletedTasks.get(selectedEvent);
            if (completed != null) {
                int rowIndex = 0;
                for (Task task : completed) {
                    JPanel taskPanel = createTaskPanel(selectedEvent, task, true, rowIndex++);
                    completedPanel.add(taskPanel);
                }
            }
        }
        
        todoPanel.revalidate();
        todoPanel.repaint();
        completedPanel.revalidate();
        completedPanel.repaint();
        
        statsLabel.setText("Filter: " + filter);
    }
}
