# 🌟 CoreCreator – AI-powered Intelligent Editor Assistant

<p align="center">
  <img src="src/main/resources/Img/logo.png" width="280" alt="Logo">
</p>

<p align="center">
  <a href="https://github.com/MellottStm/CoreCreator/releases/tag/release">
    <img src="https://img.shields.io/github/release/MellottStm/CoreCreator.svg" alt="Release">
  </a>
  <a href="https://github.com/MellottStm/CoreCreator/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">
  </a>
  <a>
    <img src="https://img.shields.io/badge/os-Windows-blue" alt="Platforms">
  </a>
</p>

---

## ✨ Core Features

- **🚀 Comprehensive project context understanding** - AI truly comprehends the entire codebase, not just individual files, and supports almost all document comprehension, including novel creation, copywriting, and more
- **💬 Natural Language Programming** - Simply state your requirements in Chinese/English, and AI will automatically fulfill them
- **🔄 One-click refactoring and optimization** - code optimization, performance enhancement, and architecture adjustment
- **🌐 Supports 100+ programming languages** (with key optimizations for Python, TypeScript, Go, Rust, Java, etc.)

## 🌴 Project Construction
```bash

#1、build
mvn clean package

# with console debug
jpackage 
    --name CoreCreator 
    --icon "src\main\resources\Img\logo.ico" 
    --input "target" 
    --main-jar "CoreCreator-1.0-SNAPSHOT-fat.jar" 
    --main-class "com.smt.Main" 
    --module-path "the path to your openjfx"   
    --add-modules javafx.controls,javafx.web,java.logging,javafx.fxml,javafx.media,javafx.graphics,javafx.base,jdk.crypto.ec,java.sql
    --java-options "-Xmx2048m -Dfile.encoding=UTF-8 -Dhttps.protocols=TLSv1.2,TLSv1.3 -Djavax.net.debug=ssl:handshake" 
    --type msi 
    --vendor "smt" 
    --win-console 
    --win-shortcut 
    --win-menu 
    --win-dir-chooser 
    --win-per-user-install

# without a console
jpackage 
    --name CoreCreator 
    --icon "src\main\resources\Img\logo.ico" 
    --input "target" --main-jar "CoreCreator-1.0-SNAPSHOT-fat.jar" 
    --main-class "com.smt.Main" 
    --module-path "the path to your openjfx"  
    --add-modules javafx.controls,javafx.web,java.logging,javafx.fxml,javafx.media,javafx.graphics,javafx.base,jdk.crypto.ec,java.sql 
    --type msi 
    --vendor "smt" 
    --win-shortcut 
    --win-menu 
    --win-dir-chooser 
    --win-per-user-install

```


## 📸 Run Preview
<p align="center">
  <img src="demo1.png" width="100%">
</p>
<p align="center">
  <img src="demo2.png" width="100%">
</p>
<p align="center">
  <img src="demo3.png" width="100%">
</p>

---

## 🚀 Get started quickly

### 1. Download and install

Go to [Release]("https://github.com/MellottStm/CoreCreator/releases/tag/release") to download the latest version:

- **CoreCreator-1.0.0-windows**

### 2. Launch CoreCreator

### 3. Open the project

### 4.The first startup requires configuring the API Key, Model Name, and Base URL; software performance is influenced by the underlying large model foundation.
