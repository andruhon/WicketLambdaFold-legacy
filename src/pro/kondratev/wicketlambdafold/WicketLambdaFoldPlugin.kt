package pro.kondratev.wicketlambdafold

import com.intellij.openapi.components.ApplicationComponent

class WicketLambdaFoldPlugin : ApplicationComponent {

    override fun getComponentName(): String {
        return "Wicket Lambda Fold Plugin"
    }

    override fun initComponent() {
    }

    override fun disposeComponent() {
    }

}