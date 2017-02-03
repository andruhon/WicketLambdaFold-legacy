WicketLambdaFold
----------------

A plugin for IntelliJ IDEA which
folds/shortens wicket lambda model's getter and setter references.
So LambdaModel.of(model, Entity::getSomething, Entity::setSomething)
becomes LambdaModel.of(model, Entity::get/setSomething).