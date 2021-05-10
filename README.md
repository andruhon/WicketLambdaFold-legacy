# ARCHIVED

Moved to https://github.com/andruhon/WicketLambdaFold

This is a legacy code base, please go to https://github.com/andruhon/WicketLambdaFold to find new code.

WicketLambdaFold
----------------

A plugin for IntelliJ IDEA which
folds/shortens wicket lambda model's getter and setter references.
So LambdaModel.of(model, Entity::getSomething, Entity::setSomething)
becomes LambdaModel.of(model, Entity::get/setSomething).

Usages of PropertyModel highlighted as warnings.


Also adds intentions to create HTML and .properties files for Wicket panels and pages.
