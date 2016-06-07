/**
 * Todo-MVC ScalaJS Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./target/scala-2.11/todo-mvc-nodejs-fastopt.js");
    const facade = com.github.ldaniels528.todomvc.TodoServerMain();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();