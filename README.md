# SnapTea
A SnapKit adapter for TeaVM

This project is a SnapKit adapter to use TeaVM to build SnapKit apps in JavaScript. To build SnapKit apps in SnapCode:

  - Clone this project in the SnapCode directory of your home directory
  - Create a new project and add SnapTea as a dependent project in project settings
  - Call snaptea.TV.set() in your main method (before anything else)
  
Then when you double-click on a .java file with a main method including TV.set(), it will:

  - Build a bin/tea directory in your bin directory with index.html, classes.js and runtime.js
  - Open the index.html in the platform browser
  
And that's it - You should see your Java desktop app in the browser!
