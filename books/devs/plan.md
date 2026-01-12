# Plan 
Al crear una base de datos no solicitar el tipo de base de datos a crear, ya que
la base de datos debe permitir crear dentro de ellas los tipos (Column, Document,
Graph, Vector, Object, File). Modifica la interface web, driver, shell, y curl y actualiza la documentacion correspondiente.

En la interface Web, mostrar un Three en el menu izquierdo donde se muestren las bases de datos. En este three
debe haber una opcion para crear bases de datos. Al crear la base de datos se debe mostrar  sub three dentro de cada 
base de datos con las opciones Document, Column, Graph, Vector, Object, Files (cada uno representa el tipo de base de datos)

En cada subtree, debe contener una barra de opciones con los botones para (añadir (dependiendo de cada tipo de engine
, indices, rules)




Monitorear el consumo de recursos de cada nodo y mostrarlos en el shell

Monitorear el consumo de recursos de cada nodo y mostrarlos en curl

Monitorear el consumo de recursos de cada nodo y mostrarlos en el driver

Actualizar la documentacion del shell, web, driver y curl indicando los procedimientos para monitorerar los recursos de los nodos