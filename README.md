# requestCleaner

<img src="https://github.com/haticeerturk/requestCleaner/blob/main/logo.png" width="400">

## Copy Request without Cookies/Tokens
This Burp Suite extension allows you to copy HTTP requests without including cookies or tokens. It removes sensitive information related to authentication, session management, and CSRF protection from the requests, making it easier to share or analyze them without exposing sensitive data.

## Installation
To install the extension, follow these steps:

- Download the latest release from the GitHub repository.
- You can easily compile the project using the following command. This command creates a .jar file under the `@jar` folder.
    ```
    gradle wrapper --gradle-version=5.1.1 # To ensure compatibility with openJDK
    ./gradlew jar
    ```
- Open Burp Suite and go to the `Extender` tab.
- Click the `Add` button and select `Extension file` from the drop-down menu.
- Select the downloaded/compiled JAR file and click `Next` to install the extension.

## Usage
To use the extension, follow these steps:

- Select one request in Burp Suite's Proxy or Repeater tab.
- Right-click on the selected request and go to the "Copy request without cookies/tokens" menu on the `Extensions` menu.
- Click on the menu option to remove cookies and tokens from the selected request.
- The modified request without cookies or tokens will be copied to the clipboard.
- You can now paste the modified request into any text editor or share them with others without sensitive information for further analysis or sharing.

> ![](https://github.com/haticeerturk/requestCleaner/blob/main/extension.gif)

Note: The extension identifies cookies and tokens based on predefined lists. You can modify the lists within the extension's source code if you have additional cookies or tokens specific to your application.

## Contributing

Contributions to this project are welcome. If you encounter any issues or have suggestions for improvements, please open an issue or submit a pull request on the project's GitHub repository.

## Contact
If you have any questions or comments about the extension, please feel free to contact me at haticeerturk27@gmail.com.

---

Enjoy! :brown_heart:
