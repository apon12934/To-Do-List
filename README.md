# 📝 Enhanced Todo List Manager

A comprehensive task management application built with Java Swing, featuring advanced task organization, priority management, and modern UI/UX design.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)
![NetBeans](https://img.shields.io/badge/NetBeans-IDE-green?style=for-the-badge)

## 🌟 Features

### Core Functionality
- **📅 Event-Based Organization**: Create and manage multiple events with their own task lists
- **✅ Task Management**: Add, edit, delete, and complete tasks with ease
- **💾 Persistent Storage**: Automatic saving and loading of events and tasks
- **🎯 Priority System**: 4-level priority system with color coding

### Advanced Features
- **📊 Progress Tracking**: Real-time progress bars and statistics
- **🔍 Search & Filter**: Find tasks instantly with advanced filtering options
- **📆 Due Date Management**: Set due dates with visual overdue indicators
- **🎨 Modern UI**: Professional interface with icons and hover effects
- **⚡ Keyboard Shortcuts**: Enhanced productivity with hotkeys
- **🌙 Theme Support**: Built-in dark/light mode toggle
- **📈 Analytics**: Task completion statistics and insights
- **💼 Data Export/Import**: CSV export for data portability

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- NetBeans IDE (recommended) or any Java IDE
- Windows/Linux/macOS

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/apon12934/JavaApplication1.git
   cd JavaApplication1
   ```

2. **Open in NetBeans:**
   - File → Open Project
   - Navigate to the project folder
   - Click "Open Project"

3. **Build and Run:**
   ```bash
   # Using NetBeans: Right-click project → Clean and Build → Run
   # Or using command line:
   javac -cp src src/TodoListApp/TodoListApp.java
   java -cp src TodoListApp.TodoListApp
   ```

## 🎮 Usage

### Basic Operations

#### Creating Events
1. Enter event name in the "Event Name" field
2. Click "Select Date" to choose event date
3. Click "Add Event" to create

#### Managing Tasks
1. Select an event from the left panel
2. Enter task description in "New Task" field
3. Press Enter or click "Add Task"
4. Set priority and due date in the task details dialog

#### Task Actions
- **✏️ Edit**: Click the edit icon to modify task details
- **🗑️ Delete**: Click the delete icon to remove tasks
- **☑️ Complete**: Check the checkbox to mark as completed

### Advanced Features

#### Search and Filter
- Use the search box to find specific tasks
- Apply filters: All, High Priority, Overdue, Due Soon, Completed
- Results update in real-time

#### Priority Management
- **🟢 Low**: Standard tasks
- **🟠 Medium**: Default priority
- **🔴 High**: Important tasks
- **🟣 Urgent**: Critical tasks requiring immediate attention

#### Keyboard Shortcuts
- `Ctrl+N`: Focus on new event field
- `Ctrl+T`: Focus on new task field
- `Enter`: Quick actions in text fields
- `F1`: Help and feature information

## 🏗️ Project Structure

```
JavaApplication1/
├── src/
│   ├── TodoListApp/
│   │   ├── TodoListApp.java      # Main application class
│   │   └── TodoListApp.form      # NetBeans form file
│   └── images/
│       ├── edit.png              # Edit icon
│       └── delete.png            # Delete icon
├── build/                        # Compiled classes
├── nbproject/                    # NetBeans project files
├── ENHANCED_FEATURES.md          # Detailed feature documentation
└── README.md                     # This file
```

## 🛠️ Technical Details

### Architecture
- **Design Pattern**: Model-View-Controller (MVC) with Observer pattern
- **GUI Framework**: Java Swing with custom components
- **Data Persistence**: File-based storage with custom serialization
- **Event Handling**: Comprehensive listener implementation

### Key Classes
- `TodoListApp`: Main application class with GUI and business logic
- `Task`: Enhanced task model with priority, due dates, and categories
- `TaskPriority`: Enum defining priority levels and colors
- `TaskItemListener`: Custom listener for task state changes

### Data Storage Format
Tasks are stored in plain text files with the format:
```
taskText|priority|dueDate|completed
```

## 🎨 UI Components

### Custom Features
- **Task Panels**: Zebra-striped layout with edit/delete buttons
- **Calendar Dialog**: Custom date picker for due dates
- **Progress Indicators**: Real-time completion tracking
- **Icon Integration**: Professional edit and delete icons
- **Hover Effects**: Interactive button feedback

### Color Scheme
- Low Priority: `#2E7D32` (Green)
- Medium Priority: `#FF9800` (Orange)  
- High Priority: `#F44336` (Red)
- Urgent Priority: `#9C27B0` (Purple)
- Overdue Tasks: Bold Red text
- Due Soon: Orange text

## 📋 Requirements

### System Requirements
- **Java**: JDK 8 or higher
- **Memory**: Minimum 512MB RAM
- **Storage**: 50MB available space
- **Display**: 1024x768 minimum resolution

### Development Requirements
- **IDE**: NetBeans 12+ (recommended) or IntelliJ IDEA
- **Build Tools**: Ant (included with NetBeans)
- **Version Control**: Git

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java naming conventions
- Add comments for complex logic
- Test all functionality before submitting
- Maintain backward compatibility

## 📄 License

This project is developed for educational purposes as part of university coursework. Feel free to use and modify for learning purposes.

## 🙏 Acknowledgments

- **Course**: University Object-Oriented Programming Course
- **IDE**: NetBeans for excellent Swing GUI development
- **Icons**: Custom icons for enhanced user experience
- **Design**: Modern UI/UX principles for better usability

## 📞 Support

If you encounter any issues or have questions:
1. Check the [ENHANCED_FEATURES.md](ENHANCED_FEATURES.md) for detailed feature documentation
2. Create an issue on GitHub
3. Review the code comments for implementation details

## 👨‍💻 Developer

**Al Amin Islam Apon**

- 📘 **Facebook**: [apon.xox](https://facebook.com/apon.xox)
- 💬 **WhatsApp**: [wa.me/8801927041100](https://wa.me/8801927041100)
- 💻 **GitHub**: [apon12934](https://github.com/apon12934)

---

**Made with ❤️ for University OOP Course - Demonstrating advanced OOP concepts and modern GUI development**
