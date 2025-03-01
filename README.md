
# Tetrecs - A JavaFX Game

Welcome to **Tetrecs**, a video game developed using **Java** and **JavaFX**. This project was created as part of coursework for the **COMP1206** module at the University of Southampton.

## 📋 Prerequisites

Before you start, make sure you have the following installed:

- **Java Development Kit (JDK) 17 or later**
- **Apache Maven (3.6+ recommended)**
- An IDE (such as IntelliJ, Eclipse, or VSCode with Maven support)

---

## 📥 Cloning the Repository

To clone this project, use:

```bash
git clone <repository-url>
cd tetrecs
```

---

## ⚙️ Building the Project

This project uses **Maven** to manage dependencies and build the application. To build, simply run:

```bash
mvn clean install
```

---

## ▶️ Running the Game

### Option 1: Using Maven (recommended)

You can launch the game directly using:

```bash
mvn javafx:run
```

### Option 2: Run the Shaded Jar

To build a standalone jar file with all dependencies included, use:

```bash
mvn clean package -Pshade
```

This produces a file located at:

```
target/tetrecs-1.0-SNAPSHOT-shaded.jar
```

Run the game using:

```bash
java -jar target/tetrecs-1.0-SNAPSHOT-shaded.jar
```

---

## 🎮 Controls and Gameplay

**Tetrecs** is a creative block puzzle game inspired by classic Tetris-like gameplay. Basic controls include:

- **Arrow Keys** - Move blocks left or right
- **Space** - Rotate block
- **Enter** - Drop block quickly

Further gameplay information can be found in the in-game tutorial.

---

## 📦 Project Structure

```
src/
├── main/
│   ├── java/                    # Source code
│   │   └── uk/ac/soton/comp1206/ # Main package
│   ├── resources/                # Game assets (graphics, sounds, etc.)
└── test/                         # Unit tests
```

---

## 📚 Dependencies

This project uses several libraries, managed via Maven:

- **JavaFX (controls, fxml, media)** for UI and game rendering
- **nv-websocket-client** for multiplayer communication (if implemented)
- **Log4j** for logging

---

## 💻 Development Setup

### IntelliJ Setup

1. Open the project folder in IntelliJ.
2. IntelliJ should automatically detect and import the Maven project.
3. Set the project SDK to **Java 17**.
4. Run using `Maven -> Plugins -> javafx -> javafx:run`.

---

## 🛠️ Troubleshooting

- If you see `java.lang.module.FindException`, double-check that you are using **Java 17 or newer**.
- Ensure your `JAVA_HOME` is set correctly.
- Platform-specific issues with JavaFX dependencies (Windows, Mac, Linux) should be handled automatically by the Maven profile.

---

## 📜 Credits

Developed by: **Your Name Here**
Course: **COMP1206 - University of Southampton**
Year: **2025**

---

## 🚀 Happy Gaming!
