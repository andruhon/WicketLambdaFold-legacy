package pro.kondratev.wicketlambdafold;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class WicketLambdaFoldBundle {

    @Nullable
    private static Reference<ResourceBundle> ourBundle;

    @NotNull
    public static final String LAMBDA_MODEL_FQN = "org.apache.wicket.model.LambdaModel";

    @NotNull
    public static final String LAMBDA_MODEL_NAME = "LambdaModel";

    @NotNull
    public static final String PROPERTY_MODEL_FQN = "org.apache.wicket.model.PropertyModel";

    @NotNull
    public static final String PROPERTY_MODEL_NAME = "org.apache.wicket.model.PropertyModel";

    @NotNull
    private static final String BUNDLE = "pro.kondratev.wicketlambdafold.WicketLambdaFoldBundle";

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private WicketLambdaFoldBundle() {
    }

    @NotNull
    public static String defaultableMessage(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.messageOrDefault(getBundle(), key, "default", true, params);
    }

    @NotNull
    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<>(bundle);
        }
        return bundle;
    }

}