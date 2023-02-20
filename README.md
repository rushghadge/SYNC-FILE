Create a local Dropbox/G-Drive like server-client architechture.
The project is a prototype for file storage architecture like G-Drive or One-Drive. T
here are one server, two Clinets.
Each clients can add/edit/delete files and server and other clients will get synchronised in real time.
Each client observes it's local directory tree where the files are stored, once found, it sends that update to server.
 Server maintains a buffer of all the updates and when another client requests for previous updates made on the server(client polls the server in 20 secs interval), the server responses with the update and those updates are made on the other clients.

 File is transferred over UDP and not TCP.
Clone the repo

 ```

Dependecies: Maven is needed.

Steps. Change the $LOCAL_DIRS in the  src/main/java/Constants.java with your local dirs where you want the local directories to be.

 RUN APP:
1.	Install Intelij and import project
2.	Edit folder paths in Constants.java file and update as per local dir structure for client server and backup directory
3.	Start Server
4.	Start Client.java 
5.	Start Client2.java
