package pro.kondratev.wicketlambdafold.codeInspection

import com.intellij.codeInspection.InspectionToolProvider

class PropertyModelInspectionProvider: InspectionToolProvider {

    @Override
    override fun getInspectionClasses(): Array<Class<*>> {
        return arrayOf(PropertyModelInspection::class.java)
    }

}