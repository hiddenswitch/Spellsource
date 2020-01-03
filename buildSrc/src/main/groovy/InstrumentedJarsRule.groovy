import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.LibraryElements

class InstrumentedJarsRule implements AttributeCompatibilityRule<LibraryElements> {

    @Override
    void execute(CompatibilityCheckDetails<LibraryElements> details) {
        if (details.consumerValue.name == 'instrumented-jar' && details.producerValue.name == 'jar') {
            details.compatible()
        }
    }
}