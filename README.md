
This project is also designed to help you better understand the usefulness of good
class design. 

	  * Instructions for how to query to, including a description of the parameters
		you can provide when performing a query. When your application performs
		a search query, **only the following parameters should be used:**

        | Parameter | Value                                                           |
        |-----------|-----------------------------------------------------------------|
        | `term`    | URL-encoded version of the **term** provided by the user.       |
        | `media`   | URL-encoded version of the **media** type provided by the user. |
        | `limit`   | `"200"`                                                         |

      * Only a distinct set of URIs should used if there
        are any duplicates. Implementers are not expected to handle situations
        where two distinct URIs refer to identical images.

      * If **less than twenty one (21)** distinct artwork image URIs are available
	    in the query response, then an alert dialog should be displayed to the
		user with an appropriate error message. In this scenario, the images in
		the main content area should not be updated.

      * If **twenty one (21) or more** distinct artwork image URIs are available in
	    the query response, then all the images associated with those distinct URIs
		should be downloaded. After all the downloads are complete, the main content
		area should be updated to display the first 20 downloaded images. The
        remaining images should not be omitted as they will be needed to facilitate
        the "random replacement" described elsewhere in this document.

 use a `Gson` object's `fromJson` method to parse
  the string directly into instances of classes that represent the data. Classes for
  an iTunes Search response and result are provided with the starter code. Instructions
  for parsing JSON-formatted strings using `fromJson` is described in the
  [JSON reading](https://github.com/cs1302uga/cs1302-tutorials/blob/master/web/json.rst).

* [iTunes Search API](https://affiliate.itunes.apple.com/resources/documentation/itunes-store-web-service-search-api/)
* [Google Gson Library](https://github.com/google/gson)


## Other

* [JavaScript Object Notation (JSON)](https://en.wikipedia.org/wiki/JSON)
* [URL Encoding](https://en.wikipedia.org/wiki/Percent-encoding)

# Appendix - FAQ

Below are some frequently asked questions related to this project.

1. <a id="query-how" />**How do I query the iTunes Search API?**

   In order query the iTunes Search API, I needed to access the iTunes Search
   API service via a carefully contructed URI. Here is a an example of a query
   URI that searches for all
   [Jack Johnson](https://en.wikipedia.org/wiki/Jack_Johnson_(musician))
   audio and video content (movies,
   podcasts, music, music videos, audiobooks, short films, and tv shows):

   ```
   https://itunes.apple.com/search?term=jack+johnson&limit=200&media=music
   ```

   Here is a breakdown of the URI:

   | URL Component                     | Meaning                               |
   |-----------------------------------|---------------------------------------|
   | `https://itunes.apple.com/search` | Endpoint URI for the Search API       |
   | `?`                               | Denotes the start of the query string |
   | `term=jack+johnson`               | Parameter `key=value` pair            |
   | `&`                               | Denotes that an additional parameter is to follow |
   | `limit=200`                       | Parameter `key=value` pair            |
   | `&`                               | Denotes that an additional parameter is to follow |
   | `media=music`                     | Parameter `key=value` pair            |

   In this example, the parameters `term`, `limit`, and `media` are passed
   to the iTunes Search API along with their URL-encoded values (e.g., the user might
   enter the term `"jack johnson"` into the query text field, but the URL-encoded
   version `"jack+johnson"` is what is used in the URI string).

   If you want to read more about each parameter in the query URI, then refer to their
   entries in the
   [iTunes Search API documentation](https://affiliate.itunes.apple.com/resources/documentation/itunes-store-web-service-search-api/).

   The body of the response, i.e., the content of a request to the iTunes Search API, is simply
   a string that is formatted using JavaScript Object Notation (JSON). You _could_
   perform string manipulation to retrieve pieces of information from this
   JSON-formatted string, however, in this project, you should parse it using a `Gson`
   object's `fromJson` method as described in the
   [JSON reading](https://github.com/cs1302uga/cs1302-tutorials/blob/master/web/json.rst).
   A complete example that illustrates how to download and parse the JSON-formatted
   string for a query to the iTunes Search API is also provided in the
   [HTTP reading](https://github.com/cs1302uga/cs1302-tutorials/blob/master/web/http.rst)

   **URL-Encoding:** You may have noticed that we said `jack+johnson` is the URL-encoded
   value for `"jack johnson"`. When constructing a URI query string (i.e., anything after the `?` in a URL or URI) in Java,
   take special care that any values (e.g., the value of the `term` parameter) are
   [URL-encoded](https://en.wikipedia.org/wiki/Percent-encoding).

   URL-encoding is easily accomplished for you using the static `encode` method in
   [`URLEncoder`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URLEncoder.html).
   You should use the non-deprecated overload of this method, supplying `StandardCharsets.UTF_8`
   for the character encoding 

   ```java
   URLEncoder.encode("jack johnson", StandardCharsets.UTF_8); // returns "jack+johnson"
   ```

  

1. **What does "local variables referenced from a lambda expression must**
   **be final or effectively final" and how do I fix it?**

   Like local and anonymous classes, a lambda expression can only access local
   variables of the enclosing block that are `final` or effectively `final`.
   That is, a variable local to method can only be involved in the body of
   a lambda expression if it is either explicitly declared as `final` or if
   its value does not change after initialization over the entire body of
   the method. A variable is local to a method (i.e., it's a local variable)
   if it's declared inside of the method or if it's a parameter to the method.
   Please note that this restriction applies to the variables themselves and
   presents an interesting scenario in the case of local reference variables.
   A local reference variable may remain effectively `final` even if the
   internal state of the object being referenced is changed so long as the
   variable itself (i.e., the reference value) does not change.

   This problem can be usually be fixed by effectively making use of
   instance variables and/or writing methods that return an instance
   of the interface being implemented via the lambda. For example,
   consider the following scenario that results in the compile-time
   error message "local variables referenced from a lambda expression must
   be final or effectively final":

   ```java
   void someMethod() {
       for (int i = 0; i < 10; ++i) {
           EventHandler<ActionEvent> handler = e -> {
               // something involving i
               System.out.println(i);
           };
       } // for
   } // someMethod
   ```

   The variable `i` is local to `someMethod` and neither `final` nor
   effectively `final` because its value changes after each iteration
   of the for-loop. In this scenario, an instance variable is unlikely
   to be appropriate as the value of `i` itself does not need to be
   accessible to the rest of the methods in the class. A suggested way
   to fix this is to create a method that returns an object of the
   interface being implemented by the lambda expression, ensuring that
   `i` is effectively final in that method. Then, we can call that
   method in `someMethod` instead of creating the lambda there directly.
   For example:

   ```java
   EventHandler<ActionEvent> createHandler(int i) {
       EventHandler<ActionEvent> handler = e -> {
           // something involving i
           System.out.println(i);
       };
       return handler;
   } // createHandler
   ```

   ```java
   void someMethod() {
       for (int i = 0; i < 10; ++i) {
           EventHandler<ActionEvent> handler = createHandler(i);
       } // for
   } // someMethod
   ```

   In this new scenario, the variable `i` is an effectively final local variable
   in the block enclosing the lambda expression in `createHandler`, thus avoiding the
   problem described by the compiler.

   **Why is this an issue?** Well, the big reason is that the language does not support it.
   Why doesn't the language support it? I speculate that the reason has to do with
   how local variables are managed internally in memory. As methods get called and
   return they occupy and free up a region of memory called the program stack. It is
   very likely that the region of memory used by the method that created the lambda
   is freed up before the object created by the lambda is used. If the body of the
   lambda expression attempts to change the value of the variable, then what does
   that mean if the variable is not longer there!?

1. **How do I make my application not freeze/hang when executing long running event handlers?**

   For the most part, your GUI application is just like any other
   Java application you have ever written. If a line of code takes a long time to
   execute, then there is a delay before the next line of code is executed.
   This can be problematic in GUI applications since the underlying GUI
   framework, essentially, pauses what it is doing in order to do what you
   ask it to do. This can cause your GUI to hang/freeze (i.e., become unresponsive)
   when you have code blocks that take more than a few milliseconds.

   The way to solve this problem is through a basic use of threads.
   The term *thread* refers to a single thread of execution, in which
   code is executed in sequential order. When you start a Java program,
   you usually start with one thread that starts executing the `main`
   method. This thread is usually called the "main" thread. When you launch
   your JavaFX application using the `Application.launch` method, part of
   the application life-cycle is the creation of a thread for your GUI
   called the "JavaFX Application Thread". By default, any code
   executed by or in response to your GUI components (e.g., the code for an
   event handler) takes place in the JavaFX Application Thread. If you
   do not want your program to hang, then you will need to create a
   separate thread for your problematic code snippets. This works because
   a Java program can have multiple threads executing concurrently.

   Although we have been using "problematic code" to describe the code
   snippet causing the problem, such a code snippet really represents some
   "task" that you want your application to perform without hanging.
   Therefore, I will try to use "task" throughout the remainder of this
   response.

   To create a new thread, you need to instatiate a
   [`Thread`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Thread.html) object
   with a [`Runnable`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runnable.html)
   implementation for your task. Since `Runnable` is a functional interface,
   this process is simplified using a lambda expression or method reference.
   Here is an example idiom of how to create and start a new thread for a task:
   ```java
   Runnable task = () -> {
       /* task code here */
   };
   Thread taskThread = new Thread(task);
   taskThread.setDaemon(true);
   taskThread.start();
   ```
   The call to `taskThread.setDaemon(true)` prevents this newly created thread from
   delaying program termination in the case where either the main thread
   or the JavaFX Application Thread terminate first. After the call to
   `taskThread.start()`, both the JavaFX Application Thread and the newly created
   thread are executing concurrently. You cannot assume that statements in
   either thread execute in any predetermined order.

   **Note:** Using a daemon thread may not be desirable when writing data to a
   file or database as the JVM may terminate the thread before it's finished.

   ```java
   runNow(() -> {
       /* task
        * code
        * here
        */
    });
    ```
    Or, if you have written a method for your task, then it looks even nicer:
    ```java
    runNow(() -> myTaskMethod());
    ```

1. <a id="not-on-fx-application-thread" />**What does "Not on FX application thread" mean and how do I fix it?**

   Usually an `IllegalStateException` with the message "Not on FX application thread"
   means that you are trying to access or modify some node (i.e., a component
   or container) in the scene graph from a code snippet that is not executing
   in the JavaFX Application Thread (see Q7 in this FAQ). If you want to fix this, then
   the code snippet that interacts with the scene graph needs to be wrapped
   in a [`Runnable`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runnable.html)
   implementation and passed to the static `runLater` method in
   [`Platform`](https://openjfx.io/javadoc/17/javafx.graphics/javafx/application/Platform.html).
   Since `Runnable` is a functional interface, this process is simplified using
   a lambda expression or method reference. Here is a basic example:
   ```java
   Runnable sceneTask = () -> {
       /* place code interacting with scene graph here */
   };
   Platform.runLater(sceneTask);
   ```
   The `runLater` method ensures that the code in your `Runnable` implementation
   executes in the JavaFX Application Thread.

   While it might be tempting to place all of your task code in the
   `Runnable` implementation provided to `runLater`, that is not a good idea
   because it will be executed on the JavaFX Application Thread. If you
   already writing code for another thread, it was likely to avoid having it
   run on the JavaFX Application Thread. Multiple calls to the `runLater`
   method can be used, as needed, to ensure only the code that interacts with
   the scene graph is executed in the JavaFX Application Thread.

   <a name="progress-tip">**PROTIP:**</a> If the lambda expression that you pass into `Platform.runLater` uses a local
   variable, then we **strongly suggest** you refactor the call into its own method
   in order to guarantee that the local variable is final or effectively final when
   used in the lambda expression. Here is an example snippet that might be problematic:

   ```java
   Platform.runLater(() -> progressBar.setProgress(0));
   for (int i = 0; i < n; i++) {
       // some task
       // code here
       Platform.runLater(() -> progressBar.setProgress(1.0 * i / n)); // OH NO!
   } // for
   Platform.runLater(() -> progressBar.setProgress(1));
   ```

   An earlier FAQ question dealt with why the code above is an issue for the compiler.
   While the suggested solution there will also work here, this simplified approach
   may be more approachable for students. Simply create a method that performs
   the desired `Platform.runLater`:

   ```java
   private void setProgress(final double progress) {
       Platform.runLater(() -> progressBar.setProgress(progress));
   } // setProgress
   ```

   With that method present, we can simplify the original snippet:

   ```java
   setProgress(0);
   for (int i = 0; i < n; i++) {
       // some task
       // code here
       setProgress(1.0 * i / n); // NICE!
   } // for
   setProgress(1);
   ```

1. **How do I make a code snippet execute repeatedly with a delay between executions?**

   <a name="timeline">For</a> this particular task, creating a thread and attempting to make it
   wait is not the way to go. Instead, the easiest way to accomplish this in a JavaFX application is using the
   [`Timeline`](https://openjfx.io/javadoc/17/javafx.graphics/javafx/animation/Timeline.html)
   and [`KeyFrame`](https://openjfx.io/javadoc/17/javafx.graphics/javafx/animation/KeyFrame.html)
   classes. Here is an example that prints the current time (using
   [`LocalTime`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalTime.html)) to
   standard output every two (2) seconds (specified using
   [`Duration`](https://openjfx.io/javadoc/17/javafx.base/javafx/util/Duration.html), indefinitely:
   ```java
   EventHandler<ActionEvent> handler = event -> System.out.println(LocalTime.now());
   KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
   Timeline timeline = new Timeline();
   timeline.setCycleCount(Timeline.INDEFINITE);
   timeline.getKeyFrames().add(keyFrame);
   timeline.play();
   ```
   The `Timeline` object also hase a `pause` method to pause the execution of the timeline.
   Remember, JavaFX event handlers are executed on the JavaFX Application Thread.

1. <a id="obj-pass"/>**How do I pass around objects effectively?**

   From time to time, you may need to access one part of your app from another part of your app.
   You used a good design (e.g., classes and inheritance), but you find that you're passing a lot
   of reference variables around, perhaps through constructors. 
   
   The recommended strategy is to NOT pass objects via constructors. Instead, expose parts of 
   your custom components via getter methods in the custom component classes, then make the connections
   that you need in your `Application` subclass. The thinking here is that a component should be reusable,
   not just in the current app, but perhaps in future apps that you create. Just like the `Button` class
   exposes methods, so should your custom component class -- an app knows about a button, but the button's
   class need not know about the app. Methods in custom component classes should deal only with the nodes
   in that component. If you're trying to make part of a custom compnent deal with something outside of 
   the custom component, then the best place to make that connection is outside
   the custom component.

1. **How do I access a local, non-downloaded resource (e.g., an image file)?**

   You should place local files under `resources` in your project directory (you may need to
   create the `resources` directory). URIs or URLs prefixed with `file:` should be relative to your
   project directory.

   Example:

   | Resource                | URI/URL                        |
   |-------------------------|--------------------------------|
   | `resources/icon.png`    | `"file:resources/icon.png"`    |
   | `resources/foo/img.png` | `"file:resources/foo/img.png"` |

