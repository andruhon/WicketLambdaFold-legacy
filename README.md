# ARCHIVED

This is a legacy codebase, new gradle build can be found in https://github.com/andruhon/WicketLambdaFold

WicketLambdaFold
----------------

A plugin for IntelliJ IDEA which
folds/shortens wicket lambda model's getter and setter references.
So LambdaModel.of(model, Entity::getSomething, Entity::setSomething)
becomes LambdaModel.of(model, Entity::get/setSomething).

Usages of PropertyModel highlighted as warnings.


Also adds intentions to create HTML and .properties files for Wicket panels and pages.
