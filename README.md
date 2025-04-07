# **TourCat**
## Development Team  
- **Devishi Soni** (Project Manager)
- **Eve Murphy-Beaudet** (Technical Manager)  
- **Matthew Espinoza** (Front-End Lead)
- **Garrett Hannah** (Software Quality Lead)
- **Jeran Jeyachandren** (Back-End Lead)

---

TourCat is a comprehensive desktop application designed to help users discover, explore, and interact with Canadian landmarks. The system provides:  

- 🗺️ **Interactive Catalog**: Browse plenty of Canadian landmarks with detailed information, allowing for users to add their own!
- 🔐 **User Accounts**: Secure login system with personalized profiles  
- ⭐ **Favorites System**: Save and organize preferred landmarks    
- 🔍 **Advanced Search**: Filter by province and type of landmark

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
1. Make sure you have Maven and Launch4J installed on your local device and the repo cloned. 
2. Open up the terminal and navigate to the directory where you've cloned the github repository.
3. Run the following comands:
### Build executable: ###
        mvn clean install
### Run application:
     java -jar target/TourCat.jar  # Cross-platform

*Note: Complete deployment from the build file is functional with certain constraints due to a lack of time to fix all bugs regarding the executable.*

4. Give the applications a few seconds to load and then you're good to go!




## Happy Touring!


