# **TourCat**
## Development Team  
- **Devishi Soni** (Project Manager)
- **Eve Murphy-Beaudet** (Technical Manager)  
- **Matthew Espinoza** (Front-End Lead)
- **Garrett Hannah** (Software Quality Lead)
- **Jeran Jeyachandren** (Back-End Lead)

---

TourCat is a desktop application designed to help users discover, explore, and interact with Canadian landmarks. The system provides:  

- **Interactive Catalog**: Browse many Canadian landmarks with detailed information and related pictures. This also includes many functionailities such as adding and deleting, viewing and editing landmarks.
- **User Accounts**: Secure login and logout system with personalized profiles.
- **Advanced Search**: Filter by province and type of landmark.

*Built with Java, TourCat combines robust backend functionality with an intuitive user interface.*  


## Installation Guide
### Prerequisites
     - Java 17+ 
     - Maven 3.8+
     - IntelliJ IDEA (recommended) or any IDE of your choice

#### Through an IDE: ####
1. **Clone the repository**:  
   ```bash  
   git clone https://github.com/DevishiSoni/Software-Design-Project.git 
   cd Software-Design-Project
#### Setup in IntelliJ:
1. Open project as Maven project
2. Wait for dependencies to resolve
3. Configure run order:
   - `TourCatServer.LoginServer`
   - `TourCatGUI.LoginGUI`
#### Launch:
- Right-click each file → "Run" in sequence
  
#### Through the build file: ####
1. Make sure you have Maven installed on your local device and the repository cloned. 
2. Open up the terminal and navigate to the directory where you've cloned the github repository.
3. Run the 'TourCatServer.Login.Server'.
4. Run the following comands in the terminal:
### Build executable: ###
        mvn clean install
### Run application:
     java -jar target/TourCat.jar  # Cross-platform
     
*Note: An error may pop up describing 'build failure', but once you run the .jar file, it will execute!*

4. Give the application a few seconds to load and then you're good to go!

## Developer Notes
*Our code still includes bugs that may hinder the ease-of-use of our program, and some functionalities like editing and deleting a location. However the major functionalities have been implemented. Please see the Project Board for further information.*

*Complete deployment from the .jar file is functional, with an exception of certain constraints due to a lack of time to fix all the bugs. One of the biggest constraints is that the editing function does not work when running from the .jar file.*

## Happy Touring!
