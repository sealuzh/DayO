## DayO prototype v.1.0.0-SNAPSHOT 23/08/2017

### Components
Application consists of two modules:  
* backend written in Kotlin and Java, using Spring Boot v.1.5.3.RELEASE  
* frontend written in TypeScript, using Angular v.4.0.0  

Backend module has a dependency on frontend module, after building the project both modules are packaged in one .jar file  
Dependencies are managed with Maven, each of the modules has its own .pom file, parent .pom file is stored in root directory of the project.

### Building the Application
To build the application:    
* run: `mvnw clean install` on Windows or `./mvnw clean install` on macOS/Linux
* .jar file is stored in /backend/target/ 

### Authorizing Application with Google
Authorization of the application to use Google Calendar Services:
1. To get credentials create new project on [Google Developer Console](https://console.developers.google.com/flows/enableapi?apiid=calendar)
2. In the created project setup the redirect URI to: http://localhost:8080/googleAuthCallback
3. Adjust `fun googleAuthorizationFlow()` method in `DayOPrototypeApplication.kt` file,  
   inserting your credentials (client_id and client_secret) into BasicAuthentication(...)
   and client_id as parameter of `AuthorizationCodeFlow.Builder` --> clientId

### Running the Application Locally 
* Install latest npm
* Install latest Angular CLI
* navigate to /backend/target/ from command line  
* run `java -jar backend-1.0.0-SNAPSHOT` (or import maven project in IntelliJ IDEA and run `DayOPrototypeApplicationKt` application)
* Navigate to /frontend
* type `npm install`
* type `npm start` - development server is running on localhost:4200 
* If you change any frontend file (*.ts, *.html, etc.) - development server will recompile the application and reload it in your browser

After the application is started locally
    server is listening on [localhost:8080](http://localhost:8080)
    client is listening on [localhost:4200](http://localhost:4200)

To use the locally running application, open [http://localhost:4200/](http://localhost:4200/) in your browser.  
Application was tested with Google Chrome, other browsers weren't checked

Normal flow after navigating to [localhost:4200](http://localhost:4200/) is: 
* entering name and password into login form
* authorization of the user with Google account (using email setup for the user in the DB)
* interaction with application

### Adding new Users
To add new users extend users list in `ConfigurationConstants.kt` file with new `UserDefinition(login, password, email)`  
Next time server is restarted new users will be added to the database automatically.  

Users are authenticated with their name, so all login values (user names) values must be unique!

###Deploying the Application
To deploy the application:
1. Change the SERVER_URL and CLIENT_URL constants in ConfigurationConstants.kt file in backend module
   (will be the same, if both modules are deployed on the same server)
2. Change the API_URL constant to the url pointing to the deployed backend in frontend/src/common/constants.ts
3. In the Google Developer Console change the redirect URL to point to the GoogleAuthCallbackServlet
    Example: url_of_the_server_where_backend_is_deployed/googleAuthCallback
