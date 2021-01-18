# CSE_219_Final_Project
My final project for my CSE 219.

This project is about creating a website generating softwarte that will automatically generate a website according to the data, style, and options provided by the users. This is an undergradute class project I did back in college, the website thats being generated is for my course website. 

# Valuable materials and coding techniques demonstrated by the project
This project teaches how to write well structured and maintainable code using an highly organized component architecture, designing a robust framework, and software design patterns which are invaluble skills regardless of the coding languages. 

# To compile
Just open this project in intellj. You need the official Oracle JDK 8. If you are using openjdk, install openjdk-8 and openjfx. Java versions other than version 8 is untested. 
  
# To run
Download my repo and then go into the release folder, open up your cmd/terminal and then run "java  -jar ./java_client.jar" without the quote. Make sure the app_data folder is in the same directory as the jar file.

# Design Patterns
These are more for my own personal references.

* **Creational**
  * [Factory](https://www.tutorialspoint.com/design_pattern/factory_pattern.htm): Class for instantiating custom objects of the same parent family. 
  * [Singleton](https://www.tutorialspoint.com/design_pattern/singleton_pattern.htm): Class in which only one instance of itself can exists. PropertyManager class in the project is a good example.
  * [Builder](https://www.tutorialspoint.com/design_pattern/builder_pattern.htm): Like factory but its object creation is more complicated and uses many steps and sub methods and it does not necceasily use interfaces and static methods. Typically uses a factory pattern class to do its jobs. AppNodeBuilder class in the project is a good example
  * [Prototype](https://www.tutorialspoint.com/design_pattern/prototype_pattern.htm): Make an object that can be easily cloneable. TeachingAssistantPrototype class is a good exmaple so is the ScheduleItem class.

* **Structural**
  * [Decorator](https://www.tutorialspoint.com/design_pattern/decorator_pattern.htm): Wraps classes instead of extending them to add functionality. Classes in the java io API are good examples.
  * [Adapter](https://www.tutorialspoint.com/design_pattern/adapter_pattern.htm): Make sure the code is still compatible by using a in between class that will interact with newer and older APIs. Think of device drivers on Windows
  * [Facade](https://www.tutorialspoint.com/design_pattern/facade_pattern.htm): One simple method that do the work of serval more specialized methods. This narrows the interfaces on which the other programmers who use the API leading to easy to use code with less misues and abuses. 
  * [Flyweight](https://www.tutorialspoint.com/design_pattern/flyweight_pattern.htm): In short, resuing the same objects. This is commonly use in games in which the creation of game objects can be quite resource heavy. 
  * [Bridge](https://www.tutorialspoint.com/design_pattern/bridge_pattern.htm): Make things work before they are designed and designed to let the abstraction and implementation vary independently
 
 * **Behavioral**
   * [Strategy](https://www.tutorialspoint.com/design_pattern/strategy_pattern.htm): More or less, just glorified use of interfaces. j_TPSTransaction class in the project is a good example. 
   * [Template](https://www.tutorialspoint.com/design_pattern/template_pattern.htm): In short, glorified use of inhertence and abstract class. AppTemplate class in the project is a good exmaple.
   * [Observer](https://www.tutorialspoint.com/design_pattern/observer_pattern.htm): An observer notifies data change and informs all its memebers. The JavaFx table coloumn uses ObservableList for this. It is used by the MVC(model-view-controller) architrure. 
   * [Command](https://www.tutorialspoint.com/design_pattern/command_pattern.htm): It is an antipattern so unlearn.
   * [Iterator](https://www.tutorialspoint.com/design_pattern/iterator_pattern.htm): Glorified use of Java iterator and enhanced for loop. Why? beacuse it is fast as in the alogrithms is O(n).
   * [State](https://www.tutorialspoint.com/design_pattern/state_pattern.htm): Uses an interafce to defined differing method for clases of different states. It is better than using instanceOf method to check for class type. 
 

