GymApp 🏋️‍♂️
A University Project for the "Programming Environments and Mobile Programming" Course




📌 Project Overview
GymApp is an Android application developed for a university project. The app allows users to manage their workout schedules, track exercises, and interact with Firebase for real-time data synchronization.

🚀 Features
✅ User Authentication (Firebase Auth)
✅ Workout Session Management
✅ QR Code Scanner (for user authentication)
✅ Workout Schedules & Exercises
✅ YouTube Video Integration for tutorials
✅ Realtime Database Updates (Firebase Realtime Database)
✅ Session Timer with Start/Pause functionality

🛠 Tech Stack
Programming Language: Kotlin
Database: Firebase Realtime Database
Authentication: Firebase Authentication
UI Framework: Android Jetpack (Fragments, RecyclerView)
Networking: OkHttp
Image Loading: Picasso
QR Code Scanning: ZXing Library
📱 Screenshots
Home Screen
![image](https://github.com/user-attachments/assets/e061e951-a52e-444c-b052-c5e8adcf7594)

Workout Session
![image](https://github.com/user-attachments/assets/524fb20a-e036-4d7e-907b-2feb59e19209)
📂 Project Structure
📦 GymApp  
│── 📁 app  
│   ├── 📁 src  
│   │   ├── 📁 main  
│   │   │   ├── 📁 java/com/example/gymapp  
│   │   │   │   ├── UserProfileActivity.kt  
│   │   │   │   ├── SessionManager.kt  
│   │   │   │   ├── HomeFragment.kt  
│   │   │   │   ├── SchedaDetailFragment.kt  
│   │   │   │   ├── models/Workout.kt  
│   │   │   │   ├── models/Scheda.kt  
│   │   │   ├── 📁 res  
│   │   │   │   ├── 📁 layout  
│   │   │   │   ├── 📁 drawable  
│   │   │   │   ├── 📁 values  
│   ├── 📄 AndroidManifest.xml  
│── 📄 README.md  
🏗 How to Set Up & Run
1️⃣ Clone the Repository

sh
Copia
Modifica
git clone https://github.com/yourusername/GymApp.git
cd GymApp
2️⃣ Open in Android Studio

Open Android Studio
Select Open an existing project
Navigate to the GymApp directory
3️⃣ Set Up Firebase

Go to Firebase Console
Create a project and enable Authentication & Realtime Database
Download the google-services.json file and place it in app/
4️⃣ Run the App

Connect an Android device or use an emulator
Click Run ▶️ in Android Studio
🤝 Contributing
We welcome contributions! To contribute:

Fork the repository
Create a new branch (feature/new-feature)
Commit changes (git commit -m 'Add new feature')
Push to the branch (git push origin feature/new-feature)
Submit a Pull Request
📜 License    
This project is licensed under the MIT License – see the LICENSE file for details.

💡 Developed for academic purposes as part of a university project. 🚀

